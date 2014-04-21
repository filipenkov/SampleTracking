package com.atlassian.gadgets.directory.internal;

import com.atlassian.gadgets.util.HttpTimeoutsProvider;
import com.atlassian.sal.api.ApplicationProperties;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.cache.MemoryCacheStorage;
import org.codehaus.httpcache4j.resolver.HTTPClientResponseResolver;
import org.springframework.beans.factory.DisposableBean;

public class HttpCache extends HTTPCache implements DisposableBean
{
    private final HttpClient client;
    private final HttpTimeoutsProvider httpTimeoutsProvider;

    public HttpCache(final ApplicationProperties applicationProperties)
    {
        super(new MemoryCacheStorage());
        this.httpTimeoutsProvider = new HttpTimeoutsProvider(applicationProperties);
        this.client = createHttpClient();
        setResolver(new HTTPClientResponseResolver(client));
    }

    private HttpClient createHttpClient()
    {
        SchemeRegistry registry = createRegistry();
        HttpParams params = createHttpParams();
        ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, registry)
        {
            @Override
            protected void finalize() throws Throwable
            {
                // prevent the ThreadSafeClientConnManager from logging - this causes exceptions due to
                // the ClassLoader probably having been removed when the plugin shuts down.  Added a
                // PluginEventListener to make sure the shutdown method is called while the plugin classloader
                // is stilll active.
            }
        };
        return new DefaultHttpClient(connectionManager, params);
    }

    private SchemeRegistry createRegistry()
    {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(
                new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(
                new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        return registry;
    }

    private HttpParams createHttpParams()
    {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params,
                HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params,
                "UTF-8");
        HttpProtocolParams.setUseExpectContinue(params,
                true);
        HttpConnectionParams.setTcpNoDelay(params,
                true);
        HttpConnectionParams.setSocketBufferSize(params,
                8192);
        HttpProtocolParams.setUserAgent(params,
                "Atlassian-Gadgets-HttpClient");
        HttpConnectionParams.setSoTimeout(params, httpTimeoutsProvider.getSocketTimeout());
        HttpConnectionParams.setConnectionTimeout(params, httpTimeoutsProvider.getConnectionTimeout());
        return params;
    }

    public void destroy() {
        client.getConnectionManager().shutdown();
    }

}
