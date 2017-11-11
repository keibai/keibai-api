package main.java.dao;

import main.java.models.Category;

public interface CategoryDAO {
    // Create
    void createCategory(Category Category);

    // Read
    Category getCategoryById(int id);
    Category getCategoryByName(String name);

    // Update
    void updateCategory(Category Category);

    // Delete
    void deleteCategory(int id);
}
