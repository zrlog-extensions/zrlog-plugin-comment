package com.zrlog.plugin.comment;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.common.type.HttpMethod;
import com.zrlog.plugin.data.codec.*;
import com.zrlog.plugin.message.CapabilityInvokeRequest;
import com.zrlog.plugin.message.CapabilityInvokeResult;
import com.zrlog.plugin.type.ActionType;
import org.junit.Test;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.*;

public class CommentClientActionHandlerTest {
    @Test
    public void hostRenderingRejectsSyncAndMutationRoutesBeforeSendingAnyHostRequest() throws Exception {
        CommentClientActionHandler handler = new CommentClientActionHandler();
        assertEquals(2, handler.capabilityLifecycleVersion());
        try (SocketChannel channel = SocketChannel.open(); Selector selector = Selector.open()) {
            CapturingSession session = new CapturingSession(channel, selector, handler);
            try {
                for (String path : new String[]{"/changyan/sync/callback", "/addComment", "/moderationApprove", "/update"}) {
                    HttpRequestInfo view = new HttpRequestInfo();
                    view.setUri(path);
                    view.setHttpMethod(HttpMethod.GET);
                    CapabilityInvokeRequest request = new CapabilityInvokeRequest();
                    request.setCapabilityKey(CapabilityInvokeRequest.HOST_VIEW_CAPABILITY);
                    request.setRequireCompletion(true);
                    request.setRequestId("1.1793300000000.reviewed-view");
                    request.setViewRequest(view);
                    handler.capabilityInvoke(session, new MsgPacket(request, ContentType.JSON,
                            MsgPacketStatus.SEND_REQUEST, 42, ActionType.CAPABILITY_INVOKE.name()));
                    assertEquals(MsgPacketStatus.RESPONSE_ERROR, session.status);
                    assertTrue(session.result.isExecutionCompleted());
                    assertFalse(session.result.isSuccess());
                    assertFalse(session.result.isOutcomeUncertain());
                    assertEquals("Only the comment widget supports host view rendering", session.result.getErrorMessage());
                }
            } finally {
                session.close();
            }
        }
    }

    private static final class CapturingSession extends IOSession {
        private CapabilityInvokeResult result;
        private MsgPacketStatus status;

        CapturingSession(SocketChannel channel, Selector selector, CommentClientActionHandler handler) {
            super(channel, selector, new SocketCodec(new SocketEncode(), new SocketDecode(Runnable::run)), handler);
        }

        @Override
        public void sendJsonMsg(Object data, String method, int id, MsgPacketStatus responseStatus) {
            assertEquals(ActionType.CAPABILITY_INVOKE.name(), method);
            assertEquals(42, id);
            result = (CapabilityInvokeResult) data;
            status = responseStatus;
        }

        @Override
        public <T> T getResponseSync(ContentType type, Object data, ActionType action, Class<T> clazz) {
            throw new AssertionError("Rejected host views must not read or mutate site state");
        }
    }
}
