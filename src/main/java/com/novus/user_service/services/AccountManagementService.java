package com.novus.user_service.services;

import com.novus.shared_models.common.Kafka.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountManagementService {

    public void processDeleteAuthenticatedUserAccount(KafkaMessage kafkaMessage) {
        log.info("Processing deleteAuthenticatedUserAccount for user: {}", kafkaMessage.getAuthenticatedUser().getUsername());
    }

    public void processCreateAdminAccount(KafkaMessage kafkaMessage) {
        log.info("Processing createAdminAccount");

        Map<String, String> request = kafkaMessage.getRequest();
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        boolean isValidEmail = Boolean.parseBoolean(request.get("isValidEmail"));
        String profileImage = request.get("profileImage");
    }

    public void processDeleteAdminAccount(KafkaMessage kafkaMessage) {
        log.info("Processing deleteAdminAccount");

        Map<String, String> request = kafkaMessage.getRequest();
        String userId = request.get("userId");
    }

}
