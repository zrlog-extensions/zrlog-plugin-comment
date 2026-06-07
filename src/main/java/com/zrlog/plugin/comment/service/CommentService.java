package com.zrlog.plugin.comment.service;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.comment.dao.CommentDAO;
import com.zrlog.plugin.render.SimpleTemplateRender;

import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import com.google.gson.Gson;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.type.ActionType;

public class CommentService {

    private static final java.util.logging.Logger LOGGER = com.zrlog.plugin.common.LoggerUtil.getLogger(CommentService.class);

    public CommentService() {

    }

    public static boolean isCommentEmailNotifyEnabled(IOSession session) {
        try {
            Map<String, Object> keyMap = new HashMap<>();
            keyMap.put("key", "commentEmailNotify");
            Map map = session.getResponseSync(ContentType.JSON, keyMap, ActionType.GET_WEBSITE, Map.class);
            if (map == null) {
                return false;
            }
            return isEnabled(map.get("commentEmailNotify"));
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Failed to load comment email notification setting", e);
            return false;
        }
    }

    private static boolean isEnabled(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String normalized = value == null ? "" : value.toString().trim().toLowerCase();
        return "true".equals(normalized) || "on".equals(normalized) || "1".equals(normalized);
    }

    public static synchronized void recordSyncHistory(IOSession session, boolean success, int count, String msg) {
        try {
            Map<String, Object> keyMap = new HashMap<>();
            keyMap.put("key", "syncHistory");
            Map map = session.getResponseSync(ContentType.JSON, keyMap, ActionType.GET_WEBSITE, Map.class);
            String historyJson = map != null ? (String) map.get("syncHistory") : null;

            List<Map<String, Object>> historyList;
            if (historyJson == null || historyJson.trim().isEmpty()) {
                historyList = new ArrayList<>();
            } else {
                historyList = new Gson().fromJson(historyJson, List.class);
            }

            Map<String, Object> newLog = new HashMap<>();
            newLog.put("time", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            newLog.put("success", success);
            newLog.put("count", count);
            newLog.put("message", msg);

            historyList.add(newLog);

            while (historyList.size() > 15) {
                historyList.remove(0);
            }

            Map<String, Object> params = new HashMap<>();
            params.put("syncHistory", new Gson().toJson(historyList));
            session.sendMsg(new MsgPacket(params, ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(),
                    ActionType.SET_WEBSITE.name()), msgPacket -> {
                // Done
            });
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to record sync history", e);
        }
    }

    public String renderBaseListCommentHtml(IOSession session, Long articleId) {
        try {
            List<Map<String, Object>> maps = CommentDAO.loadComments(session, articleId);
            StringBuilder sb = new StringBuilder();
            sb.append("<span class='totalComment'>").append(maps.size()).append(" 条评论</span>");
            for (Map<String, Object> map : maps) {
                sb.append(new SimpleTemplateRender().render("/widget/base/comment", session.getPlugin(), map));
            }
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
