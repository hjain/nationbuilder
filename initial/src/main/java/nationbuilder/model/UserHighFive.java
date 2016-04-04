package nationbuilder.model;

import nationbuilder.util.TypeEnum;

public class UserHighFive {

    String user;

    String otheruser;

    TypeEnum highfive;

    String date;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOtheruser() {
        return otheruser;
    }

    public void setOtheruser(String otheruser) {
        this.otheruser = otheruser;
    }

    public TypeEnum getHighfive() {
        return highfive;
    }

    public void setHighfive(TypeEnum highfive) {
        this.highfive = highfive;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "UserHighFive{" +
                "user='" + user + '\'' +
                ", otheruser='" + otheruser + '\'' +
                ", highfive=" + highfive +
                ", date='" + date + '\'' +
                '}';
    }
}
