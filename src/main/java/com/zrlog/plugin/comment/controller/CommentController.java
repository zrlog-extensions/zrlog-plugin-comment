package com.zrlog.plugin.comment.controller;

import com.google.gson.Gson;
import com.zrlog.plugin.comment.moderation.*;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.comment.config.ChangyanConfig;
import com.zrlog.plugin.comment.config.CommentApiResponse;
import com.zrlog.plugin.comment.config.CommentBaseConfig;
import com.zrlog.plugin.comment.config.CommentHistoryConfig;
import com.zrlog.plugin.comment.config.CommentHistoryRecord;
import com.zrlog.plugin.comment.config.CommentSubmitRequest;
import com.zrlog.plugin.comment.config.CommentUpdateRequest;
import com.zrlog.plugin.comment.config.CommentWebsiteConfig;
import com.zrlog.plugin.comment.config.WebsiteKeyRequest;
import com.zrlog.plugin.comment.dao.CommentDAO;
import com.zrlog.plugin.comment.render.CommentHtmlRenderer;
import com.zrlog.plugin.comment.service.CommentService;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.model.Comment;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.render.SimpleTemplateRender;
import com.zrlog.plugin.type.ActionType;

import java.util.*;
import java.util.logging.Logger;
import java.util.concurrent.Semaphore;

public class CommentController {

    private static final Logger LOGGER = LoggerUtil.getLogger(CommentController.class);
    private static final CommentHtmlRenderer COMMENT_HTML_RENDERER = new CommentHtmlRenderer();

    private static final Semaphore AI_REQUEST = new Semaphore(1);
    private final IOSession session;
    private final MsgPacket requestPacket;
    private final HttpRequestInfo requestInfo;
    private final Gson gson = new Gson();

    public CommentController(IOSession session, MsgPacket requestPacket, HttpRequestInfo requestInfo) {
        this.session = session;
        this.requestPacket = requestPacket;
        this.requestInfo = requestInfo;
    }

    public void update() {
        try {
            ModerationAccess.requireMutation(requestInfo, paramValue("adminToken"));
            CommentUpdateRequest update = updateRequest();
            session.getResponseSync(ContentType.JSON, update, ActionType.SET_WEBSITE, Object.class);
            CommentWebsiteConfig saved = websiteConfig("type,commentEmailNotify,changyan,base,moderation");
            if (!Objects.equals(update.getType(), saved.getType())
                    || !Objects.equals(update.getCommentEmailNotify(), saved.getCommentEmailNotify())
                    || !Objects.equals(update.getChangyan(), saved.getChangyan())
                    || !Objects.equals(update.getBase(), saved.getBase())
                    || !Objects.equals(update.getModeration(), saved.getModeration())) {
                throw new IllegalStateException("评论配置保存未确认");
            }
            CommentService.recordSyncHistory(session, true, 0, "更新插件配置参数成功");
            response(CommentApiResponse.success());
        } catch (IllegalArgumentException e) {
            response(CommentApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            response(CommentApiResponse.error("保存配置失败，请刷新后重试"));
        }
    }

    public void json() {
        session.sendJsonMsg(data(), requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }

    public void history() {
        ModerationAccess.requireAdmin(requestInfo);
        CommentHistoryConfig historyConfig = session.getResponseSync(ContentType.JSON, WebsiteKeyRequest.of("syncHistory"),
                ActionType.GET_WEBSITE, CommentHistoryConfig.class);
        session.sendJsonMsg(historyList(historyConfig), requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }

    private String doComment() {
        CommentSubmitRequest submitRequest = submitRequest();
        if (Objects.isNull(submitRequest.getLogId()) || submitRequest.getLogId().trim().isEmpty()) {
            throw new IllegalArgumentException("文章id 不能为空");
        }
        String content = submitRequest.getUserComment();
        if (Objects.isNull(content) || content.trim().isEmpty()) {
            throw new IllegalArgumentException("内容不能为空");
        }
        String userName = submitRequest.getUserName();
        if (Objects.isNull(userName) || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }

        String userHome = CommentHtmlRenderer.normalizeHttpUrl(submitRequest.getWeb());
        if (userHome == null) {
            throw new IllegalArgumentException("网站地址仅支持 http:// 或 https://");
        }
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setName(userName);
        comment.setLogId(parseArticleId(submitRequest.getLogId()));
        comment.setHome(userHome);
        comment.setMail(submitRequest.getEmail());
        comment.setHeadPortrait("");
        comment.setIp(headerValue("X-Real-IP"));
        comment.setCreatedTime(new Date());
        CommentGuard.validate(comment, paramValue("contactWebsite"));
        CommentGuard.INSTANCE.reserve(comment, System.currentTimeMillis());
        try {
            if (ModerationConfig.parse(websiteConfig("moderation").getModeration()).enabled) {
                moderationRepository().add(comment);
                return "评论已提交，审核通过后会显示";
            }
            CommentDAO.save(session, comment);
            return "评论成功";
        } catch (IllegalArgumentException e) {
            CommentGuard.INSTANCE.releaseDuplicate(comment);
            throw e;
        }

    }

    public void addComment() {
        String resultMessage = "";
        try {
            resultMessage = doComment();
        } catch (IllegalArgumentException e) {
            resultMessage = e.getMessage();
        } catch (Exception e) {
            LOGGER.warning("Comment submission failed: " + e.getClass().getSimpleName());
            resultMessage = "评论提交未确认，请刷新页面查看后再试";
        } finally {
            session.responseHtmlStr(COMMENT_HTML_RENDERER.renderResult(resultMessage, session.getPlugin()),
                    requestPacket.getMethodStr(), requestPacket.getMsgId());
        }
    }

    private Map<String, Object> data() {
        ModerationAccess.requireAdmin(requestInfo);
        CommentWebsiteConfig config = websiteConfig("changyan,base,commentEmailNotify,type,syncHistory,moderation");
        config.setUserName(requestInfo.getUserName());
        config.setUserId(requestInfo.getUserId());
        config.setFullUrl(requestInfo.getFullUrl().replace("install", ""));
        config.normalize(requestInfo.getAccessUrl() + "/p/" + session.getPlugin().getShortName() + "/changyan/sync/"
                + UUID.randomUUID().toString().replace("-", ""));
        Map<String, Object> data = new HashMap<>();
        data.put("theme", requestInfo.isDarkMode() ? "dark" : "light");
        data.put("dark", requestInfo.isDarkMode());
        data.put("setting", config);
        data.put("adminToken", ModerationAccess.token(requestInfo));
        data.put("primaryColor", requestInfo.getAdminColorPrimary());
        data.put("colorPrimary", requestInfo.getAdminColorPrimary());
        data.put("plugin", session.getPlugin());
        return data;
    }

    public void index() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("data", gson.toJson(data()));
        session.responseHtmlStr(new SimpleTemplateRender().render("/templates/index", session.getPlugin(), keyMap), requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    public void widget() {
        CommentWebsiteConfig config = websiteConfig("base,changyan,type");
        long articleId = parseArticleId(paramValue("articleId"));
        Map<String, Object> data = new HashMap<>();
        data.put("articleId", String.valueOf(articleId));
        String type = config.normalizedType();
        if (Objects.isNull(type) || type.trim().isEmpty()) {
            type = "base";
            data.put("type", "base");
        }
        if (Objects.equals(type, "base")) {
            fillBaseCommentInfo(config, data);
            data.put("comments", new CommentService().renderBaseListCommentHtml(session, articleId));
        } else if (Objects.equals(type, "changyan")) {
            ChangyanConfig changyanConfig = changyanConfig(config.getChangyan());
            String appId = changyanConfig.getAppId();
            data.put("appIdJson", CommentHtmlRenderer.toJavaScriptString(appId));
        }
        session.responseHtmlStr(new SimpleTemplateRender().render("/widget/" + type + "/index", session.getPlugin(), data), requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    private long parseArticleId(String articleId) {
        if (articleId == null) {
            return -1L;
        }
        try {
            return Long.parseLong(articleId);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private void fillBaseCommentInfo(CommentWebsiteConfig config, Map<String, Object> data) {
        CommentBaseConfig baseConfig = baseConfig(config.getBase());
        data.put("styleStr", blankToDefault(baseConfig.getStyleStr(), ""));
        data.put("mainColor", blankToDefault(baseConfig.getMainColor(), "#1677ff"));
        String baseUrl = baseConfig.getBaseUrl();
        if (Objects.isNull(baseUrl) || Objects.equals(baseUrl, "")) {
            data.put("commentUrl", "/p/comment/addComment");
        } else {
            data.put("commentUrl", baseUrl + "/p/comment/addComment");
        }
    }

    private List<CommentHistoryRecord> historyList(CommentHistoryConfig historyConfig) {
        String historyJson = historyConfig == null ? null : historyConfig.getSyncHistory();
        if (historyJson == null || historyJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        CommentHistoryRecord[] records = gson.fromJson(historyJson, CommentHistoryRecord[].class);
        if (records == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(records));
    }

    private CommentWebsiteConfig websiteConfig(String keys) {
        CommentWebsiteConfig config = session.getResponseSync(ContentType.JSON, WebsiteKeyRequest.of(keys), ActionType.GET_WEBSITE,
                CommentWebsiteConfig.class);
        if (config == null) throw new IllegalStateException("评论配置读取未确认");
        return config;
    }

    private CommentUpdateRequest updateRequest() {
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setType(paramValue("type"));
        request.setCommentEmailNotify(paramValue("commentEmailNotify"));
        request.setChangyan(paramValue("changyan"));
        request.setBase(paramValue("base"));
        request.setModeration(gson.toJson(ModerationConfig.parse(paramValue("moderation"))));
        return request;
    }

    private CommentSubmitRequest submitRequest() {
        CommentSubmitRequest request = new CommentSubmitRequest();
        request.setLogId(paramValue("logId"));
        request.setUserComment(paramValue("userComment"));
        request.setUserName(paramValue("userName"));
        request.setWeb(paramValue("web"));
        request.setEmail(paramValue("email"));
        return request;
    }

    private String paramValue(String key) {
        if (requestInfo.getParam() == null || requestInfo.getParam().get(key) == null || requestInfo.getParam().get(key).length == 0) {
            return null;
        }
        return requestInfo.getParam().get(key)[0];
    }

    private CommentBaseConfig baseConfig(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new CommentBaseConfig();
        }
        CommentBaseConfig config = gson.fromJson(value, CommentBaseConfig.class);
        return config == null ? new CommentBaseConfig() : config;
    }

    private ChangyanConfig changyanConfig(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ChangyanConfig();
        }
        ChangyanConfig config = gson.fromJson(value, ChangyanConfig.class);
        return config == null ? new ChangyanConfig() : config;
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private ModerationRepository moderationRepository() {
        return new ModerationRepository(new ModerationStore(session));
    }

    public void moderationList() {
        try {
            ModerationAccess.requireAdmin(requestInfo);
            response(CommentApiResponse.data(moderationRepository().list()));
        } catch (Exception e) {
            response(CommentApiResponse.error("无法读取待审评论，请登录后台后重试"));
        }
    }

    public void moderationApprove() {
        moderationAction(() -> {
            moderationRepository().approve(paramValue("id"), comment -> CommentDAO.save(session, comment));
            CommentService.recordSyncHistory(session, true, 1, "人工审核通过一条评论");
        });
    }

    public void moderationRemove() {
        moderationAction(() -> {
            moderationRepository().remove(paramValue("id"));
            CommentService.recordSyncHistory(session, true, 1, "移除一条待审记录");
        });
    }

    public void moderationAnalyze() {
        boolean acquired = false;
        try {
            ModerationAccess.requireMutation(requestInfo, paramValue("adminToken"));
            if (!ModerationConfig.parse(websiteConfig("moderation").getModeration()).aiEnabled) {
                throw new IllegalArgumentException("请先在评论配置中开启 AI 审核建议");
            }
            acquired = AI_REQUEST.tryAcquire();
            if (!acquired) throw new IllegalArgumentException("已有 AI 分析正在进行，请稍后再试");
            ModerationRecord record = moderationRepository().get(paramValue("id"));
            if (!"pending".equals(record.status)) throw new IllegalArgumentException("此评论已进入发布流程，请刷新列表");
            ModerationRecord.AiSuggestion suggestion = new AdminAiClient().analyze(session, requestInfo, record.comment.getContent());
            moderationRepository().saveSuggestion(record.id, suggestion);
            response(CommentApiResponse.data(suggestion));
        } catch (IllegalArgumentException e) {
            response(CommentApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            LOGGER.warning("Comment AI analysis failed: " + e.getClass().getSimpleName());
            response(CommentApiResponse.error("AI 建议暂不可用，请确认后台已更新并配置 AI；可继续人工审核"));
        } finally {
            if (acquired) AI_REQUEST.release();
        }
    }

    private void moderationAction(Runnable action) {
        try {
            ModerationAccess.requireMutation(requestInfo, paramValue("adminToken"));
            action.run();
            response(CommentApiResponse.success());
        } catch (IllegalArgumentException e) {
            response(CommentApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            response(CommentApiResponse.error("操作结果未确认，请刷新列表；发布中的评论需先核对，避免重复发布"));
        }
    }

    private String headerValue(String name) {
        if (requestInfo.getHeader() == null) return null;
        for (Map.Entry<String, String> header : requestInfo.getHeader().entrySet()) {
            if (name.equalsIgnoreCase(header.getKey())) return header.getValue();
        }
        return null;
    }

    private void response(Object data) {
        session.sendMsg(new MsgPacket(data, ContentType.JSON, MsgPacketStatus.RESPONSE_SUCCESS, requestPacket.getMsgId(),
                requestPacket.getMethodStr()));
    }
}
