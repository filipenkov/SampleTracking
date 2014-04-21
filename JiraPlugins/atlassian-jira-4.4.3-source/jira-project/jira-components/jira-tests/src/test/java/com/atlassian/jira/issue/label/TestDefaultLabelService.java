package com.atlassian.jira.issue.label;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.bc.issue.label.DefaultLabelService;
import com.atlassian.jira.bc.issue.label.LabelService;
import com.atlassian.jira.bc.issue.label.LabelService.AddLabelValidationResult;
import com.atlassian.jira.bc.issue.label.LabelService.LabelsResult;
import com.atlassian.jira.bc.issue.label.LabelService.SetLabelValidationResult;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.MockCustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import org.easymock.IExpectationSetters;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @since v4.2
 */
public class TestDefaultLabelService extends ListeningTestCase
{
    private static final long ISSUE_ID = 1L;
    private static final long CUSTOM_FIELD_ID = 2L;
    private static Label LABEL = new Label(1L, ISSUE_ID, null, "label");
    private static Set<Label> LABEL_SINGLETON = Collections.singleton(LABEL);
    private static Set<String> LABEL_STRING_SINGLETON = Collections.singleton(LABEL.getLabel());
    private static final MutableIssue ISSUE = new MockIssue(ISSUE_ID);
    private static User USER = new MockUser("admin");

    private static Map<Integer, String> PERMISSION_ERROR_MESSAGE = MapBuilder.<Integer, String>newBuilder()
            .add(Permissions.BROWSE, String.format("label.service.error.issue.no.permission %s", ISSUE.getKey()))
            .add(Permissions.EDIT_ISSUE, String.format("label.service.error.issue.no.edit.permission %s", ISSUE.getKey()))
            .toMap();

    private LabelManager mockLabelManager;
    private PermissionManager mockPermissionManager;
    private CustomFieldManager mockCustomFieldManager;
    private MockIssueManager mockIssueManager;
    private LabelService labelService;
    private FieldLayoutManager mockFieldLayoutManager;

    private static class StubI18nFactory implements I18nHelper.BeanFactory
    {
        private I18nHelper mockI18nHelper = new MockI18nHelper();

        public I18nHelper getInstance(final Locale locale)
        {
            return mockI18nHelper;
        }

        public I18nHelper getInstance(final com.opensymphony.user.User user)
        {
            return mockI18nHelper;
        }

        public I18nHelper getInstance(final User user)
        {
            return mockI18nHelper;
        }
    }

    @Before
    public void setUp() throws Exception
    {
        mockPermissionManager = createMock(PermissionManager.class);
        mockIssueManager = new MockIssueManager();
        ((MockIssueManager) mockIssueManager).addIssue(ISSUE);
        mockLabelManager = createMock(LabelManager.class);
        mockCustomFieldManager = createMock(CustomFieldManager.class);
        mockFieldLayoutManager = createMock(FieldLayoutManager.class);
        labelService = new DefaultLabelService(mockPermissionManager, mockIssueManager, mockLabelManager,
                new StubI18nFactory(), mockCustomFieldManager, mockFieldLayoutManager);
    }

    @Test
    public void testGetSystemFieldLabels()
    {
        expectHasPermission(Permissions.BROWSE)
                .andReturn(true);
        expect(mockLabelManager.getLabels(ISSUE.getId()))
                .andReturn(Collections.<Label>emptySet());
        replay(mockPermissionManager, mockLabelManager);

        final LabelsResult result = labelService.getLabels(USER, ISSUE.getId());
        verify(mockPermissionManager, mockLabelManager);

        assertEquals(Collections.<Label>emptySet(), result.getLabels());
        assertFalse(result.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testGetCustomFieldLabels()
    {
        expectHasPermission(Permissions.BROWSE)
                .andReturn(true);
        expect(mockCustomFieldManager.getCustomFieldObject(CUSTOM_FIELD_ID))
                .andReturn(new MockCustomField());
        expect(mockLabelManager.getLabels(ISSUE.getId(), CUSTOM_FIELD_ID))
                .andReturn(Collections.<Label>emptySet());
        replay(mockPermissionManager, mockCustomFieldManager, mockLabelManager);

        LabelsResult result = labelService.getLabels(USER, ISSUE.getId(), CUSTOM_FIELD_ID);
        verify(mockPermissionManager, mockCustomFieldManager, mockLabelManager);

        assertEquals(Collections.<Label>emptySet(), result.getLabels());
        assertFalse(result.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testGetSystemFieldLabelsWithoutPermission()
    {
        expectHasPermission(Permissions.BROWSE)
                .andReturn(false);
        replay(mockPermissionManager);

        final LabelsResult result = labelService.getLabels(USER, ISSUE.getId());
        verify(mockPermissionManager);

        assertEquals(Collections.<Label>emptySet(), result.getLabels());
        assertInvalidPermission(Permissions.BROWSE, result);
    }

    @Test
    public void testValidateSetLabelsWithoutPermission()
    {
        mockIssueManager.setEditable(false);
        SetLabelValidationResult result = labelService.validateSetLabels(USER, ISSUE.getId(), Collections.<String>emptySet());

        assertInvalidPermission(Permissions.EDIT_ISSUE, result);
    }

    @Test
    public void testValidateAddLabelWithoutPermission()
    {
        mockIssueManager.setEditable(false);
        AddLabelValidationResult result = labelService.validateAddLabel(USER, ISSUE.getId(), LABEL.getLabel());

        assertInvalidPermission(Permissions.EDIT_ISSUE, result);
    }

    @Test
    public void testValidateAddLabelWithInvalidCharacters()
    {
        String invalidLabel = "bad label";
        mockIssueManager.setEditable(true);

        AddLabelValidationResult result = labelService.validateAddLabel(USER, ISSUE.getId(), invalidLabel);

        ErrorCollection errors = result.getErrorCollection();
        assertTrue(String.format("'%s' should be invalid", invalidLabel), errors.getErrors().get("labels").contains(String.format("label.service.error.label.invalid %s", invalidLabel)));

    }

    @Test
    public void testValidateSetLabelsWithInvalidCharacters()
    {
        Set<String> invalidLabels = new HashSet<String>();
        invalidLabels.add("b ad");

        mockIssueManager.setEditable(true);
        SetLabelValidationResult result = labelService.validateSetLabels(USER, ISSUE.getId(), invalidLabels);

        ErrorCollection errors = result.getErrorCollection();
        assertEquals("label.service.error.label.invalid b ad", errors.getErrors().get("labels"));
    }

    @Test
    public void testValidateAddLabelWithTooLongLabel()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= LabelParser.MAX_LABEL_LENGTH; i++)
        {
            sb.append("x");
        }
        String tooLongLabel = sb.toString();
        assertTrue(tooLongLabel.length() > LabelParser.MAX_LABEL_LENGTH);

        mockIssueManager.setEditable(true);
        AddLabelValidationResult result = labelService.validateAddLabel(USER, ISSUE.getId(), tooLongLabel);

        ErrorCollection errors = result.getErrorCollection();
        assertTrue(errors.getErrors().get("labels").contains(String.format("label.service.error.label.toolong %s", tooLongLabel)));
    }

    @Test
    public void testValidateAddLabelFieldRequired()
    {
        final FieldLayout mockFieldLayout = createMock(FieldLayout.class);
        final FieldLayoutItem mockFieldLayoutItem = createMock(FieldLayoutItem.class);
        final OrderableField mockField = createMock(OrderableField.class);

        mockIssueManager.setEditable(true);
        expect(mockFieldLayoutManager.getFieldLayout(mockIssueManager.getIssueObject(ISSUE.getId()))).andReturn(mockFieldLayout);
        expect(mockFieldLayout.getFieldLayoutItem("labels")).andReturn(mockFieldLayoutItem);
        expect(mockFieldLayoutItem.isRequired()).andReturn(true);
        expect(mockFieldLayoutItem.getOrderableField()).andReturn(mockField);
        expect(mockField.getNameKey()).andReturn("issue.field.labels");

        replay(mockFieldLayoutManager, mockFieldLayout, mockFieldLayoutItem, mockField);

        final SetLabelValidationResult result = labelService.validateSetLabels(USER, ISSUE.getId(), Collections.<String>emptySet());
        assertFalse(result.isValid());
        assertEquals("issue.field.required issue.field.labels", result.getErrorCollection().getErrors().get("labels"));

        verify(mockFieldLayoutManager, mockFieldLayout, mockFieldLayoutItem, mockField);
    }

    private void assertInvalidPermission(int permission, ServiceResultImpl result)
    {
        ErrorCollection errors = result.getErrorCollection();
        assertFalse(result.isValid());
        assertTrue(errors.hasAnyErrors());
        assertTrue(errors.getErrorMessages().contains(PERMISSION_ERROR_MESSAGE.get(permission)));
    }

    private IExpectationSetters<Boolean> expectHasPermission(int permission)
    {
        return expect(mockPermissionManager.hasPermission(permission, mockIssueManager.getIssueObject(ISSUE.getId()), USER));
    }

    @Test
    public void testInvalidSetLabelValidationResultRejected()
    {
        SetLabelValidationResult mockResult = createMock(SetLabelValidationResult.class);
        expect(mockResult.isValid())
                .andReturn(false);
        replay(mockResult);

        try
        {
            labelService.setLabels(USER, mockResult, false, false);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        verify(mockResult);
    }

    @Test
    public void testInvalidAddLabelValidationResultRejected()
    {
        AddLabelValidationResult mockResult = createMock(AddLabelValidationResult.class);
        expect(mockResult.isValid())
                .andReturn(false);
        replay(mockResult);

        try
        {
            labelService.addLabel(USER, mockResult, false);
            fail();
        }
        catch (IllegalStateException e)
        {
            // expected
        }
        verify(mockResult);
    }

    @Test
    public void testSetSystemFieldLabels()
    {
        expect(mockLabelManager.setLabels(USER, ISSUE.getId(), LABEL_STRING_SINGLETON, false, false))
                .andReturn(LABEL_SINGLETON);
        replay(mockLabelManager);

        SetLabelValidationResult validationResult = createSetLabelsValidationResult(ISSUE.getId(), null, LABEL_STRING_SINGLETON);
        LabelsResult result = labelService.setLabels(USER, validationResult, false, false);
        verify(mockLabelManager);

        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertEquals(LABEL_SINGLETON, result.getLabels());
    }

    @Test
    public void testSetCustomFieldLabels()
    {
        expect(mockLabelManager.setLabels(USER, ISSUE.getId(), CUSTOM_FIELD_ID, LABEL_STRING_SINGLETON, false, false))
                .andReturn(LABEL_SINGLETON);
        replay(mockLabelManager);

        SetLabelValidationResult validationResult = createSetLabelsValidationResult(ISSUE.getId(), CUSTOM_FIELD_ID, LABEL_STRING_SINGLETON);
        LabelsResult result = labelService.setLabels(USER, validationResult, false, false);
        verify(mockLabelManager);

        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertEquals(LABEL_SINGLETON, result.getLabels());
    }

    @Test
    public void testAddSystemFieldLabel()
    {
        expect(mockLabelManager.addLabel(USER, ISSUE.getId(), LABEL.getLabel(), false))
                .andReturn(LABEL);
        expectHasPermission(Permissions.BROWSE)
                .andReturn(true);
        expect(mockLabelManager.getLabels(ISSUE.getId()))
                .andReturn(LABEL_SINGLETON);
        replay(mockLabelManager, mockPermissionManager);

        AddLabelValidationResult validationResult = createAddLabelValidationResult(ISSUE.getId(), null, LABEL.getLabel());
        LabelsResult result = labelService.addLabel(USER, validationResult, false);
        verify(mockLabelManager, mockPermissionManager);

        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertEquals(LABEL_SINGLETON, result.getLabels());
    }

    @Test
    public void testAddCustomFieldLabel()
    {
        expect(mockLabelManager.addLabel(USER, ISSUE.getId(), CUSTOM_FIELD_ID, LABEL.getLabel(), false))
                .andReturn(LABEL);
        expectHasPermission(Permissions.BROWSE)
                .andReturn(true);
        expect(mockCustomFieldManager.getCustomFieldObject(CUSTOM_FIELD_ID))
                .andReturn(new MockCustomField());
        expect(mockLabelManager.getLabels(ISSUE.getId(), CUSTOM_FIELD_ID))
                .andReturn(LABEL_SINGLETON);
        replay(mockLabelManager, mockPermissionManager, mockCustomFieldManager);

        AddLabelValidationResult validationResult = createAddLabelValidationResult(ISSUE.getId(), CUSTOM_FIELD_ID, LABEL.getLabel());
        LabelsResult result = labelService.addLabel(USER, validationResult, false);
        verify(mockLabelManager, mockPermissionManager, mockCustomFieldManager);

        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertEquals(LABEL_SINGLETON, result.getLabels());
    }

    @Test
    public void testValidateLabelSuggestions()
    {
        LabelService.LabelSuggestionResult labelSuggestionResult = labelService.getSuggestedLabels(USER, 10000L, " ");
        assertEquals(1, labelSuggestionResult.getErrorCollection().getErrorMessages().size());
        assertEquals("label.service.error.label.invalid  ", labelSuggestionResult.getErrorCollection().getErrorMessages().iterator().next());

        labelSuggestionResult = labelService.getSuggestedLabels(USER, 10000L, "abc d");
        assertEquals(1, labelSuggestionResult.getErrorCollection().getErrorMessages().size());
        assertEquals("label.service.error.label.invalid abc d", labelSuggestionResult.getErrorCollection().getErrorMessages().iterator().next());

        expect(mockCustomFieldManager.getCustomFieldObject(22323L)).andReturn(null);

        replay(mockCustomFieldManager);
        labelSuggestionResult = labelService.getSuggestedLabels(USER, 10000L, 22323L, "abcd");
        assertEquals(1, labelSuggestionResult.getErrorCollection().getErrorMessages().size());
        assertEquals("label.service.error.custom.field.doesnt.exist 22,323", labelSuggestionResult.getErrorCollection().getErrorMessages().iterator().next());

        verify(mockCustomFieldManager);
    }

    @Test
    public void testGetLabelSuggestions()
    {
        expect(mockLabelManager.getSuggestedLabels(USER, 10000L, "abcd")).andReturn(CollectionBuilder.newBuilder("abcd1", "abcd2").asSortedSet());

        replay(mockLabelManager, mockCustomFieldManager, mockPermissionManager);
        final LabelService.LabelSuggestionResult labelSuggestionResult = labelService.getSuggestedLabels(USER, 10000L, "abcd");
        assertFalse(labelSuggestionResult.getErrorCollection().hasAnyErrors());
        assertEquals(CollectionBuilder.newBuilder("abcd1", "abcd2").asSortedSet(), labelSuggestionResult.getSuggestions());

        verify(mockLabelManager, mockCustomFieldManager, mockPermissionManager);
    }

    @Test
    public void testGetLabelSuggestionsWithCustomField()
    {
        expect(mockLabelManager.getSuggestedLabels(USER, 10000L, CUSTOM_FIELD_ID, "abcd")).andReturn(CollectionBuilder.newBuilder("abcd1", "abcd2").asSortedSet());
        expect(mockCustomFieldManager.getCustomFieldObject(CUSTOM_FIELD_ID)).andReturn(new MockCustomField());

        replay(mockLabelManager, mockCustomFieldManager, mockPermissionManager);
        final LabelService.LabelSuggestionResult labelSuggestionResult = labelService.getSuggestedLabels(USER, 10000L, CUSTOM_FIELD_ID, "abcd");
        assertFalse(labelSuggestionResult.getErrorCollection().hasAnyErrors());
        assertEquals(CollectionBuilder.newBuilder("abcd1", "abcd2").asSortedSet(), labelSuggestionResult.getSuggestions());

        verify(mockLabelManager, mockCustomFieldManager, mockPermissionManager);
    }

    private static SetLabelValidationResult createSetLabelsValidationResult(Long issueId, Long customFieldId, Set<String> labels)
    {
        SetLabelValidationResult mockResult = createMock(SetLabelValidationResult.class);
        expect(mockResult.isValid()).andReturn(true).anyTimes();
        expect(mockResult.getIssueId()).andReturn(issueId).anyTimes();
        expect(mockResult.getCustomFieldId()).andReturn(customFieldId).anyTimes();
        expect(mockResult.getLabels()).andReturn(labels).anyTimes();
        replay(mockResult);
        return mockResult;
    }

    private static AddLabelValidationResult createAddLabelValidationResult(Long issueId, Long customFieldId, String label)
    {
        AddLabelValidationResult mockResult = createMock(AddLabelValidationResult.class);
        expect(mockResult.isValid()).andReturn(true).anyTimes();
        expect(mockResult.getIssueId()).andReturn(issueId).anyTimes();
        expect(mockResult.getCustomFieldId()).andReturn(customFieldId).anyTimes();
        expect(mockResult.getLabel()).andReturn(label).anyTimes();
        replay(mockResult);
        return mockResult;
    }

    @Test
    public void testGetLabelsForNonexistenIssue()
    {
        long nonexistentIssueId = 666L;
        assertTrue(nonexistentIssueId != ISSUE.getId());

        LabelsResult result = labelService.getLabels(USER, nonexistentIssueId);

        ErrorCollection errors = result.getErrorCollection();
        assertTrue(errors.getErrorMessages().contains(String.format("label.service.error.issue.doesnt.exist %s", nonexistentIssueId)));
    }

    @Test
    public void testGetLabelsForNonexistentCustomField()
    {
        expectHasPermission(Permissions.BROWSE)
                .andReturn(true);
        expect(mockCustomFieldManager.getCustomFieldObject(CUSTOM_FIELD_ID))
                .andReturn(null);
        replay(mockPermissionManager, mockCustomFieldManager);

        LabelsResult result = labelService.getLabels(USER, ISSUE.getId(), CUSTOM_FIELD_ID);
        verify(mockPermissionManager, mockCustomFieldManager);

        ErrorCollection errors = result.getErrorCollection();
        assertTrue(errors.getErrorMessages().contains(String.format("label.service.error.custom.field.doesnt.exist %s", CUSTOM_FIELD_ID)));
    }
}
