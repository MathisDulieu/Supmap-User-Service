package com.novus.user_service.services;

import com.novus.shared_models.common.Kafka.KafkaMessage;
import com.novus.shared_models.common.Log.HttpMethod;
import com.novus.shared_models.common.Log.LogLevel;
import com.novus.shared_models.common.User.User;
import com.novus.user_service.configuration.DateConfiguration;
import com.novus.user_service.dao.UserDaoUtils;
import com.novus.user_service.utils.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final LogUtils logUtils;
    private final UserDaoUtils userDaoUtils;
    private final DateConfiguration dateConfiguration;

    public void processGetAllUsers(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            authenticatedUser.setLastActivityDate(dateConfiguration.newDate());
            userDaoUtils.save(authenticatedUser);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "GET_ALL_USERS_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    String.format("User with ID '%s' called processGetAllUsers method", authenticatedUser.getId()),
                    HttpMethod.GET,
                    "/private/admin/users",
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
                    "GET_ALL_USERS_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error processing get all users request: " + e.getMessage(),
                    HttpMethod.GET,
                    "/private/admin/users",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
            throw new RuntimeException("Failed to process get all users request: " + e.getMessage(), e);
        }
    }

    public void processGetUserAdminDashboardData(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            authenticatedUser.setLastActivityDate(dateConfiguration.newDate());
            userDaoUtils.save(authenticatedUser);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "GET_ADMIN_DASHBOARD_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    "Successfully retrieved admin dashboard data",
                    HttpMethod.GET,
                    "/private/admin/user/dashboard-data",
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
                    "GET_ADMIN_DASHBOARD_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error processing get admin dashboard data request: " + e.getMessage(),
                    HttpMethod.GET,
                    "/private/admin/user/dashboard-data",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
            throw new RuntimeException("Failed to process get admin dashboard data request: " + e.getMessage(), e);
        }
    }
}