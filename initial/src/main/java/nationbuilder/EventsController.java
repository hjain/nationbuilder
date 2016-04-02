package nationbuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created with IntelliJ IDEA.
 * User: hina
 * Date: 3/29/16
 * Time: 5:05 PM
 * To change this template use File | Settings | File Templates.
 */

@RestController
@RequestMapping(value = "/events")
public class EventsController {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");

    // ToDo: Create a list of Users

    List<User> users = new ArrayList<User>();

    HashMap<String, User> userMap = new HashMap<String, User>();

    Object eventsObjects = new Object();

    TreeMap<DateTime, Object> eventsTreeMap = new TreeMap<DateTime, Object>();

    Map<String, Integer> eventsCounts = new HashMap<String, Integer>();

    private static final String USER_DOESNT_EXIST = "User doesn't exist in the chat room";
    private static final String OTHER_USER_DOESNT_EXIST = "otheruser doesn't exist";
    private static final String REQUIRED_FIELD_CHECK = "Type or User or Date or all don't exist";
    private static final String USER_REENTERED = "User already entered the chat room";
    private static final String INVALID_ACTION = "Action is not permitted";

    @RequestMapping(method = RequestMethod.POST)
    public String events(@RequestBody String inputJson) throws Exception {

        if (inputJson == null || "".equals(inputJson)) {
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
            if(node.get("type").textValue().equals(null) || node.get("type").textValue().equals("")
                    || node.get("user").textValue().equals("") || node.get("user").textValue().equals(null)
                    || node.get("date").textValue().equals("") || node.get("date").textValue().equals(null)) {
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
                if (nodeType.equals(TypeEnum.LEAVE.toString())) {

                    EventsDAOImpl eventsDAOImpl = new EventsDAOImpl();

                    userLeave = eventsDAOImpl.userLeave(node);

                } else {
                    isError = true;
                    throw new Exception(nodeUser + " " + USER_REENTERED);
                }
            } else {
                // enter and add to userMap

                if(nodeType.equals(TypeEnum.ENTER.toString())) {

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
                if(nodeType.equals(TypeEnum.COMMENT.toString())) {

                    EventsDAOImpl eventsDAOImpl = new EventsDAOImpl();
                    userComment = eventsDAOImpl.userComment(node);

                } else if(nodeType.equals(TypeEnum.HIGHFIVE.toString())) {

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

                if (nodeType.equals(TypeEnum.ENTER.toString())) {

                    // add user to eventsObject

                    eventsObjects = user;

                    userMap.put(user.getUser(), user);

                } else if(nodeType.equals(TypeEnum.LEAVE.toString())) {

                    eventsObjects = userLeave;

                    userMap.remove(nodeUser);

                } else if (nodeType.equals(TypeEnum.COMMENT.toString())) {

                    eventsObjects = userComment;

                } else if (nodeType.equals(TypeEnum.HIGHFIVE.toString())) {

                    eventsObjects = userHighFive;

                } else {
                    throw new Exception(INVALID_ACTION);
                }

                eventsTreeMap.put(dt, eventsObjects);

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

        DateTime dateFrom = new DateTime(from);
        DateTime dateTo = new DateTime(to);

        SortedMap<DateTime, Object> eventsByRange = new TreeMap<DateTime, Object>();

        eventsByRange = eventsTreeMap.subMap(dateFrom, dateTo);

        Set<Entry<DateTime, Object>> entrySetByRange = new HashSet<Entry<DateTime, Object>>();
        entrySetByRange = eventsByRange.entrySet();

        Object[] listEventByRange  = new Object[entrySetByRange.size()];

        Integer i = 0;
        for (Entry<DateTime, Object> entry : eventsByRange.entrySet()) {


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


        int enters = 0;
        int leaves = 0;
        int comments = 0;
        int highfives = 0;
        String date = new String();

        DateTime dateFrom = new DateTime(from);
        DateTime dateTo = new DateTime(to);

        SortedMap<DateTime, Object> eventsByRange = new TreeMap<DateTime, Object>();

        Map<String, EventsCount> resultMap = new HashMap<String, EventsCount>();

        EventsCount eventsCount = new EventsCount();

        if (eventsTreeMap.size() == 0) {

            resultMap.put("events", eventsCount);
            //return resultMap;
        } else {
            // initialize
            eventsCount.setDate("");
            eventsCount.setComments(0);
            eventsCount.setEnters(0);
            eventsCount.setHighfives(0);
            eventsCount.setLeaves(0);

            resultMap.put("events", eventsCount);
        }


        //ToDo: final result needs to be a hash of list of hash maps

        // Todo : get event counts by date

        Map<String, EventsCount> eventCountMap = new HashMap<String, EventsCount>();

        Set<Entry<DateTime, Object>> entrySetByRange = new HashSet<Entry<DateTime, Object>>();
        entrySetByRange = eventsByRange.entrySet();


        eventsByRange = eventsTreeMap.subMap(dateFrom, dateTo);

        HashMap<String, ArrayList<EventsCount>> test = new HashMap<String, ArrayList<EventsCount>>();

        if(by.equals("day")) {
            // get aggregation by date



            int i = 0;
            for (Entry<DateTime, Object> entry : eventsByRange.entrySet()) {

                Object obj = new Object();


                String className = entry.getValue().getClass().getName();

                if (className.equalsIgnoreCase("nationbuilder.User")) {
                    User userObj = (User) entry.getValue();

                    // keep the track of dates
                    DateTime dateTime = new DateTime(userObj.getDate());

                    String tempdate1 = dateTime.getYear() + "-" +
                            dateTime.getMonthOfYear() + "-" +
                            dateTime.getDayOfMonth()+"T00:00:00Z";

                    if (eventCountMap.containsKey(tempdate1)) {

                        enters = eventCountMap.get(tempdate1).getEnters() + 1;

                        eventCountMap.get(tempdate1).setEnters(enters);

                        System.out.println("check entries : " + eventCountMap.entrySet());

                    } else {

                        EventsCount newEventCount = getNewEventCount(tempdate1, 1, 0, 0, 0);

                        eventCountMap.put(tempdate1, newEventCount);

                        System.out.println("here");
                    }

                } else if (className.equalsIgnoreCase("nationbuilder.UserComment")) {
                    UserComment userComment = (UserComment) entry.getValue();

                    DateTime dateTime = new DateTime(userComment.getDate());

                    String tempdate1 = dateTime.getYear() + "-" +
                            dateTime.getMonthOfYear() + "-" +
                            dateTime.getDayOfMonth()+"T00:00:00Z";

                    if (eventCountMap.containsKey(tempdate1)) {

                        comments = eventCountMap.get(tempdate1).getComments() + 1;

                        eventCountMap.get(tempdate1).setComments(comments);

                        System.out.println("check entries : " + eventCountMap.entrySet());

                    } else {

                        EventsCount newEventCount = getNewEventCount(tempdate1, 0, 1, 0, 0);

                        eventCountMap.put(tempdate1, newEventCount);

                        System.out.println("here");
                    }


                } else if (className.equalsIgnoreCase("nationbuilder.UserHighFive")) {

                    UserHighFive userHighFive = (UserHighFive) entry.getValue();

                    DateTime dateTime = new DateTime(userHighFive.getDate());

                    String tempdate1 = dateTime.getYear() + "-" +
                            dateTime.getMonthOfYear() + "-" +
                            dateTime.getDayOfMonth()+"T00:00:00Z";

                    if (eventCountMap.containsKey(tempdate1)) {

                        highfives = eventCountMap.get(tempdate1).getHighfives() + 1;

                        eventCountMap.get(tempdate1).setHighfives(highfives);

                        System.out.println("check entries : " + eventCountMap.entrySet());

                    } else {

                        EventsCount newEventCount = getNewEventCount(tempdate1, 0, 0, 0, 1);

                        eventCountMap.put(tempdate1, newEventCount);

                        System.out.println("here");
                    }

                } else {

                    UserLeave userLeave = (UserLeave) entry.getValue();

                    DateTime dateTime = new DateTime(userLeave.getDate());

                    String tempdate1 = dateTime.getYear() + "-" +
                            dateTime.getMonthOfYear() + "-" +
                            dateTime.getDayOfMonth()+"T00:00:00Z";

                    if (eventCountMap.containsKey(tempdate1)) {

                        leaves = eventCountMap.get(tempdate1).getLeaves() + 1;

                        eventCountMap.get(tempdate1).setLeaves(leaves);

                        System.out.println("check entries : " + eventCountMap.entrySet());

                    } else {

                        EventsCount newEventCount = getNewEventCount(tempdate1, 0, 0, 1, 0);

                        eventCountMap.put(tempdate1, newEventCount);

                        System.out.println("here");
                    }

                    // leave

                }

                // get all the keys by date in a result map


            }

        } else if(by.equals("hour")) {

        } else if(by.equals("minute")) {

        } else {
            throw new Exception(INVALID_ACTION);
        }


        ArrayList<EventsCount> ev = new ArrayList<EventsCount>();

        HashMap<String, ArrayList<EventsCount>> result = new HashMap<String, ArrayList<EventsCount>>();

        for (Map.Entry<String, EventsCount> entry : eventCountMap.entrySet()) {

            ev.add(entry.getValue());


        }

        result.put("events", ev);

        return result;
    }

    public EventsCount getNewEventCount(String date, int enters, int comments, int leaves, int highfives) {
        EventsCount eventsCount = new EventsCount();

        eventsCount.setHighfives(highfives);
        eventsCount.setLeaves(leaves);
        eventsCount.setComments(comments);
        eventsCount.setDate(date);
        eventsCount.setEnters(enters);

        return eventsCount;
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
