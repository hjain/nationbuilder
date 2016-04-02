package nationbuilder;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created with IntelliJ IDEA.
 * User: hina
 * Date: 4/1/16
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public interface EventsDAO {


    public boolean userEnter(JsonNode node, User user) throws Exception;

    public UserLeave userLeave(JsonNode node) throws Exception;

    public UserComment userComment(JsonNode node) throws Exception;

    public UserHighFive userHighFive(JsonNode node) throws Exception;
}
