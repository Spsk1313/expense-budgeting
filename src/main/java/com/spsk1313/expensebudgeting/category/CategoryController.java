package com.spsk1313.expensebudgeting.category;


import com.spsk1313.expensebudgeting.category.dto.CategoryResponse;
import com.spsk1313.expensebudgeting.category.dto.CreateCategoryRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@PathVariable Long userId, @Valid @RequestBody CreateCategoryRequest req) {
        CategoryResponse response = categoryService.createCategory(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(@PathVariable Long userId) {
        List<CategoryResponse> response = categoryService.getCategories(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long userId, @PathVariable Long categoryId) {
        CategoryResponse response = categoryService.getCategoryById(userId, categoryId);
        return ResponseEntity.ok(response);
    }


}
