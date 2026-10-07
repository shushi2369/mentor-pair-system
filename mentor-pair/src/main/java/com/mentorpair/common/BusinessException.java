package com.mentorpair.common;

/** 业务校验失败异常，消息直接面向用户展示 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
