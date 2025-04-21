package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.User;
import java.util.List;

public interface UserService {
    User registerUser(User user);

    User authenticateUser(String username, String password);

    User getUserById(Long id);

    List<User> getAllUsers();

    User updateUser(Long id, User userDetails);

    void deleteUser(Long id);

    User suspendUser(Long id, String reason);

    User activateUser(Long id);

    User saveUserNotes(Long id, String notes);
}
