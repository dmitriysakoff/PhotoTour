import org.json.JSONArray;
import org.json.JSONObject;
import org.restexpress.Request;
import org.restexpress.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

class Route {

    private final String GOOGLE_API_KEY = "AIzaSyBH9UjrtvidgDnLUTctSfL8T1CtNYALpd8";

    public void create(Request request, Response response) throws IOException {
        System.out.println("start of post");
        StringBuilder jb = new StringBuilder();
        Data data = new Data();
        String line;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(request.getBodyAsStream(), "UTF-8"));
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }

        // read ids
        List<GeoPoint> pinsLocation = new ArrayList<>();
        JSONObject pins = new JSONObject(jb.toString());
        JSONArray pinsArray = pins.getJSONArray("pins");

        // not enough points for route
        if (pinsArray.length() < 2) {
            errorNotEnoughPointsForRoute(response);
            return;
        }

        // get lat lng of pins
        for (int i = 0; i < pinsArray.length(); i++) {
            double[] latlon = data.getVistaLatLon(Integer.parseInt(((String) pinsArray.getJSONObject(i).get("pin_id"))));
            if (latlon != null && latlon.length == 2) {
                pinsLocation.add(new GeoPoint(latlon[0], latlon[1]));
            }
        }

        List<GeoPoint> sortedPins = sortGeoPoints(pinsLocation);

        // google api request
        String googleStringResponse = googleRequest(sortedPins);

        // build route from google data
        JSONObject googleRespond = new JSONObject(googleStringResponse);

        // check status
        String status = (String) googleRespond.get("status");
        if (!status.equals("OK")) {
            errorInGoogleRequest(response, status, googleRespond);
            return;
        }

//        // status is OK, building route
//        String wktRoute = buildWKTResponse(googleRespond);
//
//        // building response to client
//        JSONObject errorObject = new JSONObject();
//        errorObject.put("wkt", wktRoute);
//        errorObject.put("error_message", JSONObject.NULL);
//        response.setContentType("application/json");
//        response.setBody(errorObject);

        response.setContentType("application/json");
        response.setBody(buildResponse(googleRespond));

        // end
        System.out.println("end of post");
    }

    private List<GeoPoint> sortGeoPoints(List<GeoPoint> points){

        List<GeoPoint> res = new ArrayList<>();

        GeoPoint start = points.remove(0);
        res.add(start);

        while(points.size()!=0){

            int minIndex =-1;
            double minDistance = Double.MAX_VALUE;

            for (int i =0; i < points.size(); i ++){
                double distance =  getDistanceFromLatLonInKm(start.latitude, start.longitude,
                        points.get(i).latitude, points.get(i).longitude);

                if (distance < minDistance){
                    minIndex = i;
                    minDistance = distance;
                }

            }

            start = points.remove(minIndex);
            res.add(start);

        }

        return res;

    }

    private double getDistanceFromLatLonInKm(double lat1,double lon1,double lat2,double lon2) {
        int R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    private double  deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

    // build wkt response to client
    private JSONObject buildResponse(JSONObject googleRespond) {

        JSONArray routes = googleRespond.getJSONArray("routes");
        JSONObject route = routes.getJSONObject(0);
        JSONArray legs = route.getJSONArray("legs");

        JSONArray wkt = new JSONArray();
        for (int i = 0; i < legs.length(); i++) {
            JSONArray steps = legs.getJSONObject(i).getJSONArray("steps");

            String lineRes = "LINESTRING(";

            for (int j = 0; j < steps.length(); j++) {
                JSONObject polyline = steps.getJSONObject(j).getJSONObject("polyline");
                String polylineValue = polyline.getString("points");
                if (!polylineValue.equals(JSONObject.NULL)) {
                    List<GeoPoint> points = decodePoly(polylineValue);

                    for (int k = 0; k < points.size(); k++) {
                        lineRes += points.get(k).toWKTString() + ", ";
                    }

                } else {
                    int totalIndex = i * steps.length() + j;
                    System.out.println("polyline_" + totalIndex + " = " + "ERROR");
                }
            }
            lineRes = lineRes.substring(0, lineRes.length() - 2);
            lineRes += ")";

            JSONObject line = new JSONObject();
            line.put("leg", lineRes);

            wkt.put(line);
        }

        JSONObject res = new JSONObject();
        res.put("legs", wkt);

        return res;
    }

    // build google request from pois
    private String googleRequest(List<GeoPoint> way) throws IOException {

        GeoPoint start = way.remove(0);
        GeoPoint end = way.remove(way.size() - 1);

        String wayPoints;
        if (way.size() != 0) {
            wayPoints = "waypoints=";
            for (int i = 0; i < way.size(); i++) {
                wayPoints += way.get(i) + "|";
            }
            wayPoints = wayPoints.substring(0, wayPoints.length() - 1);
            wayPoints += "&";
        } else {
            wayPoints = "";
        }

        URL googleApi = new URL("https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + start + "&" +
                "destination=" + end + "&" +
                "mode=walking" + "&" +
                wayPoints +
                "key=" + GOOGLE_API_KEY);
        URLConnection gc = googleApi.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        gc.getInputStream()));
        String inputLine;
        StringBuilder jb2 = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
            jb2.append(inputLine);
        in.close();

        return jb2.toString();
    }

    // alert - google error
    private void errorInGoogleRequest(Response response, String status, JSONObject googleRespond) {

        System.out.println("Error in google api request. Status: " + status);
        // send back to the client alert with error text
        String error;
        if (googleRespond.has("error_massage")) {
            error = (String) googleRespond.get("error_message");
        } else {
            error = "Ошибка сервера. Повторите попытку позднее.";
        }
        JSONObject errorObject = new JSONObject();
        errorObject.put("wkt", JSONObject.NULL);
        errorObject.put("error_message", error);
        response.setContentType("application/json");
        response.setBody(errorObject);
        System.out.println("Error to client: " + errorObject.toString());
        System.out.println("end of post");

    }

    // alert - not enough points
    private void errorNotEnoughPointsForRoute(Response response) {
        System.out.println("Error in server request.");
        String error = "Для маршрута нужно как минимум две точки.";
        JSONObject errorObject = new JSONObject();
        errorObject.put("wkt", JSONObject.NULL);
        errorObject.put("error_message", error);
        response.setContentType("application/json");
        response.setBody(errorObject);
        System.out.println("Error to client: " + errorObject.toString());
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
    private class GeoPoint {

        public double latitude;
        public double longitude;

        public GeoPoint(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return latitude + "," + longitude;
        }

        public String toWKTString() {
            return latitude + " " + longitude;
        }
    }

// build wkt response to client
//    private String buildWKTResponse(JSONObject googleRespond) {
//
//        //'MULTILINESTRING((1 1, 3 3, 5 5),(3 3, 5 5, 7 7))';
//
//        JSONArray routes = googleRespond.getJSONArray("routes");
//        JSONObject route = routes.getJSONObject(0);
//        JSONArray legs = route.getJSONArray("legs");
//
//        String multiLineRes = "MULTILINESTRING(";
//
//        for (int i = 0; i < legs.length(); i++) {
//            JSONArray steps = legs.getJSONObject(i).getJSONArray("steps");
//
//            String lineRes = "(";
//
//            for (int j = 0; j < steps.length(); j++) {
//                JSONObject polyline = steps.getJSONObject(j).getJSONObject("polyline");
//                String polylineValue = polyline.getString("points");
//                if (!polylineValue.equals(JSONObject.NULL)) {
//                    List<GeoPoint> points = decodePoly(polylineValue);
//
//                    for (int k = 0; k < points.size(); k++) {
//                        lineRes += points.get(k).toWKTString() + ", ";
//                    }
//
//                } else {
//                    int totalIndex = i * steps.length() + j;
//                    System.out.println("polyline_" + totalIndex + " = " + "ERROR");
//                }
//            }
//
//            lineRes = lineRes.substring(0, lineRes.length() - 2);
//            lineRes += "),";
//            multiLineRes += lineRes;
//
//        }
//
//        multiLineRes = multiLineRes.substring(0, multiLineRes.length() - 1);
//        multiLineRes += ")";
//
//        return multiLineRes;
//    }
}
