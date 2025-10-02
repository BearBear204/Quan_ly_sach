package ui;

import library.ClientService;

import javax.swing.*;

public class ClientUi {
    private final ClientService service;
    private final JFrame frame;
    private AdminPanel adminPanel;
    private UserPanel userPanel;

    public ClientUi() {
        this.service = new ClientService("localhost", 2000, this::handleMessage);

        frame = new JFrame("Library System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 650);
        frame.setLocationRelativeTo(null);

        showLogin();
        frame.setVisible(true);
    }

    private void showLogin() {
        frame.setContentPane(new LoginPanel(service, this::requestLogin, this::showRegister));
        frame.revalidate();
    }

    private void showRegister() {
        frame.setContentPane(new RegisterPanel(service, this::showLogin));
        frame.revalidate();
    }

    private void requestLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Vui lòng nhập đầy đủ username và password!", "Lỗi đăng nhập", JOptionPane.WARNING_MESSAGE);
            return;
        }
        service.login(username, password);
    }

    private void onLoginSuccess(String user, String role) {
        if ("admin".equalsIgnoreCase(role)) {
            adminPanel = new AdminPanel(service);
            frame.setContentPane(adminPanel);
            service.sendCommand("LIST_BOOKS");
            service.sendCommand("LIST_USERS");
            service.sendCommand("BORROWS");
            service.sendCommand("GET_PENDING"); // load danh sách pending
        } else {
            userPanel = new UserPanel(service, user, role, this::logout);
            frame.setContentPane(userPanel);
            service.sendCommand("LIST");
            service.sendCommand("BORROWS_USER");
        }
        frame.revalidate();
    }

    private void logout() {
        showLogin();
    }

    private void handleMessage(String line) {
        SwingUtilities.invokeLater(() -> {
            String msg = line.trim();

            if (msg.startsWith("LOGIN_SUCCESS")) {
                String[] parts = msg.split("\\|");
                if (parts.length >= 3) {
                    onLoginSuccess(parts[1], parts[2]);
                }
            } else if (msg.startsWith("LOGIN_FAIL")) {
                JOptionPane.showMessageDialog(frame, "Đăng nhập thất bại: " + msg.substring("LOGIN_FAIL|".length()), "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
                showLogin();

            } else if (msg.startsWith("NEW_BORROW_REQUEST")) {
                String[] parts = msg.split("\\|");
                if (parts.length == 3 && adminPanel != null) {
                    JOptionPane.showMessageDialog(frame,
                            "User " + parts[1] + " xin mượn sách: " + parts[2],
                            "Thông báo mượn sách",
                            JOptionPane.INFORMATION_MESSAGE);
                    service.sendCommand("GET_PENDING"); // cập nhật danh sách pending
                }

            } else if (msg.startsWith("BORROW_REQUEST_SUCCESS")) {
                JOptionPane.showMessageDialog(frame, "Yêu cầu mượn sách đã gửi, chờ Admin duyệt.", "Đã gửi", JOptionPane.INFORMATION_MESSAGE);

            } else if (msg.startsWith("BORROW_REQUEST_FAILED")) {
                String[] parts = msg.split("\\|");
                JOptionPane.showMessageDialog(frame, "Yêu cầu mượn sách '" + (parts.length > 1 ? parts[1] : "") + "' thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);

            } else if (msg.startsWith("BORROW_APPROVE_SUCCESS")) {
                service.sendCommand("BORROWS"); // admin refresh lịch sử mượn

            } else if (msg.startsWith("BORROW_REJECT_SUCCESS")) {
                service.sendCommand("GET_PENDING");

            } else if (msg.startsWith("BORROW_UPDATE")) {
                // Gửi vào UserPanel để hiển thị thông báo
                if (userPanel != null) userPanel.handleRow(msg);

            } else if (msg.startsWith("PENDING_LIST_START") || msg.startsWith("PENDING_LIST_END") || msg.startsWith("PENDING|")) {
                if (adminPanel != null) adminPanel.handleRow(msg);

            } else {
                // Dữ liệu chung: books, users, borrows
                if (adminPanel != null) adminPanel.handleRow(msg);
                if (userPanel != null) userPanel.handleRow(msg);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientUi::new);
    }
}
