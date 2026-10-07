package com.mentorpair.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> business(BusinessException e) {
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> maxSize(MaxUploadSizeExceededException e) {
        return Result.fail("文件大小超出限制（单个文件最大 20MB）");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> typeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.fail("请求参数不正确");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> other(Exception e) {
        log.error("系统异常", e);
        return Result.fail("系统繁忙，请稍后重试");
    }
}
