package com.campus.bookborrow.exception;

import lombok.Getter;

/**
 * 自定义业务异常类
 * 在 Service 层抛出，由 GlobalExceptionHandler 统一拦截处理
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务错误码 */
    private final Integer code;

    /**
     * 使用默认错误码 500 构造业务异常
     *
     * @param message 异常提示信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 使用自定义错误码构造业务异常
     *
     * @param code    业务错误码
     * @param message 异常提示信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
