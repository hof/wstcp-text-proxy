package org.hofcom.wsproxy;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.textline.TextLineDecoder;
import org.apache.mina.filter.codec.textline.TextLineEncoder;

public class TcpCodecFactory implements ProtocolCodecFactory {

    private final ProtocolEncoder encoder;

    private final ProtocolDecoder decoder;

    public TcpCodecFactory() {
        encoder = new TextLineEncoder();
        decoder = new TextLineDecoder();

        ((TextLineEncoder)encoder).setMaxLineLength(1024*1024*4);
        ((TextLineDecoder)decoder).setMaxLineLength(1024*1024*4);
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }
}
