package de.zeos.zen2.data;

import org.cometd.annotation.Listener;
import org.cometd.annotation.Service;
import org.cometd.annotation.Session;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;
import org.springframework.stereotype.Component;

@Service("custom")
@Component
public class CustomService {

    @Session
    private ServerSession serverSession;

    @Listener("/service/**")
    public void receiveOnCustomChannel(ServerSession remote, Mutable message) {
        String channel = message.getChannel();
        String[] parts = channel.substring(1).split("/", 3);
        if (parts.length >= 3 && parts[2].equals("custom")) {
            String app = parts[1];
            String rest = null;
            if (parts.length > 3)
                rest = parts[3];
            process(remote, message, app, rest);
        }
    }

    private void process(ServerSession remote, Mutable message, String app, String channelRest) {

    }

}
