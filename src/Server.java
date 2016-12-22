import io.netty.handler.codec.http.HttpMethod;
import org.restexpress.RestExpress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


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

    public static void main(String[] args) throws FileNotFoundException {
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

//        MetadataExtractor me = new MetadataExtractor(new FileInputStream("../Mike-i-20150426-150754.jpg"));
//        System.out.println(me.getAllInfo());

//        Data d = new Data();
//        d.addSpot("Saint Basils Cathedral", "55.752524", "37.623087", 1);
//        d.addSpot("Moscow City", "55.748670", "37.539751", 1);
//        d.addSpot("Cathedral of Christ the Saviour", "55.744583", "37.605322", 1);
//        d.addSpot("Moscow Kremlin", "55.751939", "37.617564", 1);
//        d.addSpot("Zhivopisny Bridge", "55.777094", "37.445113", 1);
//        d.addSpot("Radisson Hotel", "55.751547", "37.565577", 1);

//        Data d = new Data();
//        //System.out.println(d.getSpots(2));
//        int id = 17;
//        String[] paths = new String[]{
//                "http://res.cloudinary.com/megacloud/image/upload/v1482420579/Mike-i-20150107-010826_e51fal.jpg"};
//        for (String p : paths) {
//            if (d.addVista(p, id))
//                System.out.println(p + " added");
//        }
    }
}
