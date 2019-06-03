package com.greyu.ysj.config;

/**
 * @Description: constant
 * @Author: gre_yu@163.com
 * @Date: Created in 0:51 2018/2/1
 */
public class Constants {
    /**
     * Store the field name of the currently logged in user id
     */
    public static final String CURRENT_USER_ID = "CURRENT_USER_ID";

    /**
     * Token validity period (hours)
     */
    public static final int TOKEN_EXPIRES_HOUR = 72;

    /**
     * Store the header field of Authorization
     */
    public static final String AUTHORIZATION = "authorization";

    /**
     * Image address prefix
     */
    public static final String IMAGE_PREFIX_URL = "http://119.29.161.228/cloudimg/goods/";

    public static final String AVATAR_PREFIX_URL = "http://119.29.161.228/cloudimg/avatars";

//    public static final String IMAGE_SAVE_PATH = "D:\\test_image";
    public static final String IMAGE_SAVE_PATH = "/var/www/html/cloudimg/goods/";
//    public static final String IMAGE_SAVE_PATH = "~/cloudimg/goods/";

    /**
     * Order pending status
     */
    public static final int ORDER_WAIT = 0;

    /**
     * Status in order delivery
     */
    public static final int ORDER_DISPATCHING = 1;

    /**
     * Order confirmation delivery status
     */
    public static final int ORDER_FINISH = 2;

    /**
     * Order refund status
     */
    public static final int ORDER_REFUNDING = 3;

    /**
     * Order refund completed
     */
    public static final int ORDER_REFUND_SUCCESS = -1;

    /**
     * Order refund failed
     */
    public static final int ORDER_REFUNDING_FAILURE = -2;
}
