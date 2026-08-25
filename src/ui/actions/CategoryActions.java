package ui.actions;

public interface CategoryActions {
    void insertCategory(String name, String colorHex);
    void deleteCategory(int id);
    void reloadCategories();
}
