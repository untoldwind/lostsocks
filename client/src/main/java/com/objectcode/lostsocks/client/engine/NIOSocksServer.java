package com.objectcode.lostsocks.client.engine;

import com.objectcode.lostsocks.client.config.IConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class NIOSocksServer {
    private static final Log log = LogFactory.getLog(NIOGenericServer.class);

    private final ServerBootstrap bootstrap;

    private final IConfiguration configuration;

    private Channel binding;

    public NIOSocksServer(IConfiguration configuration) {
        this.configuration = configuration;

        bootstrap = NIOBackend.INSTANCE.getServer();
    }

    public void start() {
        if (configuration.isListenOnlyLocalhost()) {
            try {
                binding = bootstrap.bind(new InetSocketAddress(InetAddress.getLocalHost(), configuration.getSocksPort()));
            } catch (UnknownHostException e) {
                log.error("Failed to bind to localhost", e);
            }
        }
        if (binding == null) {
            binding = bootstrap.bind(new InetSocketAddress(configuration.getSocksPort()));
        }

    }
}
