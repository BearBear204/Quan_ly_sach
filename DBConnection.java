package library;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static final String URL = "jdbc:sqlite:C:/Users/vuduc/Downloads/LTM/CuoiKy/library.db";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
