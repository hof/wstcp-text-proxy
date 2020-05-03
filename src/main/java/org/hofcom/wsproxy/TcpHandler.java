package org.hofcom.wsproxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.red5.net.websocket.model.MessageType;
import org.red5.net.websocket.model.Packet;

public class TcpHandler extends IoHandlerAdapter {

    public static final Logger LOGGER = LogManager.getLogger(TcpHandler.class);

    public static final Logger DATA_LOGGER = LogManager.getLogger("data");

    private boolean logData;

    private String pingData;

    public TcpHandler(boolean logData, String pingData) {
        this.logData = logData;
        this.pingData = pingData;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        LOGGER.info("TCP-sessionCreated");

        /* set idle time */
        session.getConfig().setIdleTime(IdleStatus.WRITER_IDLE, 5);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        LOGGER.info("TCP-sessionClosed");

        IoSession wsSession = (IoSession) session.getAttribute("WS");
        if (wsSession != null) {

            // remove links
            session.removeAttribute("WS");
            wsSession.removeAttribute("TCP");
            wsSession.closeNow();
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String msg = message.toString();

        if (logData) {
            DATA_LOGGER.info("TCP > " + message.toString());
        }

        IoSession wsSession = (IoSession)session.getAttribute("WS");
        if (wsSession != null) {
            Packet packet = Packet.build(msg.getBytes("UTF8"), MessageType.TEXT);
            wsSession.write(packet);
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LOGGER.error("TCP-Exception", cause);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        session.write(pingData);
    }
}
