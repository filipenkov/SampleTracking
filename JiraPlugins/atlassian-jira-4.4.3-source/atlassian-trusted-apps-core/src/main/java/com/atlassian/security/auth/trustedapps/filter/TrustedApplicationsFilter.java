package com.atlassian.security.auth.trustedapps.filter;

import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.Null;
import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.security.auth.trustedapps.UserResolver;
import org.bouncycastle.util.encoders.Base64;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.PublicKey;

/**
 * This filter serves two purposes:
 * <ol>
 * <li>Authenticates requests from trusted applications if the right certificate is
 * present in the request.</li>
 * <li>Returns the UID and public key of this application upon request so other servers can
 * establish trusted relationship with this application as a client.</li>
 * </ol>
 * <p/>
 * For the first purpose, the filter will intercept any calls to a page '/admin/appTrustCertificate'. Directory
 * structure of the request will be ignored. The returned page will contain 2 lines:
 * <ul>
 * <li> ID </li>
 * <li> public key BASE64 encoded </li>
 * </ul>
 * <p/>
 * For the second purpose the following header parameters must be present and valid:
 * {@link CurrentApplication#HEADER_TRUSTED_APP_CERT} {@link CurrentApplication#HEADER_TRUSTED_APP_ID}
 * <p/>
 * If the authentication should fail a message will be set in the response header:
 * {@link CurrentApplication#HEADER_TRUSTED_APP_ERROR}
 */
public class TrustedApplicationsFilter implements Filter
{
    static final class Status
    {
        static final String ERROR = "ERROR";
        static final String OK = "OK";
    }

    private final CertificateServer certificateServer;
    private final Authenticator authenticator;

    private FilterConfig filterConfig = null;

    private final AuthenticationController authenticationController;
    private final AuthenticationListener authenticationListener;

    public TrustedApplicationsFilter(TrustedApplicationsManager appManager, UserResolver resolver, AuthenticationController authenticationController, AuthenticationListener authenticationListener)
    {
        this(new CertificateServerImpl(appManager), new TrustedApplicationFilterAuthenticator(appManager, resolver, authenticationController), authenticationController, authenticationListener);
    }
    
    protected TrustedApplicationsFilter(CertificateServer certificateServer, Authenticator authenticator, AuthenticationController authenticationController, AuthenticationListener authenticationListener)
    {
        Null.not("certificateServer", certificateServer);
        Null.not("authenticator", authenticator);
        Null.not("authenticationController", authenticationController);
        Null.not("authenticationListener", authenticationListener);

        this.certificateServer = certificateServer;
        this.authenticator = authenticator;
        this.authenticationController = authenticationController;
        this.authenticationListener = authenticationListener;
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
    {
        // if this is a certificate request serve the certificate back and return
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        if (getPathInfo(request).endsWith(TrustedApplicationUtils.Constant.CERTIFICATE_URL_PATH))
        {
            response.setContentType("text/plain");
            final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(response.getOutputStream());
            certificateServer.writeCertificate(outputStreamWriter);
            outputStreamWriter.flush();
            return;
        }

        final boolean isTrustedAppCall = authenticate(request, response);
        try
        {
            chain.doFilter(request, res);
        }
        finally
        {
            if (isTrustedAppCall && request.getSession(false) != null)
            {
                request.getSession().invalidate();
            }
        }
    }

    boolean authenticate(HttpServletRequest request, HttpServletResponse response)
    {
        if (authenticationController.shouldAttemptAuthentication(request))
        {
            final Authenticator.Result result = authenticator.authenticate(request, response);

            if (result.getStatus() == Authenticator.Result.Status.SUCCESS)
            {
                authenticationListener.authenticationSuccess(result, request, response);
                response.setHeader(TrustedApplicationUtils.Header.Response.STATUS, Status.OK);
                return true;
            }

            if (result.getStatus() == Authenticator.Result.Status.FAILED)
            {
                authenticationListener.authenticationFailure(result, request, response);
            }
            else
            {
                authenticationListener.authenticationError(result, request, response);
            }
        }
        else
        {
            authenticationListener.authenticationNotAttempted(request, response);
        }
        return false;
    }

    protected String getPathInfo(HttpServletRequest request)
    {
        String context = request.getContextPath();
        String uri = request.getRequestURI();
        if (context != null && context.length() > 0)
        {
            return uri.substring(context.length());
        }
        else
        {
            return uri;
        }
    }

    public void init(FilterConfig config)
    {
        this.filterConfig = config;
    }

    public void destroy()
    {
        filterConfig = null;
    }

    /**
     * @deprecated Not needed in latest version of Servlet 2.3 API
     */
    public FilterConfig getFilterConfig()
    {
        return filterConfig;
    }

    /**
     * @deprecated Not needed in latest version of Servlet 2.3 API - replaced by init().
     */
    public void setFilterConfig(FilterConfig filterConfig)
    {
        if (filterConfig != null) // it seems that Orion 1.5.2 calls this with a null config.
        {
            init(filterConfig);
        }
    }

    /**
     * serve the CurrentApplication's certificate
     */
    public interface CertificateServer
    {
        void writeCertificate(Writer writer) throws IOException;
    }

    public static class CertificateServerImpl implements CertificateServer
    {
        final TrustedApplicationsManager appManager;

        public CertificateServerImpl(TrustedApplicationsManager appManager)
        {
            this.appManager = appManager;
        }

        public void writeCertificate(Writer writer) throws IOException
        {
            CurrentApplication currentApplication = appManager.getCurrentApplication();
            PublicKey publicKey = currentApplication.getPublicKey();

            try
            {
                writer.write(currentApplication.getID());
                writer.write("\n");

                byte[] key = publicKey.getEncoded();
                writer.write(new String(Base64.encode(key), TrustedApplicationUtils.Constant.CHARSET_NAME));
                writer.write("\n");
                writer.write(TrustedApplicationUtils.Constant.VERSION.toString());
                writer.write("\n");
                writer.write(TrustedApplicationUtils.Constant.MAGIC);
                writer.flush();
            }
            catch (UnsupportedEncodingException ex)
            {
                throw new AssertionError(ex);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}