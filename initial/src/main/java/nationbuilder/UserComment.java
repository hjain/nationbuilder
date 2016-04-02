package nationbuilder;

/**
 * Created with IntelliJ IDEA.
 * User: hina
 * Date: 3/30/16
 * Time: 2:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserComment {

    String user;

    String message;

    String date;

    TypeEnum type;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }
}
