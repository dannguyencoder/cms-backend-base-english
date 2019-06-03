package com.greyu.ysj.model;

import com.greyu.ysj.config.ResultStatus;

import javax.naming.spi.DirStateFactory;

/**
 * @Description: Custom return results
 * @Author: gre_yu@163.com
 * @Date: Created in 0:38 2018/2/1
 */
public class ResultModel {
    /**
     * Return code
     */
    private int code;
    /**
     * Return result description
     */
    private String message;
    /**
     * Return data
     */
    private Object data;

    public ResultModel(int code, String message) {
        this.code = code;
        this.message = message;
        this.data = "";
    }

    public ResultModel(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResultModel(ResultStatus status) {
        this.code = status.getCode();
        this.message = status.getMessage();
        this.data = "";
    }

    public ResultModel(ResultStatus status, Object data) {
        this.code = status.getCode();
        this.message = status.getMessage();
        this.data = data;
    }

    public static ResultModel ok(Object data) {
        return new ResultModel(ResultStatus.SUCCESS, data);
    }

    public static ResultModel ok() {
        return new ResultModel(ResultStatus.SUCCESS);
    }

    public static ResultModel error(ResultStatus error) {
        return new ResultModel(error);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
