package com.ttp.evaluation.integration.common;

public class ExternalApiException extends RuntimeException {
    private final String apiName;
    private final Integer statusCode;

    public ExternalApiException(String apiName, Integer statusCode, String message) {
        super(String.format("[%s API] %s%s",
                apiName,
                statusCode != null ? "Status " + statusCode + ": " : "",
                message));
        this.apiName = apiName;
        this.statusCode = statusCode;
    }

    public String getApiName() {
        return apiName;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}