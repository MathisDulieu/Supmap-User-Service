package com.novus.user_service.services;

import com.novus.shared_models.common.AdminDashboard.AdminDashboard;
import com.novus.shared_models.common.Kafka.KafkaMessage;
import com.novus.shared_models.common.Log.HttpMethod;
import com.novus.shared_models.common.Log.LogLevel;
import com.novus.shared_models.common.User.User;
import com.novus.user_service.configuration.DateConfiguration;
import com.novus.user_service.dao.AdminDashboardDaoUtils;
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
public class FeedbackService {

    private final AdminDashboardDaoUtils adminDashboardDaoUtils;
    private final LogUtils logUtils;
    private final UserDaoUtils userDaoUtils;
    private final DateConfiguration dateConfiguration;

    public void processRateApplication(KafkaMessage kafkaMessage) {
        User authenticatedUser = kafkaMessage.getAuthenticatedUser();

        try {
            int rate = Integer.parseInt(kafkaMessage.getRequest().get("rate"));

            Optional<AdminDashboard> optionalDashboard = adminDashboardDaoUtils.find();
            if (optionalDashboard.isEmpty()) {
                throw new RuntimeException("Dashboard not found");
            }

            AdminDashboard dashboard = optionalDashboard.get();

            adminDashboardDaoUtils.save(
                    dashboard.getId(),
                    calculateNewRating(dashboard.getAppRatingByNumberOfRate(), rate),
                    dashboard.getTopContributors(),
                    dashboard.getUserGrowthStats(),
                    dashboard.getUserActivityMetrics(),
                    dashboard.getRouteRecalculations(),
                    dashboard.getIncidentConfirmationRate(),
                    dashboard.getIncidentsByType(),
                    dashboard.getTotalRoutesProposed()
            );

            authenticatedUser.setLastActivityDate(dateConfiguration.newDate());
            userDaoUtils.save(authenticatedUser);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "APP_RATING_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    String.format("User %s rated the application with a %d-star rating",
                            authenticatedUser.getUsername(), rate),
                    HttpMethod.POST,
                    "/private/user/app/rate",
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
                    "APP_RATING_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error processing application rating: " + e.getMessage(),
                    HttpMethod.POST,
                    "/private/user/app/rate",
                    "user-service",
                    stackTrace,
                    authenticatedUser.getId()
            );
            throw new RuntimeException("Failed to process application rating: " + e.getMessage(), e);
        }
    }

    private Map<Integer, Double> calculateNewRating(Map<Integer, Double> appRatingByNumberOfRate, int rate) {
        Map.Entry<Integer, Double> entry = appRatingByNumberOfRate.entrySet().iterator().next();
        int numberOfRates = entry.getKey() + 1;
        double totalRating = entry.getValue() * entry.getKey() + rate;
        double newRating = Math.round((totalRating / numberOfRates) * 100.0) / 100.0;

        appRatingByNumberOfRate.clear();
        appRatingByNumberOfRate.put(numberOfRates, newRating);

        return appRatingByNumberOfRate;
    }
}