package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.customfields.converters.GroupConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;

/**
 * @since v4.0
 */
public class TestGroupCustomFieldSearchInputTransformer extends MockControllerTestCase
{
    private CustomField customField;
    private GroupConverter groupConverter;
    private ClauseNames clauseNames;
    private String url;
    private SearchContext searchContext;
    private MockI18nHelper i18nHelper;
    private String id = "id";
    private CustomFieldInputHelper customFieldInputHelper;


    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        customField.getId();
        mockController.setDefaultReturnValue(id);
        groupConverter = mockController.getMock(GroupConverter.class);
        searchContext = mockController.getMock(SearchContext.class);
        i18nHelper = new MockI18nHelper();
        clauseNames = new ClauseNames("name");
        url = "url";
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
    }

    @Test
    public void testValidateParamsNoParams() throws Exception
    {
        mockController.replay();

        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder().toHashMap());

        GroupCustomFieldSearchInputTransformer transformer = new GroupCustomFieldSearchInputTransformer(customField, clauseNames, url, groupConverter, customFieldInputHelper);
        ErrorCollection errorCollection = new SimpleErrorCollection();
        transformer.validateParams(null, searchContext, holder, i18nHelper, errorCollection);

        assertFalse(errorCollection.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateParamsException() throws Exception
    {
        final String groupValue = "group";

        groupConverter.getGroup(groupValue);
        mockController.setThrowable(new FieldValidationException("blah"));

        mockController.replay();

        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder().add(id, new CustomFieldParamsImpl(customField, groupValue)).toHashMap());

        GroupCustomFieldSearchInputTransformer transformer = new GroupCustomFieldSearchInputTransformer(customField, clauseNames, url, groupConverter, customFieldInputHelper);
        ErrorCollection errorCollection = new SimpleErrorCollection();
        transformer.validateParams(null, searchContext, holder, i18nHelper, errorCollection);

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(errorCollection.getErrors().get(id), "admin.errors.could.not.find.groupname " + groupValue);


        mockController.verify();
    }

    @Test
    public void testValidateParamsHappyPath() throws Exception
    {
        final String groupValue = "group";

        groupConverter.getGroup(groupValue);
        mockController.setReturnValue(null);

        mockController.replay();

        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder().add(id, new CustomFieldParamsImpl(customField, groupValue)).toHashMap());

        GroupCustomFieldSearchInputTransformer transformer = new GroupCustomFieldSearchInputTransformer(customField, clauseNames, url, groupConverter, customFieldInputHelper);
        ErrorCollection errorCollection = new SimpleErrorCollection();
        transformer.validateParams(null, searchContext, holder, i18nHelper, errorCollection);

        assertFalse(errorCollection.hasAnyErrors());


        mockController.verify();
    }
}
