package org.hofcom.wsproxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.red5.net.websocket.codec.WebSocketCodecFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ProxyMain {

    public static final Logger LOGGER = LogManager.getLogger(ProxyMain.class);

    private WebsocketHandler wsHandler = null;

    private NioSocketAcceptor wsAcceptor =  null;

    private void startWebsocketServer() {
        LOGGER.info("Starting wstcp-text-proxy");

        // client handler
        wsHandler = new WebsocketHandler();

        // acceptor
        wsAcceptor = new NioSocketAcceptor();
        wsAcceptor.setHandler(wsHandler);
        wsAcceptor.setReuseAddress(true);

        // protocol filter
        IoFilter codecFilter = new ProtocolCodecFilter(new WebSocketCodecFactory());

        // setup filter chain
        wsAcceptor.getFilterChain().addLast("codec", codecFilter);
        wsAcceptor.getFilterChain().addLast("threadPool", new ExecutorFilter());

        String bindip = "";
        try {
            if (bindip.equals("")) {
                // no bind ip address supplied
                wsAcceptor.bind(new InetSocketAddress(5001));
            } else {
                wsAcceptor.bind(new InetSocketAddress(bindip, 5001));
            }
        } catch (IOException e) {
            LOGGER.error("Unable to bind", e);
        }
    }

    public void startup() {
        startWebsocketServer();
    }

    public static void main(String[] args) {
        new ProxyMain().startup();
    }

}
