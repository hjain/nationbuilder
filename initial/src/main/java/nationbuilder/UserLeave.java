package nationbuilder;

/**
 * Created with IntelliJ IDEA.
 * User: hina
 * Date: 3/30/16
 * Time: 4:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserLeave {

    String user;
    TypeEnum type;
    String date;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
