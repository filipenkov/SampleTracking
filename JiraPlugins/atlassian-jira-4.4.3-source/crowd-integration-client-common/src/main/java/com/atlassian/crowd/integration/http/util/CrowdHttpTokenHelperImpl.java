package com.atlassian.crowd.integration.http.util;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.integration.Constants;
import com.atlassian.crowd.model.authentication.CookieConfiguration;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.security.cookie.HttpOnlyCookies;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Helper class for Crowd SSO token operations.
 */
public class CrowdHttpTokenHelperImpl implements CrowdHttpTokenHelper
{
    private final static Logger LOGGER = Logger.getLogger(CrowdHttpTokenHelperImpl.class);
    
    private final CrowdHttpValidationFactorExtractor validationFactorExtractor;

    private CrowdHttpTokenHelperImpl(CrowdHttpValidationFactorExtractor validationFactorExtractor)
    {
        this.validationFactorExtractor = validationFactorExtractor;
    }

    public String getCrowdToken(HttpServletRequest request, String tokenName)
    {
        Validate.notNull(request);
        Validate.notNull(tokenName);
        boolean debugEnabled = (LOGGER != null && LOGGER.isDebugEnabled());
        // NOTE: we don't look in the session anymore as we are going to rely on
        // the user supplied cookie (as other authentications may change the cookie
        // and the cookie and session may get out of sync).

        // check request object for token
        // this will exist if an authenticate call is made on the same request as an isAuthenticated call

        if (debugEnabled)
        {
            LOGGER.debug("Checking for a SSO token that will need to be verified by Crowd.");
        }

        String token = (String) request.getAttribute(tokenName);

        // if there is no token in the request attributes, check cookies
        if (token == null)
        {
            if (debugEnabled)
            {
                LOGGER.debug("No request attribute token could be found, now checking the browser submitted cookies.");
            }

            // check the cookies
            Cookie cookies[] = request.getCookies();
            if (cookies != null && cookies.length > 0)
            {
                for (Cookie cookie : cookies)
                {
                    if (debugEnabled)
                    {
                        LOGGER.debug("Cookie name/value: " + cookie.getName() + " / " + cookie.getValue());
                    }

                    if (tokenName.equals(cookie.getName()) && cookie.getValue() != null)
                    {
                        if (debugEnabled)
                        {
                            LOGGER.debug("Accepting the SSO cookie value: " + cookie.getValue());
                        }

                        token = cookie.getValue();

                        break;
                    }
                }
            }
        }

        if (debugEnabled)
        {
            if (token == null)
            {
                LOGGER.debug("Unable to find a valid Crowd token.");
            }
            else
            {
                LOGGER.debug("Existing token value yet to be verified by Crowd: " + token);
            }
        }

        return token;
    }

    public void removeCrowdToken(HttpServletRequest request, HttpServletResponse response, ClientProperties clientProperties, CookieConfiguration cookieConfig)
    {
        Validate.notNull(request);
        Validate.notNull(clientProperties);
        if (response != null)
        {
            Validate.notNull(cookieConfig);
        }

        HttpSession session = request.getSession();

        session.removeAttribute(clientProperties.getSessionTokenKey());

        // Remove the token from the request attribute if it exists
        request.removeAttribute(clientProperties.getCookieTokenKey());

        // set the client cookie flags
        // A zero value causes the cookie to be deleted.
        // fix for Confluence where the response filter is sometimes null.
        if (response != null)
        {
            Cookie tokenCookie = buildCookie(null, clientProperties.getCookieTokenKey(), cookieConfig);

            tokenCookie.setMaxAge(0);
            HttpOnlyCookies.addHttpOnlyCookie(response, tokenCookie);
        }
    }

    public void setCrowdToken(HttpServletRequest request, HttpServletResponse response, String token, ClientProperties clientProperties, CookieConfiguration cookieConfig)
    {
        Validate.notNull(request);
        Validate.notNull(token);
        Validate.notNull(clientProperties);
        if (response != null)
        {
            Validate.notNull(cookieConfig);
        }
        HttpSession session = request.getSession();

        session.setAttribute(clientProperties.getSessionLastValidation(), new Date());

        // Set the Token on the request, so we know we are authenticated for the life of this request
        request.setAttribute(clientProperties.getCookieTokenKey(), token);

        // fix for Confluence where the response filter is sometimes null.
        if (response != null && request.getAttribute(Constants.REQUEST_SSO_COOKIE_COMMITTED) == null)
        {
            // create the cookie sent to the client
            Cookie tokenCookie = buildCookie(token, clientProperties.getCookieTokenKey(), cookieConfig);

            HttpOnlyCookies.addHttpOnlyCookie(response, tokenCookie);
            request.setAttribute(Constants.REQUEST_SSO_COOKIE_COMMITTED, Boolean.TRUE);
        }
    }

    public UserAuthenticationContext getUserAuthenticationContext(HttpServletRequest request, String username, String password, ClientProperties clientProperties)
    {
        PasswordCredential credential = new PasswordCredential(password);

        UserAuthenticationContext userAuthenticationContext = new UserAuthenticationContext();

        userAuthenticationContext.setApplication(clientProperties.getApplicationName());
        userAuthenticationContext.setCredential(credential);
        userAuthenticationContext.setName(username);
        List<ValidationFactor> validationFactors = validationFactorExtractor.getValidationFactors(request);
        userAuthenticationContext.setValidationFactors(validationFactors.toArray(new ValidationFactor[0]));

        return userAuthenticationContext;
    }

    public CrowdHttpValidationFactorExtractor getValidationFactorExtractor()
    {
        return validationFactorExtractor;
    }

    /**
     * Creates the cookie and sets attributes such as path, domain, and "secure" flag.
     *
     * @param token The SSO token to be included in the cookie
     * @param tokenCookieKey Cookie key for the token
     * @param cookieConfig Cookie configuration
     * @return new cookie
     */
    private Cookie buildCookie(final String token, final String tokenCookieKey, final CookieConfiguration cookieConfig)
    {
        String domain = cookieConfig.getDomain();
        boolean isSecure = cookieConfig.isSecure();
        Cookie tokenCookie = new Cookie(tokenCookieKey, token);

        // path
        tokenCookie.setPath(Constants.COOKIE_PATH);

        // domain
        if (domain != null && StringUtils.isNotBlank(domain) && !"localhost".equals(domain))
        {
            tokenCookie.setDomain(domain);
        }

        // "Secure" flag
        tokenCookie.setSecure(isSecure);

        return tokenCookie;
    }

    /**
     * Returns an instance of CrowdHttpTokenHelper.
     *
     * @return CrowdHttpTokenHelper.
     */
    public static CrowdHttpTokenHelper getInstance(final CrowdHttpValidationFactorExtractor validationFactorExtractor)
    {
        return new CrowdHttpTokenHelperImpl(validationFactorExtractor);
    }
}
