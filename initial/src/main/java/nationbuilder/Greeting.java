package nationbuilder;

/**
 * Created with IntelliJ IDEA.
 * User: hina
 * Date: 3/29/16
 * Time: 4:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class Greeting {

    private final long id;
    private final String content;

    public Greeting(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
