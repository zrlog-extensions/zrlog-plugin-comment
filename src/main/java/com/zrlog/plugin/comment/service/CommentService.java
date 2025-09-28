package com.zrlog.plugin.comment.service;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.comment.dao.CommentDAO;
import com.zrlog.plugin.render.SimpleTemplateRender;

import java.util.List;
import java.util.Map;

public class CommentService {

    public CommentService() {

    }

    public String renderBaseListCommentHtml(IOSession session, Long articleId) {
        try {
            List<Map<String, Object>> maps = CommentDAO.loadComments(session, articleId);
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> map : maps) {
                sb.append(new SimpleTemplateRender().render("/widget/base/comment", session.getPlugin(), map));
            }
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
