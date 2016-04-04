package nationbuilder.controller;

import com.google.gson.JsonObject;
import nationbuilder.model.*;
import nationbuilder.util.TypeEnum;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: hina
 * Date: 4/2/16
 * Time: 10:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventsControllerTest extends Mockito {

    HttpServletResponse response;

    EventsController eventsController;

    ReturnStatus expectedReturnStatusOK;

    ReturnStatus expectedReturnStatusError;

    JsonObject jsonObjectEnter;
    JsonObject jsonObjectLeave;
    JsonObject jsonObjectBlankComment;
    JsonObject jsonObjectComment;
    JsonObject jsonObjectHighFive;
    JsonObject jsonObjectOtherUser;
    JsonObject jsonObjectJane;
    JsonObject jsonObjectTony;
    JsonObject jsonObjectBob;

    JsonObject jsonObjectTestTomo;
    JsonObject jsonObjectTestEly;
    JsonObject jsonObjectTestJamie;
    JsonObject jsonObjectTestEllie;
    JsonObject jsonObjectTestHighFive;
    JsonObject jsonObjectTestComment;


    String from = new String();
    String to = new String();

    @Before
    public void setUp() throws Exception {
        response = mock(HttpServletResponse.class);
        eventsController = new EventsController();
        expectedReturnStatusOK = new ReturnStatus("ok");
        expectedReturnStatusError = new ReturnStatus("error");

        from = "1984-10-26T08:00:00Z";
        to = "1989-10-26T10:01:00Z";

        jsonObjectEnter = new JsonObject();
        jsonObjectEnter.addProperty("date", "1985-10-26T07:58:02Z");
        jsonObjectEnter.addProperty("user", "Hina");
        jsonObjectEnter.addProperty("type", "enter");

        jsonObjectOtherUser = new JsonObject();
        jsonObjectOtherUser.addProperty("date", "1985-10-25T07:58:02Z");
        jsonObjectOtherUser.addProperty("user", "Matt");
        jsonObjectOtherUser.addProperty("type", "enter");

        jsonObjectJane = new JsonObject();
        jsonObjectJane.addProperty("date", "1985-10-26T06:58:02Z");
        jsonObjectJane.addProperty("user", "Jane");
        jsonObjectJane.addProperty("type", "enter");

        jsonObjectTony = new JsonObject();
        jsonObjectTony.addProperty("date", "1985-10-26T05:58:02Z");
        jsonObjectTony.addProperty("user", "Tony");
        jsonObjectTony.addProperty("type", "enter");

        jsonObjectBob = new JsonObject();
        jsonObjectBob.addProperty("date", "1985-10-28T08:07:00Z");
        jsonObjectBob.addProperty("user", "Bob");
        jsonObjectBob.addProperty("type", "enter");

        jsonObjectLeave = new JsonObject();
        jsonObjectLeave.addProperty("date", "1985-10-26T04:01:00Z");
        jsonObjectLeave.addProperty("user", "Hina");
        jsonObjectLeave.addProperty("type", "leave");

        jsonObjectBlankComment = new JsonObject();
        jsonObjectBlankComment.addProperty("date", "1985-10-26T04:02:00Z");
        jsonObjectBlankComment.addProperty("user", "Hina");
        jsonObjectBlankComment.addProperty("type", "comment");
        jsonObjectBlankComment.addProperty("message", "");

        jsonObjectComment = new JsonObject();
        jsonObjectComment.addProperty("date", "1985-10-26T04:03:00Z");
        jsonObjectComment.addProperty("user", "Hina");
        jsonObjectComment.addProperty("type", "comment");
        jsonObjectComment.addProperty("message", "hi! there!");

        jsonObjectHighFive = new JsonObject();
        jsonObjectHighFive.addProperty("date", "1985-10-26T04:04:00Z");
        jsonObjectHighFive.addProperty("user", "Hina");
        jsonObjectHighFive.addProperty("type", "highfive");
        jsonObjectHighFive.addProperty("otheruser", "Matt");

        // Initialize some events

        jsonObjectTestEllie = new JsonObject();
        jsonObjectTestEllie.addProperty("date", "1985-10-30T08:07:00Z");
        jsonObjectTestEllie.addProperty("user", "Ellie");
        jsonObjectTestEllie.addProperty("type", "enter");

        jsonObjectTestEly = new JsonObject();
        jsonObjectTestEly.addProperty("date", "1985-10-31T08:07:03Z");
        jsonObjectTestEly.addProperty("user", "Ely");
        jsonObjectTestEly.addProperty("type", "enter");

        jsonObjectTestTomo = new JsonObject();
        jsonObjectTestTomo.addProperty("date", "1985-10-30T07:07:02Z");
        jsonObjectTestTomo.addProperty("user", "Tomo");
        jsonObjectTestTomo.addProperty("type", "enter");


        jsonObjectTestJamie = new JsonObject();
        jsonObjectTestJamie.addProperty("date", "1985-10-31T08:08:01Z");
        jsonObjectTestJamie.addProperty("user", "Jamie");
        jsonObjectTestJamie.addProperty("type", "enter");

        jsonObjectTestHighFive = new JsonObject();
        jsonObjectTestHighFive.addProperty("date", "1985-10-26T11:04:00Z");
        jsonObjectTestHighFive.addProperty("user", "Ellie");
        jsonObjectTestHighFive.addProperty("type", "highfive");
        jsonObjectTestHighFive.addProperty("otheruser", "Ely");

        jsonObjectTestComment = new JsonObject();
        jsonObjectTestComment.addProperty("date", "1985-10-27T09:09:00Z");
        jsonObjectTestComment.addProperty("user", "Jamie");
        jsonObjectTestComment.addProperty("type", "comment");
        jsonObjectTestComment.addProperty("message", "hi! there!");

        // Initialize some events
        eventsController.events(jsonObjectTestEllie.toString(), response);
        eventsController.events(jsonObjectTestEly.toString(), response);
        eventsController.events(jsonObjectTestTomo.toString(), response);
        eventsController.events(jsonObjectTestJamie.toString(), response);

        // comment and highfive
        eventsController.events(jsonObjectTestHighFive.toString(), response);
        eventsController.events(jsonObjectTestComment.toString(), response);

    }

    @Test
    public void testUserEnter() throws Exception {

        ReturnStatus actualReturnStatus = eventsController.events(jsonObjectEnter.toString(), response);

        assertEquals(expectedReturnStatusOK.toString(), actualReturnStatus.toString());
    }

    @Test
    public void testUserLeave() throws Exception {


        ReturnStatus actualReturnStatus = eventsController.events(jsonObjectLeave.toString(), response);

        // error
        assertEquals(expectedReturnStatusError.toString(), actualReturnStatus.toString());

        // enter
        eventsController.events(jsonObjectEnter.toString(), response);

        actualReturnStatus = eventsController.events(jsonObjectLeave.toString(), response);

        // ok
        assertEquals(expectedReturnStatusOK.toString(), actualReturnStatus.toString());

    }


    @Test
    public void testUserComment() throws Exception {

        // enter
        eventsController.events(jsonObjectEnter.toString(), response);

        ReturnStatus actualReturnStatus = eventsController.events(jsonObjectBlankComment.toString(), response);

        // error
        assertEquals(expectedReturnStatusError.toString(), actualReturnStatus.toString());

        // ok
        assertEquals(expectedReturnStatusOK.toString(),
                eventsController.events(jsonObjectComment.toString(), response).toString());

    }

    @Test
    public void testUserHighFive() throws Exception {

        // Hina Enter

        assertEquals(expectedReturnStatusOK.toString(),
                eventsController.events(jsonObjectEnter.toString(), response).toString());

        // error
        assertEquals(expectedReturnStatusError.toString(),
                eventsController.events(jsonObjectHighFive.toString(),response).toString());

        // other user enter ok
        assertEquals(expectedReturnStatusOK.toString(),
                eventsController.events(jsonObjectOtherUser.toString(),response).toString());

        // highfive ok
        assertEquals(expectedReturnStatusOK.toString(),
                eventsController.events(jsonObjectHighFive.toString(), response).toString());
    }

    @Test
    public void testGetAllEvents() throws Exception {

        Object[] actualResult = eventsController.getAllEvents(from, to);

        TreeMap<String, Object> eventsTreeMap = new TreeMap<String, Object>();

        User userEllie = new User();
        userEllie.setDate("1985-10-30T08:07:00Z");
        userEllie.setType(TypeEnum.ENTER);
        userEllie.setUser("Ellie");

        User userEly = new User();
        userEly.setDate("1985-10-31T08:07:03Z");
        userEly.setType(TypeEnum.ENTER);
        userEly.setUser("Ely");

        User userTomo = new User();
        userTomo.setDate("1985-10-30T07:07:02Z");
        userTomo.setType(TypeEnum.ENTER);
        userTomo.setUser("Tomo");

        User userJamie = new User();
        userJamie.setDate("1985-10-31T08:08:01Z");
        userJamie.setType(TypeEnum.ENTER);
        userJamie.setUser("Jamie");

        UserHighFive userHighFive = new UserHighFive();
        userHighFive.setDate("1985-10-26T11:04:00Z");
        userHighFive.setHighfive(TypeEnum.HIGHFIVE);
        userHighFive.setOtheruser("Ely");
        userHighFive.setUser("Ellie");

        UserComment userComment = new UserComment();

        userComment.setDate("1985-10-27T09:09:00Z");
        userComment.setType(TypeEnum.COMMENT);
        userComment.setMessage("hi! there!");
        userComment.setUser("Jamie");


        Object[] expectedResult = new Object[6];

        expectedResult[0] = userHighFive;
        expectedResult[1] = userComment;
        expectedResult[2] = userTomo;
        expectedResult[3] = userEllie;
        expectedResult[4] = userEly;
        expectedResult[5] = userJamie;

        for(int i=0;i<expectedResult.length; i++){

            assertEquals(expectedResult[i].toString(), actualResult[i].toString());
        }
    }

    public void testGetEventDate() throws Exception {

    }

    @Test
    public void testGetEventSummaryByCondition() throws Exception {

        HashMap<String, ArrayList<EventsCount>> actualResult = eventsController.getSummary(from, to, "day", response);

        ArrayList<EventsCount> counts = new ArrayList<EventsCount>();

        //String date, int enters, int comments, int highfives, int leaves
        EventsCount eventsCountOne = new EventsCount("1985-10-26T00:00:00Z", 0, 0, 1, 0);
        EventsCount eventsCountTwo = new EventsCount("1985-10-27T00:00:00Z", 0, 1, 0, 0);
        EventsCount eventsCountThree = new EventsCount("1985-10-30T00:00:00Z", 2, 0, 0, 0);
        EventsCount eventsCountFour = new EventsCount("1985-10-31T00:00:00Z", 2, 0, 0, 0);

        counts.add(eventsCountOne);
        counts.add(eventsCountTwo);
        counts.add(eventsCountThree);
        counts.add(eventsCountFour);

        HashMap<String, ArrayList<EventsCount>> expectedResult = new HashMap<String, ArrayList<EventsCount>>();

        expectedResult.put("events", counts);

        assertEquals(expectedResult.toString(), actualResult.toString());

    }

    @Test
    public void testClearData() throws Exception {

        ReturnStatus actualResult = eventsController.clearData(response);

        ReturnStatus expectedResult = new ReturnStatus("ok");

        assertEquals(expectedResult.toString(), actualResult.toString());


    }

}
