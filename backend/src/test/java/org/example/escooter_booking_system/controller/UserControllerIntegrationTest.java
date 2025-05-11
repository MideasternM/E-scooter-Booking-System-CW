package org.example.escooter_booking_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.escooter_booking_system.config.TestApplication;
import org.example.escooter_booking_system.model.User;
import org.example.escooter_booking_system.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class) // Load full context with TestApplication config
@AutoConfigureMockMvc // Configure MockMvc
@ActiveProfiles("test") // Ensure test properties are loaded
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb_user_controller;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.hibernate.ddl-auto=create-drop"
    // Other properties from your application-test.properties can be overridden here if needed
})
@Transactional // Rollback transactions after each test
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up before each test, just in case @Transactional doesn't cover all MockMvc scenarios perfectly
        // or if we decide to remove @Transactional later for some reason.
        userRepository.deleteAll(); 
        testUser = null;
    }

    // @AfterEach could also be used, but with @Transactional, data should be rolled back.
    // If not using @Transactional, @AfterEach for userRepository.deleteAll() is crucial.

    @Test
    void testRegisterUser_Success() throws Exception {
        User newUser = new User();
        newUser.setUsername("testuser_controller");
        newUser.setPassword("Password123!");
        newUser.setEmail("controller_test@example.com");
        newUser.setName("Controller Test User");
        newUser.setPhoneNumber("1234567890");
        // Ensure all non-nullable fields as per User entity are set
        // e.g. createdAt is auto-set, status has a default

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk()) // UserController returns 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is(newUser.getUsername())))
                .andExpect(jsonPath("$.email", is(newUser.getEmail())))
                .andExpect(jsonPath("$.name", is(newUser.getName())));

        // Optionally, verify directly in the DB (though @Transactional should make this redundant for cleanup)
        User foundUser = userRepository.findByUsername(newUser.getUsername());
        assertNotNull(foundUser);
        assertEquals(newUser.getEmail(), foundUser.getEmail());
    }

    // TODO: Add test for registration with duplicate username
    // TODO: Add test for registration with duplicate email
    // TODO: Add test for registration with missing required fields (validation failure)
} 