package com.spsk1313.expensebudgeting.category;

import com.spsk1313.expensebudgeting.category.dto.CategoryResponse;
import com.spsk1313.expensebudgeting.category.dto.CreateCategoryRequest;
import com.spsk1313.expensebudgeting.category.exception.CategoryNotFoundException;
import com.spsk1313.expensebudgeting.category.exception.DuplicateCategoryException;
import com.spsk1313.expensebudgeting.user.User;
import com.spsk1313.expensebudgeting.user.UserRepository;
import com.spsk1313.expensebudgeting.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public CategoryResponse createCategory(Long userId, CreateCategoryRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        String normalizedName = req.name().trim().toLowerCase(Locale.ROOT);

        if(categoryRepository.existsByNameAndTypeAndUser_Id(normalizedName, req.type(), userId)) throw new DuplicateCategoryException();

        Category category = new Category(user, normalizedName, req.type());
        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        return categoryRepository
                .findAllByUser_Id(userId)
                .stream()
                .map(CategoryService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long userId, Long categoryId) {
        Category category = categoryRepository.findByIdAndUser_Id(categoryId, userId).orElseThrow(() -> new CategoryNotFoundException(categoryId));

        return toResponse(category);
    }

    private static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getUser().getId(),
                category.getName(),
                category.getType(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
