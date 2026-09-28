package com.zrlog.plugin.comment.moderation;

import com.google.gson.Gson;
import com.zrlog.plugin.common.KvRepository;
import com.zrlog.plugin.common.model.Comment;
import com.zrlog.plugin.comment.dao.CommentDAO;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import org.junit.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class ModerationTest {
    private Comment comment(String content) {
        Comment comment = new Comment();
        comment.setLogId(1L); comment.setContent(content); comment.setName("reader");
        comment.setMail("reader@example.com"); comment.setHome(""); comment.setIp("127.0.0.1");
        return comment;
    }
    private static class MemoryKv implements KvRepository {
        String value;
        boolean ignoreWrite;
        public Optional<String> get(String key) { return Optional.ofNullable(value); }
        public void put(String key, String value) { if (!ignoreWrite) this.value = value; }
    }
    @Test public void holdsUntilHumanApprovesAndPublishesOnlyOnce() {
        MemoryKv kv = new MemoryKv();
        ModerationRepository repository = new ModerationRepository(kv);
        Comment input = comment("正常评论");
        repository.add(input);
        String id = repository.list().get(0).id;
        AtomicInteger published = new AtomicInteger();
        ModerationRecord.AiSuggestion suggestion = new ModerationRecord.AiSuggestion();
        suggestion.verdict = "normal"; suggestion.reason = "正常讨论"; suggestion.reply = "谢谢";
        repository.saveSuggestion(id, suggestion);
        assertEquals("pending", repository.get(id).status);
        assertEquals(0, published.get());
        repository.approve(id, item -> { assertEquals(input.getContent(), item.getContent()); published.incrementAndGet(); });
        assertTrue(repository.list().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> repository.approve(id, item -> published.incrementAndGet()));
        assertEquals(1, published.get());
    }
    @Test public void uncertainPublicationSurvivesRestartAndCannotBeRepeated() {
        MemoryKv kv = new MemoryKv();
        ModerationRepository repository = new ModerationRepository(kv);
        repository.add(comment("hello"));
        String id = repository.list().get(0).id;
        assertThrows(IllegalStateException.class, () -> repository.approve(id, item -> { throw new IllegalStateException("timeout"); }));
        ModerationRepository restarted = new ModerationRepository(kv);
        assertEquals("publishing", restarted.get(id).status);
        AtomicInteger retried = new AtomicInteger();
        assertThrows(IllegalArgumentException.class, () -> restarted.approve(id, item -> retried.incrementAndGet()));
        assertEquals(0, retried.get());
        restarted.remove(id);
        assertTrue(restarted.list().isEmpty());
    }
    @Test public void neverPublishesWhenQueuePersistenceFails() {
        MemoryKv kv = new MemoryKv();
        ModerationRepository repository = new ModerationRepository(kv);
        repository.add(comment("hello"));
        String id = repository.list().get(0).id;
        kv.ignoreWrite = true;
        AtomicInteger published = new AtomicInteger();
        assertThrows(IllegalStateException.class, () -> repository.approve(id, item -> published.incrementAndGet()));
        assertEquals(0, published.get());
    }
    @Test public void rejectsDuplicatesAcrossRepositoryInstances() {
        MemoryKv kv = new MemoryKv();
        new ModerationRepository(kv).add(comment("hello"));
        assertThrows(IllegalArgumentException.class, () -> new ModerationRepository(kv).add(comment("hello")));
    }
    @Test public void enforcesCooldownAndDuplicateWindow() {
        CommentGuard guard = new CommentGuard();
        guard.reserve(comment("hello world"), 0);
        assertThrows(IllegalArgumentException.class, () -> guard.reserve(comment("different"), 29000));
        assertThrows(IllegalArgumentException.class, () -> guard.reserve(comment("hello  world"), 31000));
        guard.reserve(comment("different"), 31000);
        guard.reserve(comment("hello world"), 600001);
    }
    @Test public void rejectsBotAndOversizedInputsButAcceptsOptionalWebsite() {
        CommentGuard.validate(comment("hello"), "");
        assertThrows(IllegalArgumentException.class, () -> CommentGuard.validate(comment("hello"), "bot"));
        Comment invalid = comment("hello"); invalid.setMail("reader@example.com\nBcc:bad");
        assertThrows(IllegalArgumentException.class, () -> CommentGuard.validate(invalid, ""));
        assertThrows(IllegalArgumentException.class, () -> CommentGuard.validate(comment(String.join("", Collections.nCopies(5001, "a"))), ""));
    }
    @Test public void rejectsOversizedWhitespaceAndAllowsDifferentReadersWithSameContent() {
        assertThrows(IllegalArgumentException.class, () -> CommentGuard.validate(comment("hello" + String.join("", Collections.nCopies(5001, " "))), ""));
        MemoryKv kv = new MemoryKv();
        ModerationRepository repository = new ModerationRepository(kv);
        repository.add(comment("谢谢分享"));
        Comment another = comment("谢谢分享"); another.setMail("another@example.com");
        repository.add(another);
        assertEquals(2, repository.list().size());
    }
    @Test public void guardsAdminAndMutationToken() {
        HttpRequestInfo request = new HttpRequestInfo();
        request.setUserId(-1);
        assertThrows(IllegalArgumentException.class, () -> ModerationAccess.requireAdmin(request));
        request.setUserId(1);
        assertThrows(IllegalArgumentException.class, () -> ModerationAccess.requireMutation(request, "wrong"));
        ModerationAccess.requireMutation(request, ModerationAccess.token(request));
    }
    @Test public void matchesHostSaveResponseContract() {
        assertTrue(new Gson().fromJson("{\"result\":true}", CommentDAO.SaveResult.class).result);
        assertFalse(new Gson().fromJson("{\"result\":false}", CommentDAO.SaveResult.class).result);
    }
}
