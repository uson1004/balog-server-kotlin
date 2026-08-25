package org.example.bankramenserver.global.error

import jakarta.validation.ConstraintViolationException
import org.example.bankramenserver.global.error.exception.ErrorCode
import org.example.bankramenserver.global.error.exception.GlobalException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)
    @ExceptionHandler(GlobalException::class) fun handleGlobalException(e: GlobalException): ResponseEntity<ErrorResponse> { log.error("GlobalException: {}", e.message); return ResponseEntity.status(e.errorCode.status).body(ErrorResponse.of(e.errorCode)) }
    @ExceptionHandler(MethodArgumentNotValidException::class, ConstraintViolationException::class, HttpMessageNotReadableException::class, MethodArgumentTypeMismatchException::class, MissingServletRequestParameterException::class)
    fun handleInvalidRequest(e: Exception): ResponseEntity<ErrorResponse> { log.error("InvalidRequestException: {}", e.message); return ResponseEntity.status(ErrorCode.INVALID_REQUEST.status).body(ErrorResponse.of(ErrorCode.INVALID_REQUEST)) }
    @ExceptionHandler(Exception::class) fun handleException(e: Exception): ResponseEntity<ErrorResponse> { log.error("UnhandledException: {}", e.message); return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.status).body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR)) }
}
