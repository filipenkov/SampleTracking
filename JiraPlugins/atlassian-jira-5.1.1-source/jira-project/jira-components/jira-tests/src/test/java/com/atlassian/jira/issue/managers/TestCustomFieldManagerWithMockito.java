package com.atlassian.jira.issue.managers;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.field.CustomFieldCreatedEvent;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldDescription;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.PluginAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

/**
 *
 *
 * @since v5.1
 */
@RunWith (ListeningMockitoRunner.class)
public class TestCustomFieldManagerWithMockito
{
    @Mock private PluginAccessor pluginAccessor;
    @Mock private EventPublisher eventPublisher;
    @Mock private FieldManager fieldManager;
    private OfBizDelegator delegator;
    @Mock private FieldConfigSchemeManager fieldConfigSchemeManager;
    @Mock private CustomFieldDescription customFieldDescription;
    @Mock private I18nHelper.BeanFactory i18nFactory;

    @Before
    public void setUp()
    {
        delegator = new MockOfBizDelegator();
        final MockComponentWorker componentAccessorWorker = new MockComponentWorker();
        componentAccessorWorker.registerMock(OfBizDelegator.class, delegator);
        ComponentAccessor.initialiseWorker(componentAccessorWorker);

        doNothing().when(fieldManager).refresh();
    }

    @Test
    public void testCreatePublishesEvent() throws GenericEntityException
    {
        final DefaultCustomFieldManager customFieldManager = new DefaultCustomFieldManager(pluginAccessor, delegator, fieldConfigSchemeManager, null, null, null, null, null, null, null, null, null, fieldManager, null, eventPublisher, customFieldDescription, i18nFactory)
        {
            @Override
            public CustomField getCustomFieldObject(Long id)
            {
                return null;
            }
        };
        customFieldManager.createCustomField("fieldName", "description", new MockCustomFieldType(), null, null, null);
        verify(eventPublisher).publish(isA(CustomFieldCreatedEvent.class));
    }

}
