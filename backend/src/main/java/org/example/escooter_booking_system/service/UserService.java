package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.User;

public interface UserService {
    User registerUser(User user);

    User authenticateUser(String username, String password);

    User getUserById(Long id);

    User updateUser(Long id, User userDetails);

    void deleteUser(Long id);
}
