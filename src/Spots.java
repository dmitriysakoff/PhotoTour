import org.restexpress.Request;
import org.restexpress.Response;

public class Spots {
    public String read(Request req, Response res) {
        String cityId = req.getHeader("city_id");
        return new Data().getSpots(cityId);
    }
}
