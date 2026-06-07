package com.lm.interautotestapi.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lm.interautotestapi.model.DifyRequest;
import com.lm.interautotestapi.model.DifyResponse;
import com.lm.interautotestapi.service.DifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DifyServiceImpl implements DifyService {

    @Value("${dify.base-url}")
    private String baseUrl;

    @Value("${dify.api-key}")
    private String apiKey;

    private static final int TIMEOUT_MS = 300000;

    @Override
    public DifyResponse executeWorkflow(DifyRequest request) {
        DifyResponse result = new DifyResponse();

        try {
            JSONObject body = new JSONObject();
            body.set("inputs", JSONUtil.createObj().set("query", request.getQuery()));
            body.set("response_mode", StrUtil.blankToDefault(request.getResponseMode(), "blocking"));
            body.set("user", StrUtil.blankToDefault(request.getUser(), "admin"));

            String url = baseUrl + "/workflows/run";
            log.info("[Dify] 调用工作流: url={}, query={}", url, request.getQuery());

            HttpResponse httpResponse = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .timeout(TIMEOUT_MS)
                    .execute();

            String responseBody = httpResponse.body();
            log.info("[Dify] 响应状态码: {}, body长度: {}", httpResponse.getStatus(),
                    responseBody != null ? responseBody.length() : 0);

            if (httpResponse.getStatus() == 200 && StrUtil.isNotBlank(responseBody)) {
                JSONObject respJson = JSONUtil.parseObj(responseBody);
                JSONObject data = respJson.getJSONObject("data");

                result.setSuccess(true);
                result.setWorkflowRunId(respJson.getStr("workflow_run_id"));
                result.setTaskId(respJson.getStr("task_id"));

                if (data != null) {
                    result.setStatus(data.getStr("status"));
                    result.setElapsedTime(data.getDouble("elapsed_time", 0.0));
                    result.setTotalTokens(data.getInt("total_tokens", 0));
                    result.setTotalSteps(data.getInt("total_steps", 0));

                    JSONObject outputs = data.getJSONObject("outputs");
                    if (outputs != null) {
                        result.setOutput(outputs.getStr("text", outputs.toString()));
                    }
                }
            } else {
                log.error("[Dify] 工作流调用失败: status={}, body={}", httpResponse.getStatus(), responseBody);
                result.setSuccess(false);
                result.setStatus("failed");
                result.setError("HTTP " + httpResponse.getStatus() + ": " +
                        (responseBody != null ? responseBody.substring(0, Math.min(200, responseBody.length())) : "无响应"));
            }

        } catch (Exception e) {
            log.error("[Dify] 工作流执行异常: {}", e.getMessage(), e);
            return DifyResponse.fail("执行异常: " + e.getMessage());
        }

        return result;
    }
}
