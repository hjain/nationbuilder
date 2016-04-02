package nationbuilder.dao;

import com.fasterxml.jackson.databind.JsonNode;
import nationbuilder.model.User;
import nationbuilder.model.UserComment;
import nationbuilder.model.UserHighFive;
import nationbuilder.model.UserLeave;

public interface EventsDAO {


    public boolean userEnter(JsonNode node, User user) throws Exception;

    public UserLeave userLeave(JsonNode node) throws Exception;

    public UserComment userComment(JsonNode node) throws Exception;

    public UserHighFive userHighFive(JsonNode node) throws Exception;
}
