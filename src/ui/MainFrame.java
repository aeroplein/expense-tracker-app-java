package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import ui.actions.ExpensePanelActions;


public class MainFrame extends JFrame {
    private ExpensePanelActions expenseActions;
    private ExpensePanel expensePanel;
    private JLabel statusLabel;

    public MainFrame(){
        super("Personal Expense Tracker");
        applyAppIcon();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0,0));
        getContentPane().setBackground(UIStyles.SOFT_CREAM);
        initMenuBar();
        initStatusBar();
        initCenterPanel();
        addWindowListener(new java.awt.event.WindowAdapter(){
            @Override
            public void windowClosing(java.awt.event.WindowEvent e){
                DBConnection.getInstance().close();
            }
        });
    }

    private void initMenuBar(){
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(UIStyles.SURFACE);
        menuBar.setForeground(UIStyles.TEXT);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyles.BORDER));
        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(UIStyles.TEXT);
        JMenuItem exportItem = new JMenuItem("Export to CSV.");
        JMenuItem exitItem = new JMenuItem("Exit");
        UIStyles.styleMenuItem(exportItem);
        UIStyles.styleMenuItem(exitItem);
        exportItem.addActionListener(e->expenseActions.exportToCSV());
        exitItem.addActionListener(e->{
            DBConnection.getInstance().close();
            System.exit(0);
        });

        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setForeground(UIStyles.TEXT);
        JMenuItem summaryItem = new JMenuItem("Summary & Chart");
        UIStyles.styleMenuItem(summaryItem);
        summaryItem.addActionListener(e->openSummary());
        viewMenu.add(summaryItem);
        JMenu manageMenu = new JMenu("Manage");
        manageMenu.setForeground(UIStyles.TEXT);
        JMenuItem categoriesItem = new JMenuItem("Categories");
        UIStyles.styleMenuItem(categoriesItem);
        categoriesItem.addActionListener(e -> openCategoryManager());
        manageMenu.add(categoriesItem);
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(manageMenu);
        setJMenuBar(menuBar);
    }

    private void initToolbar(){
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(UIStyles.SOFT_CREAM);
        toolBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0, UIStyles.BORDER));
        toolBar.add(
                makeIconButton("Add Expense",
                        "resources/icons/add.png",
                        "resources/icons/add_hover.png",
                        e->expenseActions.openAddDialog())
        );

        toolBar.add(makeIconButton(
                "Edit Expense",
                "resources/icons/edit.png",
                "resources/icons/edit_hover.png",
                e->expenseActions.openEditDialog()
        ));

        toolBar.add(makeIconButton(
                "Summary",
                "resources/icons/chart.png",
                "resources/icons/chart_hover.png",
                e->openSummary()
        ));
        toolBar.add(makeIconButton(
                "Export CSV",
                "resources/icons/export.png",
                "resources/icons/export_hover.png",
                e->expenseActions.exportToCSV()
        ));

    }

    private void initCenterPanel(){
        expensePanel = new ExpensePanel(this);
        expenseActions = expensePanel;
        add(expensePanel, BorderLayout.CENTER);
    }

    private void initStatusBar(){
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(UIStyles.WARM_FOG);
        statusBar.setBorder(BorderFactory.createMatteBorder(1,0,0,0, UIStyles.BORDER));
        statusBar.setPreferredSize(new Dimension(0,24));

        statusLabel = new JLabel(" Ready");
        UIStyles.styleLabel(statusLabel);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void openSummary(){
        JDialog dialog = new JDialog(this, "Summary & Chart", true);
        UIStyles.styleDialog(dialog);
        dialog.setSize(700, 520);
        dialog.setLocationRelativeTo(this);
        dialog.add(new SummaryPanel());
        dialog.setVisible(true);

    }

    public void openCategoryManager() {
        new CategoryManagerDialog(this, () -> {
            expensePanel.refreshCategories();
            setStatus("Categories refreshed.");
        }).setVisible(true);
    }

    public void setStatus(String message){
        statusLabel.setText(" "+ message);
    }

    private void applyAppIcon() {
        java.net.URL iconUrl = getClass().getClassLoader().getResource("resources/icons/expense-tracker.png");
        if (iconUrl != null) {
            setIconImage(new ImageIcon(iconUrl).getImage());
            return;
        }

        File developmentIcon = new File("src/resources/icons/expense-tracker.png");
        if (developmentIcon.isFile()) {
            setIconImage(new ImageIcon(developmentIcon.getAbsolutePath()).getImage());
        }
    }

    private JButton makeIconButton(String tooltip,
                                   String iconPath,
                                   String rolloverPath,
                                   java.awt.event.ActionListener action
                                   ){
        JButton btn = new JButton();
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        java.net.URL iconUrl= getClass().getClassLoader().getResource(iconPath);
        if (iconUrl!=null){
            ImageIcon normal = new ImageIcon(iconUrl);
            btn.setIcon(normal);

            java.net.URL rollUrl = getClass().getClassLoader().getResource(rolloverPath);
            if (rollUrl!=null){
                btn.setRolloverIcon(new ImageIcon(rollUrl));
            }

        }else{
            btn.setText(tooltip);
            btn.setBorderPainted(true);
            btn.setContentAreaFilled(true);
            UIStyles.styleButton(btn, false);
        }
        btn.addActionListener(action);
        return btn;


    }

    public static void main(String[] args) {
        try {
            Class<?> flatLightLaf = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLightLaf.getMethod("setup").invoke(null);
            UIStyles.configureDefaults();
        } catch (Exception ignored) {
            // FlatLaf is optional while developing; Swing's default look and feel remains usable.
        }

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

}
