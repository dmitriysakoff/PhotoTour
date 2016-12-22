import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.sql.*;

public class Data {

    public static File getFile(String path) {
        return new File(path);
    }

    private static final String URL = "jdbc:postgresql://188.166.36.161:5432/photos";
    private static final String USER = "postgres";
    private static final String PASSWORD = "pass17";
    private static Connection conn;

    public Data() {
        try {
            // ставим драйвер
            Class.forName("org.postgresql.Driver");
            // подключаемся к бд
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для получения всех вист конкретного спота
     *
     * @param spotId идентификатор спота из бд
     * @return строковое представление json массива всех вист
     */
    public String getVistas(int spotId) {
        return getVistas(String.valueOf(spotId));
    }

    /**
     * Получение всех вист конкретного спота
     *
     * @param spotId идентификатор спота из бд
     * @return строковое представление json массива всех вист
     */
    public String getVistas(String spotId) {
        JSONArray vistas = new JSONArray();
        try (Statement s = conn.createStatement()) {
            ResultSet res = s.executeQuery("select ST_X(geom) lat, ST_Y(geom) lon, photo_path from vistas where spot_id = " + spotId + ";");
            while (res.next()) {
                // получаем данные очередной висты
                double lat = res.getDouble("lat");
                double lon = res.getDouble("lon");
                String path = res.getString("photo_path");
                // создаём объект висты и набиваем
                JSONObject vista = new JSONObject()
                        .put("url", path)                   // путь к картинке
                        .put("location", new JSONObject()   // геопозиция
                                .put("latitude", lat)       // широта
                                .put("longitude", lon))     // долгота
                        .put("options", "");                // доп инфа
                // засовываем к другим вистам
                vistas.put(vista);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vistas.toString();
    }

    /**
     * Метод для получения всех спотов конкретного города
     *
     * @param cityId идентификатор города из бд
     * @return строковое представление json массива всех спотов
     */
    public String getSpots(int cityId) {
        return getSpots(String.valueOf(cityId));
    }

    /**
     * Метод для получения всех спотов конкретного города
     *
     * @param cityId идентификатор города из бд
     * @return строковое представление json массива всех спотов
     */
    public String getSpots(String cityId) {
        JSONArray spots = new JSONArray();
        try (Statement s = conn.createStatement()) {
            ResultSet res = s.executeQuery("select id, name, ST_X(geom) lat, ST_Y(geom) lon from spots where city_id = " + cityId + ";");
            while (res.next()) {
                // получаем данные очередного спота
                String id = res.getString("id");
                String name = res.getString("name");
                double lat = res.getDouble("lat");
                double lon = res.getDouble("lon");
                // создаём объект спота и набиваем
                JSONObject spot = new JSONObject()
                        .put("id", id)                                  // id строкой
                        .put("name", name)                              // название спота
                        .put("location", new JSONObject()               // геопозиция
                                .put("latitude", lat)                   // широта
                                .put("longitude", lon))                 // долгота
                        .put("photos", new JSONArray(getVistas(id)));   // массив вист
                // засовываем к другим спотам
                spots.put(spot);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return spots.toString();
    }

    /**
     * Метод для получения всех городов базы данных.
     * Если город не имеет спотов или вист, он в массив не попадёт
     *
     * @return строковое представление json массива всех городов
     */
    public String getCities() {
        JSONArray cities = new JSONArray();
        try (Statement s = conn.createStatement()) {
            ResultSet res = s.executeQuery("select * from cities");
            while (res.next()) {
                // получаем данные очередного города
                int id = res.getInt("id");
                String name = res.getString("name");
                String photo_path;
                double[] latlon;
                try {
                    latlon = getCityLatLon(id);
                    photo_path = getCityPhoto(id);
                } catch (IllegalArgumentException e) {
                    // если в бд мало данных по городу, не записываем его
                    continue;
                }
                // создаём объект города и набиваем
                JSONObject city = new JSONObject()
                        .put("id", String.valueOf(id))          // id строкой
                        .put("name", name)                      // название
                        .put("location", new JSONObject()       // геопозиция
                                .put("latitude", latlon[0])     // широта
                                .put("longitude", latlon[1]))   // долгода
                        .put("photo", photo_path);
                // засовываем к другим городам
                cities.put(city);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cities.toString();
    }

    /**
     * Метод для получения широты и долготы города
     * Рассчитывается как средняя точка между всеми спотами города
     *
     * @param cityId идентификатор города
     * @return двумерный массив с широтой и долготой
     * @throws IllegalArgumentException если у города нет спотов
     */
    public double[] getCityLatLon(int cityId) throws IllegalArgumentException {
        double[] latlon = new double[2];
        int count = 0; // количество спотов
        try (Statement s = conn.createStatement()) {
            ResultSet res = s.executeQuery("select ST_X(geom) lat, ST_Y(geom) lon from spots where city_id = " + cityId + ";");
            while (res.next()) {
                latlon[0] += res.getDouble("lat");
                latlon[1] += res.getDouble("lon");
                ++count;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (count == 0)
            throw new IllegalArgumentException("Нет спотов для данного города");
        // берём среднее
        latlon[0] /= count;
        latlon[1] /= count;
        return latlon;
    }

    /**
     * Метод для получения одной из фотографий города
     * Берётся случайная
     *
     * @param cityId идентификатор города
     * @return путь к фотографии
     * @throws IllegalArgumentException если у города нет фотографий
     */
    public String getCityPhoto(int cityId) throws IllegalArgumentException {
        try (Statement s = conn.createStatement()) {
            ResultSet res = s.executeQuery("select v.photo_path from spots s, vistas v where s.id = v.spot_id and s.city_id = 2 order by RANDOM()");
            if (res.next())
                return res.getString("photo_path");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Нет картинок для данного города");
    }




    public boolean addCountry(String name) {
        try (Statement s = conn.createStatement()) {
            // если страна уже есть в таблице, возвращаем true
            if (getCountryId(name) != -1)
                return true;
            // добавляем страну
            s.executeUpdate("insert into countries (name) values ('" + name + "');");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int getCountryId(String name) {
        try (Statement s = conn.createStatement()) {
            // ищем id страны
            ResultSet res = s.executeQuery("select id from countries where name='" + name + "';");
            // пытаемся достать id
            if (res.next())
                return res.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean addCity(String name, String countryName) {
        try (Statement s = conn.createStatement()) {
            // если страну не вышло внести в таблицу, город тоже не вносим
            if (!addCountry(countryName))
                return false;
            // вытавкиваем id страны
            int id = getCountryId(countryName);
            // передаём название и id для связи
            return addCity(name, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addCity(String name, int countryId) {
        try (Statement s = conn.createStatement()) {
            // если город уже есть в таблице, возвращаем true
            if (getCityId(name) != -1)
                return true;
            // добавляем город
            s.executeUpdate("insert into cities (name, country_id) values ('" + name + "', " + countryId + ");");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int getCityId(String name) {
        try (Statement s = conn.createStatement()) {
            // ищем id города
            ResultSet res = s.executeQuery("select id from cities where name='" + name + "';");
            // пытаемся достать id
            if (res.next())
                return res.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean addSpot(String name, String lat, String lon, int cityId) {
        String geom = "ST_GeomFromText('POINT(" + lat + " " + lon + ")', 26910)";
        try (Statement s = conn.createStatement()) {
            s.executeUpdate("insert into spots (name, geom, city_id) VALUES ('" + name + "', " + geom + ", " + cityId + ");");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int getSpotId(String name) {
        try (Statement s = conn.createStatement()) {
            // ищем id спота
            ResultSet res = s.executeQuery("select id from spots where name='" + name + "';");
            // пытаемся достать id
            if (res.next())
                return res.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean addVista(String path, int spotId) {
        // парсим файл
        MetadataExtractor me = new MetadataExtractor(getFile(path));
        // достаём широту и долготу и запихиваем в sql функцию wkt
        String geom = "ST_GeomFromText('POINT(" + me.getLat() + " " + me.getLon() + ")', 26910)";
        try (Statement s = conn.createStatement()) {
            s.executeUpdate("insert into vistas (geom, spot_id, photo_path) VALUES (" + geom + ", " + spotId + ", '" + path + "');");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
