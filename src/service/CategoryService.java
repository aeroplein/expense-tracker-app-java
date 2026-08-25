package service;

import dao.CategoryDAO;
import model.Category;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService(){
        this.categoryDAO=new CategoryDAO();
    }

    public List<Category> getAll(int userId) throws SQLException {
        return categoryDAO.getAll(userId);
    }

    public void addCategory(String name, String colorHex, int userId) throws SQLException{
        validateCategory(name, colorHex);
        Category category = new Category(0, name, colorHex, userId);
        categoryDAO.insert(category);
    }

    public void deleteCategory(int id) throws SQLException{
        if (id<=0) throw new IllegalArgumentException("Invalid Category ID.");
        categoryDAO.delete(id);
    }

    public void updateCategory(Category category) throws SQLException{
        validateCategory(category.getName(), category.getColorHex());
        categoryDAO.update(category);
    }

    public void validateCategory(String name, String colorHex){
        if (name==null|| name.trim().isEmpty()){
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        if (name.length()>50){
            throw new IllegalArgumentException("Category name can be maximum of 50 characters.");
        }

        if (colorHex==null || !colorHex.matches("^#[0-9A-Fa-f]{6}$")){
            throw new IllegalArgumentException("Invalid color format. It should be #E24B4A");
        }
    }


}
