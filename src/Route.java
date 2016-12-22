import org.json.JSONArray;
import org.json.JSONObject;
import org.restexpress.Request;
import org.restexpress.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

class Route {

    private final String GOOGLE_API_KEY = "&key=AIzaSyBH9UjrtvidgDnLUTctSfL8T1CtNYALpd8";

    public void create(Request request, Response response) throws IOException {
        System.out.println("start of post");
        StringBuilder jb = new StringBuilder();
        String line;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(request.getBodyAsStream(), "UTF-8"));
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }

        // read ids
        JSONArray pinsArray = new JSONArray(jb.toString());
        int[] pinsId = new int[pinsArray.length()];
        for (int i = 0; i < pinsArray.length(); i++){
            pinsId[i] = Integer.parseInt(((String) pinsArray.getJSONObject(i).get("pin_id")));
        }

        // @todo: get latlng of every pin

        // google api request
        URL googleApi = new URL("https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=Toronto&" + // после получения latlng
                "destination=Montreal&" + // после получения latlng
                "mode=walking" +
//                "waypoints=" + "|" + "|" + ... + "|"  // после получения latlng
                "&key=" + GOOGLE_API_KEY);
        URLConnection gc = googleApi.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        gc.getInputStream()));
        String inputLine;
        StringBuilder jb2 = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
            jb2.append(inputLine);
        in.close();

        // build route from google data
        JSONObject googleRespond = new JSONObject(jb2.toString());
        response.setContentType("application/json");
        String wktResult = null;

        // check status
        String status = (String) googleRespond.get("status");
        if (!status.equals("OK")){
            System.out.println("Error in google api request. Status: " + status);
            // send back to the client alert with error text
            String error;
            if (googleRespond.has("error_massage")){
                error = (String) googleRespond.get("error_message");
            }
            else{
                error = "Ошибка сервера. Повторите попытку позднее.";
            }
            JSONObject errorObject = new JSONObject();
            errorObject.put("wkt", JSONObject.NULL);
            errorObject.put("error_message", error);
            response.setBody(errorObject);
            System.out.println("Error to client: " + errorObject.toString());
            System.out.println("end of post");
            return;
        }

        // status is OK, building route
        JSONArray routes = googleRespond.getJSONArray("routes");
        JSONObject route = routes.getJSONObject(0);
        JSONArray legs = route.getJSONArray("legs");
        for (int i = 0; i < legs.length(); i ++){
            JSONArray steps = legs.getJSONObject(i).getJSONArray("steps");
            for (int j = 0; j < steps.length(); j++){
                int totalIndex = i*steps.length() + j;
                JSONObject polyline = steps.getJSONObject(j).getJSONObject("polyline");
                String polylineValue =polyline.getString("points");
                // parse polyline
                if (!polylineValue.equals(JSONObject.NULL)){
                    List<GeoPoint> points = decodePoly(polylineValue);
                }
                else{
                    System.out.println("polyline_" + totalIndex + " = " + "ERROR");
                }
            }
        }

        // @todo: return route in some sort of geo data

        System.out.println("end of post");
    }

    // decode polyline from google api
    private List<GeoPoint> decodePoly(String encoded) {

        List<GeoPoint> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            GeoPoint p = new GeoPoint(((double) lat / 1E5),
                    ((double) lng / 1E5));
            poly.add(p);
        }

        return poly;
    }

    // simple lat long point
    private class GeoPoint{

        public double latitude;
        public double longitude;

        public GeoPoint(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

}
