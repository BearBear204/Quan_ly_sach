package library;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import libraryDAO.BookDAO;
import libraryDAO.BorrowDAO;
import libraryDAO.UserDAO;

public class server {
    private static Connection conn;
    private static UserDAO userDAO;
    private static BookDAO bookDAO;
    private static BorrowDAO borrowDAO;

    public static void main(String[] args) {
        connectDB();
        userDAO = new UserDAO(conn);
        bookDAO = new BookDAO(conn);
        borrowDAO = new BorrowDAO(conn);

        try (ServerSocket ss = new ServerSocket(2000)) {
            System.out.println("📡 Server đang lắng nghe...");
            while (true) {
                Socket s = ss.accept();
                System.out.println("✅ Client kết nối: " + s.getInetAddress());
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                new Thread(() -> handleClient(br, bw)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void connectDB() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:library.db");
            System.out.println("✅ Kết nối SQLite thành công!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(BufferedReader br, BufferedWriter bw) {
        try {
            String line;
            String currentUser = "";
            while ((line = br.readLine()) != null) {
                System.out.println("📩 Client gửi: " + line);

                if (line.startsWith("LOGIN ")) {
                    String[] p = line.substring(6).trim().split("\\|");
                    if (p.length == 2) {
                        String role = userDAO.login(p[0], p[1]);
                        if (role != null) {
                            currentUser = p[0];
                            sendMessage(bw, "LOGIN_SUCCESS|" + currentUser + "|" + role);
                        } else sendMessage(bw, "LOGIN_FAILED");
                    } else sendMessage(bw, "LOGIN_FAILED");

                } else if (line.startsWith("REGISTER ")) {
                    String[] p = line.substring(9).trim().split("\\|");
                    if (p.length == 2) {
                        boolean ok = userDAO.register(p[0], p[1]);
                        sendMessage(bw, ok ? "REGISTER_SUCCESS" : "USERNAME_EXISTS");
                    } else sendMessage(bw, "REGISTER_FAILED");

                } else if (line.startsWith("BORROW ")) {
                    String[] p = line.substring(7).trim().split("\\|");
                    boolean ok = borrowDAO.borrowBook(p[0], p[1]);
                    sendMessage(bw, ok ? "BORROW_SUCCESS|" + p[1] : "BORROW_FAILED|Không thể mượn");

                } else if (line.startsWith("RETURN ")) {
                    String[] p = line.substring(7).trim().split("\\|");
                    boolean ok = borrowDAO.returnBook(p[0], p[1]);
                    sendMessage(bw, ok ? "RETURN_SUCCESS|" + p[1] : "RETURN_FAILED|Không thể trả");

                } else if (line.equals("LIST")) {
                    List<String> books = bookDAO.listBooks();
                    bw.write("LIST_START\n");
                    for (String b : books) bw.write(b + "\n");
                    bw.write("LIST_END\n");
                    bw.flush();

                } else if (line.equals("BORROWS")) {
                    sendBorrowList(bw, "ALL");
                } else if (line.equals("BORROWS_USER")) {
                    sendBorrowList(bw, currentUser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendBorrowList(BufferedWriter bw, String username) throws IOException {
        List<String> borrows = borrowDAO.listBorrows(username);
        bw.write("BORROWS_START\n");
        for (String b : borrows) bw.write(b + "\n");
        bw.write("BORROWS_END\n");
        bw.flush();
    }

    private static void sendMessage(BufferedWriter bw, String msg) throws IOException {
        bw.write(msg + "\n");
        bw.flush();
    }
}
