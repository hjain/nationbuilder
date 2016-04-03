package nationbuilder.Exception;

import nationbuilder.model.ReturnStatus;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: hina
 * Date: 4/2/16
 * Time: 9:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExceptionReturnStatus {

    private static final String OK = "ok";
    private static final String ERROR = "error";

    public @ResponseBody
    ReturnStatus getReturnStatus(HttpServletResponse response, boolean isError, String message) {

        if (isError) {

            response.setStatus(400);
            response.setHeader("Error-Message", message);

            return new ReturnStatus(ERROR);

        } else {
            return new ReturnStatus(OK);
        }
    }
}
