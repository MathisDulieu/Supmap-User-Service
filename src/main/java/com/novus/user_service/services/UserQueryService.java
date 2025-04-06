package com.novus.user_service.services;

import com.novus.shared_models.common.Kafka.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {

    public void processGetAllUsers(KafkaMessage kafkaMessage) {
        log.info("Processing getAllUsers for user: {}", kafkaMessage.getAuthenticatedUser().getUsername());
    }

    public void processGetUserAdminDashboardData(KafkaMessage kafkaMessage) {
        log.info("Processing getUserAdminDashboardData for user: {}", kafkaMessage.getAuthenticatedUser().getUsername());
    }

}
