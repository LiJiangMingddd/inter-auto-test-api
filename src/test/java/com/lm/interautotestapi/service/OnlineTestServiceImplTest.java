package com.lm.interautotestapi.service;

import com.lm.interautotestapi.service.impl.OnlineTestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class OnlineTestServiceImplTest {

    private OnlineTestServiceImpl service;
    private Method evaluateAssertMethod;
    private Method evaluateJsonPathAssertMethod;

    @BeforeEach
    void setUp() throws Exception {
        service = new OnlineTestServiceImpl(null, null);
        evaluateAssertMethod = OnlineTestServiceImpl.class.getDeclaredMethod(
                "evaluateAssert", String.class, String.class, int.class);
        evaluateAssertMethod.setAccessible(true);
        evaluateJsonPathAssertMethod = OnlineTestServiceImpl.class.getDeclaredMethod(
                "evaluateJsonPathAssert", String.class, String.class);
        evaluateJsonPathAssertMethod.setAccessible(true);
    }

    @Test
    void evaluateAssert_statusCodePass() throws Exception {
        String result = (String) evaluateAssertMethod.invoke(service,
                "status==200", "{}", 200);
        assertTrue(result.contains("通过"));
    }

    @Test
    void evaluateAssert_statusCodeFail() throws Exception {
        String result = (String) evaluateAssertMethod.invoke(service,
                "status==200", "{}", 500);
        assertTrue(result.contains("失败"));
    }

    @Test
    void evaluateAssert_jsonPathExists() throws Exception {
        String json = "{\"code\":200,\"msg\":\"success\"}";
        String result = (String) evaluateAssertMethod.invoke(service,
                "$.code", json, 200);
        assertTrue(result.contains("通过"));
    }

    @Test
    void evaluateAssert_jsonPathEquals() throws Exception {
        String json = "{\"code\":200,\"msg\":\"success\"}";
        String result = (String) evaluateAssertMethod.invoke(service,
                "$.code==200", json, 200);
        assertTrue(result.contains("通过"));
    }

    @Test
    void evaluateAssert_jsonPathEqualsFail() throws Exception {
        String json = "{\"code\":500,\"msg\":\"error\"}";
        String result = (String) evaluateAssertMethod.invoke(service,
                "$.code==200", json, 500);
        assertTrue(result.contains("失败"));
    }

    @Test
    void evaluateAssert_jsonPathNested() throws Exception {
        String json = "{\"data\":{\"user\":{\"name\":\"test\"}}}";
        String result = (String) evaluateAssertMethod.invoke(service,
                "$.data.user.name==test", json, 200);
        assertTrue(result.contains("通过"));
    }

    @Test
    void evaluateAssert_jsonPathArray() throws Exception {
        String json = "{\"list\":[1,2,3]}";
        String result = (String) evaluateAssertMethod.invoke(service,
                "$.list.length()==3", json, 200);
        assertTrue(result.contains("通过"));
    }

    @Test
    void evaluateAssert_jsonPathNotFound() throws Exception {
        String json = "{\"code\":200}";
        String result = (String) evaluateAssertMethod.invoke(service,
                "$.nonexistent", json, 200);
        assertTrue(result.contains("异常"));
    }

    @Test
    void evaluateAssert_emptyRules() throws Exception {
        String result = (String) evaluateAssertMethod.invoke(service,
                "", "{}", 200);
        assertEquals("无断言规则", result);
    }

    @Test
    void evaluateJsonPathAssert_equalsPass() throws Exception {
        String json = "{\"name\":\"test\"}";
        String result = (String) evaluateJsonPathAssertMethod.invoke(service,
                "$.name==test", json);
        assertTrue(result.contains("通过"));
    }

    @Test
    void evaluateJsonPathAssert_existsPass() throws Exception {
        String json = "{\"name\":\"test\"}";
        String result = (String) evaluateJsonPathAssertMethod.invoke(service,
                "$.name", json);
        assertTrue(result.contains("存在"));
    }

    @Test
    void evaluateJsonPathAssert_invalidPath() throws Exception {
        String json = "{\"name\":\"test\"}";
        String result = (String) evaluateJsonPathAssertMethod.invoke(service,
                "$.invalidPath", json);
        assertTrue(result.contains("异常"));
    }
}
