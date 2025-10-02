package ui;

import javax.swing.*;
import java.awt.*;

public class ModernTable extends JTable {
	private static final long serialVersionUID = 1L;
    public ModernTable() {
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setRowHeight(28);
        setShowGrid(false);
        setSelectionBackground(new Color(0,123,255));
        setSelectionForeground(Color.WHITE);

        getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        getTableHeader().setBackground(new Color(230,245,255));
        getTableHeader().setForeground(new Color(0,123,255));
    }
}
