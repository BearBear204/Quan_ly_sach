package ui;

import library.ClientService;
import javax.swing.*;
import java.awt.*;

public class BookDialog extends JDialog {
	private static final long serialVersionUID = 1L;
    public BookDialog(String mode, String[] data, ClientService service, Runnable onSuccess) {
        setTitle(mode.equals("ADD") ? "Thêm sách" : "Sửa sách");
        setSize(400,300);
        setLocationRelativeTo(null);
        setModal(true);

        JPanel panel = new JPanel(new GridLayout(6,2,10,10));
        JTextField txtName = new JTextField();
        JTextField txtAuthor = new JTextField();
        JTextField txtYear = new JTextField();
        JTextField txtType = new JTextField();
        JTextField txtQty = new JTextField();
        JTextField txtBorrowed = new JTextField();

        panel.add(new JLabel("Tên sách:")); panel.add(txtName);
        panel.add(new JLabel("Tác giả:")); panel.add(txtAuthor);
        panel.add(new JLabel("Năm:")); panel.add(txtYear);
        panel.add(new JLabel("Thể loại:")); panel.add(txtType);
        panel.add(new JLabel("Số lượng:")); panel.add(txtQty);
        panel.add(new JLabel("Đang mượn:")); panel.add(txtBorrowed);

        if(data != null){
            txtName.setText(data[0]);
            txtAuthor.setText(data[1]);
            txtYear.setText(data[2]);
            txtType.setText(data[3]);
            txtQty.setText(data[4]);
            txtBorrowed.setText(data[5]);
        }

        JButton btnSave = new JButton("Lưu");
        btnSave.addActionListener(e -> {
            String cmd = (mode.equals("ADD")?"ADD_BOOK ":"EDIT_BOOK ");
            String bookData = txtName.getText()+"|"+txtAuthor.getText()+"|"+txtYear.getText()+"|"+txtType.getText()+"|"+txtQty.getText()+"|"+txtBorrowed.getText();
            service.sendCommand(cmd+bookData);
            onSuccess.run();
            dispose();
        });

        add(panel, BorderLayout.CENTER);
        add(btnSave, BorderLayout.SOUTH);
        setVisible(true);
    }
}
