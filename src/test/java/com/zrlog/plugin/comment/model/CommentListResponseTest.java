package com.zrlog.plugin.comment.model;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommentListResponseTest {

    @Test
    public void shouldDeserializeBlogCommentApiResponseIntoTypedModels() {
        String json = "{"
                + "\"error\":0,"
                + "\"message\":\"\","
                + "\"data\":[{"
                + "\"id\":5,"
                + "\"userComment\":\"<script>alert('stored')</script>\","
                + "\"header\":\"\","
                + "\"commTime\":\"2026-07-30 12:30:00\","
                + "\"userHome\":\"https://example.com/profile\","
                + "\"userName\":\"reader\","
                + "\"gravatarId\":\"0123456789abcdef0123456789abcdef\""
                + "}]"
                + "}";

        CommentListResponse response = new Gson().fromJson(json, CommentListResponse.class);
        List<CommentRenderModel> comments = response.getData();

        assertEquals(1, comments.size());
        assertEquals(CommentRenderModel.class, comments.get(0).getClass());
        assertEquals("<script>alert('stored')</script>", comments.get(0).getUserComment());
        assertEquals("2026-07-30 12:30:00", comments.get(0).getCommTime());
        assertEquals("https://example.com/profile", comments.get(0).getUserHome());
        assertEquals("reader", comments.get(0).getUserName());
        assertEquals("0123456789abcdef0123456789abcdef", comments.get(0).getGravatarId());
    }

    @Test
    public void shouldTreatNullCommentDataAsEmpty() {
        CommentListResponse response = new Gson().fromJson(
                "{\"error\":0,\"message\":\"\",\"data\":null}", CommentListResponse.class);

        assertTrue(response.getData().isEmpty());
    }
}
