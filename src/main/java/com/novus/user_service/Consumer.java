package com.novus.user_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novus.shared_models.common.Kafka.KafkaMessage;
import com.novus.user_service.services.AccountManagementService;
import com.novus.user_service.services.FeedbackService;
import com.novus.user_service.services.UserProfileService;
import com.novus.user_service.services.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Consumer {

    @Autowired
    private final ObjectMapper objectMapper;

    private final AccountManagementService accountManagementService;
    private final FeedbackService feedbackService;
    private final UserProfileService userProfileService;
    private final UserQueryService userQueryService;

    @KafkaListener(topics = "user-service", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAuthenticationEvents(
            @Payload String messageJson,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("JSON message received from user-service topic [key: {}, partition: {}, offset: {}]", key, partition, offset);

            KafkaMessage kafkaMessage = objectMapper.readValue(messageJson, KafkaMessage.class);

            processMessage(key, kafkaMessage);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }

    private void processMessage(String operationKey, KafkaMessage kafkaMessage) {
        log.info("Processing operation: {}", operationKey);

        switch (operationKey) {
            case "getAuthenticatedUserDetails":
                userProfileService.processGetAuthenticatedUserDetails(kafkaMessage);
                break;
            case "deleteAuthenticatedUserAccount":
                accountManagementService.processDeleteAuthenticatedUserAccount(kafkaMessage);
                break;
            case "setUserProfileImage":
                userProfileService.processSetUserProfileImage(kafkaMessage);
                break;
            case "updateAuthenticatedUserDetails":
                userProfileService.processUpdateAuthenticatedUserDetails(kafkaMessage);
                break;
            case "createAdminAccount":
                accountManagementService.processCreateAdminAccount(kafkaMessage);
                break;
            case "deleteAdminAccount":
                accountManagementService.processDeleteAdminAccount(kafkaMessage);
                break;
            case "getAllUsers":
                userQueryService.processGetAllUsers(kafkaMessage);
                break;
            case "getUserAdminDashboardData":
                userQueryService.processGetUserAdminDashboardData(kafkaMessage);
                break;
            case "rateApplication":
                feedbackService.processRateApplication(kafkaMessage);
                break;
            case "updateUserLocation":
                userProfileService.processUpdateUserLocation(kafkaMessage);
                break;
            default:
                log.warn("Unknown operation: {}", operationKey);
                break;
        }
    }

}
