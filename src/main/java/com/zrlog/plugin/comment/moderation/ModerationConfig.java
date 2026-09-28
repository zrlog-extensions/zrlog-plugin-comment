package com.zrlog.plugin.comment.moderation;

import com.google.gson.Gson;

public class ModerationConfig {
    public boolean enabled;
    public boolean aiEnabled;

    public static ModerationConfig parse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ModerationConfig();
        }
        ModerationConfig config = new Gson().fromJson(json, ModerationConfig.class);
        if (config == null) {
            throw new IllegalArgumentException("评论审核配置无效");
        }
        return config;
    }
}
