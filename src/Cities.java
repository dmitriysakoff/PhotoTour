import org.restexpress.Request;
import org.restexpress.Response;

class Cities {
    public String read(Request req, Response res) {
        Data d = new Data();
        // строка JSONArray городов
        String cities = d.getCities();
        return cities;
    }
}
