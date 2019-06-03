package com.greyu.ysj.config;

/**
 * @Description:
 * @Author: gre_yu@163.com
 * @Date: Created in 0:42 2018/2/1
 */
public enum ResultStatus {
    SUCCESS(100, "success"),
    ADMIN_NOT_FOUND(-1002, "Administrator does not exist"),
    NOT_SUPER_ADMIN(-1010, "No super administrator rights"),
    USERNAME_OR_PASSWORD_ERROR(-1001, "wrong user name or password"),
    USER_NOT_FOUND(-1002, "User does not exist"),
    USER_NOT_LOGIN(-1003, "User not logged in"),
    DATA_NOT_NULL(-1004, "Field cannot be empty"),
    USERNAME_HAS_EXISTS(-1005, "Username already exists"),
    CAETGORY_NAME_HAS_EXISTS(-1005, "Category name already exists"),
    CATEGORY_NOT_FOUND(-1002, "This category does not exist"),
    CATEGORY_OWN_SECONDS(-1006, "There are subcategories under this category, and the category cannot be deleted."),
    CATEGORY_OWN_GOODS(-1006, "There are goods under this category, you cannot delete this category."),
    GOOD_NAME_HAS_EXISTS(-1005, "Product name already exists"),
    GOOD_NOT_FOUND(-1002, "Product does not exist"),
    GOOD_INSUFFICIENT(-1004, "Insufficient inventory"),
    ADDRESS_NOT_FOUND(-1002, "The shipping address does not exist"),
    ORDER_NOT_FOUND(-1002, "Order does not exist"),
    GOOD_NOT_LESS_THEN_ZERO(-1004, "The number of items added must be greater than zero"),
    CART_NOT_FOUND(-1002, "Shopping cart does not exist"),
    USER_NOT_ALLOWED(-1010, "No operating rights"),
    IMAGE_NOT_EMPTY(-1004, "Image cannot be empty"),
    ADV_NOT_FOUND(-1002, "Ad does not exist");

    /**
     * Return code
     */
    private int code;
    /**
     * Return result description
     */
    private String message;

    ResultStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
