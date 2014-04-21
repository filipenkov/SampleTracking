package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.field.CustomFieldUpdatedEvent;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Locale;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class DefaultFieldLayoutManagerTest
{
    private static final long CF_ID_NUMERIC = 10L;
    private static final String CF_ID = "customfield_" + CF_ID_NUMERIC;
    private static final String CF_NAME = "name";
    private static final String CF_UPDATED_NAME = "new name";

    @Mock FieldManager fieldManager;
    @Mock OfBizDelegator ofBizDelegator;
    @Mock ConstantsManager constantsManager;
    @Mock SubTaskManager subTaskManager;
    @Mock ProjectManager projectManager;
    @Mock EventPublisher eventPublisher;
    @Mock I18nHelper.BeanFactory i18nFactory;
    @Mock AssociationManager associationManager;
    @Mock ApplicationProperties applicationProperties;
    @Mock CustomFieldManager customFieldManager;
    @Mock GenericValue fieldLayoutGv;
    @Mock CustomField customField;
    @Mock CustomField updatedCustomField;
    @Mock GenericValue fieldLayoutItemGV;
    @Mock HackyFieldRendererRegistry hackyFieldRendererRegistry;
    @Mock ColumnLayoutManager columnLayoutManager;
    @Mock InstrumentRegistry instrumentRegistry;
    I18nHelper i18nHelper;

    @Before
    public void setUp() throws Exception
    {
        setUpI18nMocks();
        setUpCustomFieldMocks();
        setUpDefaultFieldLayoutMocks();

        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(ApplicationProperties.class, applicationProperties)
                .addMock(I18nHelper.BeanFactory.class, i18nFactory)
                .addMock(FieldManager.class, fieldManager)
                .addMock(CustomFieldManager.class, customFieldManager)
                .addMock(HackyFieldRendererRegistry.class, hackyFieldRendererRegistry)
                .addMock(InstrumentRegistry.class, instrumentRegistry)
        );
    }

    private void setUpCustomFieldMocks()
    {
        when(customField.getId()).thenReturn(CF_ID);
        when(customField.getName()).thenReturn(CF_NAME);
        when(updatedCustomField.getId()).thenReturn(CF_ID);
        when(updatedCustomField.getName()).thenReturn(CF_UPDATED_NAME);
    }

    private void setUpDefaultFieldLayoutMocks() throws GenericEntityException
    {
        when(hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(any(OrderableField.class))).thenReturn(false);

        when(fieldManager.getColumnLayoutManager()).thenReturn(columnLayoutManager);
        when(fieldManager.isOrderableField(CF_ID)).thenReturn(true);
        when(fieldManager.getOrderableField(CF_ID)).thenReturn(customField);

        when(fieldLayoutItemGV.getString("fieldidentifier")).thenReturn(CF_ID);
        when(fieldLayoutGv.getRelated("ChildFieldLayoutItem")).thenReturn(singletonList(fieldLayoutItemGV));

        when(ofBizDelegator.findByAnd("FieldLayout", singletonMap("type", "default"))).thenReturn(singletonList(fieldLayoutGv));
    }

    private void setUpI18nMocks()
    {
        i18nHelper = new MockI18nBean();
        when(i18nFactory.getInstance(any(User.class))).thenReturn(i18nHelper);
        when(i18nFactory.getInstance(any(Locale.class))).thenReturn(i18nHelper);
    }

    @Test
    public void fieldLayoutManagerShouldUpdateCacheWhenItReceivesACustomFieldNotification() throws Exception
    {
        DefaultFieldLayoutManager fieldLayoutManager = createFieldLayoutManager();

        // check precondition
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(CF_ID_NUMERIC);
        assertThat(fieldLayout.getFieldLayoutItem(CF_ID).getOrderableField().getName(), equalTo(CF_NAME));

        // "update" the name and broadcast an event
        when(fieldManager.getOrderableField(CF_ID)).thenReturn(updatedCustomField);
        fieldLayoutManager.onCustomFieldUpdated(new CustomFieldUpdatedEvent(1L, CF_ID, "theType"));

        // check postcondition
        FieldLayout fieldLayoutAfterUpdate = fieldLayoutManager.getFieldLayout(CF_ID_NUMERIC);
        assertThat(fieldLayoutAfterUpdate.getFieldLayoutItem(CF_ID).getOrderableField().getName(), equalTo(CF_UPDATED_NAME));
    }

    private DefaultFieldLayoutManager createFieldLayoutManager() throws Exception
    {
        DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, projectManager, i18nFactory, associationManager);
        fieldLayoutManager.start();

        return fieldLayoutManager;
    }
}
