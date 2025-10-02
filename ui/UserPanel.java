package ui;

import library.ClientService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;

public class UserPanel extends JPanel {
	private static final long serialVersionUID = 1L;
    private final DefaultTableModel modelBorrow = new DefaultTableModel(
            new String[]{"Sách", "Ngày mượn", "Ngày trả", "Trạng thái"}, 0);
    private final JTable tableBorrow = new ModernTable();
    private final JPanel gridPanel = new JPanel(new GridLayout(0, 3, 15, 15));
    private final ClientService service;
    private final String username;
    private final String role;
    private final Runnable onLogout;

    // thông báo popup
    private final List<String> notifications = new ArrayList<>();
    private int unreadCount = 0;
    private JLabel notificationBadge;

    // 🔔 tab thông báo
    private final DefaultListModel<String> notifModel = new DefaultListModel<>();
    private final JList<String> notifList = new JList<>(notifModel);

    public UserPanel(ClientService service, String username, String role, Runnable onLogout) {
        this.service = service;
        this.username = username;
        this.role = role;
        this.onLogout = onLogout;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // top bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel lblWelcome = new JLabel("Xin chào, " + username + " (" + role + ")");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton logoutBtn = new ModernButton("Đăng xuất", new Color(244, 67, 54), new Color(220, 50, 50));
        logoutBtn.addActionListener(e -> onLogout.run());

        JButton notifBtn = new JButton("\uD83D\uDD14");
        notifBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        notifBtn.setFocusPainted(false);
        notifBtn.setBackground(Color.WHITE);
        notifBtn.setBorder(null);
        notifBtn.addActionListener(e -> showNotificationsPopup());

        notificationBadge = new JLabel("0");
        notificationBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        notificationBadge.setForeground(Color.WHITE);
        notificationBadge.setOpaque(true);
        notificationBadge.setBackground(Color.RED);
        notificationBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
        notificationBadge.setVisible(false);

        JPanel notifPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        notifPanel.setBackground(Color.WHITE);
        notifPanel.add(notifBtn);
        notifPanel.add(notificationBadge);

        topPanel.add(lblWelcome, BorderLayout.WEST);
        topPanel.add(notifPanel, BorderLayout.EAST);
        topPanel.add(logoutBtn, BorderLayout.LINE_END);
        add(topPanel, BorderLayout.NORTH);

        // danh sách sách
        JScrollPane scrollGrid = new JScrollPane(gridPanel);
        gridPanel.setBackground(new Color(245, 245, 245));
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(scrollGrid, BorderLayout.CENTER);

        // lịch sử mượn
        tableBorrow.setModel(modelBorrow); // GÁN model vào bảng
        tableBorrow.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableBorrow.setRowHeight(25);

        tableBorrow.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        	
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = table.getValueAt(row, table.getColumnCount() - 1).toString();

                if (!isSelected) {
                    switch (status) {
                        case "Chờ duyệt" -> c.setBackground(new Color(255, 243, 205));
                        case "Đã duyệt", "Đang mượn" -> c.setBackground(new Color(220, 245, 220));
                        case "Yêu cầu trả" -> c.setBackground(new Color(220, 235, 250));
                        case "Đã từ chối" -> c.setBackground(new Color(255, 220, 220));
                        default -> c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });

        JScrollPane borrowScroll = new JScrollPane(tableBorrow);
        JPanel borrowPanel = new JPanel(new BorderLayout());
        borrowPanel.add(borrowScroll, BorderLayout.CENTER);

        JPanel returnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnReturn = new ModernButton("Trả sách", new Color(244, 67, 54), new Color(220, 50, 50));
        btnReturn.addActionListener(e -> requestReturn());
        returnPanel.add(btnReturn);
        borrowPanel.add(returnPanel, BorderLayout.SOUTH);

     // Tab thông tin cá nhân
        JPanel profilePanel = new JPanel();
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setBorder(new EmptyBorder(30, 50, 30, 50));

        GroupLayout layout = new GroupLayout(profilePanel);
        profilePanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Các thành phần
        JLabel lblUser = new JLabel("👤 Username:");
        JLabel valUser = new JLabel(username);

        JLabel lblRole = new JLabel("🔑 Role:");
        JLabel valRole = new JLabel(role);

        JLabel lblPass = new JLabel("🔒 Mật khẩu mới:");
        JPasswordField newPasswordField = new JPasswordField();
        newPasswordField.setPreferredSize(new Dimension(220, 30)); // chỉnh độ dài hợp lý

        JButton btnChangePassword = new ModernButton("Đổi mật khẩu",
                new Color(33, 150, 243),
                new Color(30, 130, 220));
        btnChangePassword.setPreferredSize(new Dimension(160, 35));
        btnChangePassword.addActionListener(e -> {
            String newPass = new String(newPasswordField.getPassword()).trim();
            if (!newPass.isEmpty()) {
                service.sendCommand("CHANGE_PASSWORD " + username + "|" + newPass);
                JOptionPane.showMessageDialog(profilePanel, "Yêu cầu đổi mật khẩu đã gửi.");
                newPasswordField.setText("");
            } else {
                JOptionPane.showMessageDialog(profilePanel, "Vui lòng nhập mật khẩu mới.");
            }
        });

        // Layout ngang/dọc
        layout.setHorizontalGroup(
        	    layout.createParallelGroup(GroupLayout.Alignment.CENTER)
        	        .addGroup(layout.createSequentialGroup()
        	            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
        	                .addComponent(lblUser)
        	                .addComponent(lblRole)
        	                .addComponent(lblPass))
        	            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	                .addComponent(valUser)
        	                .addComponent(valRole)
        	                .addComponent(newPasswordField, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        	        .addComponent(btnChangePassword, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
        	);

        	layout.setVerticalGroup(
        	    layout.createSequentialGroup()
        	        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        	            .addComponent(lblUser)
        	            .addComponent(valUser))
        	        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        	            .addComponent(lblRole)
        	            .addComponent(valRole))
        	        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        	            .addComponent(lblPass)
        	            .addComponent(newPasswordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        	        .addGap(20)
        	        .addComponent(btnChangePassword, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
        	);

        // 🔔 tab thông báo
        notifList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane notifScroll = new JScrollPane(notifList);

        // tabs
        JTabbedPane tabs = new JTabbedPane();

        ImageIcon bookIcon = new ImageIcon(new ImageIcon("C:/Users/vuduc/Downloads/LTM/CuoiKy/gif/icons8-book.gif").getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT));
        ImageIcon historyIcon = new ImageIcon(new ImageIcon("C:/Users/vuduc/Downloads/LTM/CuoiKy/gif/icons8-time.gif").getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT));
        ImageIcon userIcon = new ImageIcon(new ImageIcon("C:/Users/vuduc/Downloads/LTM/CuoiKy/gif/icons8-user.gif").getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT));
        ImageIcon bellIcon = new ImageIcon(new ImageIcon("C:/Users/vuduc/Downloads/LTM/CuoiKy/gif/icons8-bell.gif").getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT));

        tabs.addTab(null, bookIcon, listPanel, "Danh sách sách");
        tabs.addTab(null, historyIcon, borrowPanel, "Lịch sử mượn");
        tabs.addTab(null, userIcon, profilePanel, "Thông tin cá nhân");
        tabs.addTab(null, bellIcon, notifScroll, "Thông báo");

        add(tabs, BorderLayout.CENTER);
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "PENDING" -> "Chờ duyệt";
            case "BORROWED" -> "Đang mượn";
            case "RETURN_REQUEST" -> "Yêu cầu trả";
            case "APPROVED" -> "Đã duyệt";
            case "REJECTED" -> "Đã từ chối";
            default -> status;
        };
    }

    public void startList() { gridPanel.removeAll(); }
    public void endList() { gridPanel.revalidate(); gridPanel.repaint(); }
    public void startBorrows() { modelBorrow.setRowCount(0); }
    public void endBorrows() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelBorrow);
        tableBorrow.setRowSorter(sorter);
        sorter.toggleSortOrder(1);
    }

    // thêm thông báo vào danh sách + badge
    private void addNotification(String text) {
        notifications.add(text);
        unreadCount++;
        notificationBadge.setText(String.valueOf(unreadCount));
        notificationBadge.setVisible(true);
    }

    // Hiển thị popup thông báo (nút 🔔 trên top bar)
    private void showNotificationsPopup() {
        if (notifications.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có thông báo mới.");
            return;
        }
        JList<String> list = new JList<>(notifications.toArray(new String[0]));
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(360, 240));
        JOptionPane.showMessageDialog(this, scroll, "Thông báo", JOptionPane.INFORMATION_MESSAGE);

        // reset badge
        unreadCount = 0;
        notificationBadge.setVisible(false);
    }

    // Gửi yêu cầu mượn sách
    private void requestBorrow(String bookName) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn mượn sách \"" + bookName + "\"?",
                "Xác nhận mượn", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.sendCommand("BORROW_REQUEST|" + username + "|" + bookName);
            String now = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            addNotification("[" + now + "] Đã gửi yêu cầu mượn: " + bookName);

            // 🔥 yêu cầu server gửi lại lịch sử mượn
            service.sendCommand("BORROWS_USER");
        }
    }

    // Gửi yêu cầu trả sách
    private void requestReturn() {
        int row = tableBorrow.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách để trả.");
            return;
        }
        String bookName = (String) modelBorrow.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận trả sách \"" + bookName + "\"?",
                "Trả sách", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.sendCommand("RETURN_REQUEST|" + username + "|" + bookName);
            String now = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            addNotification("[" + now + "] Đã gửi yêu cầu trả: " + bookName);

            // 🔥 yêu cầu server gửi lại lịch sử mượn
            service.sendCommand("BORROWS_USER");
        }
    }

    public void handleRow(String msg) {
        // 📚 Bắt đầu / kết thúc cập nhật danh sách sách
        if ("UPDATE_BOOKS_START".equals(msg)) {
            startList();
            return;
        }
        if ("UPDATE_BOOKS_END".equals(msg)) {
            endList();
            return;
        }

        // 📖 Bắt đầu / kết thúc cập nhật lịch sử mượn
        if ("UPDATE_BORROWS_START".equals(msg)) {
            startBorrows();
            return;
        }
        if ("UPDATE_BORROWS_END".equals(msg)) {
            endBorrows();
            return;
        }

        // 👥 Server broadcast danh sách user → không cần xử lý ở UserPanel
        if ("UPDATE_USERS_START".equals(msg) || "UPDATE_USERS_END".equals(msg)) {
            return;
        }

        if (msg.startsWith("BOOK|")) {
            String[] c = msg.split("\\|");
            if (c.length == 7) {
                String title = c[1];
                String author = c[2];
                String year = c[3];
                String genre = c[4];
                String borrowed = c[5];   // số lượng đã mượn
                String quantity = c[6];   // tổng số lượng

                gridPanel.add(new BookCard(title, author, genre, year, borrowed, quantity,
                                           () -> requestBorrow(title)));
            }
            return;
        }

        // 📗 Dữ liệu lịch sử mượn
        if (msg.startsWith("BORROW|")) {
            String[] c = msg.split("\\|");
            if (c.length >= 6) {
                String bookName = c[2];
                String borrowDate = c[3];
                String returnDate = c[4];
                String status = translateStatus(c[5]);

                modelBorrow.addRow(new Object[]{bookName, borrowDate, returnDate, status});
            }
            return;
        }

        // 🔔 Thông báo duyệt / từ chối từ admin
        if (msg.startsWith("BORROW_UPDATE|")) {
            String[] parts = msg.split("\\|");
            if (parts.length >= 3) {
                String bookName = parts[1];
                String status = translateStatus(parts[2]);
                String now = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
                String entry = "[" + now + "] Yêu cầu mượn \"" + bookName + "\" đã được " + status;

                addNotification(entry);         // hiện badge đỏ
                notifModel.addElement(entry);   // lưu vào tab Thông báo

                service.sendCommand("BORROWS_USER"); // yêu cầu server gửi lại lịch sử mượn
            }
            return;
        }
    }
}

         