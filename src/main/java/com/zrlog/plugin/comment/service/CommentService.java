package com.zrlog.plugin.comment.service;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.comment.config.CommentHistoryConfig;
import com.zrlog.plugin.comment.config.CommentHistoryRecord;
import com.zrlog.plugin.comment.config.CommentWebsiteConfig;
import com.zrlog.plugin.comment.config.WebsiteKeyRequest;
import com.zrlog.plugin.comment.dao.CommentDAO;
import com.zrlog.plugin.comment.model.CommentRenderModel;
import com.zrlog.plugin.comment.render.CommentHtmlRenderer;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.type.ActionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CommentService {

    private static final java.util.logging.Logger LOGGER = com.zrlog.plugin.common.LoggerUtil.getLogger(CommentService.class);
    private static final CommentHtmlRenderer COMMENT_HTML_RENDERER = new CommentHtmlRenderer();

    public CommentService() {

    }

    public static boolean isCommentEmailNotifyEnabled(IOSession session) {
        try {
            CommentWebsiteConfig config = session.getResponseSync(ContentType.JSON, WebsiteKeyRequest.of("commentEmailNotify"),
                    ActionType.GET_WEBSITE, CommentWebsiteConfig.class);
            if (config == null) {
                return false;
            }
            return isEnabled(config.getCommentEmailNotify());
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Failed to load comment email notification setting", e);
            return false;
        }
    }

    private static boolean isEnabled(String value) {
        String normalized = value == null ? "" : value.toString().trim().toLowerCase();
        return "true".equals(normalized) || "on".equals(normalized) || "1".equals(normalized);
    }

    public static synchronized void recordSyncHistory(IOSession session, boolean success, int count, String msg) {
        try {
            Gson gson = new Gson();
            CommentHistoryConfig historyConfig = session.getResponseSync(ContentType.JSON, WebsiteKeyRequest.of("syncHistory"),
                    ActionType.GET_WEBSITE, CommentHistoryConfig.class);
            String historyJson = historyConfig != null ? historyConfig.getSyncHistory() : null;
            List<CommentHistoryRecord> historyList = historyList(gson, historyJson);

            CommentHistoryRecord newLog = CommentHistoryRecord.create(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    success, count, msg);
            historyList.add(newLog);

            while (historyList.size() > 15) {
                historyList.remove(0);
            }

            CommentHistoryConfig saveConfig = new CommentHistoryConfig();
            saveConfig.setSyncHistory(gson.toJson(historyList));
            session.sendMsg(new MsgPacket(saveConfig, ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(),
                    ActionType.SET_WEBSITE.name()), msgPacket -> {
                // Done
            });
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to record sync history", e);
        }
    }

    private static List<CommentHistoryRecord> historyList(Gson gson, String historyJson) {
        if (historyJson == null || historyJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        CommentHistoryRecord[] records = gson.fromJson(historyJson, CommentHistoryRecord[].class);
        if (records == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(records));
    }

    public String renderBaseListCommentHtml(IOSession session, Long articleId) {
        try {
            List<CommentRenderModel> comments = CommentDAO.loadComments(session, articleId);
            return COMMENT_HTML_RENDERER.renderCommentList(comments, session.getPlugin());
        } catch (Exception e) {
            return CommentHtmlRenderer.escapeHtmlText(e.getMessage());
        }
    }
}
