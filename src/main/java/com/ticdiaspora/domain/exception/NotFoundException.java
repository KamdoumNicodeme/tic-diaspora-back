package com.ticdiaspora.domain.exception;

public class NotFoundException extends BusinessException {

    public NotFoundException(String entityName, Object id) {
        super("NOT_FOUND", entityName + " introuvable: " + id);
    }
}
