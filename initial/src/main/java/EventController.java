import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * User: hina
 * Date: 3/30/16
 * Time: 4:54 PM
 * To change this template use File | Settings | File Templates.
 */

@RestController
@RequestMapping(value = "/e")
public class EventController {

    @RequestMapping(value = "/events",method = RequestMethod.POST)
    public @ResponseBody String events(@RequestBody String inputJson) throws Exception {

        // find which event it is
        // get the value of type node

        ObjectMapper mapper = new ObjectMapper();

        if (inputJson != null) {
            JsonNode node = mapper.readTree(inputJson);

            // check node size
            if (node.size() < 3) {
                throw new Exception("Input Json doesn't have any nodes");
            }


        } else {
            throw new Exception("Invalid Input JSON");
        }

        return null;
    }


}
