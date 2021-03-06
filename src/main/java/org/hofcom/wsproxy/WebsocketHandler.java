package org.hofcom.wsproxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.red5.net.websocket.model.WSMessage;

import java.net.InetSocketAddress;

/**
 * IoHandler for events on the websocket connection
 *
 */
public class WebsocketHandler extends IoHandlerAdapter {

    public static final Logger LOGGER = LogManager.getLogger(WebsocketHandler.class);

    public static final Logger DATA_LOGGER = LogManager.getLogger("data");

    private NioSocketConnector connector = new NioSocketConnector();

    private String host;

    private Integer port;

    private boolean logData;

    public WebsocketHandler(String host, Integer port, boolean logData, TcpHandler handler) {
        this.host = host;
        this.port = port;
        this.logData = logData;

        connector.setHandler(handler);
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TcpCodecFactory()));
        connector.getFilterChain().addLast("threadpool", new ExecutorFilter(10));
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LOGGER.error("Exception: ", cause);
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        LOGGER.info("sessionCreated");

        // connect and wait
        ConnectFuture cf = null;
        try {
            cf = connector.connect(new InetSocketAddress(host, port));
            cf.await();
        } catch (RuntimeIoException ioe) {
            LOGGER.error("Error connecting to tcp ", ioe);
        }

        // link the sessions
        cf.getSession().setAttribute("WS", session);
        session.setAttribute("TCP", cf.getSession());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        LOGGER.info("sessionClosed");

        IoSession tcpSession = (IoSession)session.getAttribute("TCP");
        if (tcpSession != null) {

            // remove links
            session.removeAttribute("TCP");
            tcpSession.removeAttribute("WS");
            tcpSession.closeNow();
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (message instanceof WSMessage) {
            WSMessage wsm = (WSMessage)message;

            if (logData) {
                DATA_LOGGER.info("ws < " + wsm.getMessageAsString());
            }

            // send to tcp connection
            IoSession tcpSession = (IoSession)session.getAttribute("TCP");
            tcpSession.write(wsm.getMessageAsString());
        }
    }
}