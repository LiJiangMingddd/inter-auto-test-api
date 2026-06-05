package com.lm.interautotestapi.controller;

import cn.hutool.core.io.FileUtil;
import com.lm.interautotestapi.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Controller
public class PageController {

    /**
     * 项目根目录（jar 包所在目录或 IDE 运行时的项目目录）
     */
    @Value("${user.dir}")
    private String userDir;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 读取项目根目录的 README.md 内容（在线预览用）
     * <p>
     * 读取顺序：
     * 1. 项目根目录下的 README.md（IDE 运行或 jar 包同目录）
     * 2. classpath 下的 README.md（Maven 构建时复制）
     */
    @GetMapping("/api/readme")
    @ResponseBody
    public Result<String> readme() {
        try {
            // 优先读取项目根目录下的 README.md（实时修改实时生效）
            File readmeFile = new File(userDir, "README.md");
            if (readmeFile.exists() && readmeFile.isFile()) {
                String content = FileUtil.readString(readmeFile, StandardCharsets.UTF_8);
                return Result.ok(content);
            }
            return Result.fail("README.md 文件不存在，请确认项目根目录下有 README.md");
        } catch (Exception e) {
            return Result.fail("读取 README.md 失败: " + e.getMessage());
        }
    }
}
