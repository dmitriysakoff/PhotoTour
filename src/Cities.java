import org.restexpress.Request;
import org.restexpress.Response;

class Cities {
    public String read(Request req, Response res) {
        return new Data().getCities();
    }
}
