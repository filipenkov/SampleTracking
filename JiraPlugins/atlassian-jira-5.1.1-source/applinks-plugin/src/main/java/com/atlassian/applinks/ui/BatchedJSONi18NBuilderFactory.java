package com.atlassian.applinks.ui;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.core.plugin.AuthenticationProviderModuleDescriptor;
import com.atlassian.applinks.core.rest.context.CurrentContext;
import com.atlassian.applinks.core.rest.model.I18NEntryListEntity;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugins.rest.common.json.JaxbJsonMarshaller;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.atlassian.applinks.spi.application.TypeId.getTypeId;

/**
 * Use the builder created by this component to fetch and aggregate i18n keys and values in one single JSON object.
 *
 * @since 3.0
 */
public class BatchedJSONi18NBuilderFactory
{
    private final LocaleResolver localeResolver;
    private final JaxbJsonMarshaller jaxbJsonMarshaller;
    private final PluginAccessor pluginAccessor;
    private final I18nResolver i18nResolver;
    private final TypeAccessor typeAccessor;
    private static final Logger logger = LoggerFactory.getLogger(BatchedJSONi18NBuilderFactory.class);

    public BatchedJSONi18NBuilderFactory(final LocaleResolver localeResolver,
                                         final JaxbJsonMarshaller jaxbJsonMarshaller,
                                         final PluginAccessor pluginAccessor,
                                         final I18nResolver i18nResolver,
                                         final TypeAccessor typeAccessor)
    {
        this.localeResolver = localeResolver;
        this.jaxbJsonMarshaller = jaxbJsonMarshaller;
        this.pluginAccessor = pluginAccessor;
        this.i18nResolver = i18nResolver;
        this.typeAccessor = typeAccessor;
    }

    public BatchedJSONI18nBuilder builder()
    {
        return new BatchedJSONI18nBuilder();
    }

    public class BatchedJSONI18nBuilder
    {
        private final Map<String, String> properties = new HashMap<String, String>();

        private void put(final String key, final String value)
        {
            if (properties.containsKey(key))
            {
                logger.warn("Duplicate i18n entry for key '" + key + "'");
            }
            properties.put(key, value);
        }

        private void putAll(final Map<String, String> properties)
        {
            for (final Map.Entry<String, String> e : properties.entrySet())
            {
                put(e.getKey(), e.getValue());
            }
        }

        /**
         * NB: This method should be called from a servlet or rest resource, because it attempt to look at the request
         * and to detect the request locale.
         *
         * @param prefix the prefix for the i18n keys, used to fetch a subset of all i18n keys of the application
         * @return the builder
         */
        public BatchedJSONI18nBuilder withProperties(final String prefix)
        {
            final HttpServletRequest request = CurrentContext.getHttpServletRequest();
            final Locale locale = localeResolver.getLocale(request);
            putAll(i18nResolver.getAllTranslationsForPrefix(prefix, locale));
            return this;
        }

        /**
         * @param properties a {@link Map} of properties to add
         * @return the builder
         */
        public BatchedJSONI18nBuilder with(final Map<String, String> properties)
        {
            putAll(properties);
            return this;
        }

        /**
         * Add a single key value pair
         *
         * @param key a String key
         * @param value a String value
         * @return the builder
         */
        public BatchedJSONI18nBuilder with(final String key, final String value)
        {
            put(key, value);
            return this;
        }

        /**
         * Add i18n properties for all enabled {@link ApplicationType}, {@link EntityType} and
         * {@link AuthenticationProvider}s
         *
         * @return the builder
         */
        public BatchedJSONI18nBuilder withPluggableApplinksModules()
        {
            for (final ApplicationType applicationType : typeAccessor.getEnabledApplicationTypes())
            {
                final String key = "applinks.application.type." + getTypeId(applicationType);
                put(key, i18nResolver.getText(applicationType.getI18nKey()));
            }

            for (final EntityType entityType : typeAccessor.getEnabledEntityTypes())
            {
                final String key = "applinks.entity.type." + getTypeId(entityType);
                put(key, i18nResolver.getText(entityType.getI18nKey()));
                final String pluralKey = "applinks.entity.type.plural." + getTypeId(entityType);
                put(pluralKey, i18nResolver.getText(entityType.getPluralizedI18nKey()));
            }

            for (final AuthenticationProviderModuleDescriptor authProvider : pluginAccessor.getEnabledModuleDescriptorsByClass(AuthenticationProviderModuleDescriptor.class))
            {
                final String key = "applinks.auth.provider." + authProvider.getModule().getAuthenticationProviderClass().getName();
                put(key, i18nResolver.getText(authProvider.getI18nNameKey()));
            }

            return this;
        }

        /**
         * @return a JSON object that contains all i18n keys and values added by the builder methods above.
         */
        public String build()
        {
            return jaxbJsonMarshaller.marshal(new I18NEntryListEntity(properties));
        }
    }

}
