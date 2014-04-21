package com.atlassian.jira.plugin.issueview;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.crowd.embedded.api.User;
import com.mockobjects.dynamic.Mock;
import org.apache.lucene.search.SortComparatorSource;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v4.0
 */
public class TestIssueViewRequestParamsHelper extends ListeningTestCase
{
    @Test
    public void testNoParamsDefined()
    {
        Mock mockFieldManager = new Mock(FieldManager.class);
        mockFieldManager.expectAndReturn("getOrderableFields", getOrderableFieldIds());

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl((FieldManager) mockFieldManager.proxy());
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build());

        assertFalse(issueViewFieldParams.isCustomViewRequested());
        assertFalse(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManager.verify();
    }

    @Test
    public void testNullFieldParamsDefined()
    {
        Mock mockFieldManager = new Mock(FieldManager.class);
        mockFieldManager.expectAndReturn("getOrderableFields", getOrderableFieldIds());

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl((FieldManager) mockFieldManager.proxy());
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", null));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertFalse(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManager.verify();
    }

    @Test
    public void testEmptyFieldParamsDefined()
    {
        Mock mockFieldManager = new Mock(FieldManager.class);
        mockFieldManager.expectAndReturn("getOrderableFields", getOrderableFieldIds());

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl((FieldManager) mockFieldManager.proxy());
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[0]));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertFalse(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManager.verify();
    }

    @Test
    public void testProperFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField("name1");
        mockFieldManagerControl.setReturnValue(createField("name1", null, null));
        mockFieldManager.getField("name3");
        mockFieldManagerControl.setReturnValue(createField("name3", null, null));

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "name1", "name3" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testInvalidFieldsParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField("name5");
        mockFieldManagerControl.setReturnValue(null);
        mockFieldManager.getField("name6");
        mockFieldManagerControl.setReturnValue(null);


        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "name5", "name6" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertFalse(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testValidAndInvalidFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField("name1");
        mockFieldManagerControl.setReturnValue(createField("name1", null, null));
        mockFieldManager.getField("name5");
        mockFieldManagerControl.setReturnValue(null);

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "name1", "name5" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testCustomFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getCustomField("customfield_10000");
        mockFieldManagerControl.setReturnValue(createCustomField("customfield_10000"));
        mockFieldManager.getCustomField("customfield_10001");
        mockFieldManagerControl.setReturnValue(createCustomField("customfield_10001"));

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "customfield_10000", "customfield_10001" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testAllCustomFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "allcustom" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertTrue(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @SuppressWarnings ({ "ThrowableInstanceNeverThrown" })
    @Test
    public void testInvalidCustomFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getCustomField("customfield_10000");
        mockFieldManagerControl.setThrowable(new IllegalArgumentException());

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "customfield_10000" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertFalse(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertTrue(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }


    @Test
    public void testRegualAndCustomFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField("name1");
        mockFieldManagerControl.setReturnValue(createField("name1", null, null));
        mockFieldManager.getCustomField("customfield_10001");
        mockFieldManagerControl.setReturnValue(createCustomField("customfield_10001"));

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "name1", "customfield_10001" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testRegualAndNonIssueAndAllCustomFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField("name1");
        mockFieldManagerControl.setReturnValue(createField("name1", null, null));
        mockFieldManager.getField("link");
        mockFieldManagerControl.setReturnValue(null);

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "name1", "link", "allcustom" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertTrue(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testRegualMappedFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField("project");
        mockFieldManagerControl.setReturnValue(createField("project", null, null));

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { "pid" }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testTimetrackingFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField(IssueFieldConstants.TIME_SPENT);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.TIME_SPENT, null, null));
        mockFieldManager.getField(IssueFieldConstants.TIME_ESTIMATE);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.TIME_ESTIMATE, null, null));
        mockFieldManager.getField(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE, null, null));
        mockFieldManager.getField(IssueFieldConstants.AGGREGATE_TIME_SPENT);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.AGGREGATE_TIME_SPENT, null, null));
        mockFieldManager.getField(IssueFieldConstants.AGGREGATE_TIME_ESTIMATE);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.AGGREGATE_TIME_ESTIMATE, null, null));
        mockFieldManager.getField(IssueFieldConstants.AGGREGATE_TIME_ORIGINAL_ESTIMATE);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.AGGREGATE_TIME_ORIGINAL_ESTIMATE, null, null));

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { IssueFieldConstants.TIMETRACKING }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    @Test
    public void testTimespentFieldParamsDefined()
    {
        MockControl mockFieldManagerControl = MockClassControl.createControl(FieldManager.class);
        FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getOrderableFields();
        mockFieldManagerControl.setReturnValue(getOrderableFieldIds());
        mockFieldManager.getField(IssueFieldConstants.TIME_SPENT);
        mockFieldManagerControl.setReturnValue(createField(IssueFieldConstants.TIME_SPENT, null, null));

        mockFieldManagerControl.replay();

        IssueViewRequestParamsHelper issueViewRequestParamsHelper = new IssueViewRequestParamsHelperImpl(mockFieldManager);
        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(EasyMap.build("field", new String[] { IssueFieldConstants.TIME_SPENT }));

        assertTrue(issueViewFieldParams.isCustomViewRequested());
        assertTrue(issueViewFieldParams.isAnyFieldDefined());
        assertFalse(issueViewFieldParams.isAllCustomFields());
        assertFalse(issueViewFieldParams.getFieldIds().isEmpty());
        assertTrue(issueViewFieldParams.getCustomFieldIds().isEmpty());
        assertFalse(issueViewFieldParams.getOrderableFieldIds().isEmpty());

        mockFieldManagerControl.verify();
    }

    private Set<OrderableField> getOrderableFieldIds()
    {
        Set<OrderableField> fields = new HashSet<OrderableField>();
        fields.add(createOrderableField("name1", null, null));
        fields.add(createOrderableField("name2", null, null));
        fields.add(createOrderableField("name3", null, null));
        fields.add(createOrderableField("name4", null, null));
        return fields;
    }

    private OrderableField createOrderableField(final String id, final String name, final String nameKey)
    {
        return new OrderableField()
        {

            public String getId()
            {
                return id;
            }

            public String getNameKey()
            {
                return nameKey;
            }

            public String getName()
            {
                return name;
            }

            public int compareTo(final Object o)
            {
                return 0;
            }

            public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
            {
                return null;
            }

            public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map displayParameters)
            {
                return null;
            }

            public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
            {
                return null;
            }

            public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map displayParameters)
            {
                return null;
            }

            public String getBulkEditHtml(final OperationContext operationContext, final Action action, final BulkEditBean bulkEditBean, final Map displayParameters)
            {
                return null;
            }

            public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue)
            {
                return null;
            }

            public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue, final Map displayParameters)
            {
                return null;
            }

            public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue, final Object value, final Map displayParameters)
            {
                return null;
            }

            public boolean isShown(final Issue issue)
            {
                return false;
            }

            public void populateDefaults(final Map fieldValuesHolder, final Issue issue)
            {
            }

            public void populateFromParams(final Map fieldValuesHolder, final Map parameters)
            {
            }

            public void populateFromIssue(final Map fieldValuesHolder, final Issue issue)
            {
            }

            public void validateParams(final OperationContext operationContext, final ErrorCollection errorCollectionToAddTo, final I18nHelper i18n, final Issue issue, final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
            {
            }

            public Object getDefaultValue(final Issue issue)
            {
                return null;
            }

            public void createValue(final Issue issue, final Object value)
            {
            }

            public void updateValue(final FieldLayoutItem fieldLayoutItem, final Issue issue, final ModifiedValue modifiedValue, final IssueChangeHolder issueChangeHolder)
            {
            }

            public void updateIssue(final FieldLayoutItem fieldLayoutItem, final MutableIssue issue, final Map fieldValueHolder)
            {
            }

            public void removeValueFromIssueObject(final MutableIssue issue)
            {
            }

            public boolean canRemoveValueFromIssueObject(final Issue issue)
            {
                return false;
            }

            public MessagedResult needsMove(final Collection originalIssues, final Issue targetIssue, final FieldLayoutItem targetFieldLayoutItem)
            {
                return null;
            }

            public void populateForMove(final Map fieldValuesHolder, final Issue originalIssue, final Issue targetIssue)
            {
            }

            public boolean hasValue(final Issue issue)
            {
                return false;
            }

            public String availableForBulkEdit(final BulkEditBean bulkEditBean)
            {
                return null;
            }

            public Object getValueFromParams(final Map params) throws FieldValidationException
            {
                return null;
            }

            public void populateParamsFromString(final Map fieldValuesHolder, final String stringValue, final Issue issue)
                    throws FieldValidationException
            {
            }

            public SearchHandler createAssociatedSearchHandler()
            {
                return null;
            }
        };
    }

    private Field createField(final String id, final String name, final String nameKey)
    {
        return new Field()
        {

            public String getId()
            {
                return id;
            }

            public String getNameKey()
            {
                return nameKey;
            }

            public String getName()
            {
                return name;
            }

            public int compareTo(final Object o)
            {
                return 0;
            }
        };
    }

    private CustomField createCustomField(final String id)
    {
        return new CustomField()
        {
            public boolean isInScope(final Project project, final List<String> issueTypeIds)
            {
                return false;
            }

            public boolean isInScope(final GenericValue project, final List issueTypeIds)
            {
                return false;
            }

            public GenericValue getGenericValue()
            {
                return null;
            }

            public int compare(final Issue issue1, final Issue issue2) throws IllegalArgumentException
            {
                return 0;
            }

            public CustomFieldParams getCustomFieldValues(final Map customFieldValuesHolder)
            {
                return null;
            }

            public Object getValue(final Issue issue)
            {
                return null;
            }

            public Set remove()
            {
                return null;
            }

            public Options getOptions(final String key, final JiraContextNode jiraContextNode)
            {
                return null;
            }

            public void setName(final String name)
            {
            }

            public String getDescription()
            {
                return null;
            }

            public void setDescription(final String description)
            {
            }

            public CustomFieldSearcher getCustomFieldSearcher()
            {
                return null;
            }

            public void setCustomFieldSearcher(final CustomFieldSearcher searcher)
            {
            }

            public void store()
            {
            }

            public boolean isEditable()
            {
                return false;
            }

            public Long getIdAsLong()
            {
                return null;
            }

            public List<FieldConfigScheme> getConfigurationSchemes()
            {
                return null;
            }

            public Options getOptions(final String key, final FieldConfig config, final JiraContextNode contextNode)
            {
                return null;
            }

            public FieldConfig getRelevantConfig(final Issue issue)
            {
                return null;
            }

            public void validateFromActionParams(final Map actionParameters, final ErrorCollection errorCollection, final FieldConfig config)
            {
            }

            public List getAssociatedProjectCategories()
            {
                return null;
            }

            public List getConfigurationItemTypes()
            {
                return null;
            }

            public List getAssociatedProjects()
            {
                return null;
            }

            public List getAssociatedIssueTypes()
            {
                return null;
            }

            public boolean isGlobal()
            {
                return false;
            }

            public boolean isAllProjects()
            {
                return false;
            }

            public boolean isAllIssueTypes()
            {
                return false;
            }

            public boolean isEnabled()
            {
                return false;
            }

            public CustomFieldType getCustomFieldType()
            {
                return null;
            }

            public FieldConfig getRelevantConfig(final IssueContext issueContext)
            {
                return null;
            }

            public FieldConfig getReleventConfig(final SearchContext searchContext)
            {
                return null;
            }

            public boolean isInScope(final SearchContext searchContext)
            {
                return false;
            }

            public boolean isInScope(final User user, final SearchContext searchContext)
            {
                return false;
            }

            public String getColumnHeadingKey()
            {
                return null;
            }

            public String getColumnCssClass()
            {
                return null;
            }

            public String getDefaultSortOrder()
            {
                return null;
            }

            public SortComparatorSource getSortComparatorSource()
            {
                return null;
            }

            public LuceneFieldSorter getSorter()
            {
                return null;
            }

            public String getColumnViewHtml(final FieldLayoutItem fieldLayoutItem, final Map displayParams, final Issue issue)
            {
                return null;
            }

            public String getHiddenFieldId()
            {
                return null;
            }

            public String prettyPrintChangeHistory(final String changeHistory)
            {
                return null;
            }

            public String prettyPrintChangeHistory(final String changeHistory, final I18nHelper i18nHelper)
            {
                return null;
            }

            public String getId()
            {
                return id;
            }

            public String getValueFromIssue(final Issue issue)
            {
                return null;
            }

            public boolean isRenderable()
            {
                return false;
            }

            public String getNameKey()
            {
                return null;
            }

            public String getName()
            {
                return null;
            }

            public int compareTo(final Object o)
            {
                return 0;
            }

            public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
            {
                return null;
            }

            public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map displayParameters)
            {
                return null;
            }

            public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
            {
                return null;
            }

            public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map displayParameters)
            {
                return null;
            }

            public String getBulkEditHtml(final OperationContext operationContext, final Action action, final BulkEditBean bulkEditBean, final Map displayParameters)
            {
                return null;
            }

            public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue)
            {
                return null;
            }

            public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue, final Map displayParameters)
            {
                return null;
            }

            public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue, final Object value, final Map displayParameters)
            {
                return null;
            }

            public boolean isShown(final Issue issue)
            {
                return false;
            }

            public void populateDefaults(final Map fieldValuesHolder, final Issue issue)
            {
            }

            public void populateFromParams(final Map fieldValuesHolder, final Map parameters)
            {
            }

            public void populateFromIssue(final Map fieldValuesHolder, final Issue issue)
            {
            }

            public void validateParams(final OperationContext operationContext, final ErrorCollection errorCollectionToAddTo, final I18nHelper i18n, final Issue issue, final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
            {
            }

            public Object getDefaultValue(final Issue issue)
            {
                return null;
            }

            public void createValue(final Issue issue, final Object value)
            {
            }

            public void updateValue(final FieldLayoutItem fieldLayoutItem, final Issue issue, final ModifiedValue modifiedValue, final IssueChangeHolder issueChangeHolder)
            {
            }

            public void updateIssue(final FieldLayoutItem fieldLayoutItem, final MutableIssue issue, final Map fieldValueHolder)
            {
            }

            public void removeValueFromIssueObject(final MutableIssue issue)
            {
            }

            public boolean canRemoveValueFromIssueObject(final Issue issue)
            {
                return false;
            }

            public MessagedResult needsMove(final Collection originalIssues, final Issue targetIssue, final FieldLayoutItem targetFieldLayoutItem)
            {
                return null;
            }

            public void populateForMove(final Map fieldValuesHolder, final Issue originalIssue, final Issue targetIssue)
            {
            }

            public boolean hasValue(final Issue issue)
            {
                return false;
            }

            public String availableForBulkEdit(final BulkEditBean bulkEditBean)
            {
                return null;
            }

            public Object getValueFromParams(final Map params) throws FieldValidationException
            {
                return null;
            }

            public void populateParamsFromString(final Map fieldValuesHolder, final String stringValue, final Issue issue)
                    throws FieldValidationException
            {
            }

            public SearchHandler createAssociatedSearchHandler()
            {
                return null;
            }

            public ClauseNames getClauseNames()
            {
                return null;
            }
        };
    }
}
