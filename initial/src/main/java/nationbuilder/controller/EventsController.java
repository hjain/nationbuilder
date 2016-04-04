package nationbuilder.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nationbuilder.Exception.ExceptionReturnStatus;
import nationbuilder.impl.EventsDAOImpl;
import nationbuilder.model.*;
import nationbuilder.util.TypeEnum;
import org.joda.time.DateTime;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

@RestController
@RequestMapping(value = "/events")
public class EventsController extends ExceptionReturnStatus {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");

    HashMap<String, User> userMap = new HashMap<String, User>();

    Object eventsObjects = new Object();

    TreeMap<String, Object> eventsTreeMap = new TreeMap<String, Object>();

    private static final String USER_DOESNT_EXIST = "User doesn't exist in the chat room";
    private static final String OTHER_USER_DOESNT_EXIST = "otheruser doesn't exist";
    private static final String REQUIRED_FIELD_CHECK = "Type or User or Date or all don't exist";
    private static final String USER_REENTERED = "User already entered the chat room";
    private static final String INVALID_ACTION = "Action is not permitted";
    private static final String INCORRECT_DATE = "Incorrect date format";
    private static final String NULL_MSG = "Null Message Not Allowed";
    private static final String DATE_NOT_DELETED = "Something happened! Data couldn't be deleted";
    private static final String EVENT_NOT_CAPTURED = "Event couldn't be captured";

    private static final String USER ="user";
    private static final String DATE ="date";
    private static final String TYPE ="type";
    private static final String OTHERUSER ="otheruser";



    private static final String BY_DAY = "T00:00:00Z";
    private static final String BY_HOUR = ":00:00Z";
    private static final String BY_MIN = ":00Z";

    private static final String CLASS_USER ="nationbuilder.model.User";
    private static final String CLASS_USER_COMMENT ="nationbuilder.model.UserComment";
    private static final String CLASS_USER_HIGHFIVE ="nationbuilder.model.UserHighFive";

    private static final String DELIMIT_COLON = ":";
    private static final String DELIMIT_T = "T";

    private static final String HOUR = "hour";
    private static final String DAY = "day";

    private static final String OK = "ok";

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody ReturnStatus events(@RequestBody String inputJson, HttpServletResponse response) throws Exception {

        if (inputJson == null || "".equalsIgnoreCase(inputJson)) {
            throwException("Invalid Input");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(inputJson);

        String nodeUser = new String();
        String nodeDate = new String();
        String nodeType = new String();

        boolean isError = false;

        ReturnStatus returnStatus = new ReturnStatus(OK);

        if (node.size() < 3) {
            throwException("Invalid Input");
        } else {
            if(node.get(TYPE).textValue().equalsIgnoreCase(null) || node.get(TYPE).textValue().equalsIgnoreCase("")
                    || node.get(USER).textValue().equalsIgnoreCase("") || node.get(USER).textValue().equalsIgnoreCase(null)
                    || node.get(DATE).textValue().equalsIgnoreCase("") || node.get(DATE).textValue().equalsIgnoreCase(null)) {

               return getReturnStatus(response, true, REQUIRED_FIELD_CHECK);
            }

            else {
                // check for date validation format
                try {
                    DateTime dt = new DateTime(node.get(DATE).textValue());
                } catch (Exception e) {
                    return getReturnStatus(response, true, INCORRECT_DATE);
                }

                nodeUser = node.get(USER).textValue();
                nodeDate = node.get(DATE).textValue();
                nodeType = node.get(TYPE).textValue();
            }
        }


        User user = new User();
        UserLeave userLeave = new UserLeave();

        UserComment userComment = new UserComment();
        UserHighFive userHighFive = new UserHighFive();

        if (node.size() == 3) {
            // check for enter and exit

            if (userMap.containsKey(nodeUser)) {
                if (nodeType.equalsIgnoreCase(TypeEnum.LEAVE.toString())) {

                    EventsDAOImpl eventsDAOImpl = new EventsDAOImpl();

                    userLeave = eventsDAOImpl.userLeave(node);

                } else {

                   return getReturnStatus(response, true, USER_REENTERED + " OR " + INVALID_ACTION);
                }
            } else {
                // enter and add to userMap

                if(nodeType.equalsIgnoreCase(TypeEnum.ENTER.toString())) {

                    EventsDAOImpl eventsDAOImpl = new EventsDAOImpl();

                    user = eventsDAOImpl.userEnter(node, user);

                } else {

                   return getReturnStatus(response, true, USER_DOESNT_EXIST + " OR " + INVALID_ACTION);
                }
            }


        } else {
            // check for comment and highfive

            if (!userMap.containsKey(nodeUser)) {

               return getReturnStatus(response, true, USER_DOESNT_EXIST);
            } else {
                if(nodeType.equalsIgnoreCase(TypeEnum.COMMENT.toString())) {


                    if(node.get("message").textValue().equals("") || node.get("message").textValue().equals(null)) {

                        return getReturnStatus(response, true, NULL_MSG);
                    }

                    EventsDAOImpl eventsDAOImpl = new EventsDAOImpl();
                    userComment = eventsDAOImpl.userComment(node);

                } else if(nodeType.equalsIgnoreCase(TypeEnum.HIGHFIVE.toString())) {

                    if (!userMap.containsKey(node.get(OTHERUSER).textValue())) {

                       return getReturnStatus(response, true, OTHER_USER_DOESNT_EXIST);
                    } else {

                        if(node.get(OTHERUSER).textValue().equals("") || node.get(OTHERUSER).textValue().equals(null)) {

                            return getReturnStatus(response, true, OTHER_USER_DOESNT_EXIST);
                        }

                        EventsDAOImpl eventsDAOImpl = new EventsDAOImpl();
                        userHighFive = eventsDAOImpl.userHighFive(node);
                    }


                } else {

                   return getReturnStatus(response, true, INVALID_ACTION);
                }
            }

        }


        String eventDate = nodeDate;

        try {

            if (nodeType.equalsIgnoreCase(TypeEnum.ENTER.toString())) {

                // add user to eventsObject

                eventsObjects = user;

                userMap.put(user.getUser(), user);

            } else if(nodeType.equalsIgnoreCase(TypeEnum.LEAVE.toString())) {

                eventsObjects = userLeave;

                userMap.remove(nodeUser);

            } else if (nodeType.equalsIgnoreCase(TypeEnum.COMMENT.toString())) {

                eventsObjects = userComment;

            } else if (nodeType.equalsIgnoreCase(TypeEnum.HIGHFIVE.toString())) {

                eventsObjects = userHighFive;

            } else {

                return getReturnStatus(response, true, INVALID_ACTION);
            }

            eventsTreeMap.put(eventDate, eventsObjects);

        } catch (Exception e) {

           return getReturnStatus(response, true, EVENT_NOT_CAPTURED);
        }


        return returnStatus;

    }

    /**
     * get all events in a given range
     * @param from
     * @param to
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Object[] getAllEvents(@RequestParam("from") String from,
                                               @RequestParam("to") String to) {

        SortedMap<String, Object> eventsByRange = new TreeMap<String, Object>();

        eventsByRange = eventsTreeMap.subMap(from, to);

        SortedMap<String, Object> eventsMapByRange = new TreeMap<String, Object>();
        eventsMapByRange = eventsTreeMap.subMap(from, to);

        Object[] listEventByRange  = new Object[eventsMapByRange.size()];

        Integer i = 0;
        for (Entry<String, Object> entry : eventsMapByRange.entrySet()) {

            listEventByRange[i] = entry.getValue();
            i++;
        }

        return listEventByRange;

    }

    /**
     * get events counts by day or hour or min/minute
     * @param from : from range
     * @param to : to range
     * @param by : by [hour, day, min/minute]
     * @return
     * @throws Exception
     */

    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    public @ResponseBody
    HashMap<String, ArrayList<EventsCount>> getSummary(@RequestParam("from") String from,
                                                       @RequestParam("to") String to,
                                                       @RequestParam(value = "by") String by,
                                                       HttpServletResponse response) throws Exception {

        SortedMap<String, Object> eventsByRange = new TreeMap<String, Object>();

        SortedMap<String, EventsCount> eventCountMap = new TreeMap<String, EventsCount>();

        HashMap<String, ArrayList<EventsCount>> result = new HashMap<String, ArrayList<EventsCount>>();

        Set<Entry<String, Object>> entrySetByRange = new HashSet<Entry<String, Object>>();
        entrySetByRange = eventsByRange.entrySet();


        eventsByRange = eventsTreeMap.subMap(from, to);

        HashMap<String, ArrayList<EventsCount>> test = new HashMap<String, ArrayList<EventsCount>>();

        if(by.equalsIgnoreCase(DAY)) {
            // get aggregation by date
            result = getEventSummaryByCondition(by, eventsByRange, eventCountMap, response);

        } else if(by.equalsIgnoreCase(HOUR)) {

            result = getEventSummaryByCondition(by, eventsByRange, eventCountMap, response);


        } else {
            result = getEventSummaryByCondition(by, eventsByRange, eventCountMap, response);

        }

        return result;
    }


    /**
     * get event date
     * @param by
     * @param date
     * @param response
     * @return
     * @throws Exception
     */
    public String getEventDate(String by, String date, HttpServletResponse response) throws Exception {

        String eventKeyDate = new String();

        if (by.equalsIgnoreCase(DAY)) {

            eventKeyDate = date.split(DELIMIT_T)[0] + BY_DAY;

        } else if (by.equalsIgnoreCase(HOUR)) {

            eventKeyDate = date.split(DELIMIT_COLON)[0] + BY_HOUR;

        } else {

            eventKeyDate = date.split(DELIMIT_COLON)[0] + DELIMIT_COLON + date.split(DELIMIT_COLON)[1] + BY_MIN;

        }

        return eventKeyDate;
    }

    /**
     * get aggreated result by comndition
     * @param by
     * @param eventsByRange
     * @param eventCountMap
     * @param response
     * @return
     * @throws Exception
     */
    public HashMap<String, ArrayList<EventsCount>> getEventSummaryByCondition(String by, SortedMap<String, Object> eventsByRange,
                                           Map<String, EventsCount> eventCountMap,
                                           HttpServletResponse response) throws Exception {

        for (Entry<String, Object> entry : eventsByRange.entrySet()) {

            int enters = 0;
            int leaves = 0;
            int comments = 0;
            int highfives = 0;

            String className = entry.getValue().getClass().getName();

            if (className.equalsIgnoreCase(CLASS_USER)) {
                User userObj = (User) entry.getValue();

                // set time section to zero
                String tempdate1 = getEventDate(by, userObj.getDate(), response); //.split(DELIMIT_COLON)[0] + BY_HOUR;

                if (eventCountMap.containsKey(tempdate1)) {

                    enters = eventCountMap.get(tempdate1).getEnters() + 1;

                    eventCountMap.get(tempdate1).setEnters(enters);

                } else {

                    EventsCount newEventCount = new EventsCount(tempdate1, enters + 1, comments, highfives, leaves);

                    eventCountMap.put(tempdate1, newEventCount);

                }

            } else if (className.equalsIgnoreCase(CLASS_USER_COMMENT)) {
                UserComment userComment = (UserComment) entry.getValue();

                String tempdate1 = getEventDate(by, userComment.getDate(), response);

                if (eventCountMap.containsKey(tempdate1)) {

                    comments = eventCountMap.get(tempdate1).getComments() + 1;

                    eventCountMap.get(tempdate1).setComments(comments);

                } else {

                    EventsCount newEventCount = new EventsCount(tempdate1, enters, comments + 1, highfives, leaves);

                    eventCountMap.put(tempdate1, newEventCount);

                }


            } else if (className.equalsIgnoreCase(CLASS_USER_HIGHFIVE)) {

                UserHighFive userHighFive = (UserHighFive) entry.getValue();

                String tempdate1 = getEventDate(by, userHighFive.getDate(), response);

                if (eventCountMap.containsKey(tempdate1)) {

                    highfives = eventCountMap.get(tempdate1).getHighfives() + 1;

                    eventCountMap.get(tempdate1).setHighfives(highfives);

                } else {

                    EventsCount newEventCount = new EventsCount(tempdate1, enters, comments, highfives + 1, leaves);

                    eventCountMap.put(tempdate1, newEventCount);

                }

            } else {

                UserLeave userLeave = (UserLeave) entry.getValue();

                String tempdate1 = getEventDate(by, userLeave.getDate(), response);

                if (eventCountMap.containsKey(tempdate1)) {

                    leaves = eventCountMap.get(tempdate1).getLeaves() + 1;

                    eventCountMap.get(tempdate1).setLeaves(leaves);

                } else {

                    EventsCount newEventCount = new EventsCount(tempdate1, enters, comments, highfives, leaves + 1);

                    eventCountMap.put(tempdate1, newEventCount);
                }
            }
        }

        // get the final result map
        HashMap<String, ArrayList<EventsCount>> result = getEventResultMap(eventCountMap);

        return result;


    }

    /**
     * get event result map
     * @param eventCountMap
     * @return
     */
    public HashMap<String, ArrayList<EventsCount>> getEventResultMap(Map<String, EventsCount> eventCountMap) {

        // prepare the final tree map of aggregated results

        ArrayList<EventsCount> countEntry = new ArrayList<EventsCount>();
        HashMap<String, ArrayList<EventsCount>> result = new HashMap<String, ArrayList<EventsCount>>();

        for (Map.Entry<String, EventsCount> entry : eventCountMap.entrySet()) {

            countEntry.add(entry.getValue());
        }

        result.put("events", countEntry);

        return result;


    }

    /**
     * Delete all events and data related to them
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/clear", method = RequestMethod.GET)
    public @ResponseBody ReturnStatus clearData(HttpServletResponse response) throws Exception {

        try {
            userMap.clear();
            eventsTreeMap.clear();

        } catch (Exception e) {

           return getReturnStatus(response, true, DATE_NOT_DELETED);

           // throw new Exception("Events data couldn't be deleted");
        }


        return new ReturnStatus(OK);
    }

    public void throwException(String errorMessage) throws Exception {
        throw new Exception(errorMessage);
    }

}
