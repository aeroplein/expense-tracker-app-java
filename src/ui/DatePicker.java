package ui;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** A dependency-free date field with a calendar popup for expense forms. */
public final class DatePicker extends JPanel {
    private static final Locale TURKISH = Locale.forLanguageTag("tr-TR");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd.MM.uuuu", TURKISH);

    private final JTextField valueField = new JTextField();
    private LocalDate selectedDate;
    private YearMonth displayedMonth;
    private JPopupMenu calendarPopup;

    public DatePicker(LocalDate initialDate) {
        selectedDate = initialDate == null ? LocalDate.now() : initialDate;
        displayedMonth = YearMonth.from(selectedDate);
        setLayout(new BorderLayout(4, 0));
        setOpaque(false);

        valueField.setEditable(false);
        valueField.setFocusable(false);
        UIStyles.styleInput(valueField);
        updateText();

        JButton calendarButton = new JButton("Takvim");
        calendarButton.setToolTipText("Choose a date");
        UIStyles.styleButton(calendarButton, false);
        calendarButton.addActionListener(event -> showCalendar());

        add(valueField, BorderLayout.CENTER);
        add(calendarButton, BorderLayout.EAST);
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    private void showCalendar() {
        calendarPopup = new JPopupMenu();
        calendarPopup.setBorder(BorderFactory.createLineBorder(UIStyles.BORDER));
        calendarPopup.add(buildCalendar());
        calendarPopup.show(this, 0, getHeight());
    }

    private JPanel buildCalendar() {
        JPanel calendar = new JPanel(new BorderLayout(0, 8));
        calendar.setBackground(UIStyles.SOFT_CREAM);
        calendar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel navigation = new JPanel(new BorderLayout(6, 0));
        navigation.setBackground(UIStyles.SOFT_CREAM);
        JButton previous = navigationButton("‹", -1);
        JButton next = navigationButton("›", 1);
        JLabel title = new JLabel(displayedMonth.getMonth().getDisplayName(TextStyle.FULL, TURKISH)
                + " " + displayedMonth.getYear(), SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        UIStyles.styleLabel(title);
        navigation.add(previous, BorderLayout.WEST);
        navigation.add(title, BorderLayout.CENTER);
        navigation.add(next, BorderLayout.EAST);
        calendar.add(navigation, BorderLayout.NORTH);

        JPanel days = new JPanel(new GridLayout(0, 7, 3, 3));
        days.setBackground(UIStyles.SOFT_CREAM);
        for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY}) {
            JLabel weekday = new JLabel(day.getDisplayName(TextStyle.SHORT, TURKISH), SwingConstants.CENTER);
            weekday.setFont(weekday.getFont().deriveFont(Font.BOLD, 11f));
            UIStyles.styleLabel(weekday);
            days.add(weekday);
        }

        int leadingEmptyCells = displayedMonth.atDay(1).getDayOfWeek().getValue() - 1;
        for (int index = 0; index < leadingEmptyCells; index++) days.add(new JLabel());
        for (int day = 1; day <= displayedMonth.lengthOfMonth(); day++) {
            LocalDate date = displayedMonth.atDay(day);
            JButton dayButton = new JButton(String.valueOf(day));
            UIStyles.styleCalendarDay(dayButton, date.equals(selectedDate));
            dayButton.addActionListener(event -> selectDate(date));
            days.add(dayButton);
        }
        calendar.add(days, BorderLayout.CENTER);
        return calendar;
    }

    private JButton navigationButton(String text, int monthOffset) {
        JButton button = new JButton(text);
        UIStyles.styleButton(button, false);
        button.addActionListener(event -> {
            displayedMonth = displayedMonth.plusMonths(monthOffset);
            calendarPopup.removeAll();
            calendarPopup.add(buildCalendar());
            calendarPopup.pack();
        });
        return button;
    }

    private void selectDate(LocalDate date) {
        selectedDate = date;
        displayedMonth = YearMonth.from(date);
        updateText();
        calendarPopup.setVisible(false);
    }

    private void updateText() {
        valueField.setText(DISPLAY_FORMAT.format(selectedDate));
    }
}
