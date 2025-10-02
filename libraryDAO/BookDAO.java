package libraryDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private Connection conn;

    public BookDAO(Connection conn) {
        this.conn = conn;
    }

    // Thêm sách mới
    public boolean addBook(String name, String author, String year, String category, int quantity) {
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT COUNT(*) FROM books WHERE name=?")) {
            check.setString(1, name);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("AddBook error: sách đã tồn tại");
                return false;
            }
        } catch (Exception e) {
            System.out.println("AddBook check error: " + e.getMessage());
            return false;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO books(name,author,year,category,quantity,borrowed) VALUES(?,?,?,?,?,0)")) {
            ps.setString(1, name);
            ps.setString(2, author);
            ps.setString(3, year);
            ps.setString(4, category);
            ps.setInt(5, quantity);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("AddBook error: " + e.getMessage());
            return false;
        }
    }

    // Sửa sách
    public boolean editBook(String name, String author, String year, String category, int quantity) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE books SET author=?, year=?, category=?, quantity=? WHERE name=?")) {
            ps.setString(1, author);
            ps.setString(2, year);
            ps.setString(3, category);
            ps.setInt(4, quantity);
            ps.setString(5, name);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("EditBook error: " + e.getMessage());
            return false;
        }
    }

    // Xóa sách
    public boolean deleteBook(String name) {
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT borrowed FROM books WHERE name=?")) {
            check.setString(1, name);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt("borrowed") > 0) {
                System.out.println("DeleteBook error: sách đang được mượn, không thể xóa");
                return false;
            }
        } catch (Exception e) {
            System.out.println("DeleteBook check error: " + e.getMessage());
            return false;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM books WHERE name=?")) {
            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("DeleteBook error: " + e.getMessage());
            return false;
        }
    }

    // Lấy danh sách sách
    public List<String> listBooks() {
        List<String> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM books ORDER BY name ASC")) {
            while (rs.next()) {
                String row = "BOOK|" + rs.getString("name") + "|" +
                        rs.getString("author") + "|" +
                        rs.getString("year") + "|" +
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

    // Giảm số lượng sách
    public boolean decreaseQuantity(String name, int amount) {
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT quantity FROM books WHERE name=?")) {
            check.setString(1, name);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt("quantity") < amount) {
                System.out.println("DecreaseQuantity error: không đủ sách để mượn");
                return false;
            }
        } catch (Exception e) {
            System.out.println("DecreaseQuantity check error: " + e.getMessage());
            return false;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE books SET quantity = quantity - ?, borrowed = borrowed + ? WHERE name = ?")) {
            ps.setInt(1, amount);
            ps.setInt(2, amount);
            ps.setString(3, name);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("DecreaseQuantity error: " + e.getMessage());
            return false;
        }
    }

    // Tăng số lượng sách
    public boolean increaseQuantity(String name, int amount) {
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT borrowed FROM books WHERE name=?")) {
            check.setString(1, name);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt("borrowed") < amount) {
                System.out.println("IncreaseQuantity error: borrowed < amount trả");
                return false;
            }
        } catch (Exception e) {
            System.out.println("IncreaseQuantity check error: " + e.getMessage());
            return false;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE books SET quantity = quantity + ?, borrowed = borrowed - ? WHERE name = ?")) {
            ps.setInt(1, amount);
            ps.setInt(2, amount);
            ps.setString(3, name);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("IncreaseQuantity error: " + e.getMessage());
            return false;
        }
    }
}
