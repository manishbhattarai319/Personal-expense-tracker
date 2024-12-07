import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class PersonalExpenseTracker {
    // SQLite Database URL
    private static final String DB_URL = "jdbc:sqlite:expenses.db";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            initializeDatabase(); // Initialize the database
            new ExpenseTrackerGUI(); // Start the GUI
        });
    }

    // Initialize SQLite Database and Create Table if Not Exists
    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        description TEXT NOT NULL,
                        amount REAL NOT NULL,
                        date TEXT NOT NULL
                    );
                    """;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // GUI Class
    static class ExpenseTrackerGUI extends JFrame {
        private DefaultTableModel tableModel;

        public ExpenseTrackerGUI() {
            setTitle("Personal Expense Tracker");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(600, 400);
            setLayout(new BorderLayout());

            // Table for Displaying Expenses
            tableModel = new DefaultTableModel(new String[]{"ID", "Description", "Amount", "Date"}, 0);
            JTable expenseTable = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(expenseTable);
            add(scrollPane, BorderLayout.CENTER);

            // Input Fields
            JPanel inputPanel = new JPanel(new GridLayout(4, 2));
            JTextField descriptionField = new JTextField();
            JTextField amountField = new JTextField();
            JTextField dateField = new JTextField();

            inputPanel.add(new JLabel("Description:"));
            inputPanel.add(descriptionField);
            inputPanel.add(new JLabel("Amount:"));
            inputPanel.add(amountField);
            inputPanel.add(new JLabel("Date (YYYY-MM-DD):"));
            inputPanel.add(dateField);

            JButton addButton = new JButton("Add Expense");
            JButton deleteButton = new JButton("Delete Selected");
            inputPanel.add(addButton);
            inputPanel.add(deleteButton);
            add(inputPanel, BorderLayout.NORTH);

            // Load Data into Table
            loadExpenses();

            // Add Expense Action
            addButton.addActionListener(e -> {
                String description = descriptionField.getText();
                String amount = amountField.getText();
                String date = dateField.getText();

                if (description.isEmpty() || amount.isEmpty() || date.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    addExpense(description, Double.parseDouble(amount), date);
                    loadExpenses(); // Refresh table
                    descriptionField.setText("");
                    amountField.setText("");
                    dateField.setText("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid amount entered!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Delete Expense Action
            deleteButton.addActionListener(e -> {
                int selectedRow = expenseTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Select a row to delete!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int id = (int) tableModel.getValueAt(selectedRow, 0);
                deleteExpense(id);
                loadExpenses(); // Refresh table
            });

            setVisible(true);
        }

        // Load Expenses into Table
        private void loadExpenses() {
            tableModel.setRowCount(0); // Clear the table
            ArrayList<Expense> expenses = getExpenses();
            for (Expense expense : expenses) {
                tableModel.addRow(new Object[]{expense.id, expense.description, expense.amount, expense.date});
            }
        }

        // Add Expense to Database
        private void addExpense(String description, double amount, String date) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                String sql = "INSERT INTO expenses (description, amount, date) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, description);
                    pstmt.setDouble(2, amount);
                    pstmt.setString(3, date);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Delete Expense from Database
        private void deleteExpense(int id) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                String sql = "DELETE FROM expenses WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Get All Expenses from Database
        private ArrayList<Expense> getExpenses() {
            ArrayList<Expense> expenses = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                String sql = "SELECT * FROM expenses";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        expenses.add(new Expense(
                                rs.getInt("id"),
                                rs.getString("description"),
                                rs.getDouble("amount"),
                                rs.getString("date")
                        ));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return expenses;
        }
    }

    // Expense Model Class
    static class Expense {
        int id;
        String description;
        double amount;
        String date;

        public Expense(int id, String description, double amount, String date) {
            this.id = id;
            this.description = description;
            this.amount = amount;
            this.date = date;
        }
    }
}
