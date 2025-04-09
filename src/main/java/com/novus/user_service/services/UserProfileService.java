package com.novus.user_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novus.shared_models.GeoPoint;
import com.novus.shared_models.common.Kafka.KafkaMessage;
import com.novus.shared_models.common.Log.HttpMethod;
import com.novus.shared_models.common.Log.LogLevel;
import com.novus.shared_models.common.User.User;
import com.novus.user_service.dao.UserDaoUtils;
import com.novus.user_service.utils.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final LogUtils logUtils;
    private final UserDaoUtils userDaoUtils;
    private final ObjectMapper objectMapper;

    public void processGetAuthenticatedUserDetails(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            authenticatedUser.setLastActivityDate(new Date());

            userDaoUtils.save(authenticatedUser);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "GET_USER_DETAILS_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    String.format("Retrieved details for user: %s", authenticatedUser.getUsername()),
                    HttpMethod.GET,
                    "/users/details",
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
                    "GET_USER_DETAILS_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error logging user details request: " + e.getMessage(),
                    HttpMethod.GET,
                    "/users/details",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
        }
    }

    public void processUpdateAuthenticatedUserDetails(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            authenticatedUser.setLastActivityDate(new Date());
            authenticatedUser.setUpdatedAt(new Date());

            userDaoUtils.save(authenticatedUser);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "UPDATE_USER_DETAILS_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    String.format("Updated details for user: %s", authenticatedUser.getUsername()),
                    HttpMethod.PUT,
                    "/users/details",
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
                    "UPDATE_USER_DETAILS_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error updating user details: " + e.getMessage(),
                    HttpMethod.PUT,
                    "/users/details",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
            throw new RuntimeException("Failed to update user details: " + e.getMessage(), e);
        }
    }

    public void processSetUserProfileImage(KafkaMessage kafkaMessage) {
        log.info("Processing setUserProfileImage for user: {}", kafkaMessage.getAuthenticatedUser().getUsername());

        Map<String, String> request = kafkaMessage.getRequest();
        String fileAsBase64 = request.get("file");
    }

    public void processUpdateUserLocation(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            Map<String, String> request = kafkaMessage.getRequest();
            String locationStr = request.get("location");

            if (locationStr == null || locationStr.isEmpty()) {
                throw new IllegalArgumentException("Location cannot be empty");
            }

            Double latitude;
            Double longitude;

            try {
                JsonNode locationNode = objectMapper.readTree(locationStr);

                if (locationNode.has("latitude") && locationNode.has("longitude")) {
                    latitude = locationNode.get("latitude").asDouble();
                    longitude = locationNode.get("longitude").asDouble();
                } else {
                    throw new IllegalArgumentException("Location must contain latitude and longitude");
                }
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid location format: " + locationStr);
            }

            GeoPoint newLocation = GeoPoint.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();

            authenticatedUser.setLastKnownLocation(newLocation);
            authenticatedUser.setLastActivityDate(new Date());

            userDaoUtils.save(authenticatedUser);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "UPDATE_USER_LOCATION_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    String.format("Updated location for user: %s to [lat: %f, lng: %f]", authenticatedUser.getUsername(), latitude, longitude),
                    HttpMethod.PUT,
                    "/users/location",
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
                    "UPDATE_USER_LOCATION_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error updating user location: " + e.getMessage(),
                    HttpMethod.PUT,
                    "/users/location",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
            throw new RuntimeException("Failed to update user location: " + e.getMessage(), e);
        }
    }

}
