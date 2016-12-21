import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.restexpress.Request;
import org.restexpress.Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

public class CityImage
{
    //private final static String abs_path = "/root/server/PhotoTour/images/city/";
    private final static String abs_path = "C:/Users/Dmitry/Desktop/GeoLast/PhotoTour/images/city/";

    public ChannelBuffer read(Request req, Response res) {

        String image = req.getHeader("image_uri");

        File imFile = new File(abs_path + image);

        System.out.print(imFile.exists());

        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(imFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        byte[] bytes = null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            ImageIO.write(bufferedImage, "jpg", baos);
            baos.flush();
            bytes = baos.toByteArray();
            baos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ChannelBuffer buf = ChannelBuffers.wrappedBuffer(bytes);
        res.addHeader("Content-Length", String.valueOf(buf.capacity()));
        res.setContentType("image/jpg");

        return buf;

    }
}
