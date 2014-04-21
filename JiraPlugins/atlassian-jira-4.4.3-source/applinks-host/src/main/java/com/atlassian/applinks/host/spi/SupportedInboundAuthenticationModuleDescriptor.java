package com.atlassian.applinks.host.spi;

import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.util.validation.ValidationPattern;

import static com.atlassian.plugin.util.validation.ValidationPattern.test;

/**
 * <p>
 * Marker module for supported incoming authentication types for the local application. Configured via system-plugin or
 * bundle in atlassian-plugin.xml, for example:
 * </p>
 * <p/>
 * {@code <supported-inbound-authentication key="refapp-inbound-basic" application="refapp" class="com.atlassian.applinks.authentication.basic.BasicAuthRequestFactoryImpl" />}
 * <p/>
 * <p>
 * The name attribute refers to the name of the supported application provider. The application element is a plugins2
 * mechanism scopes the module to a particular application.
 * </p>
 *
 * @since 3.0
 */
public class SupportedInboundAuthenticationModuleDescriptor extends AbstractModuleDescriptor<AuthenticationProvider>
{
    @Override
    protected void provideValidationRules(final ValidationPattern pattern)
    {
        super.provideValidationRules(pattern);
        pattern
                .rule(
                        test("@class")
                                .withError("No supported AuthenticationProvider class specified.")
                );
    }

    @Override
    public AuthenticationProvider getModule()
    {
        throw new UnsupportedOperationException("Doesn't provide a module");
    }

    public Class<? extends AuthenticationProvider> getAuthenticationProviderClass()
    {
        return getModuleClass();
    }
}
