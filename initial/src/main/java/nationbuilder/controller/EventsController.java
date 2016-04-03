package nationbuilder.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nationbuilder.Exception.ExceptionReturnStatus;
import nationbuilder.impl.EventsDAOImpl;
import nationbuilder.model.*;
import nationbuilder.util.TypeEnum;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
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

    TreeMap<String, Object> eMap = new TreeMap<String, Object>();

    private static final String USER_DOESNT_EXIST = "User doesn't exist in the chat room";
    private static final String OTHER_USER_DOESNT_EXIST = "otheruser doesn't exist";
    private static final String REQUIRED_FIELD_CHECK = "Type or User or Date or all don't exist";
    private static final String USER_REENTERED = "User already entered the chat room";
    private static final String INVALID_ACTION = "Action is not permitted";

    private static final String BY_DAY = "T00:00:00Z";
    private static final String BY_HOUR = ":00:00Z";
    private static final String BY_MIN = ":00Z";

    private static final String CLASS_USER ="nationbuilder.model.User";
    private static final String CLASS_USER_COMMENT ="nationbuilder.model.UserComment";
    private static final String CLASS_USER_HIGHFIVE ="nationbuilder.model.UserHighFive";
    private static final String CLASS_USER_LEAVE ="nationbuilder.model.UserLeave";

    private static final String DELIMIT_COLON = ":";
    private static final String DELIMIT_T = "T";

    private static final String HOUR = "hour";
    private static final String DAY = "day";
    private static final String MINUTE = "minute";
    private static final String MIN = "min";

    private static final String OK = "ok";
    private static final String ERROR = "error";

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
            if(node.get("type").textValue().equalsIgnoreCase(null) || node.get("type").textValue().equalsIgnoreCase("")
                    || node.get("user").textValue().equalsIgnoreCase("") || node.get("user").textValue().equalsIgnoreCase(null)
                    || node.get("date").textValue().equalsIgnoreCase("") || node.get("date").textValue().equalsIgnoreCase(null)) {

               return getReturnStatus(response, true, REQUIRED_FIELD_CHECK);
            }

            else {
                // check for date validation format
                try {
                    DateTime dt = new DateTime(node.get("date").textValue());
                } catch (Exception e) {
                    return getReturnStatus(response, true, "Incorrect date Format");
                }

                nodeUser = node.get("user").textValue();
                nodeDate = node.get("date").textValue();
                nodeType = node.get("type").textValue();
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

                    // returnStatus.setStatus(ERROR);

                    // throw new Exception(nodeUser + " " + USER_REENTERED);


                   return getReturnStatus(response, true, USER_REENTERED);
                }
            } else {
                // enter and add to userMap

                if(nodeType.equalsIgnoreCase(TypeEnum.ENTER.toString())) {

                    EventsDAOImpl eventsDAOImpl = new EventsDAOImpl();

                    user = eventsDAOImpl.userEnter(node, user);

                } else {
                    isError = true;

                    // returnStatus.setStatus(ERROR);
                    // throw new Exception(nodeUser + " " +USER_DOESNT_EXIST);

                   return getReturnStatus(response, true, USER_DOESNT_EXIST);
                }
            }


        } else {
            // check for comment and highfive

            if (!userMap.containsKey(nodeUser)) {
                // isError = true;

                // returnStatus.setStatus(ERROR);

                // throw new Exception(nodeUser + " " + USER_DOESNT_EXIST);

               return getReturnStatus(response, true, USER_DOESNT_EXIST);
            } else {
                if(nodeType.equalsIgnoreCase(TypeEnum.COMMENT.toString())) {


                    if(node.get("message").textValue().equals("") || node.get("message").textValue().equals(null)) {

                        return getReturnStatus(response, true, "Null Message");
                    }

                    EventsDAOImpl eventsDAOImpl = new EventsDAOImpl();
                    userComment = eventsDAOImpl.userComment(node);

                } else if(nodeType.equalsIgnoreCase(TypeEnum.HIGHFIVE.toString())) {

                    if (!userMap.containsKey(node.get("otheruser").textValue())) {
                        isError = true;

                        /*returnStatus.setStatus(ERROR);
                        throw new Exception(nodeUser + " " +OTHER_USER_DOESNT_EXIST); */

                       return getReturnStatus(response, true, OTHER_USER_DOESNT_EXIST);
                    } else {

                        if(node.get("otheruser").textValue().equals("") || node.get("otheruser").textValue().equals(null)) {

                            return getReturnStatus(response, true, OTHER_USER_DOESNT_EXIST);
                        }

                        EventsDAOImpl eventsDAOImpl = new EventsDAOImpl();
                        userHighFive = eventsDAOImpl.userHighFive(node);
                    }


                } else {
                    isError = true;

                   /* returnStatus.setStatus(ERROR);
                    throw new Exception(INVALID_ACTION);  */

                   return getReturnStatus(response, true, INVALID_ACTION);
                }
            }

        }

        HttpStatus status = httpStatus(isError);

        if (!isError) {

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

                   /* returnStatus.setStatus(ERROR);
                    throw new Exception(INVALID_ACTION);     */

                   return getReturnStatus(response, true, INVALID_ACTION);
                }

                eventsTreeMap.put(eventDate, eventsObjects);

                eMap.put(eventDate, eventsObjects);

                System.out.println("x");
            } catch (Exception e) {
                // isError = true;

               /* returnStatus.setStatus(ERROR);

                // return "{\"status\": \"error\", \"message\", \"Event couldn't be captured\"}" + "\n";
                throw new Exception("Event couldn't be captured");  */

               return getReturnStatus(response, true, "Event couldn't be captured");
            }

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

        SortedMap<String, Object> eByRange = new TreeMap<String, Object>();
        eByRange = eMap.subMap(from, to);

        Set<Entry<String, Object>> entrySetByRange = new HashSet<Entry<String, Object>>();
        entrySetByRange = eventsByRange.entrySet();

        Object[] listEventByRange  = new Object[entrySetByRange.size()];

        Integer i = 0;
        for (Entry<String, Object> entry : eventsByRange.entrySet()) {

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

        EventsCount eventsCount = new EventsCount(null, 0, 0, 0, 0);

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

                    System.out.println("here");
                }

            } else if (className.equalsIgnoreCase(CLASS_USER_COMMENT)) {
                UserComment userComment = (UserComment) entry.getValue();

                String tempdate1 = getEventDate(by, userComment.getDate(), response);

                if (eventCountMap.containsKey(tempdate1)) {

                    comments = eventCountMap.get(tempdate1).getComments() + 1;

                    eventCountMap.get(tempdate1).setComments(comments);

                    System.out.println("check entries : " + eventCountMap.entrySet());

                } else {

                    EventsCount newEventCount = new EventsCount(tempdate1, enters, comments + 1, highfives, leaves);

                    eventCountMap.put(tempdate1, newEventCount);

                    System.out.println("here");
                }


            } else if (className.equalsIgnoreCase(CLASS_USER_HIGHFIVE)) {

                UserHighFive userHighFive = (UserHighFive) entry.getValue();

                String tempdate1 = getEventDate(by, userHighFive.getDate(), response);

                if (eventCountMap.containsKey(tempdate1)) {

                    highfives = eventCountMap.get(tempdate1).getHighfives() + 1;

                    eventCountMap.get(tempdate1).setHighfives(highfives);

                    System.out.println("check entries : " + eventCountMap.entrySet());

                } else {

                    EventsCount newEventCount = new EventsCount(tempdate1, enters, comments, highfives + 1, leaves);

                    eventCountMap.put(tempdate1, newEventCount);

                    System.out.println("here");
                }

            } else {

                UserLeave userLeave = (UserLeave) entry.getValue();

                String tempdate1 = getEventDate(by, userLeave.getDate(), response);

                if (eventCountMap.containsKey(tempdate1)) {

                    leaves = eventCountMap.get(tempdate1).getLeaves() + 1;

                    eventCountMap.get(tempdate1).setLeaves(leaves);

                    System.out.println("check entries : " + eventCountMap.entrySet());

                } else {

                    EventsCount newEventCount = new EventsCount(tempdate1, enters, comments, highfives, leaves + 1);

                    eventCountMap.put(tempdate1, newEventCount);

                    System.out.println("here");
                }
            }
        }


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

           return getReturnStatus(response, true, "Data couldn't be deleted");

           // throw new Exception("Events data couldn't be deleted");
        }


        return new ReturnStatus(OK);
    }

    public void throwException(String errorMessage) throws Exception {
        throw new Exception(errorMessage);
    }

    /**
     * Check if error
     * @param isError
     * @return
     */
    public HttpStatus httpStatus(boolean isError) {

        HttpStatus status = HttpStatus.OK;

        if (isError) {
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        }

        return status;
    }

    // ToDo: check isError stuff
    // ToDo: document README
    // ToDo: Test Suite
}
