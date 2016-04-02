package nationbuilder;

/**
 * Created with IntelliJ IDEA.
 * User: hina
 * Date: 3/30/16
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public enum TypeEnum {
    ENTER("enter"),
    COMMENT("comment"),
    HIGHFIVE("highfive"),
    LEAVE("leave");

    private final String input;

    private TypeEnum(String input) {
        this.input = input;
    }

    @Override
    public String toString() {

        return input;
    }

}
