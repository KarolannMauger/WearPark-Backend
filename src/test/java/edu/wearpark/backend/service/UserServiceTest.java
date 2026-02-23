package edu.wearpark.backend.service;

import com.mongodb.client.result.UpdateResult;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.domain.UserPreferences;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnFalseWhenAllFieldsAreNull() {
        User user = new User();
        user.setId(new ObjectId());

        boolean result = userService.patchUser(user);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenNoDocumentModified() {
        ObjectId id = new ObjectId();

        User user = new User();
        user.setId(id);
        user.setFirstName("John");

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getModifiedCount()).thenReturn(0L);

        when(mongoTemplate.updateFirst(ArgumentMatchers.any(), ArgumentMatchers.any(), eq(User.class)))
                .thenReturn(updateResult);

        boolean result = userService.patchUser(user);

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenDocumentModified() {
        ObjectId id = new ObjectId();

        User user = new User();
        user.setId(id);
        user.setGender("MALE");
        user.setLastName("test");
        user.setDateOfBirth(Instant.now());
        user.setHasDiagnosis(true);
        user.setDiagnosis("123");
        user.setPreferences(new UserPreferences());

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getModifiedCount()).thenReturn(2L);

        when(mongoTemplate.updateFirst(ArgumentMatchers.any(), ArgumentMatchers.any(), eq(User.class)))
                .thenReturn(updateResult);

        boolean result = userService.patchUser(user);

        assertTrue(result);
    }
}