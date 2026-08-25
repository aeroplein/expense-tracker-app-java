package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Shared visual tokens and component styling for the light grey and pink application theme. */
public final class UIStyles {
    public static final Color APP_BACKGROUND = Color.decode("#F8F8F7");
    public static final Color SIDEBAR = Color.decode("#B8BAC1");
    public static final Color TABLE_PINK = Color.decode("#F6B6CC");
    public static final Color PRIMARY = Color.decode("#B0B3BD");
    public static final Color PRIMARY_HOVER = Color.decode("#989CA8");
    public static final Color DANGER = Color.decode("#F3B0C8");
    public static final Color DANGER_HOVER = Color.decode("#EA98B8");
    public static final Color SURFACE = Color.decode("#FFFFFF");
    public static final Color SECONDARY_HOVER = Color.decode("#ECEDEF");
    public static final Color TEXT = Color.decode("#1E222B");
    public static final Color MUTED_TEXT = Color.decode("#565C68");
    public static final Color BORDER = Color.decode("#C1C4CB");
    public static final Color FOCUS = Color.decode("#6D7485");

    /* Legacy names keep the existing screen code readable while using the new visual language. */
    public static final Color SOFT_CREAM = APP_BACKGROUND;
    public static final Color WARM_FOG = SIDEBAR;
    public static final Color BERRY_GOOD = TABLE_PINK;
    public static final Color DARK_BERRY = PRIMARY;
    public static final Color DARK_BERRY_HOVER = PRIMARY_HOVER;
    public static final Color BERRY_HOVER = SECONDARY_HOVER;

    public enum ButtonTone { PRIMARY, SECONDARY, DANGER }

    private UIStyles() {
    }

    public static void configureDefaults() {
        FontUIResource font = new FontUIResource("Segoe UI", Font.PLAIN, 14);
        UIManager.put("defaultFont", font);
        UIManager.put("Component.focusColor", FOCUS);
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 7);
        UIManager.put("Component.arrowType", "triangle");
        UIManager.put("Button.focusWidth", 2);
        UIManager.put("Button.innerFocusWidth", 0);
        UIManager.put("Button.borderWidth", 1);
        UIManager.put("Button.background", SURFACE);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.hoverBackground", SECONDARY_HOVER);
        UIManager.put("Button.pressedBackground", SECONDARY_HOVER);
        UIManager.put("Button.default.background", PRIMARY);
        UIManager.put("Button.default.hoverBackground", PRIMARY_HOVER);
        UIManager.put("TextComponent.focusWidth", 1);
        UIManager.put("TextComponent.borderWidth", 1);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("FormattedTextField.background", SURFACE);
        UIManager.put("FormattedTextField.foreground", TEXT);
        UIManager.put("ComboBox.background", SURFACE);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("MenuBar.background", SURFACE);
        UIManager.put("Menu.foreground", TEXT);
        UIManager.put("MenuItem.background", SURFACE);
        UIManager.put("MenuItem.foreground", TEXT);
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("Panel.background", APP_BACKGROUND);
        UIManager.put("OptionPane.background", APP_BACKGROUND);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.buttonAreaBorder", new EmptyBorder(10, 0, 0, 0));
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumb", PRIMARY);
        UIManager.put("ScrollBar.track", APP_BACKGROUND);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", true);
    }

    public static void styleButton(JButton button, boolean primary) {
        styleButton(button, primary ? ButtonTone.PRIMARY : ButtonTone.SECONDARY);
    }

    public static void styleButton(JButton button, ButtonTone tone) {
        Color restingBackground = switch (tone) {
            case PRIMARY -> PRIMARY;
            case DANGER -> DANGER;
            case SECONDARY -> SURFACE;
        };
        Color hoverBackground = switch (tone) {
            case PRIMARY -> PRIMARY_HOVER;
            case DANGER -> DANGER_HOVER;
            case SECONDARY -> SECONDARY_HOVER;
        };

        button.setBackground(restingBackground);
        button.setForeground(TEXT);
        button.setBorder(new LineBorder(BORDER));
        button.setMargin(new Insets(7, 14, 7, 14));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(hoverBackground);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(restingBackground);
            }
        });
    }

    public static void styleTable(JTable table) {
        table.setBackground(SURFACE);
        table.setForeground(TEXT);
        table.setGridColor(Color.decode("#E1E2E5"));
        table.setSelectionBackground(TABLE_PINK);
        table.setSelectionForeground(TEXT);
        table.setFont(table.getFont().deriveFont(Font.PLAIN, 13f));
        table.setRowHeight(28);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_PINK);
        header.setForeground(TEXT);
        header.setBorder(new LineBorder(BORDER));
        header.setFont(header.getFont().deriveFont(Font.PLAIN, 13f));
        header.setPreferredSize(new Dimension(0, 32));
        header.setReorderingAllowed(false);
    }

    public static void styleInput(JComponent input) {
        input.setBackground(SURFACE);
        input.setForeground(TEXT);
        input.setBorder(new LineBorder(BORDER));
        input.putClientProperty("JComponent.roundRect", true);
    }

    public static void styleLabel(JLabel label) {
        label.setForeground(TEXT);
    }

    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(new LineBorder(BORDER));
        scrollPane.getViewport().setBackground(SURFACE);
        scrollPane.setBackground(SURFACE);
    }

    public static void styleList(JList<?> list) {
        list.setBackground(SURFACE);
        list.setForeground(TEXT);
        list.setSelectionBackground(TABLE_PINK);
        list.setSelectionForeground(TEXT);
        list.setFixedCellHeight(30);
    }

    public static void styleRadioButton(JRadioButton button) {
        button.setBackground(SIDEBAR);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setOpaque(true);
    }

    public static void styleMenuItem(JMenuItem item) {
        item.setBackground(SURFACE);
        item.setForeground(TEXT);
    }

    public static void styleDialog(JDialog dialog) {
        dialog.getContentPane().setBackground(APP_BACKGROUND);
    }

    public static void styleCalendarDay(JButton button, boolean selected) {
        button.setBackground(selected ? TABLE_PINK : SURFACE);
        button.setForeground(TEXT);
        button.setBorder(new LineBorder(BORDER));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                if (!selected) button.setBackground(SECONDARY_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(selected ? TABLE_PINK : SURFACE);
            }
        });
    }
}
