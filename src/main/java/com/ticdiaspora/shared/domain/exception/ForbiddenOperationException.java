package com.ticdiaspora.shared.domain.exception;

public class ForbiddenOperationException extends BusinessException {

    public ForbiddenOperationException(String message) {
        super("FORBIDDEN_OPERATION", message);
    }
}
