package com.zrlog.plugin.comment.moderation;

import com.zrlog.plugin.common.model.Comment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommentGuard {
    public static final CommentGuard INSTANCE = new CommentGuard();
    private final Map<String, Long> recent = new LinkedHashMap<>();

    public static void validate(Comment comment, String honeypot) {
        if (honeypot != null && !honeypot.trim().isEmpty()) {
            throw new IllegalArgumentException("评论提交无效");
        }
        if (comment.getLogId() == null || comment.getLogId() <= 0) {
            throw new IllegalArgumentException("文章编号无效");
        }
        checkLength(comment.getContent(), 1, 5000, "评论内容需为 1～5000 字");
        checkLength(comment.getName(), 1, 80, "昵称需为 1～80 字");
        checkLength(comment.getHome(), 0, 500, "网站地址过长");
        String email = comment.getMail();
        if (email == null || email.length() > 254 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("请输入有效邮箱");
        }
    }

    /** IP comes from the host's trusted proxy headers; email is a fallback for older hosts. */
    public synchronized void reserve(Comment comment, long now) {
        recent.entrySet().removeIf(entry -> now - entry.getValue() >= 600_000L);
        String identity = comment.getIp() == null || comment.getIp().trim().isEmpty()
                ? comment.getMail().trim().toLowerCase(java.util.Locale.ROOT) : comment.getIp().trim();
        String sender = "sender:" + hash(identity);
        String duplicate = duplicateKey(comment);
        if (recent.containsKey(sender) && now - recent.get(sender) < 30_000L) {
            throw new IllegalArgumentException("提交过于频繁，请 30 秒后重试");
        }
        if (recent.containsKey(duplicate)) {
            throw new IllegalArgumentException("这条评论已提交，请勿重复提交");
        }
        if (recent.size() >= 10_000) {
            throw new IllegalArgumentException("评论提交繁忙，请稍后再试");
        }
        recent.put(sender, now);
        recent.put(duplicate, now);
    }

    public synchronized void releaseDuplicate(Comment comment) {
        recent.remove(duplicateKey(comment));
    }

    private String duplicateKey(Comment comment) {
        String normalized = Normalizer.normalize(comment.getContent(), Normalizer.Form.NFKC).replaceAll("\\s+", "").trim();
        return "content:" + hash(comment.getLogId() + ":" + comment.getMail().trim().toLowerCase(java.util.Locale.ROOT) + ":" + normalized);
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void checkLength(String value, int min, int max, String message) {
        int length = value == null ? 0 : value.length();
        if ((value == null ? 0 : value.trim().length()) < min || length > max) {
            throw new IllegalArgumentException(message);
        }
    }
}
