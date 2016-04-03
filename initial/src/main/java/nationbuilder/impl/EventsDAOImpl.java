package nationbuilder.impl;

import com.fasterxml.jackson.databind.JsonNode;
import nationbuilder.util.TypeEnum;
import nationbuilder.dao.EventsDAO;
import nationbuilder.model.User;
import nationbuilder.model.UserComment;
import nationbuilder.model.UserHighFive;
import nationbuilder.model.UserLeave;

public class EventsDAOImpl implements EventsDAO {

    @Override
    public boolean userEnter(JsonNode node, User user) throws Exception {

        String nodeUser = node.get("user").textValue();
        String nodeDate = node.get("date").textValue();

        boolean isError = false;

        user.setType(TypeEnum.ENTER);

        if(nodeDate.equals(null) || nodeDate.equals("")) {
            isError = true;
            throw new Exception("Date is invalid");
        } else {
            user.setDate(nodeDate);
        }

        if(nodeUser.equals(null) || nodeUser.equals("")) {
            isError = true;
            throw new Exception("Date is invalid");
        } else {
            user.setUser(nodeUser);
        }

        return isError;
    }

    @Override
    public UserLeave userLeave(JsonNode node) throws Exception {

        UserLeave userLeave = new UserLeave();

        userLeave.setUser(node.get("user").textValue());
        userLeave.setDate(node.get("date").textValue());
        userLeave.setType(TypeEnum.LEAVE);


        return userLeave;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserComment userComment(JsonNode node) throws Exception {

        UserComment userComment = new UserComment();

        if(node.get("message").textValue().equals("") || node.get("message").textValue().equals(null)) {

            throw new Exception("Message can't be null");
        }

        userComment.setUser(node.get("user").textValue());
        userComment.setType(TypeEnum.COMMENT);
        userComment.setDate(node.get("date").textValue());
        userComment.setMessage(node.get("message").textValue());

        return userComment;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserHighFive userHighFive(JsonNode node) throws Exception {

        UserHighFive userHighFive = new UserHighFive();

        if(node.get("otheruser").textValue().equals("") || node.get("otheruser").textValue().equals(null)) {

            throw new Exception("Otheruser can't be null");
        }

        userHighFive.setHighfive(TypeEnum.HIGHFIVE);
        userHighFive.setUser(node.get("user").textValue());
        userHighFive.setDate(node.get("date").textValue());
        userHighFive.setOtheruser(node.get("otheruser").textValue());

        return userHighFive;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
