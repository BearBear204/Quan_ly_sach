package ui;

import library.ClientService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class AdminPanel extends JPanel {
	private static final long serialVersionUID = 1L;
    private final DefaultTableModel modelBooks, modelBorrow, modelUsers;
    private final JTable tableBooks, tableBorrow, tableUsers;
    private final ClientService service;

    private final JPanel notificationsList = new JPanel();
    private int unreadNotifications = 0;
    private JTabbedPane tabs;
    private int notifTabIndex;

    public AdminPanel(ClientService service) {
        this.service = service;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        modelBooks = new DefaultTableModel(new String[]{"Tên sách", "Tác giả", "Năm", "Thể loại", "Số lượng", "Đang mượn"}, 0);
        modelBorrow = new DefaultTableModel(new String[]{"Username", "Sách", "Ngày mượn", "Ngày trả", "Trạng thái"}, 0);
        modelUsers = new DefaultTableModel(new String[]{"Username", "Role"}, 0);

        tableBooks = new ModernTable();
        tableBooks.setModel(modelBooks);

        tableBorrow = new ModernTable();
        tableBorrow.setModel(modelBorrow);

        tableUsers = new ModernTable();
        tableUsers.setModel(modelUsers);

        tabs = new JTabbedPane();

     // Load icon gif
     ImageIcon bookIcon = new ImageIcon(new ImageIcon("C:/Users/vuduc/Downloads/LTM/CuoiKy/gif/icons8-book.gif")
             .getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT));
     ImageIcon historyIcon = new ImageIcon(new ImageIcon("C:/Users/vuduc/Downloads/LTM/CuoiKy/gif/icons8-time.gif")
             .getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT));
     ImageIcon userIcon = new ImageIcon(new ImageIcon("C:/Users/vuduc/Downloads/LTM/CuoiKy/gif/icons8-user.gif")
             .getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT));
     ImageIcon bellIcon = new ImageIcon(new ImageIcon("C:/Users/vuduc/Downloads/LTM/CuoiKy/gif/icons8-bell.gif")
             .getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT));

     // Thêm tab chỉ có icon
     tabs.addTab(null, bookIcon, createBooksTab(), "Quản lý sách");
     tabs.addTab(null, historyIcon, createBorrowTab(), "Lịch sử mượn");
     tabs.addTab(null, userIcon, createUsersTab(), "Quản lý User");
     notifTabIndex = tabs.getTabCount();
     tabs.addTab(null, bellIcon, createNotificationsTab(), "Thông báo");

     // Reset số thông báo chưa đọc khi click tab
     tabs.addChangeListener(e -> {
         if (tabs.getSelectedIndex() == notifTabIndex) {
             unreadNotifications = 0;
             updateNotifTabTitle();
         }
     });

     add(tabs, BorderLayout.CENTER);
    }

    private JPanel createBooksTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(tableBooks);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new ModernButton("Thêm sách", new Color(76,175,80), new Color(56,155,60));
        JButton btnEdit = new ModernButton("Sửa sách", new Color(33,150,243), new Color(30,130,220));
        JButton btnDelete = new ModernButton("Xóa sách", new Color(244,67,54), new Color(220,50,50));

        // Gắn sự kiện cho nút Thêm
        btnAdd.addActionListener(e -> {
            JTextField name = new JTextField();
            JTextField author = new JTextField();
            JTextField year = new JTextField();
            JTextField category = new JTextField();
            JTextField quantity = new JTextField();

            JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
            form.add(new JLabel("Tên sách:")); form.add(name);
            form.add(new JLabel("Tác giả:")); form.add(author);
            form.add(new JLabel("Năm:")); form.add(year);
            form.add(new JLabel("Thể loại:")); form.add(category);
            form.add(new JLabel("Số lượng:")); form.add(quantity);

            int result = JOptionPane.showConfirmDialog(this, form, "Thêm sách", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                service.sendCommand("ADD_BOOK|" + name.getText() + "|" + author.getText() + "|" +
                        year.getText() + "|" + category.getText() + "|" + quantity.getText());
            }
        });

        // Gắn sự kiện cho nút Sửa
        btnEdit.addActionListener(e -> {
            int row = tableBooks.getSelectedRow();
            if (row != -1) {
                String bookName = modelBooks.getValueAt(row, 0).toString();
                String author = modelBooks.getValueAt(row, 1).toString();
                String year = modelBooks.getValueAt(row, 2).toString();
                String category = modelBooks.getValueAt(row, 3).toString();
                String quantity = modelBooks.getValueAt(row, 4).toString();

                JTextField fAuthor = new JTextField(author);
                JTextField fYear = new JTextField(year);
                JTextField fCategory = new JTextField(category);
                JTextField fQuantity = new JTextField(quantity);

                JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
                form.add(new JLabel("Tác giả:")); form.add(fAuthor);
                form.add(new JLabel("Năm:")); form.add(fYear);
                form.add(new JLabel("Thể loại:")); form.add(fCategory);
                form.add(new JLabel("Số lượng:")); form.add(fQuantity);

                int result = JOptionPane.showConfirmDialog(this, form, "Sửa sách: " + bookName, JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    service.sendCommand("EDIT_BOOK|" + bookName + "|" + fAuthor.getText() + "|" +
                            fYear.getText() + "|" + fCategory.getText() + "|" + fQuantity.getText());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sách để sửa!");
            }
        });

        // Gắn sự kiện cho nút Xóa
        btnDelete.addActionListener(e -> {
            int row = tableBooks.getSelectedRow();
            if (row != -1) {
                String bookName = modelBooks.getValueAt(row, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(this, "Xóa sách \"" + bookName + "\"?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    service.sendCommand("DELETE_BOOK|" + bookName);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sách để xóa!");
            }
        });

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBorrowTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(tableBorrow), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createUsersTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(new JScrollPane(tableUsers), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLockUser = new ModernButton("Khóa tài khoản", new Color(255, 152, 0), new Color(230, 130, 0));
        JButton btnChangePassword = new ModernButton("Đổi mật khẩu", new Color(33, 150, 243), new Color(30, 130, 220));

        actions.add(btnLockUser);
        actions.add(btnChangePassword);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createNotificationsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        notificationsList.setLayout(new BoxLayout(notificationsList, BoxLayout.Y_AXIS));
        notificationsList.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(notificationsList);
        panel.add(scroll, BorderLayout.CENTER);

        JButton refreshBtn = new ModernButton("Làm mới", new Color(33, 150, 243), new Color(30, 130, 220));
        panel.add(refreshBtn, BorderLayout.SOUTH);

        return panel;
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

    private void updateNotifTabTitle() {
        if (unreadNotifications > 0) {
            // Giữ nguyên icon, chỉ đổi tooltip
            tabs.setToolTipTextAt(notifTabIndex, "Có " + unreadNotifications + " thông báo mới");
        } else {
            tabs.setToolTipTextAt(notifTabIndex, "Thông báo");
        }
    }

    public void startNotifications() { notificationsList.removeAll(); }
    public void endNotifications() { notificationsList.revalidate(); notificationsList.repaint(); }

    public void startBooks() { modelBooks.setRowCount(0); }
    public void endBooks() { }
    public void startUsers() { modelUsers.setRowCount(0); }
    public void endUsers() { }
    public void startBorrows() { modelBorrow.setRowCount(0); }
    public void endBorrows() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelBorrow);
        tableBorrow.setRowSorter(sorter);
        sorter.toggleSortOrder(2); // sắp xếp theo cột ngày mượn
    }

    public void handleRow(String msg) {
        // 📚 Bắt đầu / kết thúc cập nhật danh sách sách
        if ("UPDATE_BOOKS_START".equals(msg)) {
            modelBooks.setRowCount(0);
            return;
        }
        if ("UPDATE_BOOKS_END".equals(msg)) {
            return;
        }

        // 👥 Bắt đầu / kết thúc cập nhật danh sách user
        if ("UPDATE_USERS_START".equals(msg)) {
            modelUsers.setRowCount(0);
            return;
        }
        if ("UPDATE_USERS_END".equals(msg)) {
            return;
        }

        // 📖 Bắt đầu / kết thúc cập nhật danh sách mượn
        if ("UPDATE_BORROWS_START".equals(msg)) {
            modelBorrow.setRowCount(0);
            return;
        }
        if ("UPDATE_BORROWS_END".equals(msg)) {
            return;
        }

        // 🔔 Bắt đầu / kết thúc danh sách yêu cầu mượn/trả
        if ("PENDING_LIST_START".equals(msg)) {
            startNotifications();
            return;
        }
        if ("PENDING_LIST_END".equals(msg)) {
            endNotifications();
            return;
        }

        // 📘 Dữ liệu sách
        if (msg.startsWith("BOOK|")) {
            String[] p = msg.split("\\|");
            if (p.length == 7) {
                modelBooks.addRow(new Object[]{
                    p[1], p[2], p[3], p[4], p[5], p[6]
                });
            }
            return;
        }

        // 👤 Dữ liệu user
        if (msg.startsWith("USER|")) {
            String[] p = msg.split("\\|");
            if (p.length == 3) {
                modelUsers.addRow(new Object[]{p[1], p[2]});
            }
            return;
        }

        // 📗 Dữ liệu borrow
        if (msg.startsWith("BORROW|")) {
            String[] p = msg.split("\\|");
            if (p.length >= 6) {
                modelBorrow.addRow(new Object[]{
                    p[1], p[2], p[3], p[4], translateStatus(p[5])
                });
            }
            return;
        }

        // 📨 Yêu cầu mượn/trả đang chờ xử lý
        if (msg.startsWith("PENDING|")) {
            String[] p = msg.split("\\|"); // PENDING|username|book|borrowDate|returnDate|status
            if (p.length >= 6) {
                notificationsList.add(new RequestCard(
                    p[1], p[2], p[3], p[4], translateStatus(p[5]),
                    () -> {
                        service.sendCommand("BORROW_APPROVE|" + p[1] + "|" + p[2]);
                        service.sendCommand("BORROWS_ALL");
                    },
                    () -> {
                        service.sendCommand("BORROW_REJECT|" + p[1] + "|" + p[2]);
                    }
                ));
                notificationsList.add(Box.createVerticalStrut(10));
            }
            return;
        }
    }
}