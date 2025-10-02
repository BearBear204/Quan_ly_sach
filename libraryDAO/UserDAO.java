package libraryDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean register(String username, String password) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users(username,password,role,locked) VALUES(?,?,?,0)")) {
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
                "SELECT role, locked FROM users WHERE username=? AND password=?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("locked") == 1) {
                    System.out.println("Login error: user bị khóa");
                    return null;
                }
                return rs.getString("role");
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return null;
    }

    public boolean changePassword(String username, String newPassword) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET password=? WHERE username=?")) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("ChangePassword error: " + e.getMessage());
            return false;
        }
    }

    public boolean lockUser(String username) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET locked=1 WHERE username=?")) {
            ps.setString(1, username);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("LockUser error: " + e.getMessage());
            return false;
        }
    }

    public List<String> listUsers() {
        List<String> list = new ArrayList<>();
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT username, role FROM users");
            while (rs.next()) {
                String row = rs.getString("username") + "|" + rs.getString("role");
                list.add(row);
            }
        } catch (Exception e) {
            System.out.println("ListUsers error: " + e.getMessage());
        }
        return list;
    }
}
