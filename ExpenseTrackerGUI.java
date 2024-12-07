import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ExpenseTrackerGUI extends JFrame {
    private DefaultTableModel tableModel;

    public ExpenseTrackerGUI() {
        setTitle("Personal Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // Table for displaying expenses
        tableModel = new DefaultTableModel(new String[]{"ID", "Description", "Amount", "Date"}, 0);
        JTable expenseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        add(scrollPane, BorderLayout.CENTER);

        // Input fields
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

        // Load data
        loadExpenses();

        // Button actions
        addButton.addActionListener(e -> {
            String description = descriptionField.getText();
            String amount = amountField.getText();
            String date = dateField.getText();
            DatabaseHelper.addExpense(description, Double.parseDouble(amount), date);
            loadExpenses();
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = expenseTable.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                DatabaseHelper.deleteExpense(id);
                loadExpenses();
            }
        });

        setVisible(true);
    }

    private void loadExpenses() {
        tableModel.setRowCount(0);
        DatabaseHelper.getExpenses().forEach(expense -> tableModel.addRow(new Object[]{
            expense.getId(), expense.getDescription(), expense.getAmount(), expense.getDate()
        }));
    }
}
