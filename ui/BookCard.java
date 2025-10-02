package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class BookCard extends JPanel {
    private static final long serialVersionUID = 1L;

    // Constructor mới: nhận thêm borrowed và quantity
    public BookCard(String title, String author, String genre, String year,
                    String borrowed, String quantity, Runnable onBorrow) {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));

        // Tiêu đề sách
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(0, 123, 255));

        JLabel lblAuthor   = new JLabel("Tác giả: " + author);
        JLabel lblGenre    = new JLabel("Thể loại: " + genre);
        JLabel lblYear     = new JLabel("Năm: " + year);
        JLabel lblBorrowed = new JLabel("Đã mượn: " + borrowed);
        JLabel lblQuantity = new JLabel("Số lượng: " + quantity);

        JPanel infoPanel = new JPanel(new GridLayout(0, 1));
        infoPanel.setOpaque(false);
        infoPanel.add(lblTitle);
        infoPanel.add(lblAuthor);
        infoPanel.add(lblGenre);
        infoPanel.add(lblYear);
        infoPanel.add(lblBorrowed);
        infoPanel.add(lblQuantity);

        JButton btnBorrow = new ModernButton("Mượn",
                new Color(0, 123, 255),
                new Color(0, 150, 220));
        btnBorrow.addActionListener(e -> onBorrow.run());

        add(infoPanel, BorderLayout.CENTER);
        add(btnBorrow, BorderLayout.SOUTH);

        // Hover effect
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(new Color(240, 250, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(Color.WHITE);
            }
        });
    }
}
