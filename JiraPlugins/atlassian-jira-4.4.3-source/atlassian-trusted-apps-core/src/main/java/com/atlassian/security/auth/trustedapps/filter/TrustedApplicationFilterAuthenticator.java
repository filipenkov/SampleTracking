package com.atlassian.security.auth.trustedapps.filter;

import java.security.Principal;
import java.security.PublicKey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.security.auth.trustedapps.ApplicationCertificate;
import com.atlassian.security.auth.trustedapps.DefaultEncryptedCertificate;
import com.atlassian.security.auth.trustedapps.InvalidCertificateException;
import com.atlassian.security.auth.trustedapps.filter.RequestSignatureTool.UnableToVerifySignatureException;
import com.atlassian.security.auth.trustedapps.TransportErrorMessage;
import com.atlassian.security.auth.trustedapps.TrustedApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.security.auth.trustedapps.UserResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustedApplicationFilterAuthenticator implements Authenticator
{
    private static final Logger log = LoggerFactory.getLogger(TrustedApplicationFilterAuthenticator.class);

    final TrustedApplicationsManager appManager;
    final UserResolver resolver;
    final AuthenticationController authenticationController;

    public TrustedApplicationFilterAuthenticator(TrustedApplicationsManager appManager, UserResolver resolver, AuthenticationController authenticationController)
    {
        this.appManager = appManager;
        this.resolver = resolver;
        this.authenticationController = authenticationController;
    }
    
    private static boolean atLeast(Integer protocolVersion, int required)
    {
        return protocolVersion != null && protocolVersion.intValue() >= required;
    }
    
    public Result authenticate(HttpServletRequest request, HttpServletResponse response)
    {
        final String certStr = request.getHeader(TrustedApplicationUtils.Header.Request.CERTIFICATE);
        if (isBlank(certStr))
        {
            return new Result.NoAttempt();
        }

        final String id = request.getHeader(TrustedApplicationUtils.Header.Request.ID);
        if (isBlank(id))
        {
            final Result.Error result = new Result.Error(new TransportErrorMessage.ApplicationIdNotFoundInRequest());
            setFailureHeader(response, result.getMessage());
            return result;
        }

        final String key = request.getHeader(TrustedApplicationUtils.Header.Request.SECRET_KEY);
        if (isBlank(key))
        {
            final Result.Error result = new Result.Error(new TransportErrorMessage.SecretKeyNotFoundInRequest());
            setFailureHeader(response, result.getMessage());
            return result;
        }

        final String magicNumber = request.getHeader(TrustedApplicationUtils.Header.Request.MAGIC);
        // magic number validation is only done from protocol version 2, version 1 had no version header
        final String version = request.getHeader(TrustedApplicationUtils.Header.Request.VERSION);
        final Integer protocolVersion;
        try
        {
            protocolVersion = (!isBlank(version)) ? Integer.parseInt(version) : null;
        }
        catch (NumberFormatException e)
        {
            final Result.Error result = new Result.Error(new TransportErrorMessage.BadProtocolVersion(version));
            setFailureHeader(response, result.getMessage());
            return result;
        }

        if (atLeast(protocolVersion, 1))
        {
            if (isBlank(magicNumber))
            {
                final Result.Error result = new Result.Error(new TransportErrorMessage.MagicNumberNotFoundInRequest());
                setFailureHeader(response, result.getMessage());
                return result;
            }
        }

        TrustedApplication app = appManager.getTrustedApplication(id);
        if (app == null)
        {
            final Result.Failure result = new Result.Failure(new TransportErrorMessage.ApplicationUnknown(id));
            setFailureHeader(response, result.getMessage());
            return result;
        }

        final ApplicationCertificate certificate;
        try
        {
            certificate = app.decode(new DefaultEncryptedCertificate(id, key, certStr, protocolVersion, magicNumber), request);
        }
        catch (InvalidCertificateException ex)
        {
            log.warn("Failed to login trusted application: " + app.getID() + " due to: " + ex);
            // debug for stacktrace, no need for isDebugEnabled check as there is no string concatenation
            log.debug("Failed to login trusted application cause", ex);
            final Result.Error result = new Result.Error(ex.getTransportErrorMessage());
            setFailureHeader(response, result.getMessage());
            return result;
        }
        
        String signedRequestUrl;
        
        final String signature = request.getHeader(TrustedApplicationUtils.Header.Request.SIGNATURE);
        
        if (atLeast(protocolVersion, 2) && signature == null)
        {
            final Result.Error result = new Result.Error(new TransportErrorMessage.BadSignature());
            setFailureHeader(response, result.getMessage());
            return result;
        }
        
        if (signature != null)
        {
            String expectedSignedUrl;
            
            StringBuffer sb = request.getRequestURL();
            String q = request.getQueryString();
            if (q != null)
            {
                sb.append('?');
                sb.append(q);
            }
            
            expectedSignedUrl = sb.toString();
            
            try
            {
                PublicKey publicKey = app.getPublicKey();
                if (new RequestSignatureTool().verify(certificate.getCreationTime().getTime(), expectedSignedUrl, publicKey, signature))
                {
                    signedRequestUrl = expectedSignedUrl;
                }
                else
                {
                    log.warn("Failed to login trusted application: " + app.getID() + " due to bad URL signature.");
                    
                    final Result.Error result = new Result.Error(new TransportErrorMessage.BadSignature(expectedSignedUrl));
                    setFailureHeader(response, result.getMessage());
                    return result;
                }
            }
            catch (UnableToVerifySignatureException e)
            {
                log.warn("Failed to login trusted application: " + app.getID() + " due to: " + e);
                
                final Result.Error result = new Result.Error(new TransportErrorMessage.BadSignature(expectedSignedUrl));
                setFailureHeader(response, result.getMessage());
                return result;
            }
        }
        else
        {
            signedRequestUrl = null;
        }
        
        // Allow a request past this point without a signed URL
        
        final Principal user = resolver.resolve(certificate);
        if (user == null)
        {
            log.warn("User '" + certificate.getUserName() + "' referenced by trusted application: '" + app.getID() + "' is not found.");
            final Result.Failure result = new Result.Failure(new TransportErrorMessage.UserUnknown(certificate.getUserName()));
            setFailureHeader(response, result.getMessage());
            return result;
        }
        else if (!authenticationController.canLogin(user, request))
        {
            // user exists but is not allowed to login
            log.warn("User '" + certificate.getUserName() + "' referenced by trusted application: '" + app.getID() + "' cannot login.");
            final Result.Failure result = new Result.Failure(new TransportErrorMessage.PermissionDenied());
            setFailureHeader(response, result.getMessage());
            return result;
        }

        if (signedRequestUrl != null)
        {
            return new Result.Success(user, signedRequestUrl);
        }
        else
        {
            return new Result.Success(user);
        }
    }

    private static void setFailureHeader(HttpServletResponse response, String message)
    {
        response.setHeader(TrustedApplicationUtils.Header.Response.STATUS, TrustedApplicationsFilter.Status.ERROR);
        response.addHeader(TrustedApplicationUtils.Header.Response.ERROR, message);
        if (log.isDebugEnabled())
        {
            log.debug(message, new RuntimeException(message));
        }
    }

    private static boolean isBlank(String input)
    {
        return (input == null) || input.trim().length() == 0;
    }
}