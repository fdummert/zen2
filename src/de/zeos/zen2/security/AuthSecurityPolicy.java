package de.zeos.zen2.security;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.springframework.stereotype.Component;

import de.zeos.zen2.Configurer;

/**
 * @author ECT\dumer-fl
 * @version $LastChangedRevision$
 * 
 */
@Component
public class AuthSecurityPolicy implements SecurityPolicy {

    public static final String AUTH_KEY = Configurer.ZEOS_KEY + ".security";
    public static final String APP_KEY = Configurer.ZEOS_KEY + ".application";
    public static final String CHANNELS_KEY = Configurer.ZEOS_KEY + ".channels";
    public static final String SERVICE_CHANNEL = "/service/";
    public static final String APP_CHANNEL = "/app/";
    public static final String PUBLIC_CHANNEL = "/public/";

    @Inject
    private SecurityHandler securityHandler;

    @Override
    public boolean canHandshake(BayeuxServer server, ServerSession session, ServerMessage message) {
        if (session.isLocalSession())
            return true;
        Map<String, Object> ext = message.getExt();
        try {
            String app = (String) ext.get(APP_KEY);
            @SuppressWarnings("unchecked")
            Authorization auth = this.securityHandler.authenticate(app, (Map<String, Object>) ext.get(AUTH_KEY));
            session.setAttribute(AUTH_KEY, auth);
            session.setAttribute(APP_KEY, app);
            Set<Pattern> channelPatterns = new HashSet<Pattern>();
            for (String s : auth.getChannels())
                channelPatterns.add(Pattern.compile(s));
            session.setAttribute(CHANNELS_KEY, channelPatterns);
            ServerMessage.Mutable handshakeReply = message.getAssociated();
            ext = handshakeReply.getExt(true);
            ext.put(AUTH_KEY, auth);
            ext.put(APP_KEY, app);
        } catch (AuthenticationException e) {
            String msg = e.getMessage();
            if (msg != null) {
                ServerMessage.Mutable handshakeReply = message.getAssociated();
                handshakeReply.put("exception", msg);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean canCreate(BayeuxServer server, ServerSession session, String channelId, ServerMessage message) {
        if (session == null)
            return false;
        if (session.isLocalSession())
            return true;
        if (ChannelId.isMeta(channelId))
            return false;
        return isAllowed(session, channelId);
    }

    @Override
    public boolean canPublish(BayeuxServer server, ServerSession session, ServerChannel channel, ServerMessage message) {
        if (!session.isHandshook())
            return false;
        if (session.isLocalSession())
            return true;
        if (channel.isMeta())
            return false;
        return isAllowed(session, channel.toString());
    }

    @Override
    public boolean canSubscribe(BayeuxServer server, ServerSession session, ServerChannel channel, ServerMessage message) {
        if (session.isLocalSession())
            return true;
        if (channel.isMeta())
            return false;
        return isAllowed(session, channel.toString());
    }

    private boolean isAllowed(ServerSession session, String channel) {
        if (!channel.startsWith(SERVICE_CHANNEL) && !channel.startsWith(APP_CHANNEL) && !channel.startsWith(PUBLIC_CHANNEL))
            return false;
        Authorization auth = (Authorization) session.getAttribute(AUTH_KEY);
        if (auth.getChannels().contains(channel))
            return true;
        @SuppressWarnings("unchecked")
        Set<Pattern> patterns = (Set<Pattern>) session.getAttribute(CHANNELS_KEY);
        for (Pattern p : patterns) {
            if (p.matcher(channel).matches())
                return true;
        }
        return false;
    }
}
