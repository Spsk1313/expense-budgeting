package com.spsk1313.expensebudgeting.user;

import com.spsk1313.expensebudgeting.user.dto.CreateUserRequest;
import com.spsk1313.expensebudgeting.user.dto.UserResponse;
import com.spsk1313.expensebudgeting.user.exception.DuplicateEmailException;
import com.spsk1313.expensebudgeting.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(CreateUserRequest req) {
        String normalizedEmail = req.email().trim().toLowerCase();
        String normalizedName = req.name().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException();
        }

        User user = new User(normalizedName, normalizedEmail);
        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return toResponse(user);
    }

    private static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}