package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class RequestCard extends JPanel {
	private static final long serialVersionUID = 1L;
    public RequestCard(String username, String book, String borrowDate, String returnDate, String status,
                       Runnable onApprove, Runnable onReject) {
        setLayout(new BorderLayout(10,10));
        setBackground(Color.WHITE);
        setBorder(new CompoundBorder(
            new LineBorder(new Color(200,200,200), 1, true),
            new EmptyBorder(10,10,10,10)
        ));

        JLabel lblUser = new JLabel("👤 " + username);
        JLabel lblBook = new JLabel("📚 " + book);
        JLabel lblBorrow = new JLabel("Mượn: " + borrowDate);
        JLabel lblReturn = new JLabel("Trả: " + returnDate);
        JLabel lblStatus = new JLabel("Trạng thái: " + status);

        JPanel infoPanel = new JPanel(new GridLayout(0,1));
        infoPanel.setOpaque(false);
        infoPanel.add(lblUser);
        infoPanel.add(lblBook);
        infoPanel.add(lblBorrow);
        infoPanel.add(lblReturn);
        infoPanel.add(lblStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnApprove = new ModernButton("Duyệt", new Color(0,123,255), new Color(0,150,220));
        btnApprove.addActionListener(e -> onApprove.run());

        JButton btnReject = new ModernButton("Từ chối", new Color(220,53,69), new Color(200,0,0));
        btnReject.addActionListener(e -> onReject.run());

        btnPanel.add(btnApprove);
        btnPanel.add(btnReject);

        add(infoPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // Hover effect
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(new Color(250,250,255));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(Color.WHITE);
            }
        });
    }
}
