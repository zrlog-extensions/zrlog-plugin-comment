package com.zrlog.plugin.comment.render;

import com.zrlog.plugin.common.model.Comment;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommentNotificationTemplateTest {

    @Test
    public void shouldEscapeCommentFieldsInNotificationEmail() {
        Comment comment = new Comment();
        comment.setName("\"><img src=x onerror=\"alert(1)\">");
        comment.setContent("</td><script>alert('mail')</script>\n下一行");

        String html = new CommentHtmlRenderer().renderNotification(
                comment, "4.0</small><img src=x onerror=alert(1)>", null);

        assertFalse(html.contains("<script>alert('mail')</script>"));
        assertFalse(html.contains("<img src=x onerror=\"alert(1)\">"));
        assertFalse(html.contains("</small><img src=x onerror=alert(1)>"));
        assertTrue(html.contains("&lt;script&gt;alert(&#39;mail&#39;)&lt;/script&gt;\n下一行"));
        assertTrue(html.contains("&quot;&gt;&lt;img src=x onerror=&quot;alert(1)&quot;&gt;"));
        assertTrue(html.contains("4.0&lt;/small&gt;&lt;img src=x onerror=alert(1)&gt;"));
        assertFalse(html.contains("${"));
    }
}
