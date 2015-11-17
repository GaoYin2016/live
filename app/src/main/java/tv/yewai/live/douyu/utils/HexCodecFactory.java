package tv.yewai.live.douyu.utils;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class HexCodecFactory  implements ProtocolCodecFactory {

    public ProtocolDecoder getDecoder(IoSession session)
            throws Exception {
        ProtocolDecoder pd = new ProtocolDecoder() {
            public void decode(IoSession session, IoBuffer ioBuffer, ProtocolDecoderOutput out)  throws Exception {
                out.write(HexUtils.ioBufferToHexString(ioBuffer));
            }

            public void dispose(IoSession session) throws Exception {
            }

            public void finishDecode(IoSession session, ProtocolDecoderOutput arg1) throws Exception {
            }
        };
        return pd;
    }

    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        ProtocolEncoder pe = new ProtocolEncoder() {
            public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
                out.write(HexUtils.hexString2IoBuffer(message.toString()));
            }

            public void dispose(IoSession session) throws Exception {
            }
        };
        return pe;
    }
}
