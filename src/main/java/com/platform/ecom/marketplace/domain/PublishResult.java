package com.platform.ecom.marketplace.domain;

public record PublishResult(boolean success, String externalPublishId, String errorMessage) {
    public static PublishResult success(String externalPublishId) {
        return new PublishResult(true, externalPublishId, null);
    }

    public static PublishResult failure(String errorMessage) {
        return new PublishResult(false, null, errorMessage);
    }
}
