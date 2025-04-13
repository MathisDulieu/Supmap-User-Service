package com.novus.user_service.services;

import com.novus.shared_models.GeoPoint;
import com.novus.shared_models.common.Kafka.KafkaMessage;
import com.novus.shared_models.common.Log.HttpMethod;
import com.novus.shared_models.common.Log.LogLevel;
import com.novus.shared_models.common.User.User;
import com.novus.user_service.configuration.DateConfiguration;
import com.novus.user_service.configuration.EnvConfiguration;
import com.novus.user_service.dao.UserDaoUtils;
import com.novus.user_service.utils.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final LogUtils logUtils;
    private final UserDaoUtils userDaoUtils;
    private final EnvConfiguration envConfiguration;
    private final JwtTokenService jwtTokenService;
    private final EmailService emailService;
    private final DateConfiguration dateConfiguration;

    public void processGetAuthenticatedUserDetails(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            authenticatedUser.setLastActivityDate(dateConfiguration.newDate());

            userDaoUtils.save(authenticatedUser);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "GET_USER_DETAILS_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    String.format("Retrieved details for user: %s", authenticatedUser.getUsername()),
                    HttpMethod.GET,
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
                    "GET_USER_DETAILS_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error logging user details request: " + e.getMessage(),
                    HttpMethod.GET,
                    "/private/user",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
        }
    }

    public void processUpdateAuthenticatedUserDetails(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            Map<String, String> request = kafkaMessage.getRequest();
            boolean hasUpdatedEmail = Boolean.parseBoolean(request.get("hasUpdatedEmail"));

            authenticatedUser.setLastActivityDate(dateConfiguration.newDate());
            authenticatedUser.setUpdatedAt(dateConfiguration.newDate());

            if (hasUpdatedEmail) {
                authenticatedUser.setValidEmail(false);
                String emailConfirmationToken = jwtTokenService.generateEmailConfirmationToken(authenticatedUser.getId());
                emailService.sendEmail(authenticatedUser.getEmail(), "Confirm your Supmap account", getAccountRegistrationEmail(emailConfirmationToken, authenticatedUser.getUsername()));
            }

            userDaoUtils.save(authenticatedUser);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "UPDATE_USER_DETAILS_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    String.format("Updated details for user: %s", authenticatedUser.getUsername()),
                    HttpMethod.PUT,
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
                    "UPDATE_USER_DETAILS_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error updating user details: " + e.getMessage(),
                    HttpMethod.PUT,
                    "/private/user",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
            throw new RuntimeException("Failed to update user details: " + e.getMessage(), e);
        }
    }

    public void processSetUserProfileImage(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            Map<String, String> request = kafkaMessage.getRequest();
            String imageUrl = request.get("imageUrl");

            authenticatedUser.setProfileImage(imageUrl);
            authenticatedUser.setUpdatedAt(dateConfiguration.newDate());
            authenticatedUser.setLastActivityDate(dateConfiguration.newDate());

            userDaoUtils.save(authenticatedUser);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "SET_USER_PROFILE_IMAGE_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    String.format("Updated profile image for user: %s", authenticatedUser.getUsername()),
                    HttpMethod.PUT,
                    "/private/user/profile-image",
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
                    "SET_USER_PROFILE_IMAGE_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error updating user profile image: " + e.getMessage(),
                    HttpMethod.PUT,
                    "/private/user/profile-image",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
            throw new RuntimeException("Failed to update user profile image: " + e.getMessage(), e);
        }
    }

    public void processUpdateUserLocation(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            Map<String, String> request = kafkaMessage.getRequest();
            double latitude = Double.parseDouble(request.get("latitude"));
            double longitude = Double.parseDouble(request.get("longitude"));

            GeoPoint newLocation = GeoPoint.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();

            authenticatedUser.setLastKnownLocation(newLocation);
            authenticatedUser.setLastActivityDate(dateConfiguration.newDate());
            authenticatedUser.setUpdatedAt(dateConfiguration.newDate());

            userDaoUtils.save(authenticatedUser);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "UPDATE_USER_LOCATION_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    String.format("Updated location for user: %s to [lat: %f, lng: %f]", authenticatedUser.getUsername(), latitude, longitude),
                    HttpMethod.PUT,
                    "/private/user/location",
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
                    "/private/user/location",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
            throw new RuntimeException("Failed to update user location: " + e.getMessage(), e);
        }
    }

    public String getAccountRegistrationEmail(String emailConfirmationToken, String username) {
        String confirmationLink = envConfiguration.getMailRegisterConfirmationLink() + emailConfirmationToken;

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Welcome to SupMap!</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;\">\n" +
                "    <div style=\"text-align: center; margin-bottom: 20px;\">\n" +
                "        <img src=\"https://i.ibb.co/NLf7Xgw/supmap-without-text.png\" alt=\"SupMap Logo\" style=\"max-width: 150px; height: auto;\">\n" +
                "    </div>\n" +
                "    <div style=\"background-color: #f9f9f9; padding: 20px; border-radius: 5px; border-left: 4px solid #4285f4;\">\n" +
                "        <h2 style=\"color: #4285f4; margin-top: 0;\">Welcome " + username + "!</h2>\n" +
                "        <p>Thank you for signing up for SupMap. We are thrilled to have you on board!</p>\n" +
                "        <p>To complete your registration and activate your account, please click the button below:</p>\n" +
                "        <div style=\"text-align: center; margin: 30px 0;\">\n" +
                "            <a href=\"" + confirmationLink + "\" style=\"background-color: #4285f4; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;\">Confirm my email</a>\n" +
                "        </div>\n" +
                "        <p>If the button does not work, you can also copy and paste the following link into your browser:</p>\n" +
                "        <p style=\"background-color: #f0f0f0; padding: 10px; border-radius: 5px; word-break: break-all;\"><a href=\"" + confirmationLink + "\" style=\"color: #4285f4; text-decoration: none;\">" + confirmationLink + "</a></p>\n" +
                "        <p><strong>Important:</strong> This link will expire in 48 hours for security reasons.</p>\n" +
                "        <p>With SupMap, you will be able to:</p>\n" +
                "        <ul style=\"background-color: #fff; padding: 15px; border-radius: 5px; margin: 15px 0; border: 1px solid #ddd;\">\n" +
                "            <li>Simplify your route management</li>\n" +
                "            <li>Optimize your mapping projects</li>\n" +
                "            <li>Access exclusive features</li>\n" +
                "            <li>Collaborate with your team</li>\n" +
                "        </ul>\n" +
                "        <p>If you did not create an account on SupMap, please ignore this email.</p>\n" +
                "    </div>\n" +
                "    <div style=\"margin-top: 30px; font-size: 14px; color: #666; border-top: 1px solid #ddd; padding-top: 20px;\">\n" +
                "        <p>Best regards,<br>\n" +
                "        The SupMap Team</p>\n" +
                "        <div style=\"margin-top: 15px;\">\n" +
                "            <p>SupMap - Simplify your routes and projects.</p>\n" +
                "            <p>📞 Support: <a href=\"tel:+33614129625\" style=\"color: #4285f4; text-decoration: none;\">+33 6 14 12 96 25</a><br>\n" +
                "            📩 Email: <a href=\"mailto:supmap.application@gmail.com\" style=\"color: #4285f4; text-decoration: none;\">supmap.application@gmail.com</a><br>\n" +
                "            🌐 Website: <a href=\"https://supmap-application.com\" style=\"color: #4285f4; text-decoration: none;\">https://supmap-application.com</a><br>\n" +
                "            📱 Available on iOS and Android!</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

}
