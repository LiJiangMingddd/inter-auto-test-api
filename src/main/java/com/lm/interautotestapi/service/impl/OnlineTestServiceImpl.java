package com.lm.interautotestapi.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.jayway.jsonpath.JsonPath;
import com.lm.interautotestapi.entity.ApiInterface;
import com.lm.interautotestapi.entity.ApiTestcase;
import com.lm.interautotestapi.model.OnlineTestRequest;
import com.lm.interautotestapi.model.OnlineTestResponse;
import com.lm.interautotestapi.service.ApiInterfaceService;
import com.lm.interautotestapi.service.ApiTestcaseService;
import com.lm.interautotestapi.service.OnlineTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineTestServiceImpl implements OnlineTestService {

    private final ApiTestcaseService apiTestcaseService;
    private final ApiInterfaceService apiInterfaceService;

    private static final int TIMEOUT_MS = 30000;

    @Override
    public OnlineTestResponse executeTest(OnlineTestRequest request) {
        ApiTestcase testcase = apiTestcaseService.getById(request.getTestcaseId());
        if (testcase == null) {
            return OnlineTestResponse.fail("用例不存在，ID=" + request.getTestcaseId());
        }

        ApiInterface apiInterface = apiInterfaceService.getById(testcase.getInterfaceId());
        if (apiInterface == null) {
            return OnlineTestResponse.fail("关联接口不存在，ID=" + testcase.getInterfaceId());
        }

        String url = resolveUrl(apiInterface, testcase.getEnv(), request.getOverrideUrl());
        if (StrUtil.isBlank(url)) {
            return OnlineTestResponse.fail("未找到可用地址，env=" + testcase.getEnv());
        }

        String method = StrUtil.blankToDefault(apiInterface.getMethod(), "GET").toUpperCase();
        String caseData = StrUtil.blankToDefault(testcase.getCaseData(), "");

        OnlineTestResponse response = new OnlineTestResponse();
        response.setRequestMethod(method);
        response.setRequestUrl(url);
        response.setRequestBody(caseData);

        try {
            Method httpMethod = Method.valueOf(method);
            HttpRequest httpRequest = new HttpRequest(url).method(httpMethod).timeout(TIMEOUT_MS);

            if (StrUtil.isNotBlank(caseData) && !httpMethod.name().equals("GET")) {
                httpRequest.body(caseData, "application/json;charset=UTF-8");
            }

            Map<String, String> headerMap = new LinkedHashMap<>();
            headerMap.put("Content-Type", "application/json;charset=UTF-8");
            headerMap.put("Accept", "application/json");
            httpRequest.headerMap(headerMap, true);
            response.setRequestHeaders(formatHeaders(headerMap));

            long start = System.currentTimeMillis();
            HttpResponse httpResponse = httpRequest.execute();
            long duration = System.currentTimeMillis() - start;

            response.setStatusCode(httpResponse.getStatus());
            response.setStatusText(httpResponse.getStatus() == 200 ? "OK" : "HTTP " + httpResponse.getStatus());
            response.setDurationMs(duration);
            response.setResponseBody(httpResponse.body());
            response.setSuccess(httpResponse.getStatus() >= 200 && httpResponse.getStatus() < 300);

            if (StrUtil.isNotBlank(testcase.getCheckRules())) {
                String assertResult = evaluateAssert(testcase.getCheckRules(), httpResponse.body(), httpResponse.getStatus());
                response.setAssertResult(assertResult);
            }

            log.info("[OnlineTest] 用例 {} 测试完成: url={}, status={}, duration={}ms",
                    testcase.getCaseTitle(), url, httpResponse.getStatus(), duration);

        } catch (Exception e) {
            log.error("[OnlineTest] 用例 {} 测试异常: url={}, error={}", testcase.getCaseTitle(), url, e.getMessage(), e);
            response.setSuccess(false);
            response.setError("请求异常: " + e.getMessage());
        }

        return response;
    }

    private String resolveUrl(ApiInterface apiInterface, String env, String overrideUrl) {
        if (StrUtil.isNotBlank(overrideUrl)) {
            return overrideUrl;
        }
        if ("dev".equalsIgnoreCase(env)) {
            return StrUtil.blankToDefault(apiInterface.getUrlDev(), "");
        } else if ("uat".equalsIgnoreCase(env)) {
            return StrUtil.blankToDefault(apiInterface.getUrlUat(), "");
        } else if ("pro".equalsIgnoreCase(env)) {
            return StrUtil.blankToDefault(apiInterface.getUrlPro(), "");
        }
        String url = StrUtil.blankToDefault(apiInterface.getUrlDev(),
                StrUtil.blankToDefault(apiInterface.getUrlUat(),
                        StrUtil.blankToDefault(apiInterface.getUrlPro(), "")));
        return url;
    }

    private String evaluateAssert(String checkRules, String responseBody, int statusCode) {
        try {
            StringBuilder result = new StringBuilder();
            String[] rules = checkRules.split(";");

            for (String rule : rules) {
                String trimmed = rule.trim();
                if (trimmed.isEmpty()) continue;

                if (trimmed.startsWith("status==") || trimmed.startsWith("status ==")) {
                    String expectedStatus = trimmed.replaceAll("[^0-9]", "");
                    boolean pass = expectedStatus.equals(String.valueOf(statusCode));
                    result.append(trimmed).append(" → ").append(pass ? "✓ 通过" : "✗ 失败(actual=" + statusCode + ")").append("\n");
                } else if (trimmed.startsWith("$.")) {
                    String assertResult = evaluateJsonPathAssert(trimmed, responseBody);
                    result.append(assertResult).append("\n");
                } else {
                    if (responseBody.contains(trimmed.replace("contains(\"", "").replace("\")", "").replace("contains(", "").replace(")", ""))) {
                        result.append(trimmed).append(" → ✓ 通过\n");
                    } else {
                        result.append(trimmed).append(" → ✗ 未匹配\n");
                    }
                }
            }

            return result.length() > 0 ? result.toString().trim() : "无断言规则";
        } catch (Exception e) {
            return "断言解析异常: " + e.getMessage();
        }
    }

    private String evaluateJsonPathAssert(String rule, String responseBody) {
        try {
            String[] parts = rule.split("==", 2);
            if (parts.length == 2) {
                String jsonPath = parts[0].trim();
                String expected = parts[1].trim().replaceAll("^\"|\"$", "");

                Object actual = JsonPath.read(responseBody, jsonPath);
                String actualStr = actual != null ? actual.toString() : "null";

                if (expected.equals(actualStr)) {
                    return rule + " → ✓ 通过";
                } else {
                    return rule + " → ✗ 失败(actual=" + actualStr + ")";
                }
            }
            Object actual = JsonPath.read(responseBody, rule);
            return rule + " → ✓ 存在(actual=" + actual + ")";
        } catch (Exception e) {
            return rule + " → ✗ 异常: " + e.getMessage();
        }
    }

    private String formatHeaders(Map<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
        return sb.toString().trim();
    }
}
