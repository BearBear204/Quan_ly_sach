package libraryDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {
    private final Connection conn;

    public BorrowDAO(Connection conn) {
        this.conn = conn;
    }

    // User gửi yêu cầu mượn sách → PENDING
    public boolean addBorrowRequest(String username, String bookName) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO borrows(username, bookName, borrowDate, returnDate, status) " +
                "VALUES(?,?,datetime('now','localtime','+7 hours'),NULL,'PENDING')")) {
            ps.setString(1, username);
            ps.setString(2, bookName);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("AddBorrowRequest error: " + e.getMessage());
            return false;
        }
    }

    // Admin xác nhận mượn → đổi PENDING -> BORROWED, trừ quantity, tăng borrowed
    public boolean confirmBorrow(String username, String bookName) {
        try {
            conn.setAutoCommit(false);

            // Kiểm tra sách còn số lượng
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT quantity FROM books WHERE name=?")) {
                check.setString(1, bookName);
                ResultSet rs = check.executeQuery();

                if (!rs.next()) {
                    System.out.println("ConfirmBorrow error: sách không tồn tại");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }

                int quantity = rs.getInt("quantity");
                rs.close();

                if (quantity <= 0) {
                    System.out.println("ConfirmBorrow error: không còn sách để mượn");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }
            }

            // Cập nhật trạng thái borrow
            try (PreparedStatement updStatus = conn.prepareStatement(
                    "UPDATE borrows SET status='BORROWED', borrowDate=datetime('now','localtime','+7 hours') " +
                            "WHERE username=? AND bookName=? AND status='PENDING'")) {
                updStatus.setString(1, username);
                updStatus.setString(2, bookName);
                int updated = updStatus.executeUpdate();

                if (updated == 0) {
                    System.out.println("ConfirmBorrow error: không tìm thấy yêu cầu mượn");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }
            }

            // Giảm quantity và tăng borrowed
            try (PreparedStatement updBook = conn.prepareStatement(
                    "UPDATE books SET borrowed=borrowed+1, quantity=quantity-1 WHERE name=?")) {
                updBook.setString(1, bookName);
                int bookUpdated = updBook.executeUpdate();

                if (bookUpdated == 0) {
                    System.out.println("ConfirmBorrow error: không thể cập nhật sách");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("ConfirmBorrow: thành công cho " + username + " mượn sách " + bookName);
            return true;
        } catch (Exception e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (Exception ex) {}
            System.out.println("ConfirmBorrow error: " + e.getMessage());
            return false;
        }
    }

    // Update status (dùng cho REJECTED)
    public boolean updateStatus(String username, String bookName, String newStatus) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE borrows SET status=? WHERE username=? AND bookName=? AND status='PENDING'")) {
            ps.setString(1, newStatus);
            ps.setString(2, username);
            ps.setString(3, bookName);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("UpdateStatus error: " + e.getMessage());
            return false;
        }
    }

    // User gửi yêu cầu trả → RETURN_REQUEST
    public boolean requestReturn(String username, String bookName) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE borrows SET status='RETURN_REQUEST' " +
                        "WHERE username=? AND bookName=? AND status='BORROWED'")) {
            ps.setString(1, username);
            ps.setString(2, bookName);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("RequestReturn error: " + e.getMessage());
            return false;
        }
    }

    // Admin xác nhận trả → RETURNED, tăng quantity, giảm borrowed
    public boolean confirmReturn(String username, String bookName) {
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement updStatus = conn.prepareStatement(
                    "UPDATE borrows SET status='RETURNED', returnDate=datetime('now','localtime','+7 hours') " +
                            "WHERE username=? AND bookName=? AND status='RETURN_REQUEST'")) {
                updStatus.setString(1, username);
                updStatus.setString(2, bookName);
                int updated = updStatus.executeUpdate();

                if (updated == 0) {
                    System.out.println("ConfirmReturn error: không tìm thấy yêu cầu trả");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }
            }

            try (PreparedStatement updBook = conn.prepareStatement(
                    "UPDATE books SET borrowed=borrowed-1, quantity=quantity+1 WHERE name=?")) {
                updBook.setString(1, bookName);
                int bookUpdated = updBook.executeUpdate();

                if (bookUpdated == 0) {
                    System.out.println("ConfirmReturn error: không thể cập nhật sách");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    return false;
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("ConfirmReturn: thành công cho " + username + " trả sách " + bookName);
            return true;
        } catch (Exception e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (Exception ex) {}
            System.out.println("ConfirmReturn error: " + e.getMessage());
            return false;
        }
    }

    public List<String> listBorrows(String username) {
        List<String> list = new ArrayList<>();
        try {
            PreparedStatement ps;
            if ("ALL".equals(username)) {
                ps = conn.prepareStatement("SELECT * FROM borrows ORDER BY borrowDate DESC");
            } else {
                ps = conn.prepareStatement("SELECT * FROM borrows WHERE username=? ORDER BY borrowDate DESC");
                ps.setString(1, username);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String row = rs.getString("username") + "|" +
                        rs.getString("bookName") + "|" +
                        rs.getString("borrowDate") + "|" +
                        (rs.getString("returnDate") == null ? "Chưa trả" : rs.getString("returnDate")) + "|" +
                        rs.getString("status");
                list.add(row);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            System.out.println("ListBorrows error: " + e.getMessage());
        }
        return list;
    }

    public List<String> listPendingRequests() {
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM borrows WHERE status='PENDING' OR status='RETURN_REQUEST' ORDER BY borrowDate DESC")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String row = rs.getString("username") + "|" +
                        rs.getString("bookName") + "|" +
                        rs.getString("borrowDate") + "|" +
                        (rs.getString("returnDate") == null ? "Chưa trả" : rs.getString("returnDate")) + "|" +
                        rs.getString("status");
                list.add(row);
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("ListPendingRequests error: " + e.getMessage());
        }
        return list;
    }
}
