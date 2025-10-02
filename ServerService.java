package library;

import libraryDAO.BookDAO;
import libraryDAO.BorrowDAO;
import libraryDAO.UserDAO;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerService {
    private final UserDAO userDAO;
    private final BookDAO bookDAO;
    private final BorrowDAO borrowDAO;
    private final List<BufferedWriter> clientWriters;

    public ServerService(UserDAO userDAO, BookDAO bookDAO, BorrowDAO borrowDAO, List<BufferedWriter> clientWriters) {
        this.userDAO = userDAO;
        this.bookDAO = bookDAO;
        this.borrowDAO = borrowDAO;
        this.clientWriters = clientWriters;
    }

    public boolean processCommand(String line, String currentUser, BufferedWriter bw) throws IOException {
        boolean changed = false;

        if (line.startsWith("LOGIN ")) {
            handleLogin(line, bw);

        } else if (line.startsWith("REGISTER ")) {
            handleRegister(line, bw);

        } else if (line.startsWith("BORROW_REQUEST|")) {
            changed = handleBorrowRequest(line, bw);

        } else if (line.startsWith("BORROW_APPROVE|")) {
            changed = handleBorrowApprove(line, bw);

        } else if (line.startsWith("BORROW_REJECT|")) {
            changed = handleBorrowReject(line, bw);

        } else if (line.equals("GET_PENDING")) {
            sendPendingList(bw);

        } else if (line.equals("LIST")) {
            sendBookListForUser(bw);

        } else if (line.equals("LIST_BOOKS")) {
            sendBookListForAdmin(bw);

        } else if (line.equals("LIST_USERS")) {
            sendUserList(bw);

        } else if (line.equals("BORROWS")) {
            sendBorrowList(bw, "ALL");

        } else if (line.equals("BORROWS_USER")) {
            sendBorrowList(bw, currentUser);

        } else if (line.startsWith("RETURN_REQUEST|")) {
            changed = handleReturnRequest(line, bw);

        } else if (line.startsWith("CONFIRM_RETURN|")) {
            changed = handleConfirmReturn(line, bw);

        } else if (line.startsWith("LOCK_USER|")) {
            handleLockUser(line, bw);

        } else if (line.startsWith("CHANGE_PASSWORD ")) {
            handleChangePassword(line, bw);

        } else if (line.startsWith("CHANGE_USER_PASSWORD|")) {
            handleChangeUserPassword(line, bw);

        } else if (line.startsWith("ADD_BOOK|")) {
            changed = handleAddBook(line, bw);

        } else if (line.startsWith("EDIT_BOOK|")) {
            changed = handleEditBook(line, bw);

        } else if (line.startsWith("DELETE_BOOK|")) {
            changed = handleDeleteBook(line, bw);
        }

        return changed;
    }

    private void handleLogin(String line, BufferedWriter bw) throws IOException {
        String[] parts = line.substring(6).split("\\|");
        if (parts.length == 2) {
            String username = parts[0];
            String password = parts[1];
            String role = userDAO.login(username, password);

            if (role != null) {
                sendMessage(bw, "LOGIN_SUCCESS|" + username + "|" + role);
            } else {
                sendMessage(bw, "LOGIN_FAIL|Sai username hoặc mật khẩu");
            }
        } else {
            sendMessage(bw, "LOGIN_FAIL|Sai định dạng");
        }
    }

    private void handleRegister(String line, BufferedWriter bw) throws IOException {
        String[] parts = line.substring(9).split("\\|");
        if (parts.length == 2) {
            boolean ok = userDAO.register(parts[0], parts[1]);
            sendMessage(bw, ok ? "REGISTER_SUCCESS" : "USERNAME_EXISTS");
        } else {
            sendMessage(bw, "REGISTER_FAIL");
        }
    }

 // USER gửi yêu cầu mượn → lưu PENDING, thông báo cho admin
    private boolean handleBorrowRequest(String line, BufferedWriter bw) throws IOException {
        String[] parts = line.split("\\|");
        if (parts.length == 3) {
            String username = parts[1];
            String bookName = parts[2];

            boolean ok = borrowDAO.addBorrowRequest(username, bookName); // status = PENDING
            sendMessage(bw, ok ? "BORROW_REQUEST_SUCCESS|" + bookName 
                               : "BORROW_REQUEST_FAILED|" + bookName);

            if (ok) {
                // 🔥 Gửi lại lịch sử riêng cho user vừa mượn
                sendBorrowList(bw, username);

                // Thông báo có yêu cầu mới để admin biết
                broadcastAllClients("NEW_BORROW_REQUEST|" + username + "|" + bookName);

                // 🔄 Cập nhật realtime cho tất cả client (sách, user, borrow ALL)
                broadcastAllClients(getAllData());
            }
            return ok;
        }
        sendMessage(bw, "BORROW_REQUEST_FAILED");
        return false;
    }

    // ADMIN reject → chuyển PENDING → REJECTED
    private boolean handleBorrowReject(String line, BufferedWriter bw) throws IOException {
        String[] parts = line.split("\\|");
        if (parts.length == 3) {
            String username = parts[1];
            String bookName = parts[2];

            boolean ok = borrowDAO.updateStatus(username, bookName, "REJECTED");
            if (ok) {
                Server.sendToUser(username, "BORROW_UPDATE|" + bookName + "|REJECTED");
                sendMessage(bw, "BORROW_REJECT_SUCCESS|" + username + "|" + bookName);
                broadcastAllClients(getAllData()); // 🔄 realtime
            } else {
                sendMessage(bw, "BORROW_REJECT_FAILED|" + bookName);
            }
            return ok;
        }
        sendMessage(bw, "BORROW_REJECT_FAILED");
        return false;
    }

    // Gửi danh sách sách
    private void sendBookListForUser(BufferedWriter bw) throws IOException {
        bw.write("BOOKS_LIST_START\n");
        for (String book : bookDAO.listBooks()) {
            bw.write(book + "\n");
        }
        bw.write("BOOKS_LIST_END\n");
        bw.flush();
    }

    private void sendBookListForAdmin(BufferedWriter bw) throws IOException {
        bw.write("BOOKS_LIST_START\n");
        for (String book : bookDAO.listBooks()) {
            bw.write(book + "\n");
        }
        bw.write("BOOKS_LIST_END\n");
        bw.flush();
    }

    // Gửi danh sách user
    private void sendUserList(BufferedWriter bw) throws IOException {
        for (String user : userDAO.listUsers()) {
            bw.write("USER|" + user + "\n");
        }
        bw.flush();
    }

    // Gửi lịch sử mượn
    private void sendBorrowList(BufferedWriter bw, String username) throws IOException {
        bw.write("UPDATE_BORROWS_START\n");
        for (String borrow : borrowDAO.listBorrows(username)) {
            bw.write("BORROW|" + borrow + "\n");
        }
        bw.write("UPDATE_BORROWS_END\n");
        bw.flush();
    }

    private void sendPendingList(BufferedWriter bw) throws IOException {
        bw.write("PENDING_LIST_START\n");
        for (String pending : borrowDAO.listPendingRequests()) {
            bw.write("PENDING|" + pending + "\n");
        }
        bw.write("PENDING_LIST_END\n");
        bw.flush();
    }

    // USER yêu cầu trả
    private boolean handleReturnRequest(String line, BufferedWriter bw) throws IOException {
        String[] p = line.split("\\|");
        if (p.length == 3) {
            boolean ok = borrowDAO.requestReturn(p[1], p[2]);
            sendMessage(bw, ok ? "RETURN_REQUEST_SUCCESS|" + p[2] : "RETURN_REQUEST_FAILED|" + p[2]);
            if (ok) broadcastAllClients(getAllData()); // 🔄 realtime
            return ok;
        }
        sendMessage(bw, "RETURN_REQUEST_FAILED");
        return false;
    }

    // ADMIN approve → PENDING → BORROWED
    private boolean handleBorrowApprove(String line, BufferedWriter bw) throws IOException {
        String[] parts = line.split("\\|");
        if (parts.length == 3) {
            String username = parts[1];
            String bookName = parts[2];

            boolean ok = borrowDAO.confirmBorrow(username, bookName);
            if (ok) {
                Server.sendToUser(username, "BORROW_UPDATE|" + bookName + "|APPROVED");
                sendMessage(bw, "BORROW_APPROVE_SUCCESS|" + username + "|" + bookName);
                broadcastAllClients(getAllData()); // 🔄 realtime
            } else {
                sendMessage(bw, "BORROW_APPROVE_FAILED|" + bookName);
            }
            return ok;
        }
        sendMessage(bw, "BORROW_APPROVE_FAILED");
        return false;
    }

    // ADMIN confirm return → RETURN_REQUEST → RETURNED
    private boolean handleConfirmReturn(String line, BufferedWriter bw) throws IOException {
        String[] p = line.split("\\|");
        if (p.length == 3) {
            boolean ok = borrowDAO.confirmReturn(p[1], p[2]);
            if (ok) {
                sendMessage(bw, "CONFIRM_RETURN_SUCCESS|" + p[1] + "|" + p[2]);
                broadcastAllClients(getAllData()); // 🔄 realtime
            } else {
                sendMessage(bw, "CONFIRM_RETURN_FAILED|" + p[2]);
            }
            return ok;
        }
        sendMessage(bw, "CONFIRM_RETURN_FAILED");
        return false;
    }

    // Khóa user
    private void handleLockUser(String line, BufferedWriter bw) throws IOException {
        String[] p = line.split("\\|");
        if (p.length == 2) {
            boolean ok = userDAO.lockUser(p[1]);
            sendMessage(bw, ok ? "LOCK_USER_SUCCESS|" + p[1] : "LOCK_USER_FAILED|" + p[1]);
            if (ok) broadcastAllClients(getAllData());
        } else {
            sendMessage(bw, "LOCK_USER_FAILED");
        }
    }

    // Đổi mật khẩu cá nhân
    private void handleChangePassword(String line, BufferedWriter bw) throws IOException {
        String[] p = line.substring("CHANGE_PASSWORD ".length()).split("\\|");
        if (p.length == 2) {
            boolean ok = userDAO.changePassword(p[0], p[1]);
            sendMessage(bw, ok ? "CHANGE_PASSWORD_SUCCESS" : "CHANGE_PASSWORD_FAILED");
            if (ok) broadcastAllClients(getAllData());
        } else {
            sendMessage(bw, "CHANGE_PASSWORD_FAILED");
        }
    }

    // Admin đổi mật khẩu user
    private void handleChangeUserPassword(String line, BufferedWriter bw) throws IOException {
        String[] p = line.split("\\|");
        if (p.length == 3) {
            boolean ok = userDAO.changePassword(p[1], p[2]);
            sendMessage(bw, ok ? "CHANGE_USER_PASSWORD_SUCCESS|" + p[1] : "CHANGE_USER_PASSWORD_FAILED|" + p[1]);
            if (ok) broadcastAllClients(getAllData());
        } else {
            sendMessage(bw, "CHANGE_USER_PASSWORD_FAILED");
        }
    }

 // Thêm sách
    private boolean handleAddBook(String line, BufferedWriter bw) throws IOException {
        String[] p = line.split("\\|");
        if (p.length == 6) {
            boolean ok = bookDAO.addBook(p[1], p[2], p[3], p[4], Integer.parseInt(p[5]));
            sendMessage(bw, ok ? "ADD_BOOK_SUCCESS|" + p[1] : "ADD_BOOK_FAILED|" + p[1]);
            if (ok) broadcastAllClients(getAllData());
            return ok;
        }
        sendMessage(bw, "ADD_BOOK_FAILED");
        return false;
    }

    private boolean handleEditBook(String line, BufferedWriter bw) throws IOException {
        String[] p = line.split("\\|");
        if (p.length == 6) {
            boolean ok = bookDAO.editBook(p[1], p[2], p[3], p[4], Integer.parseInt(p[5]));
            sendMessage(bw, ok ? "EDIT_BOOK_SUCCESS|" + p[1] : "EDIT_BOOK_FAILED|" + p[1]);
            if (ok) broadcastAllClients(getAllData());
            return ok;
        }
        sendMessage(bw, "EDIT_BOOK_FAILED");
        return false;
    }

    private boolean handleDeleteBook(String line, BufferedWriter bw) throws IOException {
        String[] p = line.split("\\|");
        if (p.length == 2) {
            boolean ok = bookDAO.deleteBook(p[1]);
            sendMessage(bw, ok ? "DELETE_BOOK_SUCCESS|" + p[1] : "DELETE_BOOK_FAILED|" + p[1]);
            if (ok) broadcastAllClients(getAllData());
            return ok;
        }
        sendMessage(bw, "DELETE_BOOK_FAILED");
        return false;
    }

    public String getAllData() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE_BOOKS_START\n");
        for (String book : bookDAO.listBooks()) sb.append(book).append("\n");
        sb.append("UPDATE_BOOKS_END\n");

        sb.append("UPDATE_USERS_START\n");
        for (String user : userDAO.listUsers()) sb.append(user).append("\n");
        sb.append("UPDATE_USERS_END\n");

        sb.append("UPDATE_BORROWS_START\n");
        for (String borrow : borrowDAO.listBorrows("ALL")) sb.append(borrow).append("\n");
        sb.append("UPDATE_BORROWS_END\n");

        return sb.toString();
    }

    private void sendMessage(BufferedWriter bw, String msg) throws IOException {
        bw.write(msg + "\n");
        bw.flush();
    }

    private void broadcastAllClients(String message) {
        synchronized (clientWriters) {
            List<BufferedWriter> disconnectedClients = new ArrayList<>();
            for (BufferedWriter writer : clientWriters) {
                try {
                    writer.write(message + "\n");
                    writer.flush();
                } catch (IOException e) {
                    disconnectedClients.add(writer);
                }
            }
            clientWriters.removeAll(disconnectedClients);
        }
    }
}
