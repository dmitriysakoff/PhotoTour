import io.netty.handler.codec.http.HttpMethod;
import org.restexpress.RestExpress;

public class Server {

//    private static final int SERVER_PORT = 8080;
    private static final int SERVER_PORT = 8889;

    public static RestExpress startServer(String[] args) throws Exception {
        RestExpress server = new RestExpress();
        Cities c = new Cities();
        Route r = new Route();
        CityImage ci = new CityImage();

        server.uri("/cities", c)
                .method(HttpMethod.GET)
                .noSerialization();

        server.uri("/route", r)
                .method(HttpMethod.POST);

        server.uri("/images/city", ci)
                .method(HttpMethod.GET);

        server.bind(SERVER_PORT);
        return server;
    }

    public static void main(String[] args) {
        try {
            RestExpress server = startServer(args);
            System.out.println("Hit enter to stop it...");
            System.in.read();
            server.shutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
