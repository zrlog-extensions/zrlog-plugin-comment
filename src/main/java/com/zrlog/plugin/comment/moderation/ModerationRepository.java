package com.zrlog.plugin.comment.moderation;

import com.google.gson.Gson;
import com.zrlog.plugin.common.KvRepository;
import com.zrlog.plugin.common.model.Comment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Callers share this monitor so a decision cannot race another submission or decision. */
public class ModerationRepository {
    public static final Object LOCK = new Object();
    private static final String KEY = "moderationQueue";
    private static final int MAX_RECORDS = 100;
    private final KvRepository kv;
    private final Gson gson = new Gson();

    public ModerationRepository(KvRepository kv) {
        this.kv = kv;
    }

    public List<ModerationRecord> list() {
        synchronized (LOCK) {
            String json = kv.get(KEY).orElse("[]");
            ModerationRecord[] records = gson.fromJson(json, ModerationRecord[].class);
            if (records == null) {
                throw new IllegalStateException("审核队列读取失败");
            }
            return new ArrayList<>(Arrays.asList(records));
        }
    }

    public void add(Comment comment) {
        synchronized (LOCK) {
            List<ModerationRecord> records = list();
            for (ModerationRecord record : records) {
                if (record.comment.getLogId().equals(comment.getLogId())
                        && record.comment.getContent().trim().equals(comment.getContent().trim())
                        && record.comment.getMail().trim().equalsIgnoreCase(comment.getMail().trim())) {
                    throw new IllegalArgumentException("这条评论已提交，请勿重复提交");
                }
            }
            if (records.size() >= MAX_RECORDS) {
                throw new IllegalArgumentException("待审评论较多，请稍后再试");
            }
            ModerationRecord record = new ModerationRecord();
            record.id = UUID.randomUUID().toString();
            record.comment = comment;
            record.createdAt = System.currentTimeMillis();
            record.status = "pending";
            records.add(record);
            write(records);
        }
    }

    public ModerationRecord get(String id) {
        for (ModerationRecord record : list()) {
            if (record.id.equals(id)) {
                return record;
            }
        }
        throw new IllegalArgumentException("评论不存在或已处理，请刷新列表");
    }

    public void saveSuggestion(String id, ModerationRecord.AiSuggestion suggestion) {
        synchronized (LOCK) {
            List<ModerationRecord> records = list();
            ModerationRecord record = find(records, id);
            requirePending(record);
            record.suggestion = suggestion;
            write(records);
        }
    }

    public void approve(String id, Publisher publisher) {
        synchronized (LOCK) {
            List<ModerationRecord> records = list();
            ModerationRecord record = find(records, id);
            requirePending(record);
            // Persist before publishing. A lost response must never cause an automatic duplicate.
            record.status = "publishing";
            write(records);
            publisher.publish(record.comment);
            records.remove(record);
            write(records);
        }
    }

    public void remove(String id) {
        synchronized (LOCK) {
            List<ModerationRecord> records = list();
            records.remove(find(records, id));
            write(records);
        }
    }

    private void write(List<ModerationRecord> records) {
        String json = gson.toJson(records);
        kv.put(KEY, json);
        if (!json.equals(kv.get(KEY).orElse(null))) {
            throw new IllegalStateException("审核队列保存未确认，请刷新后重试");
        }
    }

    private ModerationRecord find(List<ModerationRecord> records, String id) {
        for (ModerationRecord record : records) {
            if (record.id.equals(id)) {
                return record;
            }
        }
        throw new IllegalArgumentException("评论不存在或已处理，请刷新列表");
    }

    private void requirePending(ModerationRecord record) {
        if (!"pending".equals(record.status)) {
            throw new IllegalArgumentException("发布结果待核对，请先在站点评论管理中核对，勿重复发布");
        }
    }

    public interface Publisher {
        void publish(Comment comment);
    }
}
