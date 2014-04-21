package com.atlassian.applinks.application.subversion;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.manifest.ApplicationStatus;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestProducer;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * @since   3.0
 */
public class SubversionManifestProducer implements ManifestProducer
{
    public ApplicationStatus getStatus(URI url) {
        // TODO: if svn does not run on HTTP, it's not as easy to verify whether it's up and running.
        return ApplicationStatus.AVAILABLE;
    }

    public Manifest getManifest(final URI url) throws ManifestNotFoundException
    {
        return new Manifest()
        {
            public ApplicationId getId()
            {
                return ApplicationIdUtil.generate(url);
            }

            public String getName()
            {
                return "Subversion";
            }

            public TypeId getTypeId()
            {
                return SubversionApplicationType.TYPE_ID;
            }

            public Long getBuildNumber()
            {
                return 0L;
            }

            public String getVersion()
            {
                return null;
            }

            public URI getUrl()
            {
                return URIUtil.copyOf(url);
            }

            public Version getAppLinksVersion()
            {
                return null;
            }
            
            public Boolean hasPublicSignup()
            {
                return null;
            }

            public Set<Class<? extends AuthenticationProvider>> getInboundAuthenticationTypes()
            {
                return ImmutableSet.<Class<? extends AuthenticationProvider>>of(BasicAuthenticationProvider.class);
            }

            public Set<Class<? extends AuthenticationProvider>> getOutboundAuthenticationTypes()
            {
                return Collections.emptySet();
            }
        };
    }
}
