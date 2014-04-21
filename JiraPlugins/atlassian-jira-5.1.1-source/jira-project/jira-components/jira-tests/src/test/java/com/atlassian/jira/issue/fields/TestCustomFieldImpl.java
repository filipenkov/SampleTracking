package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigImpl;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.mockobjects.dynamic.Mock;
import org.easymock.EasyMock;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class TestCustomFieldImpl extends ListeningTestCase
{
    @org.mockito.Mock
    private CustomFieldDescription customFieldDescription;
    @org.mockito.Mock
    private I18nHelper.BeanFactory i18nFactory;

    @Test
    public void testFieldConfigComparison()
    {
        final FieldConfig fieldConfigX = new FieldConfigImpl(new Long(1), null, null, null, null);
        final FieldConfig fieldConfigY = new FieldConfigImpl(new Long(2), null, null, null, null);

        assertEquals(CustomFieldImpl.areDifferent(null, null), false);
        assertEquals(CustomFieldImpl.areDifferent(fieldConfigX, null), true);
        assertEquals(CustomFieldImpl.areDifferent(null, fieldConfigX), true);
        assertEquals(CustomFieldImpl.areDifferent(fieldConfigX, fieldConfigX), false);
        assertEquals(CustomFieldImpl.areDifferent(fieldConfigX, fieldConfigY), true);
        assertEquals(CustomFieldImpl.areDifferent(fieldConfigY, fieldConfigX), true);
    }

    @Test
    public void testExceptionDuringSearcherInit() throws Exception
    {
        final String customFieldKey = "mockkey";
        final GenericValue customFieldGV = new MockGenericValue("customField",
                MapBuilder.<String, Object>newBuilder()
                        .add("id", (long) 1)
                        .add("customfieldsearcherkey", customFieldKey).toMap());

        final CustomFieldSearcher searcher = EasyMock.createMock(CustomFieldSearcher.class);
        searcher.init((CustomField) EasyMock.anyObject());
        EasyMock.expectLastCall().andThrow(new RuntimeException());

        final CustomFieldManager customFieldManager = EasyMock.createMock(CustomFieldManager.class);
        EasyMock.expect(customFieldManager.getCustomFieldSearcher(customFieldKey)).andReturn(searcher);

        final FieldConfigSchemeClauseContextUtil contextUtil = null;
        final JiraAuthenticationContext authenticationContext = null;
        final ConstantsManager constantsManager = null;
        final FieldConfigSchemeManager fieldConfigSchemeManager = null;
        final PermissionManager permissionManager = null;
        final RendererManager rendererManager = null;

        final CustomFieldImpl customField = new CustomFieldImpl(customFieldGV, customFieldManager, authenticationContext, constantsManager, fieldConfigSchemeManager, permissionManager, rendererManager, contextUtil, customFieldDescription, i18nFactory);

        EasyMock.replay(searcher, customFieldManager);

        // we should swallow the exception from the searcher and return a null searcher instead.
        assertNull(customField.getCustomFieldSearcher());

        EasyMock.verify(searcher, customFieldManager);
    }

    @Test
    public void testIssueComparatorEquals()
    {
        final Mock mockCustomField = new Mock(CustomField.class);
        mockCustomField.setStrict(true);

        final CustomFieldImpl.CustomFieldIssueSortComparator comparator = new CustomFieldImpl.CustomFieldIssueSortComparator((CustomField) mockCustomField.proxy());
        assertEquals(comparator, comparator);

        final String cfId = "customfield_2000";
        mockCustomField.expectAndReturn("getId", cfId);

        assertEquals(comparator.hashCode(), comparator.hashCode());
        assertEquals(cfId.hashCode(), comparator.hashCode());
        mockCustomField.verify();

        final Mock mockCustomField2 = new Mock(CustomField.class);
        mockCustomField2.setStrict(true);
        mockCustomField2.expectAndReturn("getId", cfId);

        final CustomFieldImpl.CustomFieldIssueSortComparator comparator2 = new CustomFieldImpl.CustomFieldIssueSortComparator((CustomField) mockCustomField2.proxy());
        assertEquals(comparator, comparator2);
        mockCustomField2.verify();
        assertEquals(comparator.hashCode(), comparator2.hashCode());

        final Mock mockCustomField3 = new Mock(CustomField.class);
        mockCustomField3.setStrict(true);
        final String anotherCfId = "customfield_1001";
        mockCustomField3.expectAndReturn("getId", anotherCfId);

        final CustomFieldImpl.CustomFieldIssueSortComparator comparator3 = new CustomFieldImpl.CustomFieldIssueSortComparator((CustomField) mockCustomField3.proxy());
        assertFalse(comparator.equals(comparator3));
        assertFalse(comparator2.equals(comparator3));
        mockCustomField3.verify();

        assertFalse(comparator.hashCode() == comparator3.hashCode());
        assertFalse(comparator2.hashCode() == comparator3.hashCode());

        assertEquals(anotherCfId.hashCode(), comparator3.hashCode());
    }
}
