package de.zeos.zen2;

import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.cometd.annotation.ServerAnnotationProcessor;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerTransport;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.transport.JSONTransport;
import org.cometd.websocket.server.JettyWebSocketTransport;
import org.cometd.websocket.server.WebSocketTransport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import de.zeos.zen2.security.AuthSecurityPolicy;

@Component
public class Configurer implements DestructionAwareBeanPostProcessor, ServletContextAware {

    public static final String ZEOS_KEY = "de.zeos.zen2";

    @Inject
    private BayeuxServerImpl bayeuxServer;
    @Inject
    private AuthSecurityPolicy policy;
    private ServerAnnotationProcessor processor;

    @PostConstruct
    private void init() {
        this.bayeuxServer.setSecurityPolicy(this.policy);
        this.processor = new ServerAnnotationProcessor(this.bayeuxServer);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
        this.processor.processDependencies(bean);
        this.processor.processConfigurations(bean);
        this.processor.processCallbacks(bean);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String name) throws BeansException {
        this.processor.deprocessCallbacks(bean);
    }

    @Bean
    public JacksonJSONContext jacksonJSONContext() {
        return new JacksonJSONContext();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public BayeuxServerImpl bayeuxServer() {
        return new BayeuxServerImpl();
    }

    @Override
    public void setServletContext(ServletContext servletContext) {

        JettyWebSocketTransport wsTransport = new JettyWebSocketTransport(this.bayeuxServer);
        wsTransport.setOption(WebSocketTransport.MAX_MESSAGE_SIZE_OPTION, 16000000);
        wsTransport.setOption(WebSocketTransport.JSON_CONTEXT_OPTION, jacksonJSONContext());

        this.bayeuxServer.setTransports(new ArrayList<ServerTransport>(Arrays.asList(wsTransport, new JSONTransport(this.bayeuxServer))));
        servletContext.setAttribute(BayeuxServer.ATTRIBUTE, this.bayeuxServer);
    }
}