package libraryDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {
    private Connection conn;

    public BorrowDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean borrowBook(String username, String bookName) {
        try {
            conn.setAutoCommit(false);

            // kiểm tra còn sách
            PreparedStatement check = conn.prepareStatement(
                "SELECT quantity, borrowed FROM books WHERE name=?");
            check.setString(1, bookName);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                conn.rollback(); conn.setAutoCommit(true); return false;
            }
            int quantity = rs.getInt("quantity");
            int borrowed = rs.getInt("borrowed");
            if (borrowed >= quantity) {
                conn.rollback(); conn.setAutoCommit(true); return false;
            }

            // ghi lịch sử mượn (GMT+7)
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO borrows(username, bookName, borrowDate, returnDate) VALUES(?,?,datetime('now','localtime','+7 hours'),NULL)");
            ps.setString(1, username);
            ps.setString(2, bookName);
            ps.executeUpdate();
            ps.close();

            // cập nhật số borrowed
            PreparedStatement upd = conn.prepareStatement(
                "UPDATE books SET borrowed=borrowed+1 WHERE name=?");
            upd.setString(1, bookName);
            upd.executeUpdate();
            upd.close();

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (Exception ex) {}
            System.out.println("BorrowBook error: " + e.getMessage());
            return false;
        }
    }

    public boolean returnBook(String username, String bookName) {
        try {
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(
                "UPDATE borrows SET returnDate=datetime('now','localtime','+7 hours') " +
                "WHERE username=? AND bookName=? AND returnDate IS NULL");
            ps.setString(1, username);
            ps.setString(2, bookName);
            int updated = ps.executeUpdate();
            ps.close();
            if (updated == 0) {
                conn.rollback(); conn.setAutoCommit(true);
                return false;
            }

            PreparedStatement upd = conn.prepareStatement(
                "UPDATE books SET borrowed=borrowed-1 WHERE name=?");
            upd.setString(1, bookName);
            upd.executeUpdate();
            upd.close();

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (Exception ex) {}
            System.out.println("ReturnBook error: " + e.getMessage());
            return false;
        }
    }

    public List<String> listBorrows(String username) {
        List<String> list = new ArrayList<>();
        try {
            PreparedStatement ps;
            if(username.equals("ALL")) {
                ps = conn.prepareStatement("SELECT * FROM borrows ORDER BY borrowDate DESC");
            } else {
                ps = conn.prepareStatement("SELECT * FROM borrows WHERE username=? ORDER BY borrowDate DESC");
                ps.setString(1, username);
            }
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String row = rs.getString("username") + "|" +
                             rs.getString("bookName") + "|" +
                             rs.getString("borrowDate") + "|" +
                             (rs.getString("returnDate")==null ? "Chưa trả" : rs.getString("returnDate"));
                list.add(row);
            }
            rs.close(); ps.close();
        } catch(Exception e) {
            System.out.println("ListBorrows error: " + e.getMessage());
        }
        return list;
    }
}
