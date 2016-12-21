import io.netty.handler.codec.http.HttpMethod;
import org.restexpress.RestExpress;

public class Server {
    public static RestExpress startServer(String[] args) throws Exception {
        RestExpress server = new RestExpress();
        Cities c = new Cities();
        CityImage ci = new CityImage();

        server.uri("/cities", c)
                .method(HttpMethod.GET)
                .noSerialization();

        server.uri("/images/city", ci)
                .method(HttpMethod.GET)
                .noSerialization();

        server.bind(8080);
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
