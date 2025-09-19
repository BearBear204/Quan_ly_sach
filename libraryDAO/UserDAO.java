package libraryDAO;

import java.sql.*;

public class UserDAO {
    private Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean register(String username, String password) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users(username,password,role) VALUES(?,?,?)")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, "user");
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Register error: " + e.getMessage());
            return false;
        }
    }

    public String login(String username, String password) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT role FROM users WHERE username=? AND password=?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("role");
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return null;
    }
}
