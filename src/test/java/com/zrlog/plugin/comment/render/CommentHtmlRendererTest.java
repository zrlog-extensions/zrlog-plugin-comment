package com.zrlog.plugin.comment.render;

import com.zrlog.plugin.comment.model.CommentRenderModel;
import com.zrlog.plugin.render.SimpleTemplateRender;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CommentHtmlRendererTest {

    private final CommentHtmlRenderer renderer = new CommentHtmlRenderer();

    @Test
    public void shouldRenderStoredAttackPayloadsAsText() {
        CommentRenderModel comment = comment(
                "x\" onerror=\"alert(1)",
                "javascript:alert(1)",
                "\"><img src=x onerror=\"alert(1)\">",
                "<svg onload=alert(1)>",
                "<script>alert('stored')</script>\n第二行");

        String html = renderer.renderCommentList(Collections.singletonList(comment), null);

        assertTrue(html.contains("d41d8cd98f00b204e9800998ecf8427e"));
        assertTrue(html.contains("<span class=\"commentUserName\">"));
        assertFalse(html.contains("<a href="));
        assertFalse(html.contains("<script>alert('stored')</script>"));
        assertFalse(html.contains("<img src=x onerror=\"alert(1)\">"));
        assertFalse(html.contains("<svg onload=alert(1)>"));
        assertTrue(html.contains("&lt;script&gt;alert(&#39;stored&#39;)&lt;/script&gt;\n第二行"));
        assertTrue(html.contains("&quot;&gt;&lt;img src=x onerror=&quot;alert(1)&quot;&gt;"));
        assertFalse(html.contains("${"));
    }

    @Test
    public void shouldKeepSafeHomepageAndNormalCommentFormatting() {
        CommentRenderModel comment = comment(
                "0123456789ABCDEF0123456789ABCDEF",
                " HTTPS://example.com/profile?from=blog&lang=zh ",
                "小明 & \"admin\"",
                "2026-07-30 12:30:00",
                "第一行\r\n第二行");

        String html = renderer.renderCommentList(Collections.singletonList(comment), null);

        assertTrue(html.contains("0123456789abcdef0123456789abcdef"));
        assertTrue(html.contains("<a href=\"HTTPS://example.com/profile?from=blog&amp;lang=zh\""));
        assertTrue(html.contains("小明 &amp; &quot;admin&quot;"));
        assertTrue(html.contains("第一行\r\n第二行"));
        assertFalse(html.contains("null"));
        assertFalse(html.contains("${"));
    }

    @Test
    public void shouldOnlyAcceptAbsoluteHttpHomepageUrls() {
        assertEquals("", CommentHtmlRenderer.normalizeHttpUrl(null));
        assertEquals("", CommentHtmlRenderer.normalizeHttpUrl("  "));
        assertEquals("https://example.com/path", CommentHtmlRenderer.normalizeHttpUrl(" https://example.com/path "));
        assertEquals("HTTP://localhost:8080", CommentHtmlRenderer.normalizeHttpUrl("HTTP://localhost:8080"));

        assertNull(CommentHtmlRenderer.normalizeHttpUrl("javascript:alert(1)"));
        assertNull(CommentHtmlRenderer.normalizeHttpUrl("JaVaScRiPt:alert(1)"));
        assertNull(CommentHtmlRenderer.normalizeHttpUrl("data:text/html,<script>alert(1)</script>"));
        assertNull(CommentHtmlRenderer.normalizeHttpUrl("vbscript:msgbox(1)"));
        assertNull(CommentHtmlRenderer.normalizeHttpUrl("//example.com/path"));
        assertNull(CommentHtmlRenderer.normalizeHttpUrl("https://user:password@example.com"));
        assertNull(CommentHtmlRenderer.normalizeHttpUrl("https://"));
        assertNull(CommentHtmlRenderer.normalizeHttpUrl("java\nscript:alert(1)"));
        assertNull(CommentHtmlRenderer.normalizeHttpUrl("https://example.com/\" onmouseover=\"alert(1)"));
    }

    @Test
    public void shouldEscapeReflectedResultAndJavaScriptConfiguration() {
        String result = renderer.renderResult(
                "For input string: \"</h4><img src=x onerror=alert(1)>\"", null);

        assertFalse(result.contains("</h4><img src=x onerror=alert(1)>"));
        assertTrue(result.contains("&lt;/h4&gt;&lt;img src=x onerror=alert(1)&gt;"));

        Map<String, Object> data = new HashMap<>();
        data.put("articleId", "1");
        data.put("appIdJson", CommentHtmlRenderer.toJavaScriptString(
                "</script><script>alert('configured')</script>"));
        String widget = new SimpleTemplateRender().render("/widget/changyan/index", null, data);

        assertFalse(widget.contains("</script><script>alert('configured')</script>"));
        assertTrue(widget.contains("\\u003c/script\\u003e\\u003cscript\\u003e"));
        assertTrue(widget.contains("encodeURIComponent(appid)"));
        assertFalse(widget.contains("${articleId}"));
        assertFalse(widget.contains("${appIdJson}"));
    }

    private static CommentRenderModel comment(String gravatarId, String userHome, String userName,
                                              String commTime, String userComment) {
        CommentRenderModel comment = new CommentRenderModel();
        comment.setGravatarId(gravatarId);
        comment.setUserHome(userHome);
        comment.setUserName(userName);
        comment.setCommTime(commTime);
        comment.setUserComment(userComment);
        return comment;
    }
}
