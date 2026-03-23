package dao;

import db.DBConnection;
import model.Expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExpenseDAO {
    private final Connection connection;
    public ExpenseDAO(){
        this.connection= DBConnection.getInstance().getConnection();
    }

    public void insert(Expense expense) throws SQLException{
        String sql = """
                INSERT INTO expenses (amount, description, category_id, user_id, expense_date)
                VALUES (?,?,?,?,?)
                """;
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setDouble(1, expense.getAmount());
            ps.setString(2, expense.getDescription());
            ps.setInt(3, expense.getCategory_id());
            ps.setInt(4, expense.getUser_id());
            ps.setDate(5, Date.valueOf(expense.getExpenseDate()));
            ps.executeUpdate();
        }
    }

    public void update(Expense expense) throws SQLException{
        String sql =
                """
                        UPDATE expenses
                        SET amount=?, description=?, category_id=?, expense_date=?
                        WHERE id=?
                        """;
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setDouble(1, expense.getAmount());
            ps.setString(2, expense.getDescription());
            ps.setInt(3, expense.getCategory_id());
            ps.setDate(4, Date.valueOf(expense.getExpenseDate()));
            ps.setInt(5, expense.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException{
        String sql = """
                DELETE FROM expenses WHERE id=?
                """;
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Expense getById(int id) throws SQLException{
        String sql = """
                SELECT e.*, c.name AS category_name
                FROM expenses e
                LEFT JOIN categories c ON category_id=c.id
                WHERE e.id=?
                """;
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        }
        return null;
    }

    public List<Expense> getFiltered(int userId, String period) throws SQLException{
        String dateFilter = switch (period){
            case "DAILY"    -> " AND e.expense_date=CURRENT_DATE";
            case "WEEKLY"   -> " AND e.expense_date >=CURRENT_DATE- INTERVAL '7 days'";
            case "MONTLY"   -> " AND e.expense_date >= DATE_TRUNC('month', CURRENT_DATE')";
            default -> "";
        };

        String sql =
                """
                SELECT e.*, c.name AS category_name
                FROM expenses e
                LEFT JOIN categories ON e.category_id=c.id
                WHERE e.user_id=?
                """ + dateFilter + """
                ORDER BY e.expense_date DESC, e.created_at DESC
                """;

        List<Expense> list = new ArrayList<>();
        try(PreparedStatement ps= connection.prepareStatement(sql)){
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while(rs.next())
                list.add(mapRow(rs));
        }
        return list;
    }

    public Map<String, Double> getTotalsByCategory(int userId) throws SQLException{
        String sql =
                """
                SELECT c.name, SUM(e.amount) AS total
                FROM expenses e
                JOIN categories ON e.category_id = c.id
                WHERE e.user_id=?
                AND e.expense_date>=DATE_TRUNC('month', CURRENT_DATE)
                GROUP_BY c.name
                ORDER BY total DESC""";
        Map<String, Double> map = new LinkedHashMap<>();
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setInt(1, userId);
            ResultSet rs= ps.executeQuery();
            while(rs.next())
                map.put(rs.getString("name"), rs.getDouble("total"));
        }
        return map;
    }

    public Expense mapRow(ResultSet rs) throws SQLException{
        Expense e = new Expense(
                rs.getInt("id"),
                rs.getDouble("amount"),
                rs.getString("description"),
                rs.getInt("category_id"),
                rs.getInt("user_id"),
                rs.getDate("expense_date").toLocalDate()
        );
        e.setCategoryName(rs.getString("category_name"));
        return e;

    }
}
