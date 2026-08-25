package service;

import dao.ExpenseDAO;
import model.Expense;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ExpenseService {
    private final ExpenseDAO expenseDAO;
    public ExpenseService(){
        this.expenseDAO=new ExpenseDAO();
    }

    public void addExpense(Expense expense) throws SQLException{
        validateExpense(expense);
        expenseDAO.insert(expense);
    }

    public void updateExpense(Expense expense) throws SQLException{
        validateExpense(expense);
        expenseDAO.update(expense);
    }

    public void deleteExpense(int id) throws SQLException{
        if (id<=0) throw new IllegalArgumentException("Invalid expense ID.");
        expenseDAO.delete(id);
    }

    public List<Expense> getFiltered(int userId, String period) throws SQLException{
        return expenseDAO.getFiltered(userId, period);
    }

    public Expense getById(int id) throws SQLException{
        if (id<=0) throw new IllegalArgumentException("Invalid expense ID.");
        return expenseDAO.getById(id);
    }

    public Map<String, Double> getTotalsByCategory(int userId) throws SQLException{
        return expenseDAO.getTotalsByCategory(userId);
    }

    public double calculateTotal(List<Expense> expenses){
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    private void validateExpense(Expense expense){
        if (expense.getAmount()<=0){
            throw new IllegalArgumentException("Expense has to be greater than zero.");
        }
        if (expense.getExpenseDate()==null){
            throw new IllegalArgumentException("Data cannot be empty.");
        }
        if (expense.getUser_id()<=0){
            throw new IllegalArgumentException("Invalid user.");
        }
    }
}
