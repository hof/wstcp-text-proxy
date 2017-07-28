package org.hofcom.wsproxy;

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

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        System.out.println("Exception: " + cause.toString());
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("sessionCreated");

        // connector of TCP session
        NioSocketConnector connector = new NioSocketConnector();
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TcpCodecFactory()));
        connector.getFilterChain().addLast("threadpool", new ExecutorFilter(1));

        // handler the TCP session
        TcpHandler tcpHandler = new TcpHandler();

        // connect and wait
        ConnectFuture cf = null;
        try {
            connector.setHandler(tcpHandler);
            cf = connector.connect(new InetSocketAddress("10.86.179.43", 5000));
            cf.await();
        } catch (RuntimeIoException ioe) {
            System.out.println("Error connecting to tcp " + ioe.toString());
        }

        // link the sessions
        cf.getSession().setAttribute("WS", session);
        session.setAttribute("TCP", cf.getSession());
    }
                    
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        System.out.println("sessionClosed");

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
            System.out.println("ws < " + wsm.getMessageAsString());

            // send to tcp connection
            IoSession tcpSession = (IoSession)session.getAttribute("TCP");
            tcpSession.write(wsm.getMessageAsString());
        }
    }
}
