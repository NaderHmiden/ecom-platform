package com.platform.ecom.identity.api;

public record LoginResponse(String accessToken, long expiresInSeconds) {
}