package com.lm.interautotestapi.model;

import lombok.Data;

@Data
public class DifyResponse {

    private boolean success;
    private String workflowRunId;
    private String taskId;
    private String status;
    private String output;
    private String error;
    private double elapsedTime;
    private int totalTokens;
    private int totalSteps;

    public static DifyResponse fail(String error) {
        DifyResponse r = new DifyResponse();
        r.setSuccess(false);
        r.setError(error);
        r.setStatus("failed");
        return r;
    }
}
