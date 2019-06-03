package com.greyu.ysj.authorization.manager;

import com.greyu.ysj.authorization.model.TokenModel;

/**
 * @Description: Interface to operate on Token
 * @Author: gre_yu@163.com
 * @Date: Created in 0:59 2018/2/1
 */
public interface TokenManager {
    /**
     * Create a token associated with the specified user id
     * @param userId
     * @return 生成的token
     */
    TokenModel createToken(int userId);

    /**
     * Parse token from string
     * @param authentication Encrypted string
     * @return
     */
    TokenModel getToken(String authentication);

    /**
     * Check if the token is valid
     * @param model
     * @return is it effective
     */
    boolean checkToken(TokenModel model);

    /**
     * Clear token
     * @param userId UserId of the logged in user
     */
    void deleteToken(int userId);
}
