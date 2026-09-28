package com.zrlog.plugin.comment.moderation;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.common.KvRepository;
import com.zrlog.plugin.comment.config.WebsiteKeyRequest;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.type.ActionType;
import java.util.Collections;
import java.util.Optional;

/** A missing transport response must not be mistaken for an empty queue. */
public class ModerationStore implements KvRepository {
    private final IOSession session;
    public ModerationStore(IOSession session) { this.session = session; }

    @Override public Optional<String> get(String key) {
        StoredQueue response = session.getResponseSync(ContentType.JSON, WebsiteKeyRequest.of(key),
                ActionType.GET_WEBSITE, StoredQueue.class);
        if (response == null) throw new IllegalStateException("审核队列读取未确认");
        return Optional.ofNullable(response.moderationQueue);
    }

    @Override public void put(String key, String value) {
        Object response = session.getResponseSync(ContentType.JSON, Collections.singletonMap(key, value),
                ActionType.SET_WEBSITE, Object.class);
        if (response == null) throw new IllegalStateException("审核队列保存未确认");
    }

    public static class StoredQueue {
        public String moderationQueue;
    }
}
