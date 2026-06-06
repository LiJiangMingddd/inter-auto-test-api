package com.lm.interautotestapi.model;

import lombok.Data;

@Data
public class OnlineTestResponse {

    private boolean success;

    private int statusCode;

    private String statusText;

    private long durationMs;

    private String responseBody;

    private String requestUrl;

    private String requestMethod;

    private String requestHeaders;

    private String requestBody;

    private String assertResult;

    private String error;

    public static OnlineTestResponse fail(String error) {
        OnlineTestResponse r = new OnlineTestResponse();
        r.setSuccess(false);
        r.setError(error);
        return r;
    }
}
