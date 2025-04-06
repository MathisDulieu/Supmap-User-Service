package com.novus.user_service.dao;

import com.novus.database_utils.User.UserDao;
import com.novus.shared_models.common.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserDaoUtils {

    private final UserDao<User> userDao;

    public UserDaoUtils(MongoTemplate mongoTemplate) {
        this.userDao = new UserDao<>(mongoTemplate);
    }

}
