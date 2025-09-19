package libraryDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private Connection conn;

    public BookDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean addBook(String name, String author, int year, String category, int quantity) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO books(name,author,year,category,quantity,borrowed) VALUES(?,?,?,?,?,0)")) {
            ps.setString(1, name);
            ps.setString(2, author);
            ps.setInt(3, year);
            ps.setString(4, category);
            ps.setInt(5, quantity);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("AddBook error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBook(String name, String author, int year, String category, int quantity) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE books SET author=?, year=?, category=?, quantity=? WHERE name=?")) {
            ps.setString(1, author);
            ps.setInt(2, year);
            ps.setString(3, category);
            ps.setInt(4, quantity);
            ps.setString(5, name);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("UpdateBook error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBook(String name) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE name=?")) {
            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("DeleteBook error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBorrowed(String bookName, int delta) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE books SET borrowed=borrowed+? WHERE name=?")) {
            ps.setInt(1, delta);
            ps.setString(2, bookName);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("UpdateBorrowed error: " + e.getMessage());
            return false;
        }
    }

    public List<String> listBooks() {
        List<String> list = new ArrayList<>();
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM books");
            while (rs.next()) {
                String row = rs.getString("name") + "|" +
                             rs.getString("author") + "|" +
                             rs.getInt("year") + "|" +
                             rs.getString("category") + "|" +
                             rs.getInt("quantity") + "|" +
                             rs.getInt("borrowed");
                list.add(row);
            }
        } catch (Exception e) {
            System.out.println("ListBooks error: " + e.getMessage());
        }
        return list;
    }
}
