package com.novus.user_service.services;

import com.novus.shared_models.common.Kafka.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    public void processGetAuthenticatedUserDetails(KafkaMessage kafkaMessage) {
        log.info("Processing getAuthenticatedUserDetails for user: {}", kafkaMessage.getAuthenticatedUser().getUsername());
    }

    public void processUpdateAuthenticatedUserDetails(KafkaMessage kafkaMessage) {
        log.info("Processing updateAuthenticatedUserDetails for user: {}", kafkaMessage.getAuthenticatedUser().getUsername());}

    public void processSetUserProfileImage(KafkaMessage kafkaMessage) {
        log.info("Processing setUserProfileImage for user: {}", kafkaMessage.getAuthenticatedUser().getUsername());

        Map<String, String> request = kafkaMessage.getRequest();
        String fileAsBase64 = request.get("file");
    }

    public void processUpdateUserLocation(KafkaMessage kafkaMessage) {
        log.info("Processing updateUserLocation for user: {}", kafkaMessage.getAuthenticatedUser().getUsername());

        Map<String, String> request = kafkaMessage.getRequest();
        String location = request.get("location");
    }

}
