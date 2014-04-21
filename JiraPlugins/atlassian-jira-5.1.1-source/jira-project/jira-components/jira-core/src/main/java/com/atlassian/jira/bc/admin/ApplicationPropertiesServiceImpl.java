package com.atlassian.jira.bc.admin;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.event.config.ApplicationPropertyChangeEvent;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.validation.Validated;
import com.atlassian.validation.Validator;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;

import static com.atlassian.jira.event.config.ApplicationPropertyChangeEvent.KEY_METADATA;

public class ApplicationPropertiesServiceImpl implements ApplicationPropertiesService
{


    private static final Logger log = Logger.getLogger(ApplicationPropertiesServiceImpl.class);

    private final ApplicationPropertiesStore applicationPropertiesStore;
    private EventPublisher eventPublisher;

    public ApplicationPropertiesServiceImpl(ApplicationPropertiesStore applicationPropertiesStore, EventPublisher eventPublisher)
    {
        this.applicationPropertiesStore = applicationPropertiesStore;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<ApplicationProperty> getEditableApplicationProperties()
    {
        return applicationPropertiesStore.getEditableApplicationProperties();
    }

    @Override
    public ApplicationProperty getApplicationProperty(final String key)
    {
        return applicationPropertiesStore.getApplicationPropertyFromKey(key);
    }

    @Override
    public Validated<ApplicationProperty> setApplicationProperty(final String key, String value)
    {
        ApplicationProperty applicationProperty = applicationPropertiesStore.getApplicationPropertyFromKey(key);
        String oldValue = applicationProperty.getCurrentValue();

        log.debug("validating value: " + value);
        ApplicationPropertyMetadata metadata = applicationProperty.getMetadata();
        Validator.Result result = metadata.validate(value);
        if (result.isValid())
        {
            applicationProperty = applicationPropertiesStore.setApplicationProperty(key, value);

            eventPublisher.publish(createEvent(metadata, oldValue, value));
        }
        return new Validated<ApplicationProperty>(result, applicationProperty);

    }

    private ApplicationPropertyChangeEvent createEvent(ApplicationPropertyMetadata metadata, String oldValue, String newValue)
    {
        HashMap<String,Object> params = new HashMap<String,Object>();
        params.put(KEY_METADATA, Assertions.notNull("metadata", metadata));
        params.put(ApplicationPropertyChangeEvent.KEY_OLD_VALUE, oldValue);
        params.put(ApplicationPropertyChangeEvent.KEY_NEW_VALUE, newValue);
        return new ApplicationPropertyChangeEvent(params);
    }


}