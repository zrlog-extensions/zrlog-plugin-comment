package com.zrlog.plugin.comment.moderation;

import com.zrlog.plugin.data.codec.HttpRequestInfo;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

public final class ModerationAccess {
    private static final String TOKEN = UUID.randomUUID().toString();

    private ModerationAccess() { }

    public static void requireAdmin(HttpRequestInfo request) {
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new IllegalArgumentException("请登录后台后操作");
        }
    }

    public static String token(HttpRequestInfo request) {
        requireAdmin(request);
        return TOKEN;
    }

    public static void requireMutation(HttpRequestInfo request, String token) {
        requireAdmin(request);
        if (token == null || !MessageDigest.isEqual(TOKEN.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("页面已过期，请刷新后重试");
        }
    }
}
