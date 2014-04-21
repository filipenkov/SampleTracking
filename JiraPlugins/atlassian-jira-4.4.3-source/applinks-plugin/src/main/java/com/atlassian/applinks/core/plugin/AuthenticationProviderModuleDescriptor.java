package com.atlassian.applinks.core.plugin;

import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationPattern;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import java.util.Comparator;

import static com.atlassian.plugin.util.validation.ValidationPattern.test;

/**
 * atlassian-plugin.xml excerpt:
 * {@code
 * <applinks-authentication-provider key="seraphAuthenticator"
 * class="com.atlassian.applinks.core.auth.basic.BasicAuthenticationProviderPluginModule">
 * </applinks-authentication-provider>
 * }
 *
 * @since 3.0
 */
public class AuthenticationProviderModuleDescriptor extends AbstractModuleDescriptor<AuthenticationProviderPluginModule>
{
    public static Comparator<AuthenticationProviderModuleDescriptor> BY_WEIGHT = new Comparator<AuthenticationProviderModuleDescriptor>()
    {
        public int compare(final AuthenticationProviderModuleDescriptor o1, final AuthenticationProviderModuleDescriptor o2)
        {
            return o1.getWeight() - o2.getWeight();
        }
    };

    private int weight;

    public AuthenticationProviderModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public void init(@NotNull final Plugin plugin, @NotNull final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        weight = DescriptorWeightAttributeParser.getWeight(element);
    }

    @Override
    protected void provideValidationRules(final ValidationPattern pattern)
    {
        super.provideValidationRules(pattern);
        pattern
                .rule(
                        test("@class")
                                .withError("No AuthenticationProviderPluginModule implementation class specified."),
                        test("@i18n-name-key")
                                .withError("No i18n-name-key specified.")
                );
    }

    @Override
    public AuthenticationProviderPluginModule getModule()
    {
        return moduleFactory.createModule(moduleClassName, AuthenticationProviderModuleDescriptor.this);
    }

    public int getWeight()
    {
        return weight;
    }
}
