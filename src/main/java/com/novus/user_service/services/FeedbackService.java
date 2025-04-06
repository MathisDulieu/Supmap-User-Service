package com.novus.user_service.services;

import com.novus.shared_models.common.Kafka.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    public void processRateApplication(KafkaMessage kafkaMessage) {
        log.info("Processing rateApplication for user: {}", kafkaMessage.getAuthenticatedUser().getUsername());

        Map<String, String> request = kafkaMessage.getRequest();
        String rate = request.get("rate");
    }

}
