package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.User;
import org.example.escooter_booking_system.repository.UserRepository;
// import org.example.escooter_booking_system.service.UserServiceImpl; // UserServiceImpl 在同一个包下，通常不需要显式导入
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor; // 用于捕获参数

import java.util.Optional;
import java.util.Date; // 需要为 suspendUser 和 activateUser

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // 导入所有Mockito的静态方法，如 when, verify, any等

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User userDetailsToUpdate;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setPhoneNumber("1234567890");
        testUser.setStatus("Active"); // 初始状态

        userDetailsToUpdate = new User();
        userDetailsToUpdate.setUsername("updateduser");
        userDetailsToUpdate.setPassword("newPassword!@#");
        userDetailsToUpdate.setEmail("updated@example.com");
        userDetailsToUpdate.setPhoneNumber("0987654321");
    }

    // --- 测试 registerUser ---
    @Test
    void registerUser_shouldSaveAndReturnUser() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newbie");
        newUser.setPassword("newPass");
        newUser.setEmail("newbie@example.com");

        User savedUser = new User(); // 模拟保存后带有ID的User
        savedUser.setId(2L);
        savedUser.setUsername(newUser.getUsername());
        savedUser.setPassword(newUser.getPassword());
        savedUser.setEmail(newUser.getEmail());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.registerUser(newUser);

        // Assert
        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(newUser.getUsername(), result.getUsername());
        verify(userRepository, times(1)).save(newUser);
    }

    // --- 测试 authenticateUser ---
    @Test
    void authenticateUser_whenUserNotFound_shouldReturnNull() {
        // Arrange
        when(userRepository.findByUsername("unknownuser")).thenReturn(null);

        // Act
        User result = userService.authenticateUser("unknownuser", "password");

        // Assert
        assertNull(result);
    }

    @Test
    void authenticateUser_whenPasswordIncorrect_shouldReturnNull() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        // Act
        User result = userService.authenticateUser("testuser", "wrongpassword");

        // Assert
        assertNull(result);
    }

    @Test
    void authenticateUser_whenCredentialsCorrect_shouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        // Act
        User result = userService.authenticateUser("testuser", "password123");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals("testuser", result.getUsername());
    }

    // --- 测试 getUserById ---
    @Test
    void getUserById_whenUserExists_shouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnNull() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        User result = userService.getUserById(99L);

        // Assert
        assertNull(result);
    }

    // --- 测试 updateUser ---
    @Test
    void updateUser_whenUserDoesNotExist_shouldReturnNull() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        User result = userService.updateUser(99L, userDetailsToUpdate);

        // Assert
        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_whenUserExists_shouldUpdateAndReturnUser() {
        // Arrange
        // testUser (从findById返回) 会在service方法中被修改，然后传递给save
        // 我们需要捕获传递给save的User对象，以验证其属性是否被正确更新

        User userFromDb = new User(); // 创建一个单独的对象模拟从DB获取的
        userFromDb.setId(1L);
        userFromDb.setUsername("testuser");
        userFromDb.setPassword("password123");
        userFromDb.setEmail("test@example.com");
        userFromDb.setPhoneNumber("1234567890");


        when(userRepository.findById(1L)).thenReturn(Optional.of(userFromDb));
        // 当 userRepository.save 被调用时，返回那个被传入的 User 对象
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));


        // Act
        User updatedUserResult = userService.updateUser(1L, userDetailsToUpdate);

        // Assert
        assertNotNull(updatedUserResult);
        assertEquals(userDetailsToUpdate.getUsername(), updatedUserResult.getUsername());
        assertEquals(userDetailsToUpdate.getPassword(), updatedUserResult.getPassword());
        assertEquals(userDetailsToUpdate.getEmail(), updatedUserResult.getEmail());
        assertEquals(userDetailsToUpdate.getPhoneNumber(), updatedUserResult.getPhoneNumber());
        assertEquals(1L, updatedUserResult.getId()); // ID不应改变

        // 验证 save 方法被调用了一次，并且参数是预期的
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(userDetailsToUpdate.getUsername(), savedUser.getUsername());
        assertEquals(userDetailsToUpdate.getPassword(), savedUser.getPassword());
        assertEquals(userDetailsToUpdate.getEmail(), savedUser.getEmail());
        assertEquals(userDetailsToUpdate.getPhoneNumber(), savedUser.getPhoneNumber());
    }
} 