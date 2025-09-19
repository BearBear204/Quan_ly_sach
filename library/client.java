package library;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class client {
    private Socket clientSocket;
    private BufferedReader br;
    private BufferedWriter bw;
    private JFrame frame;

    private JPanel loginPanel, registerPanel, adminPanel, userPanel;
    private JTextField txtUsername, txtRegUsername;
    private JPasswordField txtPassword, txtRegPassword, txtRegConfirm;

    private JTable tableAdmin, tableUser, tableBorrowAdmin, tableBorrowUser;
    private DefaultTableModel modelAdmin, modelUser, modelBorrowAdmin, modelBorrowUser;

    private String currentUser, currentRole;
    private boolean readingList = false, readingBorrow = false;
    private StringBuilder listBuffer = new StringBuilder(), borrowBuffer = new StringBuilder();

    public client() {
        setupConnection();
        setupPanels();
        startReceiver();
    }

    // ---------------- GUI -----------------
    private void setupPanels() {
        frame = new JFrame("Library System");
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loginPanel = createLoginPanel();
        registerPanel = createRegisterPanel();
        adminPanel = createMainPanel(true);
        userPanel = createMainPanel(false);

        showPanel(loginPanel);
        frame.setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel title = new JLabel("Đăng nhập hệ thống");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setMaximumSize(new Dimension(400, 100));
        form.setBackground(new Color(245, 245, 245));
        form.add(new JLabel("Username:")); txtUsername = new JTextField(); form.add(txtUsername);
        form.add(new JLabel("Password:")); txtPassword = new JPasswordField(); form.add(txtPassword);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBackground(new Color(245, 245, 245));
        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(76, 175, 80));
        btnLogin.setForeground(Color.WHITE);
        JButton btnGoRegister = new JButton("Register");
        btnGoRegister.setBackground(new Color(33, 150, 243));
        btnGoRegister.setForeground(Color.WHITE);
        btnPanel.add(btnLogin); btnPanel.add(btnGoRegister);

        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(form);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnPanel);

        btnLogin.addActionListener(e -> login());
        btnGoRegister.addActionListener(e -> showPanel(registerPanel));

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel title = new JLabel("Đăng ký tài khoản mới");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setMaximumSize(new Dimension(400, 150));
        form.setBackground(new Color(245, 245, 245));
        form.add(new JLabel("Username:")); txtRegUsername = new JTextField(); form.add(txtRegUsername);
        form.add(new JLabel("Password:")); txtRegPassword = new JPasswordField(); form.add(txtRegPassword);
        form.add(new JLabel("Confirm:")); txtRegConfirm = new JPasswordField(); form.add(txtRegConfirm);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBackground(new Color(245, 245, 245));
        JButton btnRegister = new JButton("Đăng ký");
        btnRegister.setBackground(new Color(76, 175, 80));
        btnRegister.setForeground(Color.WHITE);
        JButton btnBack = new JButton("Quay lại");
        btnBack.setBackground(new Color(33, 150, 243));
        btnBack.setForeground(Color.WHITE);
        btnPanel.add(btnRegister); btnPanel.add(btnBack);

        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(form);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnPanel);

        btnRegister.addActionListener(e -> register());
        btnBack.addActionListener(e -> showPanel(loginPanel));

        return panel;
    }

    private JPanel createMainPanel(boolean isAdmin) {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnsBooks = {"Tên sách", "Tác giả", "Năm", "Thể loại", "Số lượng", "Đang mượn"};
        String[] columnsBorrowAdmin = {"Username", "Sách", "Ngày mượn", "Ngày trả"};
        String[] columnsBorrowUser = {"Sách", "Ngày mượn", "Ngày trả"};

        if (isAdmin) {
            modelAdmin = new DefaultTableModel(columnsBooks, 0);
            tableAdmin = new JTable(modelAdmin);

            modelBorrowAdmin = new DefaultTableModel(columnsBorrowAdmin, 0);
            tableBorrowAdmin = new JTable(modelBorrowAdmin);

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tableAdmin), new JScrollPane(tableBorrowAdmin));
            split.setResizeWeight(0.7);
            panel.add(split, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            JButton btnAdd = new JButton("Thêm"), btnEdit = new JButton("Sửa"), btnDelete = new JButton("Xóa"), btnReload = new JButton("Reload");
            btnAdd.setBackground(new Color(76, 175, 80)); btnAdd.setForeground(Color.WHITE);
            btnEdit.setBackground(new Color(255, 193, 7)); btnEdit.setForeground(Color.BLACK);
            btnDelete.setBackground(new Color(244, 67, 54)); btnDelete.setForeground(Color.WHITE);
            btnReload.setBackground(new Color(33, 150, 243)); btnReload.setForeground(Color.WHITE);

            btnPanel.add(btnAdd); btnPanel.add(btnEdit); btnPanel.add(btnDelete); btnPanel.add(btnReload);
            panel.add(btnPanel, BorderLayout.SOUTH);

            btnAdd.addActionListener(e -> openBookDialog(true, -1));
            btnEdit.addActionListener(e -> {
                int row = tableAdmin.getSelectedRow();
                if (row >= 0) openBookDialog(false, row);
                else JOptionPane.showMessageDialog(frame, "Chọn sách để sửa!");
            });
            btnDelete.addActionListener(e -> {
                int row = tableAdmin.getSelectedRow();
                if (row >= 0) {
                    String name = (String) modelAdmin.getValueAt(row, 0);
                    int confirm = JOptionPane.showConfirmDialog(frame, "Xóa sách " + name + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) sendCommand("DEL " + name);
                } else JOptionPane.showMessageDialog(frame, "Chọn sách để xóa!");
            });
            btnReload.addActionListener(e -> {
                sendCommand("LIST");
                sendCommand("BORROWS");
            });

        } else {
            modelUser = new DefaultTableModel(columnsBooks, 0);
            tableUser = new JTable(modelUser);

            modelBorrowUser = new DefaultTableModel(columnsBorrowUser, 0);
            tableBorrowUser = new JTable(modelBorrowUser);

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tableUser), new JScrollPane(tableBorrowUser));
            split.setResizeWeight(0.7);
            panel.add(split, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            JButton btnBorrow = new JButton("Mượn"), btnReturn = new JButton("Trả"), btnReload = new JButton("Reload");
            btnBorrow.setBackground(new Color(76, 175, 80)); btnBorrow.setForeground(Color.WHITE);
            btnReturn.setBackground(new Color(244, 67, 54)); btnReturn.setForeground(Color.WHITE);
            btnReload.setBackground(new Color(33, 150, 243)); btnReload.setForeground(Color.WHITE);
            btnPanel.add(btnBorrow); btnPanel.add(btnReturn); btnPanel.add(btnReload);
            panel.add(btnPanel, BorderLayout.SOUTH);

            btnBorrow.addActionListener(e -> performUserAction("BORROW"));
            btnReturn.addActionListener(e -> performUserAction("RETURN"));
            btnReload.addActionListener(e -> sendCommand("BORROWS_USER"));
        }

        return panel;
    }

    private void openBookDialog(boolean isAdd, int row) {
        JDialog dialog = new JDialog(frame, isAdd ? "Thêm sách" : "Sửa sách", true);
        dialog.setSize(350, 250);
        dialog.setLayout(new GridLayout(6, 2, 5, 5));
        dialog.setLocationRelativeTo(frame);

        JTextField fName = new JTextField(isAdd ? "" : (String) modelAdmin.getValueAt(row, 0));
        JTextField fAuthor = new JTextField(isAdd ? "" : (String) modelAdmin.getValueAt(row, 1));
        JTextField fYear = new JTextField(isAdd ? "" : String.valueOf(modelAdmin.getValueAt(row, 2)));
        JTextField fCategory = new JTextField(isAdd ? "" : (String) modelAdmin.getValueAt(row, 3));
        JTextField fQuantity = new JTextField(isAdd ? "" : String.valueOf(modelAdmin.getValueAt(row, 4)));

        dialog.add(new JLabel("Tên sách:")); dialog.add(fName);
        dialog.add(new JLabel("Tác giả:")); dialog.add(fAuthor);
        dialog.add(new JLabel("Năm:")); dialog.add(fYear);
        dialog.add(new JLabel("Thể loại:")); dialog.add(fCategory);
        dialog.add(new JLabel("Số lượng:")); dialog.add(fQuantity);

        JButton btnSave = new JButton(isAdd ? "Thêm" : "Lưu");
        JButton btnCancel = new JButton("Hủy");
        dialog.add(btnSave); dialog.add(btnCancel);

        btnSave.addActionListener(e -> {
            String cmd = (isAdd ? "ADD " : "UPDATE ") + fName.getText() + "|" + fAuthor.getText() + "|" + fYear.getText() + "|" + fCategory.getText() + "|" + fQuantity.getText();
            sendCommand(cmd);
            dialog.dispose();
        });
        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void performUserAction(String action) {
        int row = tableUser.getSelectedRow();
        if (row >= 0) {
            String book = (String) modelUser.getValueAt(row, 0);
            sendCommand(action + " " + currentUser + "|" + book);
        } else JOptionPane.showMessageDialog(frame, "Chọn sách để " + (action.equals("BORROW") ? "mượn" : "trả") + "!");
    }

    private void showPanel(JPanel panel) {
        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    // ---------------- Network ----------------
    private void setupConnection() {
        try {
            clientSocket = new Socket("localhost", 2000);
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Không kết nối server!");
        }
    }

    private void startReceiver() {
        new Thread(() -> {
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    String msg = line.trim();
                    if (msg.equals("LIST_START")) {
                        readingList = true;
                        listBuffer.setLength(0);
                        continue;
                    }
                    if (msg.equals("LIST_END")) {
                        readingList = false;
                        SwingUtilities.invokeLater(() -> updateTable(modelAdmin, modelUser, listBuffer.toString()));
                        continue;
                    }
                    if (msg.equals("BORROWS_START")) {
                        readingBorrow = true;
                        borrowBuffer.setLength(0);
                        continue;
                    }
                    if (msg.equals("BORROWS_END")) {
                        readingBorrow = false;
                        SwingUtilities.invokeLater(() -> updateTableBorrow(borrowBuffer.toString()));
                        continue;
                    }
                    if (readingList) listBuffer.append(msg).append("\n");
                    else if (readingBorrow) borrowBuffer.append(msg).append("\n");
                    else SwingUtilities.invokeLater(() -> handleServerMessage(msg));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateTable(DefaultTableModel adminModel, DefaultTableModel userModel, String data) {
        DefaultTableModel model = "admin".equalsIgnoreCase(currentRole) ? adminModel : userModel;
        model.setRowCount(0);
        if (data.isEmpty()) return;
        for (String r : data.split("\n")) {
            String[] cols = r.split("\\|");
            if (cols.length == 6) model.addRow(cols);
        }
    }

    private void updateTableBorrow(String data) {
        if ("admin".equalsIgnoreCase(currentRole)) {
            modelBorrowAdmin.setRowCount(0);
            for (String r : data.split("\n")) {
                String[] cols = r.split("\\|");
                if (cols.length == 4) modelBorrowAdmin.addRow(cols);
            }
        } else {
            modelBorrowUser.setRowCount(0);
            for (String r : data.split("\n")) {
                String[] cols = r.split("\\|");
                if (cols.length == 4) modelBorrowUser.addRow(new String[]{cols[1], cols[2], cols[3]});
            }
        }
    }

    private void handleServerMessage(String msg) {
        if (msg.startsWith("LOGIN_SUCCESS")) {
            String[] p = msg.split("\\|");
            currentUser = p[1];
            currentRole = p[2];
            JOptionPane.showMessageDialog(frame, "Đăng nhập thành công! Chào " + currentUser);
            showPanel("admin".equalsIgnoreCase(currentRole) ? adminPanel : userPanel);
            sendCommand("LIST");
            sendCommand(currentRole.equals("admin") ? "BORROWS" : "BORROWS_USER");
        } else if (msg.startsWith("LOGIN_FAILED"))
            JOptionPane.showMessageDialog(frame, "Đăng nhập thất bại!");
        else if (msg.startsWith("REGISTER_SUCCESS"))
            JOptionPane.showMessageDialog(frame, "Đăng ký thành công!");
        else if (msg.startsWith("USERNAME_EXISTS"))
            JOptionPane.showMessageDialog(frame, "Tên đăng nhập đã tồn tại!");
        else if (msg.startsWith("REGISTER_FAILED"))
            JOptionPane.showMessageDialog(frame, "Đăng ký thất bại!");
        else if (msg.startsWith("BORROW_SUCCESS") || msg.startsWith("RETURN_SUCCESS"))
            sendCommand("LIST");
    }

    private void sendCommand(String cmd) {
        try {
            bw.write(cmd);
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- Actions ----------------
    private void login() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Điền đầy đủ!");
            return;
        }
        sendCommand("LOGIN " + user + "|" + pass);
    }

    private void register() {
        String user = txtRegUsername.getText().trim();
        String pass = new String(txtRegPassword.getPassword()).trim();
        String confirm = new String(txtRegConfirm.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Điền đầy đủ!");
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(frame, "Mật khẩu không khớp!");
            return;
        }
        sendCommand("REGISTER " + user + "|" + pass);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(client::new);
    }
}
