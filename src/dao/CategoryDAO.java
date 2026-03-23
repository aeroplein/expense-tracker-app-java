package dao;

import db.DBConnection;
import model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private final Connection connection;

    public CategoryDAO(){
        this.connection= DBConnection.getInstance().getConnection();
    }

    public List<Category> getAll(int userId) throws SQLException{
        String sql = """
                SELECT * FROM categories
                WHERE user_id = ?
                ORDER BY name ASC
                """;
        List<Category> list = new ArrayList<>();

        try(PreparedStatement ps= connection.prepareStatement(sql)){
            ps.setInt(1, userId);
            ResultSet rs= ps.executeQuery();
            while(rs.next())
                list.add(mapRow(rs));
        }
        return list;
    }

    public Category getById(int id) throws SQLException{
        String sql = "SELECT * FROM categories WHERE id = ?";

        try(PreparedStatement ps =connection.prepareStatement(sql)){
            ps.setInt(1, id);
            ResultSet rs=ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        }
        return null;
    }

    public void insert(Category category) throws SQLException{
        String sql = """
                INSERT INTO categories (name, color_hex, user_id)
                VALUES (?,?,?)
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, category.getName());
            ps.setString(2, category.getColorHex());
            ps.setInt(3, category.getUserId());
            ps.executeUpdate();
        }
    }

    public void update(Category category) throws SQLException{
        String sql = """
                UPDATE categories
                SET name = ?, color_hex=?
                WHERE id = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, category.getName());
            ps.setString(2, category.getColorHex());
            ps.setInt(3, category.getUserId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException{
        String sql = "DELETE FROM categories WHERE id = ?";

        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException{
        return new Category(
                rs.getInt   ("id"),
                rs.getString("name"),
                rs.getString("color_hex"),
                rs.getInt   ("user_id")
        );
    }
}
