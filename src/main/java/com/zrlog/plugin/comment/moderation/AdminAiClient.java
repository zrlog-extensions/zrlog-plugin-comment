package com.zrlog.plugin.comment.moderation;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.client.HttpClientUtils;
import com.zrlog.plugin.common.model.PublicInfo;
import com.zrlog.plugin.common.type.HttpMethod;
import com.zrlog.plugin.data.codec.BaseHttpRequestInfo;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.HttpResponseInfo;
import com.zrlog.plugin.type.ActionType;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;

/** Only calls the host's authenticated internal API. No model credentials or provider protocols here. */
public class AdminAiClient {
    public ModerationRecord.AiSuggestion analyze(IOSession session, HttpRequestInfo incoming, String content) {
        ModerationAccess.requireAdmin(incoming);
        PublicInfo info = session.getResponseSync(ContentType.JSON, Collections.emptyMap(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        if (info == null || info.getApiHomeUrl() == null) {
            throw new IllegalStateException("无法连接后台 AI 服务");
        }
        BaseHttpRequestInfo request = new BaseHttpRequestInfo();
        request.setAccessUrl(info.getApiHomeUrl().replaceAll("/+$", "") + "/api/admin/internal/ai/comment/analyze");
        request.setHttpMethod(HttpMethod.POST);
        request.setHeader(Collections.singletonMap("Content-Type", "application/json"));
        request.setRequestBody(new Gson().toJson(new AnalyzeRequest(content)).getBytes(StandardCharsets.UTF_8));
        HttpResponseInfo response = HttpClientUtils.sendHttpRequest(request, session, Duration.ofSeconds(35));
        if (response == null || response.getStatusCode() != 200) {
            throw new IllegalStateException("后台 AI 服务不可用，请确认后台与 plugin-core 均已更新；可继续人工审核");
        }
        AnalyzeResponse result = new Gson().fromJson(new String(response.getResponseBody(), StandardCharsets.UTF_8), AnalyzeResponse.class);
        if (result == null || result.error != 0 || result.data == null || result.data.verdict == null) {
            throw new IllegalStateException("未获得 AI 建议，请检查站点 AI 配置或稍后重试；可继续人工审核");
        }
        return result.data;
    }

    public static class AnalyzeRequest {
        public String content;
        public AnalyzeRequest() { }
        public AnalyzeRequest(String content) { this.content = content; }
    }

    public static class AnalyzeResponse {
        public int error;
        public ModerationRecord.AiSuggestion data;
    }
}
