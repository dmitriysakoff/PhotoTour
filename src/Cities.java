import org.json.JSONArray;
import org.json.JSONObject;
import org.restexpress.Request;
import org.restexpress.Response;

class Cities {
    public String read(Request req, Response res) {

        JSONObject city1 = new JSONObject()
                .put("name", "Рим")
                .put("id", "1")
                .put("location", new JSONObject()
                        .put("longitude", 41.5400)
                        .put("latitude", 12.3000))
                .put("photo", "http://188.166.36.161:8080/images/city?image_uri=moscow.jpg");

        JSONObject city2 = new JSONObject()
                .put("name", "Москва")
                .put("id", "2")
                .put("location", new JSONObject()
                        .put("longitude", 55.4521)
                        .put("latitude", 37.3704))
                .put("photo", "http://188.166.36.161:8080/images/city?image_uri=rome.jpg");

        JSONArray array = new JSONArray();
        array.put(city1);
        array.put(city2);

        return array.toString();
    }
}
