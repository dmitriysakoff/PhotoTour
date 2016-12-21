import org.json.JSONArray;
import org.json.JSONObject;
import org.restexpress.Request;
import org.restexpress.Response;

public class Cities {
    public String read(Request req, Response res) {

        JSONObject city1 = new JSONObject();
        city1
        .put("name", "Рим")
                .put("id", "1")
                .put("location", new JSONObject()
                        .put("longitude", 41.5400)
                        .put("latitude", 12.3000))
                .put("photo", "http://localhost:8080/photoFace_war/images/city?image_uri=Moscow.jpg");

        JSONObject city2 = new JSONObject()
                .put("name", "Москва")
                .put("id", "2")
                .put("location", new JSONObject()
                        .put("longitude", 55.4521)
                        .put("latitude", 37.3704))
                .put("photo", "http://localhost:8080/photoFace_war/images/city?image_uri=Rome.jpg");

        JSONArray array = new JSONArray();
        array.put(city1);
        array.put(city2);

        return array.toString();
    }
}
