package com.atlassian.applinks.core.auth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.AuthorisationAdminURIGenerator;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.auth.Anonymous;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.ImpersonatingAuthenticationProvider;
import com.atlassian.applinks.api.auth.NonImpersonatingAuthenticationProvider;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;

public class ApplicationLinkRequestFactoryFactoryImpl implements ApplicationLinkRequestFactoryFactory
{
    private static final Logger log = LoggerFactory.getLogger(ApplicationLinkRequestFactoryFactoryImpl.class);

    private final ApplicationLinkRequestFactory delegateRequestFactory;
    private final UserManager userManager;
    private final AuthenticatorAccessor authenticatorAccessor;

    public ApplicationLinkRequestFactoryFactoryImpl(final RequestFactory requestFactory, final UserManager userManager,
                                                    final AuthenticatorAccessor authenticatorAccessor)
    {
        checkNotNull(requestFactory);
        this.delegateRequestFactory = new SalRequestFactoryAdapter(requestFactory);
        this.userManager = checkNotNull(userManager);
        this.authenticatorAccessor = checkNotNull(authenticatorAccessor);
    }

    public ApplicationLinkRequestFactory getApplicationLinkRequestFactory(final ApplicationLink link)
    {
        checkNotNull(link);

        ApplicationLinkRequestFactory requestFactory = null;

        final String username = userManager.getRemoteUsername();
        if (username != null)
        {
            final ImpersonatingAuthenticationProvider authenticationProvider =
                    authenticatorAccessor.getAuthenticationProvider(link, ImpersonatingAuthenticationProvider.class);
            if (authenticationProvider != null)
            {
                requestFactory = authenticationProvider.getRequestFactory(username);
            }
        }

        if (requestFactory == null)
        {
            final NonImpersonatingAuthenticationProvider authenticationProvider =
                    authenticatorAccessor.getAuthenticationProvider(link, NonImpersonatingAuthenticationProvider.class);
            if (authenticationProvider != null)
            {
                requestFactory = authenticationProvider.getRequestFactory();
            }
        }

        if (requestFactory == null)
        {
            log.debug(String.format("No authenticator configured for link \"%s\", outgoing requests will be anonymous",
                    link));
            requestFactory = this.delegateRequestFactory;
        }

        return AbsoluteURLRequestFactory.create(requestFactory, link);
    }

    public ApplicationLinkRequestFactory getApplicationLinkRequestFactory(final ApplicationLink link, final Class<? extends AuthenticationProvider> providerClass)
    {
        checkNotNull(link);
        checkNotNull(providerClass);

        ApplicationLinkRequestFactory factory = null;

        if (providerClass == Anonymous.class)
        {
            factory = AbsoluteURLRequestFactory.create(delegateRequestFactory, link);
        }
        else
        {
            final AuthenticationProvider authenticationProvider = authenticatorAccessor
                    .getAuthenticationProvider(link, providerClass);

            final String username = userManager.getRemoteUsername();

            if (authenticationProvider != null)
            {
                ApplicationLinkRequestFactory delegate;
                if (authenticationProvider instanceof ImpersonatingAuthenticationProvider)
                {
                    delegate = ((ImpersonatingAuthenticationProvider) authenticationProvider).getRequestFactory(username);
                }
                else if (authenticationProvider instanceof NonImpersonatingAuthenticationProvider)
                {
                    delegate = ((NonImpersonatingAuthenticationProvider) authenticationProvider).getRequestFactory();
                }
                else
                {
                    throw new IllegalArgumentException(String.format("Only AuthenticationProviders that are subclasses " +
                            "of %s or %s and the Anonymous authenticator are supported",
                            ImpersonatingAuthenticationProvider.class.getSimpleName(),
                            NonImpersonatingAuthenticationProvider.class.getSimpleName()));
                }
                return AbsoluteURLRequestFactory.create(delegate, link);
            }
        }

        return factory;
    }

    protected static class AbsoluteURLRequestFactory implements ApplicationLinkRequestFactory
    {
        protected final ApplicationLinkRequestFactory requestFactory; //protected for unit tests
        private final ApplicationLink link;

        public static ApplicationLinkRequestFactory create(ApplicationLinkRequestFactory requestFactory, ApplicationLink link)
        {
            if (requestFactory instanceof AuthorisationAdminURIGenerator)
            {
                return new AbsoluteURLRequestFactoryWithAdminURI(requestFactory, (AuthorisationAdminURIGenerator) requestFactory, link);
            }
            else
            {
                return new AbsoluteURLRequestFactory(requestFactory, link);
            }
        }
        
        public AbsoluteURLRequestFactory(final ApplicationLinkRequestFactory requestFactory, final ApplicationLink link)
        {
            this.requestFactory = checkNotNull(requestFactory);
            this.link = checkNotNull(link);
        }

        public ApplicationLinkRequest createRequest(final Request.MethodType methodType, final String uri) throws CredentialsRequiredException
        {
            checkNotNull(uri);

            boolean isAbsoluteUri = false;
            try
            {
                isAbsoluteUri = new URI(uri).isAbsolute();
            }
            catch (URISyntaxException e)
            {
                log.warn(String.format("Couldn't parse uri %s supplied to RequestFactory.createRequest(), assuming relative.", uri));
            }

            final String updatedUri;
            if (isAbsoluteUri)
            {
                updatedUri = uri;
            }
            else
            {
                updatedUri = link.getRpcUrl() + (uri.startsWith("/") ? uri : "/" + uri);
            }
            return requestFactory.createRequest(methodType, updatedUri);
        }

        public URI getAuthorisationURI()
        {
            return requestFactory.getAuthorisationURI();
        }

        public URI getAuthorisationURI(final URI callback)
        {
            return requestFactory.getAuthorisationURI(callback);
        }
    }

    protected static class AbsoluteURLRequestFactoryWithAdminURI
        extends AbsoluteURLRequestFactory
        implements AuthorisationAdminURIGenerator
    {
        private AuthorisationAdminURIGenerator adminUriGenerator;
        
        public AbsoluteURLRequestFactoryWithAdminURI(final ApplicationLinkRequestFactory requestFactory,
                                                     final AuthorisationAdminURIGenerator adminUriGenerator,
                                                     final ApplicationLink link)
        {
            super(requestFactory, link);
            this.adminUriGenerator = adminUriGenerator;
        }

        public URI getAuthorisationAdminURI()
        {
            return adminUriGenerator.getAuthorisationAdminURI();
        }
    }
    
    protected class SalRequestFactoryAdapter implements ApplicationLinkRequestFactory
    {
        private final RequestFactory<Request<?, ?>> adaptedFactory;

        public SalRequestFactoryAdapter(final RequestFactory requestFactory)
        {
            adaptedFactory = requestFactory;
        }

        public URI getAuthorisationURI()
        {
            return null;
        }

        public URI getAuthorisationURI(final URI callback)
        {
            return null;
        }

        public ApplicationLinkRequest createRequest(final Request.MethodType methodType, final String url)
        {
            return new ApplicationLinkRequestAdaptor(adaptedFactory.createRequest(methodType, url));
        }
    }
}
