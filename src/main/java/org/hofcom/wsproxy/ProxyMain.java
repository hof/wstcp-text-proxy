package org.hofcom.wsproxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.red5.net.websocket.SecureWebSocketConfiguration;
import org.red5.net.websocket.codec.WebSocketCodecFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class ProxyMain {

    public static final Logger LOGGER = LogManager.getLogger(ProxyMain.class);

    private WebsocketHandler wsHandler = null;

    private NioSocketAcceptor wsAcceptor =  null;

    // --- configuration

    @Parameter(names = {"--keystore","-ksf"}, description = "Key store filename")
    private String keystoreFile = "wstcp.jks";

    @Parameter(names = {"--keystore-password","-ksp"}, description = "Key store password")
    private String keystorePassword = "password";

    @Parameter(names = {"--truststore","-tsf"}, description = "Trust store filename")
    private String truststoreFile = "wstcp.jks";

    @Parameter(names = {"--truststore-password","-tsp"}, description = "Trust store password")
    private String truststorePassword = "password";

    @Parameter(names = {"--bind-ip","-ip"}, description = "IP address to listen on")
    private String bindip = "0.0.0.0";

    @Parameter(names = {"--bind-port","-p"}, description = "IP port to listen on")
    private Integer bindport = 5002;

    @Parameter(names = {"--forward-host","-fh"}, description = "TCP hostname/IP to forward to")
    private String hostname = "127.0.0.1";

    @Parameter(names = {"--forward-port","-fp"}, description = "TCP port to forward to")
    private Integer port = 5000;

    @Parameter(names = {"--log","-l"}, description = "Log the data")
    private boolean logData = false;

    @Parameter(names = {"--ping","-p"}, description = "Data to ping inactive TCP connection with")
    private String pingData = "{\"class\":\"ping\"}";

    private void startWebsocketServer() {
        LOGGER.info("Starting wstcp-text-proxy");

        // client handler
        wsHandler = new WebsocketHandler(hostname, port, logData, new TcpHandler(logData, pingData));

        // acceptor
        wsAcceptor = new NioSocketAcceptor();
        wsAcceptor.setHandler(wsHandler);
        wsAcceptor.setReuseAddress(true);

        // SSL configuration
        SecureWebSocketConfiguration config = new SecureWebSocketConfiguration();

        config.setKeystoreFile(keystoreFile);
        config.setKeystorePassword(keystorePassword);
        config.setTruststoreFile(truststoreFile);
        config.setTruststorePassword(truststorePassword);
        SslFilter sslFilter = null;
        try {
            sslFilter = config.getSslFilter();
        } catch (Exception ex) {
            LOGGER.error(ex);
            return;
        }

        // protocol filter
        IoFilter codecFilter = new ProtocolCodecFilter(new WebSocketCodecFactory());

        // setup filter chain
        wsAcceptor.getFilterChain().addLast("sslFilter", sslFilter);
        wsAcceptor.getFilterChain().addLast("codec", codecFilter);
        wsAcceptor.getFilterChain().addLast("threadPool", new ExecutorFilter());

        try {
                wsAcceptor.bind(new InetSocketAddress(bindip, bindport));
        } catch (IOException e) {
            LOGGER.error("Unable to bind", e);
        }
    }

    public static void main(String... argv) {
        ProxyMain main = new ProxyMain();
        JCommander.newBuilder()
            .addObject(main)
            .build()
            .parse(argv);
        main.startWebsocketServer();
    }
}