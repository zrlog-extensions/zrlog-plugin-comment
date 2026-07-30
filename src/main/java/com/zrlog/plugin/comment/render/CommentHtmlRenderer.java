package com.zrlog.plugin.comment.render;

import com.google.gson.Gson;
import com.zrlog.plugin.comment.model.CommentRenderModel;
import com.zrlog.plugin.common.model.Comment;
import com.zrlog.plugin.message.Plugin;
import com.zrlog.plugin.render.SimpleTemplateRender;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class CommentHtmlRenderer {

    private static final String DEFAULT_GRAVATAR_ID = "d41d8cd98f00b204e9800998ecf8427e";
    private static final Pattern GRAVATAR_ID_PATTERN = Pattern.compile("[a-fA-F0-9]{32}");
    private static final Gson GSON = new Gson();

    private final SimpleTemplateRender templateRender = new SimpleTemplateRender();

    public String renderCommentList(List<CommentRenderModel> comments, Plugin plugin) {
        StringBuilder html = new StringBuilder();
        int commentCount = comments == null ? 0 : comments.size();
        html.append("<span class='totalComment'>").append(commentCount).append(" 条评论</span>");
        if (comments == null) {
            return html.toString();
        }
        for (CommentRenderModel comment : comments) {
            html.append(templateRender.render("/widget/base/comment", plugin, commentTemplateData(comment)));
        }
        return html.toString();
    }

    public String renderResult(String resultMessage, Plugin plugin) {
        Map<String, Object> data = new HashMap<>();
        data.put("resultMsg", escapeHtmlText(resultMessage));
        return templateRender.render("/result/comment", plugin, data);
    }

    public String renderNotification(Comment comment, String version, Plugin plugin) {
        Map<String, Object> data = new HashMap<>();
        data.put("content", escapeHtmlText(comment == null ? null : comment.getContent()));
        data.put("title", "-");
        data.put("titleUrl", "#");
        data.put("titleUrlText", "-");
        data.put("username", escapeHtmlText(comment == null ? null : comment.getName()));
        data.put("version", escapeHtmlText(version));
        return templateRender.render("/email/notify-email", plugin, data);
    }

    public static String normalizeHttpUrl(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        String normalized = value.trim();
        try {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();
            String normalizedScheme = scheme == null ? "" : scheme.toLowerCase(Locale.ROOT);
            if (scheme == null
                    || (!"http".equals(normalizedScheme) && !"https".equals(normalizedScheme))
                    || uri.getRawAuthority() == null
                    || uri.getRawAuthority().trim().isEmpty()
                    || uri.getRawUserInfo() != null) {
                return null;
            }
            return normalized;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String escapeHtmlText(Object value) {
        return escapeHtml(value);
    }

    public static String escapeHtmlAttribute(Object value) {
        return escapeHtml(value);
    }

    public static String toJavaScriptString(String value) {
        return GSON.toJson(value == null ? "" : value);
    }

    private Map<String, Object> commentTemplateData(CommentRenderModel comment) {
        Map<String, Object> data = new HashMap<>();
        String userName = escapeHtmlText(comment == null ? null : comment.getUserName());
        data.put("gravatarId", normalizedGravatarId(comment == null ? null : comment.getGravatarId()));
        data.put("userNameAttribute", escapeHtmlAttribute(comment == null ? null : comment.getUserName()));
        data.put("userIdentityHtml", renderUserIdentity(userName, comment == null ? null : comment.getUserHome()));
        data.put("commTime", escapeHtmlText(comment == null ? null : comment.getCommTime()));
        data.put("userComment", escapeHtmlText(comment == null ? null : comment.getUserComment()));
        return data;
    }

    private String renderUserIdentity(String escapedUserName, String rawUserHome) {
        String userHome = normalizeHttpUrl(rawUserHome);
        if (userHome == null || userHome.isEmpty()) {
            return "<span class=\"commentUserName\">" + escapedUserName + "</span>";
        }
        return "<a href=\"" + escapeHtmlAttribute(userHome)
                + "\" rel=\"nofollow ugc noreferrer\" class=\"commentUserName\">"
                + escapedUserName + "</a>";
    }

    private String normalizedGravatarId(String gravatarId) {
        if (gravatarId == null || !GRAVATAR_ID_PATTERN.matcher(gravatarId).matches()) {
            return DEFAULT_GRAVATAR_ID;
        }
        return gravatarId.toLowerCase(Locale.ROOT);
    }

    private static String escapeHtml(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        StringBuilder escaped = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            switch (character) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#39;");
                    break;
                default:
                    escaped.append(character);
            }
        }
        return escaped.toString();
    }
}
