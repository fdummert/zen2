package de.zeos.cometd.security;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.springframework.stereotype.Component;

import de.zeos.cometd.Configurer;

/**
 * @author ECT\dumer-fl
 * @version $LastChangedRevision$
 * 
 */
@Component
public class AuthSecurityPolicy implements SecurityPolicy {

    public static final String AUTH_KEY = Configurer.ZEOS_KEY + ".security";
    public static final String APP_KEY = Configurer.ZEOS_KEY + ".application";
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
            ServerMessage.Mutable handshakeReply = message.getAssociated();
            ext = handshakeReply.getExt(true);
            ext.put(AUTH_KEY, auth);
            ext.put(APP_KEY, app);
        } catch (AuthenticationException e) {
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
        channel = getBestMatchingChannel(auth.getChannels(), channel);
        return channel != null;
    }

    private String getBestMatchingChannel(Set<String> channels, String channel) {
        ChannelId candidate = null;
        String candidateScope = null;
        for (String c : channels) {
            int scopePos = c.indexOf("*/");
            String scope = null;
            if (scopePos > 0) {
                scope = c.substring(scopePos + 1);
                c = c.substring(0, scopePos + 1);
            }
            ChannelId ch = new ChannelId(c);
            if (matches(ch, scope, new ChannelId(channel))) {
                if (candidate == null || (!ch.isWild() && candidate.isWild()) || (ch.isWild() && candidate.isDeepWild()) || ch.depth() > candidate.depth() || depth(scope) > depth(candidateScope)) {
                    candidate = ch;
                    candidateScope = scope;
                }
            }
        }
        return candidate == null ? null : candidate.toString() + (candidateScope == null ? "" : candidateScope);
    }

    private boolean matches(ChannelId permissionChannel, String permissionScope, ChannelId concreteChannel) {
        if (permissionScope != null) {
            String concreteStr = concreteChannel.toString();
            return concreteStr.endsWith(permissionScope) && permissionChannel.matches(new ChannelId(concreteStr.substring(0, concreteStr.length() - permissionScope.length())));
        }
        if (permissionChannel.matches(concreteChannel))
            return true;
        if (permissionChannel.isDeepWild() && concreteChannel.isWild()) {
            String permissionPrefix = (permissionChannel.getParent() == null ? "" : permissionChannel.getParent()) + "/";
            String concretePrefix = (concreteChannel.getParent() == null ? "" : concreteChannel.getParent()) + "/";
            if (concretePrefix.startsWith(permissionPrefix))
                return true;
        }
        return false;
    }

    private int depth(String scope) {
        return scope == null ? 0 : scope.split("/").length;
    }
}
