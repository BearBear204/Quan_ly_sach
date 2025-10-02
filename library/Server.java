package library;

import libraryDAO.BookDAO;
import libraryDAO.BorrowDAO;
import libraryDAO.UserDAO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Connection conn;
    private static UserDAO userDAO;
    private static BookDAO bookDAO;
    private static BorrowDAO borrowDAO;
    private static ServerService libraryService;

    // Lưu tất cả writers để broadcast chung (sách, user, lịch sử...)
    private static final List<BufferedWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());
    // Lưu mapping user -> writer để gửi riêng
    private static final Map<String, BufferedWriter> onlineUsers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        connectDB();

        userDAO = new UserDAO(conn);
        bookDAO = new BookDAO(conn);
        borrowDAO = new BorrowDAO(conn);
        libraryService = new ServerService(userDAO, bookDAO, borrowDAO, clientWriters);

        try (ServerSocket serverSocket = new ServerSocket(2000)) {
            System.out.println("📡 Server đang lắng nghe...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("✅ Client kết nối: " + clientSocket.getInetAddress());

                BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                clientWriters.add(bw);
                sendInitialData(bw);

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
        String currentUser = null;
        try {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                System.out.println("📩 Client gửi: " + line);

                // Tạm nhận username từ lệnh LOGIN để lưu mapping
                if (line.startsWith("LOGIN ")) {
                    String[] parts = line.substring(6).split("\\|");
                    if (parts.length >= 2) {
                        currentUser = parts[0];
                    }
                }

                boolean changedData = libraryService.processCommand(line, currentUser, bw);

                // Sau khi LOGIN thành công, ServerService sẽ gửi LOGIN_SUCCESS|username|role
                // Lúc đó ta có thể đưa vào map onlineUsers
                if (line.startsWith("LOGIN ")) {
                    // Hơi thô, nhưng đủ: parse lại role từ DB
                    String[] lp = line.substring(6).split("\\|");
                    String u = lp[0], p = lp[1];
                    String role = userDAO.login(u, p);
                    if (role != null) {
                        onlineUsers.put(u, bw);
                    }
                }

                // Broadcast toàn bộ data (sách/users/borrows) khi có thay đổi lớn
                if (changedData) {
                    broadcastAllClients(libraryService.getAllData());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clientWriters.remove(bw);
            if (currentUser != null) {
                onlineUsers.remove(currentUser);
            }
            try { bw.close(); } catch (Exception ignored) {}
            try { br.close(); } catch (Exception ignored) {}
        }
    }

    private static void sendInitialData(BufferedWriter bw) {
        try {
            bw.write(libraryService.getAllData() + "\n");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastAllClients(String message) {
        synchronized (clientWriters) {
            List<BufferedWriter> disconnected = new ArrayList<>();
            for (BufferedWriter writer : clientWriters) {
                try {
                    writer.write(message + "\n");
                    writer.flush();
                } catch (IOException e) {
                    disconnected.add(writer);
                }
            }
            clientWriters.removeAll(disconnected);
        }
    }

    // Gửi riêng một message tới đúng user
    public static void sendToUser(String username, String message) {
        BufferedWriter writer = onlineUsers.get(username);
        if (writer != null) {
            try {
                writer.write(message + "\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
