import io.netty.handler.codec.http.HttpMethod;
import org.restexpress.RestExpress;

public class Server {

    private static final int SERVER_PORT = 8080;
//    private static final int SERVER_PORT = 8889;

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
                .method(HttpMethod.POST);

        if(args.length >= 1){
            int port = Integer.valueOf(args[0]);
            server.bind(port);
        } else {
            server.bind(SERVER_PORT);
        }

        return server;
    }

    public static void main(String[] args) {
        try {
            RestExpress server = startServer(args);
            System.out.println("Listening...");

            while (true){
                try { Thread.sleep(1000);}
                catch (Exception ex){}
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
