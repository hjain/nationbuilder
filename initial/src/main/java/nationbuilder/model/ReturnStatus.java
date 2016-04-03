package nationbuilder.model;

/**
 * Created with IntelliJ IDEA.
 * User: hina
 * Date: 4/2/16
 * Time: 7:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReturnStatus {

    String status;

    public ReturnStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "{" +
                "\"status\":\"" + status +
                "\"}";
    }


}
