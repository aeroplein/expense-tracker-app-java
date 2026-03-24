package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private ExpensePanel expensePanel;
    private JLabel statusLabel;

    public MainFrame(){
        super("Personal Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0,0));
        initMenuBar();
        initToolbar();
        initCenterPanel();
        initStatusBar();
        addWindowListener(new java.awt.event.WindowAdapter(){
            @Override
            public void windowClosing(java.awt.event.WindowEvent e){
                DBConnection.getInstance().close();
            }
        });
    }

    private void initMenuBar(){
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export to CSV.");
        JMenuItem exitItem = new JMenuItem("Exit");
        exportItem.addActionListener(e->expensePanel.exportToCSV());
        exitItem.addActionListener(e->{
            DBConnection.getInstance().close();
            System.exit(0);
        });

        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("View");
        JMenuItem summaryItem = new JMenuItem("Summary & Chart");
        summaryItem.addActionListener(e->openSummary());
        viewMenu.add(summaryItem);
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);
    }

}
