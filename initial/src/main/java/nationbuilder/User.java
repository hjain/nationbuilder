package nationbuilder;

/**
 * Created with IntelliJ IDEA.
 * User: hina
 * Date: 3/29/16
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class User {

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
