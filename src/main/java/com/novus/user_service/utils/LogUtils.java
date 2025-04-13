package com.novus.user_service.utils;

import com.novus.shared_models.common.Log.HttpMethod;
import com.novus.shared_models.common.Log.Log;
import com.novus.shared_models.common.Log.LogLevel;
import com.novus.user_service.UuidProvider;
import com.novus.user_service.configuration.DateConfiguration;
import com.novus.user_service.dao.LogDaoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogUtils {

    private final UuidProvider uuidProvider;
    private final LogDaoUtils logDaoUtils;
    private final DateConfiguration dateConfiguration;

    public void buildAndSaveLog(LogLevel logLevel, String action, String ipAddress, String message, HttpMethod httpMethod,
                                String requestPath, String service, String stackTrace, String userId) {

        Log log = Log.builder()
                .id(uuidProvider.generateUuid())
                .level(logLevel)
                .action(action)
                .ipAddress(ipAddress)
                .message(message)
                .requestMethod(httpMethod)
                .requestPath(requestPath)
                .service(service)
                .stackTrace(stackTrace)
                .timestamp(dateConfiguration.newDate())
                .userId(userId)
                .build();

        logDaoUtils.save(log);
    }

}
