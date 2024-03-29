package com.objectcode.lostsocks.client.config;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Realm;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicConfiguration implements IConfiguration {
    protected AsyncHttpClient httpClient;

    protected List<SimpleWildcard> localNetworks = new ArrayList<SimpleWildcard>();

    protected boolean configurationChanged = false;

    @Override
    public AsyncHttpClient createHttpClient() {
        if (httpClient == null) {
            synchronized (this) {
                AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();

                if (isUseProxy()) {
                    ProxyServer proxyServer = new ProxyServer(getProxyHost(), Integer.parseInt(getProxyPort()), getProxyUser(), getProxyPassword());

                    configBuilder = configBuilder.setProxyServer(proxyServer);
                }
                configBuilder.setMaxRequestRetry(3);
                configBuilder.setAllowPoolingConnection(true);
                configBuilder.setAllowSslConnectionPool(true);
                NettyAsyncHttpProvider provider = new NettyAsyncHttpProvider(configBuilder.build());
                httpClient = new AsyncHttpClient(provider, configBuilder.build());
            }
        }

        return httpClient;
    }

    @Override
    public Realm getRealm() {
        Realm.RealmBuilder builder = new Realm.RealmBuilder();
        builder.setScheme(Realm.AuthScheme.BASIC);
        builder.setPrincipal(getUser());
        builder.setPassword(getPassword());
        builder.setUsePreemptiveAuth(true);

        return builder.build();
    }

    @Override
    public List<SimpleWildcard> getLocalNetworks() {
        return localNetworks;
    }

    @Override
    public void addLocalNetwork(SimpleWildcard wildcard) {
        localNetworks.add(wildcard);

        configurationChanged = true;
    }

    @Override
    public void removeLocalNetwork(int index) {
        localNetworks.remove(index);

        configurationChanged = true;
    }
}
