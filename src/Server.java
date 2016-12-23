import io.netty.handler.codec.http.HttpMethod;
import org.restexpress.RestExpress;

import java.io.IOException;


public class Server {
    //    private static final int SERVER_PORT = 8080;
    private static final int SERVER_PORT = 8889;

    public static RestExpress startServer(String[] args) throws Exception {
        RestExpress server = new RestExpress();
        Cities c = new Cities();
        Spots s = new Spots();
        Route r = new Route();

        server.uri("/cities", c)
                .method(HttpMethod.GET)
                .noSerialization();

        server.uri("/pois", s)
                .method(HttpMethod.GET)
                .noSerialization();

        server.uri("/route", r)
                .method(HttpMethod.POST)
                .noSerialization();

        if (args.length >= 1) {
            int port = Integer.valueOf(args[0]);
            server.bind(port);
        }
        else {
            server.bind(SERVER_PORT);
        }

        return server;
    }

    public static void main(String[] args) throws IOException {
        try {
            RestExpress server = startServer(args);
            System.out.println("Listening...");

            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // metadata
//        MetadataExtractor me = new MetadataExtractor(new URL("http://res.cloudinary.com/megacloud/image/upload/v1482430579/Berlin/mi-20160422-182633_ygntum.jpg").openStream());
//        System.out.println(me.getLat());
//        System.out.println(me.getLon());

        // добавление городов
//        Data d = new Data();
//        d.addCity("Berlin", "Germany");

        // добавление спотов
//        Data d = new Data();
//        d.addSpot("Brandenburg Gate", "52.516274", "13.377707", 5);
//        d.addSpot("Reichstag Building", "52.518594", "13.376176", 5);
//        d.addSpot("St. Marys Church", "52.520605", "13.407111", 5);

        // добавление вист
//        Data d = new Data();
//        //System.out.println(d.getSpots(2));
//        int id = 22;
//        String[] paths = new String[]{
//                "http://res.cloudinary.com/megacloud/image/upload/v1482430587/Berlin/mi-20160422-203242_gvwuhz.jpg",
//                "http://res.cloudinary.com/megacloud/image/upload/v1482430578/Berlin/mi-20160422-203535_jupcpv.jpg"};
//        for (String p : paths) {
//            if (d.addVista(p, id))
//                System.out.println(p + " added");
//        }
    }
}
