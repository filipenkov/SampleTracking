package com.atlassian.crowd.manager.validation;

import com.atlassian.crowd.manager.cache.NotInCacheException;
import com.atlassian.crowd.manager.property.PropertyManager;
import com.atlassian.crowd.manager.proxy.TrustedProxyManager;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.crowd.util.I18nHelper;
import com.atlassian.crowd.util.InetAddressCacheUtil;
import com.atlassian.ip.IPMatcher;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * Implements ClientValidationManager.
 */
public class ClientValidationManagerImpl implements ClientValidationManager
{
    private final static Logger LOGGER = Logger.getLogger(ClientValidationManagerImpl.class);
    private final InetAddressCacheUtil cacheUtil;
    private final PropertyManager propertyManager;
    private final TrustedProxyManager trustedProxyManager;
    private final I18nHelper i18nHelper;

    public ClientValidationManagerImpl(final InetAddressCacheUtil cacheUtil, final PropertyManager propertyManager, final TrustedProxyManager trustedProxyManager, final I18nHelper i18nHelper)
    {
        this.cacheUtil = cacheUtil;
        this.propertyManager = propertyManager;
        this.trustedProxyManager = trustedProxyManager;
        this.i18nHelper = i18nHelper;
    }

    public void validate(final Application application, final HttpServletRequest request)
            throws ClientValidationException
    {
        Validate.notNull(application);
        Validate.notNull(request);
        
        validateApplicationActive(application);
        validateRemoteAddress(application, request);
    }

    private void validateApplicationActive(Application application) throws ClientValidationException
    {
        if (!application.isActive())
        {
            throw new ClientValidationException(i18nHelper.getText("application.inactive.error", application.getName()));
        }
    }

    /**
     * Checks if the remote address is valid in the cache first before performing a more expensive remote address validation.
     *
     * @param application application to validate the remote address against
     * @param request HTTP request
     * @throws ClientValidationException if the request address failed IP validation
     */
    private void validateRemoteAddress(final Application application, final HttpServletRequest request)
            throws ClientValidationException
    {
        final InetAddress clientAddress = XForwardedForUtil.getTrustedAddress(trustedProxyManager, request); // uses XFF if IP is a trusted proxy

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Client address: " + clientAddress.getHostAddress());
        }

        boolean addressValid;

        if (propertyManager.isCacheEnabled())
        {
            try
            {
                addressValid = cacheUtil.getPermitted(application, clientAddress);
            }
            catch (NotInCacheException e)
            {
                addressValid = match(application.getRemoteAddresses(), clientAddress);

                cacheUtil.setPermitted(application, clientAddress, addressValid);
            }
        }
        else
        {
            addressValid = match(application.getRemoteAddresses(), clientAddress);
        }

        if (!addressValid)
        {
            String errorMsg = i18nHelper.getText("client.forbidden.exception", clientAddress.getHostAddress(), application.getName());
            LOGGER.info(errorMsg);

            throw new ClientValidationException(errorMsg);
        }
    }

    private boolean match(Iterable<RemoteAddress> allowedAddresses, InetAddress requestAddress)
    {
        final IPMatcher.Builder ipMatcherBuilder = IPMatcher.builder();
        for (RemoteAddress allowedAddress : allowedAddresses)
        {
            ipMatcherBuilder.addPatternOrHost(allowedAddress.getAddress());
        }
        return ipMatcherBuilder.build().matches(requestAddress);
    }
}
