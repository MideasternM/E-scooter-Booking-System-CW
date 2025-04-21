package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.User;
import org.example.escooter_booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User registerUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setUsername(userDetails.getUsername());
            user.setPassword(userDetails.getPassword());
            user.setEmail(userDetails.getEmail());
            user.setPhoneNumber(userDetails.getPhoneNumber());
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User suspendUser(Long id, String reason) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setStatus("Suspended");
            user.setSuspensionReason(reason);
            user.setSuspendedAt(new Date());
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User activateUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setStatus("Active");
            user.setSuspensionReason(null);
            user.setSuspendedAt(null);
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User saveUserNotes(Long id, String notes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setNotes(notes);
            return userRepository.save(user);
        }
        return null;
    }
}
