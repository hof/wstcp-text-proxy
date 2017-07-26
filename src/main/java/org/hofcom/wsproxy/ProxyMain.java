package org.hofcom.wsproxy;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.red5.net.websocket.codec.WebSocketCodecFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ProxyMain {

    private WebsocketHandler wsHandler = null;

    private NioSocketAcceptor wsAcceptor =  null;

    private void startWebsocketServer() {

        // client handler
        wsHandler = new WebsocketHandler();

        // acceptor
        wsAcceptor = new NioSocketAcceptor();
        wsAcceptor.setHandler(wsHandler);

        SocketSessionConfig config = wsAcceptor.getSessionConfig();
        config.setReuseAddress(true);

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
            System.out.println("Unable to bind");
        }
    }

    public void startup() {
        startWebsocketServer();
    }

    public static void main(String[] args) {
        new ProxyMain().startup();
    }

}
