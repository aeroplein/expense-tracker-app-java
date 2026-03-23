package model;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

public class Expense {

    private int id;
    private double amount;
    private String description;
    private int category_id;
    private String categoryName;
    private int user_id;
    private LocalDate expenseDate;

    public Expense(double amount, String description, int category_id,
                   int user_id, LocalDate expenseDate){
        this.amount=amount;
        this.description=description;
        this.category_id=category_id;
        this.user_id=user_id;
        this.expenseDate=expenseDate;
    }

    public Expense(int id, double amount, String description,
                   int category_id, int user_id, LocalDate expenseDate){
        this(amount, description, category_id, user_id, expenseDate);
        this.id=id;
    }

    public double getAmount() {
        return amount;
    }

    public int getCategory_id() {
        return category_id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public int getId() {
        return id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    @Override
    public String toString() {
        return String.format("Expense{id=%d, amount=%.2f, category='%s', date=%s}",
                id, amount, categoryName, expenseDate);

    }
}
