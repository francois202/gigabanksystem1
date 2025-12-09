package integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gigabank.accountmanagement.GigaBankApplication;
import gigabank.accountmanagement.dto.request.UserUpdateRequest;
import gigabank.accountmanagement.dto.response.UserAccountResponse;
import gigabank.accountmanagement.model.UserEntity;
import gigabank.accountmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GigaBankApplication.class)
@AutoConfigureMockMvc
@EmbeddedKafka(ports = 9092)
@ActiveProfiles("test")
public class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache("usersCache");
        if (cache != null) {
            cache.clear();
        }
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    void getUser_shouldCacheResult() throws Exception {
        UserEntity user = new UserEntity();
        user.setEmail("test@email.com");
        user.setName("test_name");
        user.setPhoneNumber("+1234567890");
        UserEntity savedUser = userRepository.save(user);
        String email = savedUser.getEmail();

        mockMvc.perform(get("/api/user-actions/" + email))
                .andExpect(status().isOk());

        Cache cache = cacheManager.getCache("usersCache");
        assertNotNull(cache, "usersCache should exist");

        Cache.ValueWrapper wrapper = cache.get(email);
        assertNotNull(wrapper, "User should be cached");

        Object cachedObject = wrapper.get();
        assertNotNull(cachedObject);
        assertInstanceOf(UserAccountResponse.class, cachedObject);

        UserAccountResponse cachedResponse = (UserAccountResponse) cachedObject;
        assertEquals(email, cachedResponse.getEmail());

        userRepository.deleteById(savedUser.getId());

        mockMvc.perform(get("/api/user-actions/" + email))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    void updateUser_shouldEvictCache() throws Exception {
        UserEntity user = new UserEntity();
        user.setEmail("test@email.com");
        user.setName("test_name");
        user.setPhoneNumber("+1234567890");
        UserEntity savedUser = userRepository.save(user);
        String email = savedUser.getEmail();

        mockMvc.perform(get("/api/user-actions/" + email))
                .andExpect(status().isOk());

        Cache cache = cacheManager.getCache("usersCache");
        assertNotNull(cache);
        assertNotNull(cache.get(email), "User should be cached before update");

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("new_name");
        updateRequest.setPhoneNumber("+9876543210");

        mockMvc.perform(put("/api/user-actions/" + email)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new_name"))
                .andExpect(jsonPath("$.phoneNumber").value("+9876543210"));

        assertNull(cache.get(email), "Cache should be evicted after update");

        mockMvc.perform(get("/api/user-actions/" + email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new_name"))
                .andExpect(jsonPath("$.phoneNumber").value("+9876543210"));
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    void deleteUser_shouldEvictCache() throws Exception {
        UserEntity user = new UserEntity();
        user.setEmail("test@email.com");
        user.setName("test_name");
        user.setPhoneNumber("+1234567890");
        UserEntity savedUser = userRepository.save(user);
        String email = savedUser.getEmail();

        mockMvc.perform(get("/api/user-actions/" + email))
                .andExpect(status().isOk());

        Cache cache = cacheManager.getCache("usersCache");
        assertNotNull(cache);
        assertNotNull(cache.get(email), "User should be cached before delete");

        mockMvc.perform(delete("/api/user-actions/" + email))
                .andExpect(status().isNoContent());

        assertNull(cache.get(email), "Cache should be evicted after delete");

        assertFalse(userRepository.findByEmail(email).isPresent());

        mockMvc.perform(get("/api/user-actions/" + email))
                .andExpect(status().isNotFound());
    }
}