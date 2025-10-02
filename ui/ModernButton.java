package ui;

import javax.swing.*;
import java.awt.*;

public class ModernButton extends JButton {
	private static final long serialVersionUID = 1L;
    public ModernButton(String text, Color baseColor, Color hoverColor) {
        super(text);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setBackground(baseColor);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorderPainted(false);

        // Hover effect
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(baseColor);
            }
        });
    }
}
