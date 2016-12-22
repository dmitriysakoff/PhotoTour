import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class MetadataExtractor {
    private Metadata metadata;
    private double lat;
    private double lon;
    //private String altitude;
    private String allInfo;

    public MetadataExtractor(InputStream inputStream) {
        try {
            // считываем метаданные
            metadata = ImageMetadataReader.readMetadata(inputStream);
            this.allInfo = "";
            // парсим
            parse();
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getAllInfo() {
        return allInfo;
    }

    private void parse() throws IOException, ImageProcessingException {
        for (Directory directory : metadata.getDirectories()) {
            if ("GPS".equals(directory.getName()))
                parseGPS(directory.getTags());
            // todo: добавить дату и город из Exif SubIFD - Date/Time Original

            for (Tag tag : directory.getTags())
                this.allInfo += directory.getName() + " - " + tag.getTagName() + " = " + tag.getDescription() + "\n";
        }
    }

    private void parseGPS(Collection<Tag> tags) {
        double lat = 0, lon = 0;
        boolean N = true, E = true;
        for (Tag tag : tags) {
            String desc = tag.getDescription();
            //System.out.println(desc);
            switch (tag.getTagName()) {
                case "GPS Latitude Ref":
                    N = "N".equals(desc);
                    break;
                case "GPS Longitude Ref":
                    E = "E".equals(desc);
                    break;
                case "GPS Latitude":
                    lat = DMStoDouble(desc);
                    break;
                case "GPS Longitude":
                    lon = DMStoDouble(desc);
                    break;
//                case "GPS Altitude":
//                    this.altitude = desc;
//                    break;
            }
        }
        // с учётом полушария
        this.lat = lat * (N ? 1 : -1);
        this.lon = lon * (E ? 1 : -1);
    }

    private double DMStoDouble(String dms) {
        // отделяем градусы, минуты и секунды
        String[] arr = dms.split(" ");
        // отрезаем обозначения
        for (int i = 0; i < arr.length; ++i)
            arr[i] = arr[i].substring(0, arr[i].length() - 1);
        // заменим запятую на точку у секунд
        arr[2] = arr[2].replace(',', '.');
        // градусы + (минуты * 60 + секунды) / 3600
        return Double.parseDouble(arr[0]) + (Double.parseDouble(arr[1]) * 60 + Double.parseDouble(arr[2])) / 3600.0;
    }
}