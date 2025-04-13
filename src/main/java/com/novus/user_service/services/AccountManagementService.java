package com.novus.user_service.services;

import com.novus.shared_models.common.Kafka.KafkaMessage;
import com.novus.shared_models.common.Log.HttpMethod;
import com.novus.shared_models.common.Log.LogLevel;
import com.novus.shared_models.common.User.User;
import com.novus.shared_models.common.User.UserRole;
import com.novus.user_service.UuidProvider;
import com.novus.user_service.configuration.DateConfiguration;
import com.novus.user_service.dao.UserDaoUtils;
import com.novus.user_service.utils.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountManagementService {

    private final UserDaoUtils userDaoUtils;
    private final LogUtils logUtils;
    private final UuidProvider uuidProvider;
    private final DateConfiguration dateConfiguration;

    public void processDeleteAuthenticatedUserAccount(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            Optional<User> optionalUser = userDaoUtils.findById(authenticatedUser.getId());
            if (optionalUser.isEmpty()) {
                throw new RuntimeException("User not found with id : " + authenticatedUser.getId());
            }

            userDaoUtils.deleteUser(authenticatedUser);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "DELETE_USER_ACCOUNT_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    String.format("User %s successfully deleted their account", authenticatedUser.getUsername()),
                    HttpMethod.DELETE,
                    "/private/user",
                    "user-service",
                    null,
                    authenticatedUser.getId()
            );
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();

            logUtils.buildAndSaveLog(
                    LogLevel.ERROR,
                    "DELETE_USER_ACCOUNT_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error deleting user account: " + e.getMessage(),
                    HttpMethod.DELETE,
                    "/private/user",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
            throw new RuntimeException("Failed to delete user account: " + e.getMessage(), e);
        }
    }

    public void processCreateAdminAccount(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            Map<String, String> request = kafkaMessage.getRequest();
            String username = request.get("username");
            String email = request.get("email");
            String password = request.get("password");
            boolean isValidEmail = Boolean.parseBoolean(request.get("isValidEmail"));
            String profileImage = request.get("profileImage");

            User adminUser = User.builder()
                    .id(uuidProvider.generateUuid())
                    .username(username)
                    .email(email)
                    .password(password)
                    .isValidEmail(isValidEmail)
                    .profileImage(profileImage)
                    .role(UserRole.ADMIN)
                    .build();

            authenticatedUser.setLastActivityDate(dateConfiguration.newDate());

            userDaoUtils.save(adminUser);
            userDaoUtils.save(authenticatedUser);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "CREATE_ADMIN_ACCOUNT_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    String.format("Admin account created for username: %s with ID: %s", username, adminUser.getId()),
                    HttpMethod.POST,
                    "/protected/user/create-admin",
                    "user-service",
                    null,
                    authenticatedUser.getId()
            );
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();

            logUtils.buildAndSaveLog(
                    LogLevel.ERROR,
                    "CREATE_ADMIN_ACCOUNT_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error creating admin account: " + e.getMessage(),
                    HttpMethod.POST,
                    "/protected/user/create-admin",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
            throw new RuntimeException("Failed to create admin account: " + e.getMessage(), e);
        }
    }

    public void processDeleteAdminAccount(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            Map<String, String> request = kafkaMessage.getRequest();
            String userId = request.get("userId");

            Optional<User> optionalUser = userDaoUtils.findById(userId);
            if (optionalUser.isEmpty()) {
                throw new IllegalArgumentException("User not found with id : " + userId);
            }

            authenticatedUser.setLastActivityDate(dateConfiguration.newDate());
            userDaoUtils.save(authenticatedUser);

            userDaoUtils.deleteUser(optionalUser.get());

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "DELETE_ADMIN_ACCOUNT_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    String.format("Admin account deleted for user with ID: %s", userId),
                    HttpMethod.DELETE,
                    "/protected/user/delete-admin/{id}",
                    "user-service",
                    null,
                    authenticatedUser.getId()
            );
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();

            logUtils.buildAndSaveLog(
                    LogLevel.ERROR,
                    "DELETE_ADMIN_ACCOUNT_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error deleting admin account: " + e.getMessage(),
                    HttpMethod.DELETE,
                    "/protected/user/delete-admin/{id}",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
            throw new RuntimeException("Failed to delete admin account: " + e.getMessage(), e);
        }
    }
}
