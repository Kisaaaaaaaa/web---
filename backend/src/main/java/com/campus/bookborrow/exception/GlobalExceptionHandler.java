package com.campus.bookborrow.exception;

import com.campus.bookborrow.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * 全局异常处理器
 * 拦截所有 Controller 层抛出的异常，统一封装为 Result 返回
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常 → code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // ==================== 参数校验异常 ====================

    /**
     * 处理 @Validated 参数校验失败异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验异常 → {}", msg);
        return Result.error(400, msg);
    }

    // ==================== 数据库异常 ====================

    /**
     * 处理数据库操作异常（SQLException / DataAccessException）
     */
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public Result<?> handleDatabaseException(Exception e) {
        log.error("数据库异常 → {}", e.getMessage(), e);
        return Result.error(500, "数据库操作异常，请联系管理员");
    }

    // ==================== 兜底异常 ====================

    /**
     * 处理所有未捕获的异常，作为兜底
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统未知异常 → {}", e.getMessage(), e);
        return Result.error(500, "系统内部错误，请联系管理员");
    }
}
