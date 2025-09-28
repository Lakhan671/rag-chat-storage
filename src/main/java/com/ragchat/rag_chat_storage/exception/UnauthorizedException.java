package com.ragchat.rag_chat_storage.exception;


public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
