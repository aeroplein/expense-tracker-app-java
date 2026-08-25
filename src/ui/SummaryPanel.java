package ui;

import service.ExpenseService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Monthly read-only report. It reuses the service layer; it does not issue SQL itself. */
public class SummaryPanel extends JPanel {
    private static final int DEMO_USER_ID = 1;
    private final ExpenseService expenseService = new ExpenseService();
    private final JLabel totalLabel = new JLabel();
    private final JLabel countLabel = new JLabel();
    private final DefaultTableModel categoryModel = new DefaultTableModel(new String[]{"Category", "Amount"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final CategoryChart chart = new CategoryChart();

    public SummaryPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        header.add(totalLabel);
        header.add(countLabel);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        header.add(refreshButton);
        add(header, BorderLayout.NORTH);

        JTable categoryTable = new JTable(categoryModel);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(categoryTable), chart);
        splitPane.setResizeWeight(0.45);
        add(splitPane, BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        try {
            var monthlyExpenses = expenseService.getFiltered(DEMO_USER_ID, "MONTHLY");
            Map<String, Double> totals = expenseService.getTotalsByCategory(DEMO_USER_ID);
            totalLabel.setText(String.format("Monthly total: %.2f", expenseService.calculateTotal(monthlyExpenses)));
            countLabel.setText("Entries: " + monthlyExpenses.size());
            categoryModel.setRowCount(0);
            for (Map.Entry<String, Double> entry : totals.entrySet()) {
                categoryModel.addRow(new Object[]{entry.getKey(), String.format("%.2f", entry.getValue())});
            }
            chart.setValues(totals);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Summary could not be loaded. Check the database connection.",
                    "Expense Tracker", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class CategoryChart extends JPanel {
        private Map<String, Double> values = new LinkedHashMap<>();

        CategoryChart() {
            setPreferredSize(new Dimension(500, 220));
            setBackground(Color.WHITE);
        }

        void setValues(Map<String, Double> values) {
            this.values = new LinkedHashMap<>(values);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                int left = 120;
                int top = 24;
                int rowHeight = 28;
                int barWidth = Math.max(1, getWidth() - left - 90);
                double maximum = values.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
                if (values.isEmpty()) {
                    g.drawString("No expenses for this month.", 16, 28);
                    return;
                }
                int index = 0;
                for (Map.Entry<String, Double> entry : values.entrySet()) {
                    int y = top + index * rowHeight;
                    int width = maximum == 0 ? 0 : (int) (barWidth * entry.getValue() / maximum);
                    g.setColor(Color.DARK_GRAY);
                    g.drawString(entry.getKey(), 12, y + 15);
                    g.setColor(new Color(70, 130, 180));
                    g.fillRect(left, y, width, 18);
                    g.setColor(Color.DARK_GRAY);
                    g.drawString(String.format("%.2f", entry.getValue()), left + width + 6, y + 15);
                    index++;
                }
            } finally {
                g.dispose();
            }
        }
    }
}
