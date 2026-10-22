import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * LibraryManagementSystem
 */
public final class LibraryManagementSystem extends JFrame {
    private static final long serialVersionUID = 1L;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] {"Book ID", "Title", "Author", "Publisher", "Quantity"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bookTable = new JTable(tableModel);
    private final JTextField idField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextField authorField = new JTextField();
    private final JTextField publisherField = new JTextField();
    private final JTextField quantityField = new JTextField();

    public LibraryManagementSystem() {
        super("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new javax.swing.JLabel("Book ID"));
        form.add(idField);
        form.add(new javax.swing.JLabel("Title"));
        form.add(titleField);
        form.add(new javax.swing.JLabel("Author"));
        form.add(authorField);
        form.add(new javax.swing.JLabel("Publisher"));
        form.add(publisherField);
        form.add(new javax.swing.JLabel("Quantity"));
        form.add(quantityField);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(event -> addBook());
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(event -> updateBook());
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(event -> deleteBook());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> displayBooks());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(event -> clearFields());

        JPanel actions = new JPanel();
        actions.add(addButton);
        actions.add(updateButton);
        actions.add(deleteButton);
        actions.add(refreshButton);
        actions.add(clearButton);

        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.add(form, BorderLayout.CENTER);
        top.add(actions, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && bookTable.getSelectedRow() >= 0) {
                int row = bookTable.convertRowIndexToModel(bookTable.getSelectedRow());
                idField.setText(tableModel.getValueAt(row, 0).toString());
                titleField.setText(tableModel.getValueAt(row, 1).toString());
                authorField.setText(tableModel.getValueAt(row, 2).toString());
                publisherField.setText(tableModel.getValueAt(row, 3).toString());
                quantityField.setText(tableModel.getValueAt(row, 4).toString());
            }
        });
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        setSize(800, 500);
        setLocationRelativeTo(null);
        displayBooks();
        setVisible(true);
    }

    public void searchBooks() {
        String sql = "SELECT book_id, title, author, publisher, quantity FROM books";
        tableModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getInt("quantity")
                });
            }
        } catch (SQLException ex) {
            showDatabaseError("Could not search books", ex);
        }
    }

    private void addBook() {
        Book book = getBookFromFields();
        if (book == null) {
            return;
        }

        String sql = "INSERT INTO books (book_id, title, author, publisher, quantity) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, book.getBookId());
            ps.setString(2, book.getTitle());
            ps.setString(3, authorField.getText().trim());
            ps.setString(4, book.getPublisher());
            ps.setInt(5, book.getQuantity());
            ps.executeUpdate();
            displayBooks();
            clearFields();
            JOptionPane.showMessageDialog(this, "Book added successfully!");
        } catch (SQLException ex) {
            showDatabaseError("Could not add book", ex);
        }
    }

    // UPDATE
    public void updateBook() {
        Book book = getBookFromFields();
        if (book == null) {
            return;
        }

        String sql = "UPDATE books SET title = ?, author = ?, publisher = ?, quantity = ? "
                   + "WHERE book_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, authorField.getText().trim());
            ps.setString(3, book.getPublisher());
            ps.setInt(4, book.getQuantity());
            ps.setInt(5, book.getBookId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                displayBooks();
                clearFields();
                JOptionPane.showMessageDialog(this, "Book updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Book ID not found.");
            }
        } catch (SQLException ex) {
            showDatabaseError("Could not update book", ex);
        }
    }

    private void displayBooks() {
        searchBooks();
    }

    private Book getBookFromFields() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String publisher = publisherField.getText().trim();
        if (title.isEmpty() || author.isEmpty() || publisher.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the title, author, and publisher.");
            return null;
        }

        try {
            int id = Integer.parseInt(idField.getText().trim());
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (id <= 0 || quantity < 0) {
                JOptionPane.showMessageDialog(this, "Book ID must be positive and quantity cannot be negative.");
                return null;
            }
            return new Book(id, title, publisher, quantity);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid Book ID and quantity.");
            return null;
        }
    }

    // DELETE
    private void deleteBook() {
        try {
            int id = Integer.parseInt(idField.getText().trim());

            int choice = JOptionPane.showConfirmDialog(
                    this, "Are you sure you want to delete this book?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            String sql = "DELETE FROM books WHERE book_id = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, id);
                int rows = ps.executeUpdate();

                if (rows > 0) {
                    displayBooks();
                    clearFields();
                    JOptionPane.showMessageDialog(this, "Book deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Book ID not found.");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid Book ID.");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1451) {
                JOptionPane.showMessageDialog(
                        this,
                        "This book is linked to an issue record. It cannot be deleted until that record is handled."
                );
            } else {
                showDatabaseError("Could not delete book", ex);
            }
        }
    }

    private void clearFields() {
        idField.setText("");
        titleField.setText("");
        authorField.setText("");
        publisherField.setText("");
        quantityField.setText("");
        bookTable.clearSelection();
    }

    private void showDatabaseError(String action, SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                action + ".\nPlease check your MySQL connection and table structure.\n"
                        + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryManagementSystem::new);
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JTable getBookTable() {
        return bookTable;
    }

    public JTextField getIdField() {
        return idField;
    }

    public JTextField getTitleField() {
        return titleField;
    }

    public JTextField getAuthorField() {
        return authorField;
    }

    public JTextField getPublisherField() {
        return publisherField;
    }

    public JTextField getQuantityField() {
        return quantityField;
    }

}
