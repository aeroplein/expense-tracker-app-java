package service;

import dao.ExpenseDAO;
import model.Expense;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class ReportService {

    private final ExpenseDAO expenseDAO;

    public ReportService(){
        this.expenseDAO = new ExpenseDAO();
    }

    public void exportToCSV(String filePath, List<Expense> expenses) throws IOException{
        try(PrintWriter pw= new PrintWriter(new FileWriter(filePath))){
            pw.println("ID,Date,Description,Category,Amount");
            for (Expense e:expenses){
                pw.printf("%d,%s,%s,%s,%.2f%n",
                        e.getId(), e.getExpenseDate(),
                        escapeCsv(e.getDescription()),
                        escapeCsv(e.getCategoryName()),
                        e.getAmount()
                        );
            }
        }
    }

    public double getMonthlyTotal(int userId) throws SQLException{
        return expenseDAO.getFiltered(userId, "MONTHLY").stream().mapToDouble(Expense::getAmount).sum();
    }

    private String escapeCsv(String value){
        if (value==null) return "";
        if (value.contains(",") || value.contains("\"")){
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
