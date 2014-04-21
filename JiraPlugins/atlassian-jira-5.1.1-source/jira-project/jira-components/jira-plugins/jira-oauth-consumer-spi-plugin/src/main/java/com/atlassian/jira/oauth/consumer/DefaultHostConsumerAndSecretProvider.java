package com.atlassian.jira.oauth.consumer;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.auth.trustedapps.CurrentApplicationFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerCreationException;
import com.atlassian.oauth.consumer.core.ConsumerServiceStore;
import com.atlassian.oauth.consumer.core.HostConsumerAndSecretProvider;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.util.concurrent.LazyReference;

import java.security.GeneralSecurityException;
import java.security.KeyPair;

/**
 * Provides the default consumer and secret for this JIRA instance.
 *
 * @since v4.0
 */
public class DefaultHostConsumerAndSecretProvider implements HostConsumerAndSecretProvider
{
    public static final String HOST_SERVICENAME = "__HOST_SERVICE__";

    private final ApplicationProperties applicationProperties;
    private final ConsumerServiceStore consumerStore;
    private final CurrentApplicationFactory currentApplicationFactory;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LazyReference<ConsumerServiceStore.ConsumerAndSecret> consumerAndSecretRef;

    public DefaultHostConsumerAndSecretProvider(ApplicationProperties applicationProperties,
            ConsumerServiceStore consumerStore, CurrentApplicationFactory currentApplicationFactory)
    {
        this.applicationProperties = applicationProperties;
        this.consumerStore = consumerStore;
        this.currentApplicationFactory = currentApplicationFactory;
        this.jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();

        consumerAndSecretRef = new LazyReference<ConsumerServiceStore.ConsumerAndSecret>()
        {
            @Override
            protected ConsumerServiceStore.ConsumerAndSecret create() throws Exception
            {
                return createHostConsumerAndSecret();
            }
        };
    }

    public ConsumerServiceStore.ConsumerAndSecret get()
    {
        final ConsumerServiceStore.ConsumerAndSecret consumerAndSecret = consumerStore.get(HOST_SERVICENAME);
        if (consumerAndSecret == null)
        {
            final ConsumerServiceStore.ConsumerAndSecret hostConsumerAndSecret = consumerAndSecretRef.get();
            consumerStore.put(HOST_SERVICENAME, hostConsumerAndSecret);
            return consumerStore.get(HOST_SERVICENAME);
        }
        return consumerAndSecret;
    }

    public ConsumerServiceStore.ConsumerAndSecret put(final ConsumerServiceStore.ConsumerAndSecret consumerAndSecret)
    {
        consumerStore.put(consumerAndSecret.getServiceName(), consumerAndSecret);
        return consumerStore.get(consumerAndSecret.getServiceName());
    }

    private ConsumerServiceStore.ConsumerAndSecret createHostConsumerAndSecret()
    {
        //use the same key as trusted apps here
        final String key = currentApplicationFactory.getCurrentApplication().getID();
        KeyPair keyPair;
        try
        {
            keyPair = RSAKeys.generateKeyPair();
        }
        catch (GeneralSecurityException e)
        {
            throw new ConsumerCreationException("Could not create key pair for consumer", e);
        }

        final I18nHelper i18nBean = jiraAuthenticationContext.getI18nHelper();
        final String description = i18nBean.getText("oauth.host.consumer.default.description",
                applicationProperties.getString(APKeys.JIRA_BASEURL));

        Consumer consumer = Consumer.key(key)
                .name(applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE))
                .publicKey(keyPair.getPublic())
                .description(description)
                .build();
        return new ConsumerServiceStore.ConsumerAndSecret(HOST_SERVICENAME, consumer, keyPair.getPrivate());
    }
}
