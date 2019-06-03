package com.greyu.ysj.authorization.model;

/**
 * @Description: Token's Model class, you can add fields to improve security, such as timestamp, url signature
 * @Author: gre_yu@163.com
 * @Date: Created in 0:57 2018/2/1
 */
public class TokenModel {

    private int userId;

    // Randomly generated uuid
    private String token;

    public TokenModel(int userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "TokenModel{" +
                "userId=" + userId +
                ", token='" + token + '\'' +
                '}';
    }
}
