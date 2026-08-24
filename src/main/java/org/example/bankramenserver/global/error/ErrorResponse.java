package org.example.bankramenserver.global.error;

import lombok.Builder;
import lombok.Getter;
import org.example.bankramenserver.global.error.exception.ErrorCode;

@Getter
@Builder
public class ErrorResponse {

    private final int status;
    private final String code;
    private final String message;

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();
    }
}
