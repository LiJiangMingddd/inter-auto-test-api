package com.lm.interautotestapi.service;

import com.lm.interautotestapi.entity.ApiInterface;
import com.lm.interautotestapi.entity.ApiTestcase;
import com.lm.interautotestapi.model.BatchImportRequest;
import com.lm.interautotestapi.model.BatchImportResponse;
import com.lm.interautotestapi.model.BatchInterfaceItem;
import com.lm.interautotestapi.model.BatchTestcaseItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiService {

    private final ApiInterfaceService apiInterfaceService;
    private final ApiTestcaseService apiTestcaseService;

    /**
     * 批量导入接口和用例（事务性，单个失败不影响其他）
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchImportResponse batchImport(BatchImportRequest request) {
        log.info("===== [OpenApi] 开始批量导入：interfaces={} 条 =====", request.getInterfaces().size());

        BatchImportResponse response = new BatchImportResponse();
        List<BatchImportResponse.ImportError> errors = new ArrayList<>();
        int ifaceSuccess = 0;

        for (int i = 0; i < request.getInterfaces().size(); i++) {
            BatchInterfaceItem item = request.getInterfaces().get(i);
            try {
                ApiInterface apiIf = new ApiInterface();
                apiIf.setApiName(item.getApiName());
                apiIf.setApiInfo(item.getApiInfo());
                apiIf.setMethod(item.getMethod());
                apiIf.setUrlDev(item.getUrlDev());
                apiIf.setUrlUat(item.getUrlUat());
                apiIf.setUrlPro(item.getUrlPro());
                apiIf.setServiceCode(item.getServiceCode());
                apiIf.setEnabled(item.getEnabled() != null ? item.getEnabled() : 1);
                apiInterfaceService.save(apiIf);
                log.info("[OpenApi] 接口创建成功：id={}, name={}", apiIf.getId(), apiIf.getApiName());

                if (item.getTestcases() != null && !item.getTestcases().isEmpty()) {
                    List<ApiTestcase> tcList = new ArrayList<>();
                    for (BatchTestcaseItem tcItem : item.getTestcases()) {
                        ApiTestcase tc = new ApiTestcase();
                        tc.setInterfaceId(apiIf.getId());
                        tc.setCaseTitle(tcItem.getCaseTitle());
                        tc.setCaseData(tcItem.getCaseData());
                        tc.setCheckRules(tcItem.getCheckRules());
                        tc.setExpectedResults(tcItem.getExpectedResults());
                        tc.setEnv(tcItem.getEnv() != null ? tcItem.getEnv() : "dev");
                        tc.setEnabled(tcItem.getEnabled() != null ? tcItem.getEnabled() : 1);
                        tc.setSortOrder(tcItem.getSortOrder() != null ? tcItem.getSortOrder() : 0);
                        tcList.add(tc);
                    }
                    apiTestcaseService.saveBatch(tcList);
                    response.setTestcaseSuccess(response.getTestcaseSuccess() + tcList.size());
                    log.info("[OpenApi]    └─ 用例批量创建成功：{} 条", tcList.size());
                }

                ifaceSuccess++;

            } catch (Exception e) {
                log.error("[OpenApi] 导入失败 [index={}, name={}]：{}", i, item.getApiName(), e.getMessage(), e);
                BatchImportResponse.ImportError err = new BatchImportResponse.ImportError();
                err.setIndex(i);
                err.setApiName(item.getApiName());
                err.setReason(e.getMessage());
                errors.add(err);
            }
        }

        response.setInterfaceSuccess(ifaceSuccess);
        response.setInterfaceFailed(request.getInterfaces().size() - ifaceSuccess);
        response.setErrors(errors);
        response.setSuccess(errors.isEmpty());

        log.info("===== [OpenApi] 批量导入完成：接口成功={}, 失败={}, 用例成功={} =====",
                ifaceSuccess, response.getInterfaceFailed(), response.getTestcaseSuccess());
        return response;
    }
}
