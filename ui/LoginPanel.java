package ui;

import library.ClientService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.BiConsumer;

public class LoginPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;

    public interface LoginHandler { void login(String username, String password); }

    public LoginPanel(LoginHandler onLogin, Runnable onRegister) {
        initUI(onLogin, onRegister);
    }

    public LoginPanel(ClientService service, BiConsumer<String, String> onLoginSuccess, Runnable onRegister) {
        this((username, password) -> {
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Vui lòng nhập đầy đủ username và password!",
                        "Lỗi đăng nhập", JOptionPane.WARNING_MESSAGE);
                return;
            }
            service.sendCommand("LOGIN " + username + "|" + password);
            // TODO: khi server trả về thành công thì gọi onLoginSuccess.accept(username, password);
        }, onRegister);
    }

    private void initUI(LoginHandler onLogin, Runnable onRegister) {
        setLayout(new GridBagLayout());
        setBackground(new Color(230, 245, 255)); // nền xanh nhạt

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));

        GroupLayout layout = new GroupLayout(card);
        card.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel title = new JLabel("Đăng nhập hệ thống", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 123, 255));

        JLabel lblUsername = new JLabel("Tên đăng nhập:");
        txtUsername = new JTextField();
        txtUsername.setPreferredSize(new Dimension(220, 30));

        JLabel lblPassword = new JLabel("Mật khẩu:");
        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(220, 30));

        lblError = new JLabel(" ");
        lblError.setForeground(Color.RED);
        lblError.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        JButton btnLogin = new JButton("Đăng nhập");
        stylePrimaryButton(btnLogin);
        btnLogin.addActionListener(e -> onLogin.login(
                txtUsername.getText().trim(),
                new String(txtPassword.getPassword()).trim()
        ));

        JButton btnRegister = new JButton("Đăng ký");
        styleAccentButton(btnRegister);
        btnRegister.addActionListener(e -> onRegister.run());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnLogin);
        btnPanel.add(btnRegister);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(title)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(lblUsername)
                        .addComponent(lblPassword))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(txtUsername)
                        .addComponent(txtPassword)))
                .addComponent(lblError)
                .addComponent(btnPanel)
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(title)
                .addGap(20)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsername)
                    .addComponent(txtUsername))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword)
                    .addComponent(txtPassword))
                .addComponent(lblError)
                .addGap(15)
                .addComponent(btnPanel)
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        add(card, gbc);
    }

    public void showError(String message) {
        lblError.setText(message);
    }

    // Style helper
    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(new Color(0, 123, 255));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(new Color(0, 150, 220)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(new Color(0, 123, 255)); }
        });
    }

    private void styleAccentButton(JButton btn) {
        btn.setBackground(new Color(0, 180, 140));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(new Color(0, 200, 160)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(new Color(0, 180, 140)); }
        });
    }
}
