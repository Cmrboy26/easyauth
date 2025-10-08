package net.cmr.easyauth.service;

public class AdditionalStepsRequiredException extends RuntimeException {
    public AdditionalStepsRequiredException(String message) {
        super(message);
    }
}
