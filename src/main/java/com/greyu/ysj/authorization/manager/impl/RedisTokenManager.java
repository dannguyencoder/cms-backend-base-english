package com.greyu.ysj.authorization.manager.impl;

import com.greyu.ysj.authorization.manager.TokenManager;
import com.greyu.ysj.authorization.model.TokenModel;
import com.greyu.ysj.config.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: gre_yu@163.com
 * @Date: Created in 8:26 2018/2/1
 */
@Component
public class RedisTokenManager implements TokenManager {

    private RedisTemplate<Integer, String> redis;

    @Autowired
    @Qualifier("redisTemplate")
    public void setRedis(RedisTemplate redis) {
        this.redis = redis;
//        After the generic is set to Long, the corresponding serialization scheme must be changed.
        redis.setKeySerializer(new JdkSerializationRedisSerializer());
    }

    @Override
    public TokenModel createToken(int userId) {
//        Use UUID as the source token
        String token = UUID.randomUUID().toString().replace("-", "");
        TokenModel model = new TokenModel(userId, token);
//        Store to redis and set expiration time
        redis.boundValueOps(userId).set(token, Constants.TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);
        return model;
    }

    @Override
    public TokenModel getToken(String authentication) {
        if (authentication == null || authentication.length() == 0) {
            return null;
        }
        String[] param = authentication.split("_");
        if (param.length != 2) {
            return null;
        }
//        Use the userId and the source token to simply concatenate tokens to increase encryption.
        int userId = Integer.parseInt(param[0]);
        String token = param[1];
        return new TokenModel(userId, token);
    }

    @Override
    public boolean checkToken(TokenModel model) {
        if (model == null) {
            return false;
        }
        String token = redis.boundValueOps(model.getUserId()).get();
        if (token == null || !token.equals(model.getToken())) {
            return false;
        }
//        If the verification is successful, the user has performed a valid operation to extend the expiration time of the token.
        redis.boundValueOps(model.getUserId()).expire(Constants.TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);
        return true;
    }

    @Override
    public void deleteToken(int userId) {
        redis.delete(userId);
    }
}
