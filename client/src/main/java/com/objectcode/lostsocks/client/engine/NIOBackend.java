package com.objectcode.lostsocks.client.engine;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NIOBackend {
    public static NIOBackend INSTANCE = new NIOBackend();

    private final Executor bossExecutor = Executors.newCachedThreadPool();
    private final Executor workerExecutor = Executors.newCachedThreadPool();
    private final Timer timer = new HashedWheelTimer();

    public Timer getTimer() {
        return timer;
    }

    public ClientBootstrap createClient() {
        return new ClientBootstrap(
                new NioClientSocketChannelFactory(bossExecutor, workerExecutor));
    }

    public ServerBootstrap createServer() {
        return new ServerBootstrap(
                new NioServerSocketChannelFactory(bossExecutor, workerExecutor));
    }
}
