package com.lm.interautotestapi.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void ok_withoutData_shouldReturnSuccessWithNullData() {
        Result<Void> result = Result.ok();
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void ok_withData_shouldReturnSuccessWithData() {
        String data = "testData";
        Result<String> result = Result.ok(data);
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMsg());
        assertEquals("testData", result.getData());
    }

    @Test
    void ok_withList_shouldReturnSuccessWithList() {
        List<String> list = Arrays.asList("a", "b", "c");
        Result<List<String>> result = Result.ok(list);
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMsg());
        assertEquals(3, result.getData().size());
    }

    @Test
    void fail_withMessage_shouldReturnError() {
        Result<Void> result = Result.fail("参数错误");
        assertEquals(500, result.getCode());
        assertEquals("参数错误", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void fail_withCodeAndMessage_shouldReturnError() {
        Result<Void> result = Result.fail(400, "参数校验失败");
        assertEquals(400, result.getCode());
        assertEquals("参数校验失败", result.getMsg());
        assertNull(result.getData());
    }
}
