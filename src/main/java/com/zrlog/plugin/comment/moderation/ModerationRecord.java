package com.zrlog.plugin.comment.moderation;

import com.zrlog.plugin.common.model.Comment;

public class ModerationRecord {
    public String id;
    public Comment comment;
    public String status;
    public long createdAt;
    public AiSuggestion suggestion;

    public static class AiSuggestion {
        public String verdict;
        public String reason;
        public String reply;
    }
}
