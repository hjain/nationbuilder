package nationbuilder.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nationbuilder.model.EventsCount;
import nationbuilder.impl.EventsDAOImpl;
import nationbuilder.util.TypeEnum;
import nationbuilder.model.User;
import nationbuilder.model.UserComment;
import nationbuilder.model.UserHighFive;
import nationbuilder.model.UserLeave;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

@RestController
@RequestMapping(value = "/events")
public class EventsController {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");

    // ToDo: Create a list of Users

    List<User> users = new ArrayList<User>();

    HashMap<String, User> userMap = new HashMap<String, User>();

    Object eventsObjects = new Object();

    TreeMap<String, Object> eventsTreeMap = new TreeMap<String, Object>();

    TreeMap<String, Object> eMap = new TreeMap<String, Object>();

    Map<String, Integer> eventsCounts = new HashMap<String, Integer>();

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

    @RequestMapping(method = RequestMethod.POST)
    public String events(@RequestBody String inputJson) throws Exception {

        if (inputJson == null || "".equalsIgnoreCase(inputJson)) {
            throwException("Invalid Input");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(inputJson);

        String nodeUser = new String();
        String nodeDate = new String();
        String nodeType = new String();

        boolean isError = false;

        if (node.size() < 3) {
            throwException("Invalid Input");
        } else {
            if(node.get("type").textValue().equalsIgnoreCase(null) || node.get("type").textValue().equalsIgnoreCase("")
                    || node.get("user").textValue().equalsIgnoreCase("") || node.get("user").textValue().equalsIgnoreCase(null)
                    || node.get("date").textValue().equalsIgnoreCase("") || node.get("date").textValue().equalsIgnoreCase(null)) {
                throwException(REQUIRED_FIELD_CHECK);
            }

            else {
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
                    isError = true;
                    throw new Exception(nodeUser + " " + USER_REENTERED);
                }
            } else {
                // enter and add to userMap

                if(nodeType.equalsIgnoreCase(TypeEnum.ENTER.toString())) {

                    EventsDAOImpl eventsDAOImpl = new EventsDAOImpl();

                    isError = eventsDAOImpl.userEnter(node, user);

                } else {
                    isError = true;
                    throw new Exception(nodeUser + " " +USER_DOESNT_EXIST);
                }
            }


        } else {
            // check for comment and highfive

            if (!userMap.containsKey(nodeUser)) {
                isError = true;
                throw new Exception(nodeUser + " " + USER_DOESNT_EXIST);
            } else {
                if(nodeType.equalsIgnoreCase(TypeEnum.COMMENT.toString())) {

                    EventsDAOImpl eventsDAOImpl = new EventsDAOImpl();
                    userComment = eventsDAOImpl.userComment(node);

                } else if(nodeType.equalsIgnoreCase(TypeEnum.HIGHFIVE.toString())) {

                    if (!userMap.containsKey(node.get("otheruser").textValue())) {
                        isError = true;
                        throw new Exception(nodeUser + " " +OTHER_USER_DOESNT_EXIST);
                    } else {


                        EventsDAOImpl eventsDAOImpl = new EventsDAOImpl();
                        userHighFive = eventsDAOImpl.userHighFive(node);
                    }


                } else {
                    isError = true;
                    throw new Exception(INVALID_ACTION);
                }
            }

        }

        HttpStatus status = httpStatus(isError);

        if (!isError) {

            String eventDate = nodeDate;

            DateTime dt = new DateTime(eventDate);

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
                    throw new Exception(INVALID_ACTION);
                }

                eventsTreeMap.put(eventDate, eventsObjects);

                eMap.put(eventDate, eventsObjects);

                System.out.println("x");
            } catch (Exception e) {
                isError = true;
                throw new Exception("Event couldn't be captured");
            }

        }

        return "{\"status\":"+ status + "}" + "\n";

    }

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

    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    public @ResponseBody
    HashMap<String, ArrayList<EventsCount>> getSummary(@RequestParam("from") String from,
                                                       @RequestParam("to") String to,
                                                       @RequestParam(value = "by") String by) throws Exception {

        SortedMap<String, Object> eventsByRange = new TreeMap<String, Object>();

        Map<String, EventsCount> resultMap = new HashMap<String, EventsCount>();

        EventsCount eventsCount = new EventsCount(null, 0, 0, 0, 0);

        //resultMap.put("events", eventsCount);

        SortedMap<String, EventsCount> eventCountMap = new TreeMap<String, EventsCount>();

        HashMap<String, ArrayList<EventsCount>> result = new HashMap<String, ArrayList<EventsCount>>();

        Set<Entry<String, Object>> entrySetByRange = new HashSet<Entry<String, Object>>();
        entrySetByRange = eventsByRange.entrySet();


        eventsByRange = eventsTreeMap.subMap(from, to);

        HashMap<String, ArrayList<EventsCount>> test = new HashMap<String, ArrayList<EventsCount>>();

        if(by.equalsIgnoreCase(DAY)) {
            // get aggregation by date

            result = getEventSummaryByCondition(by, eventsByRange, eventCountMap);

        } else if(by.equalsIgnoreCase(HOUR)) {

           result = getEventSummaryByCondition(by, eventsByRange, eventCountMap);

        } else if(by.equalsIgnoreCase(MINUTE) || by.equalsIgnoreCase(MIN)) {

            result = getEventSummaryByCondition(by, eventsByRange, eventCountMap);

        } else {
            throw new Exception(INVALID_ACTION);
        }

        return result;
    }


    public String getEventDate(String by, String date) throws Exception {

        String eventKeyDate = new String();

        if (by.equalsIgnoreCase(DAY)) {

            eventKeyDate = date.split(DELIMIT_T)[0] + BY_DAY;

        } else if (by.equalsIgnoreCase(HOUR)) {

            eventKeyDate = date.split(DELIMIT_COLON)[0] + BY_HOUR;

        } else if (by.equalsIgnoreCase(MIN) || by.equalsIgnoreCase(MINUTE)) {

            eventKeyDate = date.split(DELIMIT_COLON)[0] + DELIMIT_COLON + date.split(DELIMIT_COLON)[1] + BY_MIN;

        } else {
            // invalid option
            throw new Exception("Invalid By option for Event Summary. Please use hour, day or minute");
        }

        return eventKeyDate;
    }

    public HashMap<String, ArrayList<EventsCount>> getEventSummaryByCondition(String by, SortedMap<String, Object> eventsByRange,
                                           Map<String, EventsCount> eventCountMap) throws Exception {

        for (Entry<String, Object> entry : eventsByRange.entrySet()) {

            int enters = 0;
            int leaves = 0;
            int comments = 0;
            int highfives = 0;

            String className = entry.getValue().getClass().getName();

            if (className.equalsIgnoreCase(CLASS_USER)) {
                User userObj = (User) entry.getValue();

                // set time section to zero
                String tempdate1 = getEventDate(by, userObj.getDate()); //.split(DELIMIT_COLON)[0] + BY_HOUR;

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

                String tempdate1 = getEventDate(by, userComment.getDate());

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

                String tempdate1 = getEventDate(by, userHighFive.getDate());

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

                String tempdate1 = getEventDate(by, userLeave.getDate());

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
    public String clearData() throws Exception {

        try {
            userMap.clear();
            eventsTreeMap.clear();

        } catch (Exception e) {

            throw new Exception("Events data couldn't be deleted");
        }


        return "{\"status\": \"ok\"}" + "\n";
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

}
