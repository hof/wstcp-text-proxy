package org.hofcom.wsproxy;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.red5.net.websocket.model.MessageType;
import org.red5.net.websocket.model.Packet;

public class TcpHandler extends IoHandlerAdapter {

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("TCP-sessionCreated");

        /* set idle time */
        session.getConfig().setIdleTime(IdleStatus.WRITER_IDLE, 5);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        System.out.println("TCP-sessionClosed");

        IoSession wsSession = (IoSession) session.getAttribute("WS");
        if (wsSession != null) {
            wsSession.closeNow();
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String msg = message.toString();
        if (!msg.startsWith("{\"class\":\"rate\"")) {
            System.out.println("TCP > " + message.toString());
        }

        IoSession wsSession = (IoSession)session.getAttribute("WS");
        if (wsSession != null) {
            Packet packet = Packet.build(msg.getBytes("UTF8"), MessageType.TEXT);
            wsSession.write(packet);
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        System.out.println("TCP-Exception");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        session.write("{\"class\":\"ping\"}");
        // System.out.println("TCP < ping");
    }
}