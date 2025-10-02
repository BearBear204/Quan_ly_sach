package library;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientService {
    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private Consumer<String> messageHandler;
    private String role;
    private String username;

    public ClientService(String host, int port, Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
        try {
            socket = new Socket(host, port);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            startReceiver();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Không thể kết nối server!");
        }
    }

    private void startReceiver() {
        new Thread(() -> {
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    messageHandler.accept(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Client-Receiver").start();
    }

    public void sendCommand(String cmd) {
        try {
            if (bw != null) {
                bw.write(cmd);
                bw.newLine();
                bw.flush();
            } else {
                System.err.println("BufferedWriter is null, không thể gửi lệnh: " + cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void login(String username, String password) {
        sendCommand("LOGIN " + username + "|" + password);
    }

    public void register(String username, String password) {
        sendCommand("REGISTER " + username + "|" + password);
    }

    public void requestBorrow(String username, String bookName) {
        sendCommand("BORROW_REQUEST|" + username + "|" + bookName);
    }

    // Admin-only
    public void approveBorrow(String username, String bookName) {
        sendCommand("BORROW_APPROVE|" + username + "|" + bookName);
    }

    // Admin-only
    public void rejectBorrow(String username, String bookName) {
        sendCommand("BORROW_REJECT|" + username + "|" + bookName);
    }

    public void changePassword(String username, String newPass) {
        sendCommand("CHANGE_PASSWORD " + username + "|" + newPass);
    }

    public void addBook(String name, String author, String year, String category, int quantity) {
        sendCommand("ADD_BOOK|" + name + "|" + author + "|" + year + "|" + category + "|" + quantity);
    }

    public void editBook(String name, String author, String year, String category, int quantity) {
        sendCommand("EDIT_BOOK|" + name + "|" + author + "|" + year + "|" + category + "|" + quantity);
    }

    public void deleteBook(String name) {
        sendCommand("DELETE_BOOK|" + name);
    }

    public void lockUser(String username) {
        sendCommand("LOCK_USER|" + username);
    }

    public void requestReturn(String username, String bookName) {
        sendCommand("RETURN_REQUEST|" + username + "|" + bookName);
    }

    public void confirmReturn(String username, String bookName) {
        sendCommand("CONFIRM_RETURN|" + username + "|" + bookName);
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public void close() {
        try {
            if (bw != null) bw.close();
            if (br != null) br.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}
