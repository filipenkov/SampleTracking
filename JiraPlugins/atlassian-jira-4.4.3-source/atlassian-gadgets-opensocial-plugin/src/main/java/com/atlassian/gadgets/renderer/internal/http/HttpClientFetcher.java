package com.atlassian.gadgets.renderer.internal.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;

import com.atlassian.gadgets.opensocial.spi.Whitelist;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.shindig.gadgets.http.HttpCache;
import org.apache.shindig.gadgets.http.HttpCacheKey;
import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.http.HttpResponseBuilder;

/**
 * Implementation of a {@link HttpFetcher} using {@link HttpClient}.
 */
@Singleton
public class HttpClientFetcher implements HttpFetcher {
  private static final int CONNECT_TIMEOUT_MS = 5000;

  private final Log log = LogFactory.getLog(HttpClientFetcher.class);

  private final HttpCache cache;
  private final Whitelist whitelist;

  @Inject
  public HttpClientFetcher(HttpCache cache, Whitelist whitelist) {
    this.cache = cache;
    this.whitelist = whitelist;
  }

  /** {@inheritDoc} */
  public HttpResponse fetch(HttpRequest request) {
    if (!whitelist.allows(request.getUri().toJavaUri())) {
      log.warn("A request to " + request.getUri() +
          " has been denied.  To allow requests to this URL add the application URL to your whitelist (http://confluence.atlassian.com/x/KQfCDQ ).");
      return new HttpResponseBuilder().
          setHttpStatusCode(HttpResponse.SC_FORBIDDEN).
          setHeader("Content-Type", "text/plain").
          setResponseString("Requests to " + request.getUri() + " are not allowed.  See your administrator about configuring a whitelist entry for this destination (http://confluence.atlassian.com/x/KQfCDQ ).").
          create();
    }
    HttpCacheKey cacheKey = new HttpCacheKey(request);
    HttpResponse response = cache.getResponse(cacheKey, request);
    if (response != null) {
      return response;
    }
    try {
      HttpUriRequest hcRequest = newHcRequest(request);
      if (request.getPostBodyLength() > 0) {
        ((HttpEntityEnclosingRequest) hcRequest).setEntity(new InputStreamEntity(request.getPostBody(), request.getPostBodyLength()));
      }
      org.apache.http.HttpResponse hcResponse;
      try {
        // first try with TLS, the most common SSL protocol
        hcResponse = newHttpClient("TLSv1", request).execute(hcRequest);
      } catch (SSLException e) {
          log.debug("SSL Exception establishing connection with TLSv1 protocol. Falling back to SSLv3.", e);
          // if TLS failed, try with SSLv3
          hcResponse = newHttpClient("SSLv3", request).execute(hcRequest);
      }
      response = makeResponse(hcResponse);
      return cache.addResponse(cacheKey, request, response);
    } catch (IOException e) {
      if (e instanceof java.net.SocketTimeoutException || e instanceof java.net.SocketException) {
          return HttpResponse.timeout();
      } else {
          log.error("Unable to retrieve response", e);
      }
      return HttpResponse.error();
    }
  }

  private HttpClient newHttpClient(String sslProtocol, HttpRequest request)
  {
      HttpParams params = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT_MS);
      //AG-1059: Need to follow redirects to ensure that if the host app is setup with Apache and mod_jk we can still
      //fetch internal gadgets.
      if(request.getFollowRedirects())
      {
          params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
          params.setIntParameter(ClientPNames.MAX_REDIRECTS, 3);
      }
      params.setParameter(ClientPNames.DEFAULT_HEADERS, ImmutableSet.of(new BasicHeader("Accept-Encoding", "gzip, deflate")));
      DefaultHttpClient client = new DefaultHttpClient(params);
      //AG-1044: Need to use the JVM's default proxy configuration for requests.
      ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
              client.getConnectionManager().getSchemeRegistry(),
              ProxySelector.getDefault());
      client.setRoutePlanner(routePlanner);

      client.addResponseInterceptor(new GzipDeflatingInterceptor());
      client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", new CustomSSLSocketFactory(sslProtocol), 443));
      return client;
  }

  private HttpUriRequest newHcRequest(HttpRequest request) throws IOException {
    URI uri = request.getUri().toJavaUri();
    HttpUriRequest httpUriRequest = HttpMethod.valueOf(request.getMethod()).newMessage(uri);

    for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
      httpUriRequest.addHeader(entry.getKey(), StringUtils.join(entry.getValue(), ','));
    }
    return httpUriRequest;
  }

  private HttpResponse makeResponse(org.apache.http.HttpResponse hcResponse) throws IOException {
    HttpResponseBuilder builder = new HttpResponseBuilder();
    for (Header header : hcResponse.getAllHeaders()) {
        builder.addHeader(header.getName(), header.getValue());
    }

    return builder
        .setHttpStatusCode(hcResponse.getStatusLine().getStatusCode())
        .setResponse(IOUtils.toByteArray(hcResponse.getEntity().getContent()))
        .create();
  }

  /**
   * Creates SSLSockets that don't use the SSLv2Hello protocol.  This is primarily to work around TripIt.coms SSL...
   * quirks.
   */
  private static final class CustomSSLSocketFactory implements LayeredSocketFactory {
    private final LayeredSocketFactory factory = SSLSocketFactory.getSocketFactory();
    private final String protocol;

    public CustomSSLSocketFactory(String protocol) {
      this.protocol = protocol;
    }

    public Socket createSocket() throws IOException {
      SSLSocket socket = (SSLSocket) factory.createSocket();
      socket.setEnabledProtocols(new String[] { protocol });
      return socket;
    }

    public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort, HttpParams params)
        throws IOException, UnknownHostException, ConnectTimeoutException {
      return factory.connectSocket(sock, host, port, localAddress, localPort, params);
    }

    public boolean isSecure(Socket sock) throws IllegalArgumentException {
      return factory.isSecure(sock);
    }

    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
      return factory.createSocket(socket, host, port, autoClose);
    }
  }

  private static final class GzipDeflatingInterceptor implements HttpResponseInterceptor {
    public void process(final org.apache.http.HttpResponse response, final HttpContext context) throws HttpException, IOException {
      HttpEntity entity = response.getEntity();
      Header ceheader = entity.getContentEncoding();
      if (ceheader != null) {
        HeaderElement[] codecs = ceheader.getElements();
        for (int i = 0; i < codecs.length; i++) {
          if (codecs[i].getName().equalsIgnoreCase("gzip")) {
            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
            return;
          } else if (codecs[i].getName().equalsIgnoreCase("deflate")) {
            response.setEntity(new InflatingEntity(response.getEntity()));
            return;
          }
        }
      }
    }
  }

  private static abstract class InputStreamDecoratingHttpEntity extends HttpEntityWrapper {
    public InputStreamDecoratingHttpEntity(HttpEntity wrapped) {
      super(wrapped);
    }

    @Override
    public final InputStream getContent() throws IOException, IllegalStateException {
      // the wrapped entity's getContent() decides about repeatability
      return decorate(wrappedEntity.getContent());
    }

    @Override
    public final long getContentLength() {
      // length of ungzipped content is not known
      return -1;
    }

    protected abstract InputStream decorate(InputStream is) throws IOException;
  }

  private static final class GzipDecompressingEntity extends InputStreamDecoratingHttpEntity {
    public GzipDecompressingEntity(final HttpEntity entity) {
      super(entity);
    }

    @Override
    protected InputStream decorate(InputStream is) throws IOException {
      return new GZIPInputStream(is);
    }
  }

  private static final class InflatingEntity extends InputStreamDecoratingHttpEntity {
    public InflatingEntity(final HttpEntity entity) {
      super(entity);
    }

    @Override
    protected InputStream decorate(InputStream is) throws IOException {
      return new InflaterInputStream(is, new Inflater(true));
    }
  }

  private static enum HttpMethod {
    GET, POST, DELETE, PUT, HEAD, OPTIONS, TRACE;

    public HttpUriRequest newMessage(URI uri) {
      switch (this) {
        case GET:     return new HttpGet(uri);
        case POST:    return new HttpPost(uri);
        case DELETE:  return new HttpDelete(uri);
        case PUT:     return new HttpPut(uri);
        case HEAD:    return new HttpHead(uri);
        case OPTIONS: return new HttpOptions(uri);
        case TRACE:   return new HttpTrace(uri);
      }
      throw new IllegalStateException("Just not possible");
    }
  }
}