package com.atlassian.jira.issue.fields;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollectionAssert;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Responsible for holding unit tests for the TimeTrackingSystemField. This tests verify behaviour in modern mode.
 *
 * @since v4.0
 */
public class TestTimeTrackingSystemField extends MockControllerTestCase
{
    private VelocityTemplatingEngine mockTemplatingEngine;
    private ApplicationProperties mockApplicationProperties;
    private IssueManager mockIssueManager;
    private JiraAuthenticationContext mockJiraAuthenticationContext;
    private PermissionManager mockPermissionManager;
    private JiraDurationUtils mockJiraDurationUtils;
    private MockIssue mockIssue;

    @Before
    public void setUp() throws Exception
    {
        mockTemplatingEngine = getMock(VelocityTemplatingEngine.class);
        mockApplicationProperties = getMock(ApplicationProperties.class);
        mockIssueManager = getMock(IssueManager.class);
        mockJiraAuthenticationContext = getMock(JiraAuthenticationContext.class);
        mockPermissionManager = getMock(PermissionManager.class);
        mockJiraDurationUtils = getMock(JiraDurationUtils.class);
        mockIssue = new MockIssue();
    }

    @Test
    public void testPopulatesDefaultsSetsANullValueForEstimates() throws Exception
    {
        expectThatTimeTrackingLegacyModeIsTurnedOff();

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);
       
        Map fieldsValueHolder = EasyMap.build();

        mockIssue.setOriginalEstimate(1000000L);
        mockIssue.setEstimate(1000000L);

        timeTrackingField.populateDefaults(fieldsValueHolder, mockIssue);

        assertContainsTimeTrackingValue(fieldsValueHolder);

        TimeTrackingSystemField.TimeTrackingValue timeTrackingValue = (TimeTrackingSystemField.TimeTrackingValue)
                fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);

        assertNull(timeTrackingValue.getOriginalEstimateDisplayValue());
        assertNull(timeTrackingValue.getRemainingEstimateDisplayValue());
    }

    @Test
    public void testPopulateFromIssueAddsANullValueToTheFieldsValueHolderWhenTheEstimatesAreNull() throws Exception
    {
        expectThatTimeTrackingLegacyModeIsTurnedOff();

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        Map fieldsValueHolder = EasyMap.build();

        timeTrackingField.populateFromIssue(fieldsValueHolder, mockIssue);

        assertContainsTimeTrackingValue(fieldsValueHolder);

        TimeTrackingSystemField.TimeTrackingValue timeTrackingValue = (TimeTrackingSystemField.TimeTrackingValue)
                fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);

        assertNull(timeTrackingValue.getOriginalEstimateDisplayValue());
        assertNull(timeTrackingValue.getRemainingEstimateDisplayValue());
    }

    @Test
    public void testPopulateFromIssueAddsDisplayFormattedEstimatesToTheFieldsValueHolder() throws Exception
    {
        final long originalEstimateIssueValue = 600000L;
        final String expectedOriginalEstimateFormattedValue = "10m";

        final long remainingEstimateIssueValue = 900000L;
        final String expectedRemainingEstimateFormattedValue = "15m";

        expectThatTimeTrackingLegacyModeIsTurnedOff();

        // Expect calls to format the raw estimates in the issue for display.
        expect(mockJiraDurationUtils.getShortFormattedDuration(originalEstimateIssueValue, new Locale("en_UK"))).
                andReturn(expectedOriginalEstimateFormattedValue);
        expect(mockJiraDurationUtils.getShortFormattedDuration(remainingEstimateIssueValue, new Locale("en_UK"))).
                andReturn(expectedRemainingEstimateFormattedValue);

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        Map fieldsValueHolder = EasyMap.build();

        mockIssue.setOriginalEstimate(originalEstimateIssueValue);
        mockIssue.setEstimate(remainingEstimateIssueValue);

        timeTrackingField.populateFromIssue(fieldsValueHolder, mockIssue);

        assertContainsTimeTrackingValue(fieldsValueHolder);

        TimeTrackingSystemField.TimeTrackingValue timeTrackingValue = (TimeTrackingSystemField.TimeTrackingValue)
                fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);

        assertNotNull(timeTrackingValue.getOriginalEstimateDisplayValue());
        assertEquals(timeTrackingValue.getOriginalEstimateDisplayValue(), expectedOriginalEstimateFormattedValue);

        assertNotNull(timeTrackingValue.getRemainingEstimateDisplayValue());
        assertEquals(timeTrackingValue.getRemainingEstimateDisplayValue(), expectedRemainingEstimateFormattedValue);
    }

    @Test
    public void testValidateParamsAddsAnErrorIfTimeTrackingIsNotEnabled() throws Exception
    {
        OperationContext mockOperationContext = getMock(OperationContext.class);

        Map fieldsValueHolder =
                EasyMap.build(IssueFieldConstants.TIMETRACKING, new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(new String[] { "1h" }).
                        setRemainingEstimate(new String[] { "30m" }).
                        build());

        final String expectedErrorMessage = "TIME TRACKING IS DISABLED OMG!!!";

        final I18nHelper mockI18nHelper = getMock(I18nHelper.class);

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = getMock(FieldScreenRenderLayoutItem.class);

        expect(mockOperationContext.getFieldValuesHolder()).andReturn(fieldsValueHolder);

        expectThatTimeTrackingIsDisabled();

        expectThatHasWorkStartedIsCalled();

        expectThatTimeTrackingIsARequiredField(mockFieldScreenRenderLayoutItem);
        expect(mockI18nHelper.getText("createissue.error.timetracking.disabled")).andReturn(expectedErrorMessage);

        final TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        ErrorCollection errorCollection = new SimpleErrorCollection();

        timeTrackingField.validateParams(mockOperationContext, errorCollection, mockI18nHelper, mockIssue,
                mockFieldScreenRenderLayoutItem);

        assertTrue(errorCollection.hasAnyErrors());

        final Map<String, String> fieldErrors = errorCollection.getErrors();
        assertNotNull(fieldErrors);
        assertTrue(fieldErrors.containsKey(IssueFieldConstants.TIMETRACKING));

        final String actualErrorMessage = fieldErrors.get(IssueFieldConstants.TIMETRACKING);
        assertNotNull(actualErrorMessage);
        assertEquals(actualErrorMessage, expectedErrorMessage);
    }

    @Test
    public void testValidateParamsDoesNotAddAnErrorIfBothEstimatesAreBlankAndTheFieldIsNotRequired() throws Exception
    {
        OperationContext mockOperationContext = getMock(OperationContext.class);

        Map fieldsValueHolder =
                MapBuilder.newBuilder(
                        IssueFieldConstants.TIMETRACKING,
                        new TimeTrackingSystemField.TimeTrackingValue.Builder().
                            setInLegacyMode(false).
                            setOriginalEstimate(new String[] { "    " }).
                            setRemainingEstimate(new String[] { "" }).
                            build()).toMutableMap();

        final I18nHelper mockI18nHelper = getMock(I18nHelper.class);

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = getMock(FieldScreenRenderLayoutItem.class);

        expect(mockOperationContext.getFieldValuesHolder()).andStubReturn(fieldsValueHolder);

        expectThatTimeTrackingIsEnabled();

        expectThatHasWorkStartedIsCalled();

        expectThatTimeTrackingIsNotARequiredField(mockFieldScreenRenderLayoutItem);

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        ErrorCollection errorCollection = new SimpleErrorCollection();

        timeTrackingField.validateParams(mockOperationContext, errorCollection, mockI18nHelper, mockIssue,
                mockFieldScreenRenderLayoutItem);

        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testValidateParamsOnCreateAddsErrorIfBothEstimatesAreBlankAndTheFieldIsRequired() throws Exception
    {
        OperationContext mockOperationContext = getMock(OperationContext.class);

        Map fieldsValueHolder =
                MapBuilder.newBuilder(
                        IssueFieldConstants.TIMETRACKING,
                        new TimeTrackingSystemField.TimeTrackingValue.Builder().
                            setCreateIssue(new String[] {"true"}).
                            setInLegacyMode(false).
                            setOriginalEstimate(new String[] { "    " }).
                            setRemainingEstimate(new String[] { "" }).
                            build()).toMutableMap();

        final I18nHelper mockI18nHelper = new MockI18nBean();

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = getMock(FieldScreenRenderLayoutItem.class);

        expect(mockOperationContext.getFieldValuesHolder()).andStubReturn(fieldsValueHolder);

        expectThatTimeTrackingIsEnabled();

        expectThatHasWorkStartedIsCalled();

        expectThatTimeTrackingIsARequiredField(mockFieldScreenRenderLayoutItem);

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        ErrorCollection errorCollection = new SimpleErrorCollection();

        timeTrackingField.validateParams(mockOperationContext, errorCollection, mockI18nHelper, mockIssue,
                mockFieldScreenRenderLayoutItem);

        ErrorCollectionAssert.assertFieldError(errorCollection, TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE, "Original Estimate is required.");
        ErrorCollectionAssert.assertFieldError(errorCollection, TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE, "Remaining Estimate is required.");
    }

    @Test
    public void testValidateParamsOnCreateDoesntAddErrorIfOriginalEstimateIsBlankAndTheFieldIsRequired() throws Exception
    {
        OperationContext mockOperationContext = getMock(OperationContext.class);

        Map fieldsValueHolder =
                MapBuilder.newBuilder(
                        IssueFieldConstants.TIMETRACKING,
                        new TimeTrackingSystemField.TimeTrackingValue.Builder().
                            setCreateIssue(new String[] {"true"}).
                            setInLegacyMode(false).
                            setOriginalEstimate(new String[] { "    " }).
                            setRemainingEstimate(new String[] { "2h" }).
                            build()).toMutableMap();

        final I18nHelper mockI18nHelper = new MockI18nBean();

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = getMock(FieldScreenRenderLayoutItem.class);

        expect(mockOperationContext.getFieldValuesHolder()).andStubReturn(fieldsValueHolder);
        expect(mockJiraAuthenticationContext.getLocale()).andReturn(new Locale("en_UK"));
        expect(mockJiraDurationUtils.parseDuration("2h", new Locale("en_UK"))).andReturn(7200L);
        expectThatTimeTrackingIsEnabled();

        expectThatTimeTrackingIsARequiredField(mockFieldScreenRenderLayoutItem);

        expectThatHasWorkStartedIsCalled();

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        ErrorCollection errorCollection = new SimpleErrorCollection();

        timeTrackingField.validateParams(mockOperationContext, errorCollection, mockI18nHelper, mockIssue,
                mockFieldScreenRenderLayoutItem);

        ErrorCollectionAssert.assertNoErrors(errorCollection);

        // value of Remaining Estimate will be copied over to Original Estimate
        TimeTrackingSystemField.TimeTrackingValue value = (TimeTrackingSystemField.TimeTrackingValue) fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);
        assertEquals("2h", value.getRemainingEstimateDisplayValue());
        assertEquals(value.getOriginalEstimateDisplayValue(), value.getRemainingEstimateDisplayValue());
    }

    @Test
    public void testValidateParamsOnCreateDoesntAddErrorIfRemainingEstimateIsBlankAndTheFieldIsRequired() throws Exception
    {
        OperationContext mockOperationContext = getMock(OperationContext.class);

        Map fieldsValueHolder =
                MapBuilder.newBuilder(
                        IssueFieldConstants.TIMETRACKING,
                        new TimeTrackingSystemField.TimeTrackingValue.Builder().
                            setCreateIssue(new String[] {"true"}).
                            setInLegacyMode(false).
                            setOriginalEstimate(new String[] { "3h" }).
                            setRemainingEstimate(new String[] { "" }).
                            build()).toMutableMap();

        final I18nHelper mockI18nHelper = new MockI18nBean();

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = getMock(FieldScreenRenderLayoutItem.class);

        expect(mockOperationContext.getFieldValuesHolder()).andStubReturn(fieldsValueHolder);
        expect(mockJiraAuthenticationContext.getLocale()).andReturn(new Locale("en_UK"));
        expect(mockJiraDurationUtils.parseDuration("3h", new Locale("en_UK"))).andReturn(10800L);
        expectThatTimeTrackingIsEnabled();

        expectThatTimeTrackingIsARequiredField(mockFieldScreenRenderLayoutItem);

        expectThatHasWorkStartedIsCalled();

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        ErrorCollection errorCollection = new SimpleErrorCollection();

        timeTrackingField.validateParams(mockOperationContext, errorCollection, mockI18nHelper, mockIssue,
                mockFieldScreenRenderLayoutItem);

        ErrorCollectionAssert.assertNoErrors(errorCollection);

        // value of Remaining Estimate will be copied over to Original Estimate
        TimeTrackingSystemField.TimeTrackingValue value = (TimeTrackingSystemField.TimeTrackingValue) fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);
        assertEquals("3h", value.getRemainingEstimateDisplayValue());
        assertEquals(value.getOriginalEstimateDisplayValue(), value.getRemainingEstimateDisplayValue());
    }

    @Test
    public void testValidateParamsHappyFieldIsRequiredBothValueSupplied() throws Exception
    {
        OperationContext mockOperationContext = getMock(OperationContext.class);

        Map fieldsValueHolder =
                MapBuilder.newBuilder(
                        IssueFieldConstants.TIMETRACKING,
                        new TimeTrackingSystemField.TimeTrackingValue.Builder().
                            setInLegacyMode(false).
                            setOriginalEstimate(new String[] { "3h" }).
                            setRemainingEstimate(new String[] { "2h" }).
                            build()).toMutableMap();

        final I18nHelper mockI18nHelper = new MockI18nBean();

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = getMock(FieldScreenRenderLayoutItem.class);

        expect(mockOperationContext.getFieldValuesHolder()).andStubReturn(fieldsValueHolder);
        expect(mockJiraAuthenticationContext.getLocale()).andReturn(new Locale("en_UK")).times(2);
        expect(mockJiraDurationUtils.parseDuration("3h", new Locale("en_UK"))).andReturn(10800L);
        expect(mockJiraDurationUtils.parseDuration("2h", new Locale("en_UK"))).andReturn(7200L);
        expectThatTimeTrackingIsEnabled();

        expectThatTimeTrackingIsARequiredField(mockFieldScreenRenderLayoutItem);

        expectThatHasWorkStartedIsCalled();

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        ErrorCollection errorCollection = new SimpleErrorCollection();

        timeTrackingField.validateParams(mockOperationContext, errorCollection, mockI18nHelper, mockIssue,
                mockFieldScreenRenderLayoutItem);

        ErrorCollectionAssert.assertNoErrors(errorCollection);

        // value of Remaining Estimate will be copied over to Original Estimate
        TimeTrackingSystemField.TimeTrackingValue value = (TimeTrackingSystemField.TimeTrackingValue) fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);
        assertEquals("2h", value.getRemainingEstimateDisplayValue());
        assertEquals("3h", value.getOriginalEstimateDisplayValue());
    }

    @Test
    public void testValidateParamsAddsErrorMessagesIfBothEstimatesAreBlankWhenTheFieldIsRequired() throws Exception
    {
        OperationContext mockOperationContext = getMock(OperationContext.class);

        Map fieldsValueHolder =
                EasyMap.build(IssueFieldConstants.TIMETRACKING, new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(new String[] { "" }).
                        setRemainingEstimate(new String[] { "   " }).
                        build());

        final String expectedErrorMessage = "TIME TRACKING ESTIMATE IS BLANK OMG!!!";

        final I18nHelper mockI18nHelper = getMock(I18nHelper.class);

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = getMock(FieldScreenRenderLayoutItem.class);

        expect(mockOperationContext.getFieldValuesHolder()).andStubReturn(fieldsValueHolder);
        expectThatTimeTrackingIsEnabled();

        expect(mockI18nHelper.getText("common.concepts.original.estimate")).andReturn("ESTIMATE");
        expect(mockI18nHelper.getText("issue.field.required", "ESTIMATE")).andReturn(expectedErrorMessage);

        expect(mockI18nHelper.getText("common.concepts.remaining.estimate")).andReturn("ESTIMATE");
        expect(mockI18nHelper.getText("issue.field.required", "ESTIMATE")).andReturn(expectedErrorMessage);

        expectThatTimeTrackingIsARequiredField(mockFieldScreenRenderLayoutItem);

        expectThatHasWorkStartedIsCalled();

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        ErrorCollection errorCollection = new SimpleErrorCollection();

        timeTrackingField.validateParams(mockOperationContext, errorCollection, mockI18nHelper, mockIssue,
                mockFieldScreenRenderLayoutItem);

        assertTrue(errorCollection.hasAnyErrors());

        final Map<String, String> fieldErrors = errorCollection.getErrors();
        assertNotNull(fieldErrors);

        assertTrue(fieldErrors.containsKey(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE));

        final String actualErrorMessageOriginalEstimate = fieldErrors.get(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE);
        assertNotNull(actualErrorMessageOriginalEstimate);
        assertEquals(actualErrorMessageOriginalEstimate, expectedErrorMessage);

        assertTrue(fieldErrors.containsKey(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE));

        final String actualErrorMessageRemainingEstimate = fieldErrors.get(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE);
        assertNotNull(actualErrorMessageRemainingEstimate);
        assertEquals(actualErrorMessageRemainingEstimate, expectedErrorMessage);
    }

    @Test
    public void testValidateParamsAddsAnErrorIfTheOriginalEstimateIsInvalid() throws Exception
    {
        OperationContext mockOperationContext = getMock(OperationContext.class);

        final String originalEstimateInput = "30hours 15min. This input will be validated as incorrect.";
        final String originalEstimateExpectedErrorMessage = "The Original Estimate Input is Incorrect";

        final String remainingEstimateInput = "30h. This input will be validated as correct.";

        Map fieldsValueHolder =
                EasyMap.build(IssueFieldConstants.TIMETRACKING, new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(new String[] { originalEstimateInput }).
                        setRemainingEstimate(new String[] { remainingEstimateInput }).
                        build());

        final I18nHelper mockI18nHelper = getMock(I18nHelper.class);

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = getMock(FieldScreenRenderLayoutItem.class);

        expect(mockOperationContext.getFieldValuesHolder()).andStubReturn(fieldsValueHolder);
        expectThatTimeTrackingIsEnabled();

        expectThatTimeTrackingIsNotARequiredField(mockFieldScreenRenderLayoutItem);

        expect(mockI18nHelper.getText("createissue.error.original.estimate.invalid")).andReturn(originalEstimateExpectedErrorMessage);

        expectThatHasWorkStartedIsCalled();

        TimeTrackingSystemField timeTrackingField = new TimeTrackingSystemField(mockTemplatingEngine,
                mockApplicationProperties, mockIssueManager, mockJiraAuthenticationContext, mockPermissionManager,
                mockJiraDurationUtils)
        {
            @Override
            protected boolean isDurationInvalid(final String duration)
            {
                if (duration.equals(originalEstimateInput)) {return true;}
                if (duration.equals(remainingEstimateInput)) {return false;}
                throw new IllegalArgumentException("This method only expects to receive the following duration values: "
                        + "originalEstimateInput = " + originalEstimateInput + ", remainingEstimateInput = "
                        + remainingEstimateInput + "." );
            }
        };

        replay();

        ErrorCollection errorCollection = new SimpleErrorCollection();

        timeTrackingField.validateParams(mockOperationContext, errorCollection, mockI18nHelper, mockIssue,
                mockFieldScreenRenderLayoutItem);

        assertTrue(errorCollection.hasAnyErrors());

        final Map<String, String> fieldErrors = errorCollection.getErrors();
        assertNotNull(fieldErrors);

        assertTrue(fieldErrors.containsKey(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE));

        final String originalEstimateActualErrorMessage = fieldErrors.get(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE);
        assertNotNull(originalEstimateActualErrorMessage);
        assertEquals(originalEstimateActualErrorMessage, originalEstimateExpectedErrorMessage);

        assertFalse(fieldErrors.containsKey(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE));
    }

    @Test
    public void testValidateParamsDoesNotAddAnErrorIfBothEstimatesAreValid() throws Exception
    {
        OperationContext mockOperationContext = getMock(OperationContext.class);

        final String originalEstimateInput = "30hours 15min. This input will be validated as correct.";
        final String remainingEstimateInput = "30h. This input will be validated as correct.";

        Map fieldsValueHolder =
                EasyMap.build(IssueFieldConstants.TIMETRACKING, new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(new String[] { originalEstimateInput }).
                        setRemainingEstimate(new String[] { remainingEstimateInput }).
                        build());

        final I18nHelper mockI18nHelper = getMock(I18nHelper.class);

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem = getMock(FieldScreenRenderLayoutItem.class);

        expect(mockOperationContext.getFieldValuesHolder()).andStubReturn(fieldsValueHolder);
        expectThatTimeTrackingIsEnabled();

        expectThatTimeTrackingIsNotARequiredField(mockFieldScreenRenderLayoutItem);

        expectThatHasWorkStartedIsCalled();

        TimeTrackingSystemField timeTrackingField = new TimeTrackingSystemField(mockTemplatingEngine,
                mockApplicationProperties, mockIssueManager, mockJiraAuthenticationContext, mockPermissionManager,
                mockJiraDurationUtils)
        {
            @Override
            protected boolean isDurationInvalid(final String duration)
            {
                if (duration.equals(originalEstimateInput) || duration.equals(remainingEstimateInput)) {return false;}
                throw new IllegalArgumentException("This method only expects to receive the following duration values: "
                        + "originalEstimateInput = " + originalEstimateInput + ", remainingEstimateInput = "
                        + remainingEstimateInput + "." );
            }
        };

        replay();

        ErrorCollection errorCollection = new SimpleErrorCollection();

        timeTrackingField.validateParams(mockOperationContext, errorCollection, mockI18nHelper, mockIssue,
                mockFieldScreenRenderLayoutItem);

        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testUpdateIssueSetsBothEstimatesWhenThereIsNoTargetSubField() throws Exception
    {
        FieldLayoutItem mockFieldLayoutItem = getMock(FieldLayoutItem.class);
        JiraAuthenticationContext mockAuthenticationContext = getMock(JiraAuthenticationContext.class);
        JiraDurationUtils jiraDurationUtils = getMock(JiraDurationUtils.class);

        final String originalEstimateInput = "30";
        final String remainingEstimateInput = "20";
        final long originalEstimateInputInMillis = 180000L;
        final long remainingEstimateInputInMillis = 120000L;


        mockIssue.setOriginalEstimate(60000L);
        mockIssue.setEstimate(60000L);

        Map fieldsValueHolder =
                EasyMap.build(IssueFieldConstants.TIMETRACKING, new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(new String[] { originalEstimateInput }).
                        setRemainingEstimate(new String[] { remainingEstimateInput }).
                        build());

        expect(mockJiraAuthenticationContext.getLocale()).andReturn(new Locale("en_UK")).times(2);
        expect(mockJiraDurationUtils.parseDuration(originalEstimateInput, new Locale("en_UK"))).andReturn(originalEstimateInputInMillis);
        expect(mockJiraDurationUtils.parseDuration(remainingEstimateInput, new Locale("en_UK"))).andReturn(remainingEstimateInputInMillis);

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        timeTrackingField.updateIssue(mockFieldLayoutItem, mockIssue, fieldsValueHolder);

        assertEquals(mockIssue.getOriginalEstimate(), new Long (originalEstimateInputInMillis));
        assertEquals(mockIssue.getEstimate(), new Long (remainingEstimateInputInMillis));

        final Map<String, ModifiedValue> modifiedFields = mockIssue.getModifiedFields();
        assertTrue(modifiedFields.containsKey(IssueFieldConstants.TIMETRACKING));
        final ModifiedValue modifiedValue = modifiedFields.get(IssueFieldConstants.TIMETRACKING);

        assertEquals(modifiedValue.getNewValue(),
                new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(originalEstimateInputInMillis).
                        setRemainingEstimate(remainingEstimateInputInMillis).
                        build());

        assertEquals(modifiedValue.getOldValue(),
                new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(60000L).
                        setRemainingEstimate(60000L).
                        build());
    }

    @Test
    public void testUpdateIssueSetsOnlyTheRemainingEstimateIfTheTargetSubFieldIndicatesIt() throws Exception
    {
        FieldLayoutItem mockFieldLayoutItem = getMock(FieldLayoutItem.class);

        final String originalEstimateInput = "30. This should not get set in the issue.";

        final String remainingEstimateInput = "20";
        final long remainingEstimateInputInMillis = 120000L;

        mockIssue.setOriginalEstimate(60000L);
        mockIssue.setEstimate(60000L);

        Map fieldsValueHolder = EasyMap.build(IssueFieldConstants.TIMETRACKING, new TimeTrackingSystemField.TimeTrackingValue.Builder().
                setInLegacyMode(false).
                setTargetSubField(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE).
                setOriginalEstimate(new String[] { originalEstimateInput }).
                setRemainingEstimate(new String[] { remainingEstimateInput }).
                build());

        expect(mockJiraAuthenticationContext.getLocale()).andReturn(new Locale("en_UK"));
        expect(mockJiraDurationUtils.parseDuration(remainingEstimateInput, new Locale("en_UK"))).andReturn(remainingEstimateInputInMillis);

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        timeTrackingField.updateIssue(mockFieldLayoutItem, mockIssue, fieldsValueHolder);

        assertEquals(mockIssue.getOriginalEstimate(), new Long (60000L));
        assertEquals(mockIssue.getEstimate(), new Long (remainingEstimateInputInMillis));

        final Map<String, ModifiedValue> modifiedFields = mockIssue.getModifiedFields();
        assertTrue(modifiedFields.containsKey(IssueFieldConstants.TIMETRACKING));
        final ModifiedValue modifiedValue = modifiedFields.get(IssueFieldConstants.TIMETRACKING);

        assertEquals(modifiedValue.getNewValue(),
                new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setTargetSubField(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE).
                        setRemainingEstimate(remainingEstimateInputInMillis).
                        build());

        assertEquals(modifiedValue.getOldValue(),
                new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setTargetSubField(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE).
                        setOriginalEstimate(60000L).
                        setRemainingEstimate(60000L).
                        build());
    }

    @Test
    public void testUpdateValueDoesNotAddAChangeHistoryItemIfThePreviousAndNewValueOfTheOriginalEstimateAreEqual()
            throws Exception
    {
        final Long originalEstimateInputInMillis = 60000L;
        final Long remainingEstimateInputInMillis = 120000L;

        final Long originalEstimateInitialValue = originalEstimateInputInMillis;
        final Long remainingEstimateInitialValue = 60000L;

        FieldLayoutItem mockFieldLayoutItem = getMock(FieldLayoutItem.class);

        final Issue mockIssue = getMock(Issue.class);

        final ModifiedValue modifiedTimeTrackingValue = new ModifiedValue(
                new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(originalEstimateInitialValue).
                        setRemainingEstimate(remainingEstimateInitialValue).
                        build(),
                new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(originalEstimateInputInMillis).
                        setRemainingEstimate(remainingEstimateInputInMillis).
                        build());

        final DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        timeTrackingField.updateValue(mockFieldLayoutItem, mockIssue, modifiedTimeTrackingValue, issueChangeHolder);

        final List changeItems = issueChangeHolder.getChangeItems();
        assertNotNull(changeItems);
        assertEquals(changeItems.size(), 1);

        assertFalse(changeItems.contains(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ORIGINAL_ESTIMATE,
                originalEstimateInitialValue.toString(), originalEstimateInitialValue.toString(),
                originalEstimateInputInMillis.toString(), originalEstimateInputInMillis.toString())));

        assertTrue(changeItems.contains(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ESTIMATE,
                remainingEstimateInitialValue.toString(), remainingEstimateInitialValue.toString(),
                remainingEstimateInputInMillis.toString(), remainingEstimateInputInMillis.toString())));
    }

    @Test
    public void testUpdateValueDoesNotAddAChangeHistoryItemIfThePreviousAndNewValueOfTheOriginalEstimateAreNull()
            throws Exception
    {
        final Long originalEstimateInputInMillis = null;
        final Long remainingEstimateInputInMillis = 120000L;

        final Long originalEstimateInitialValue = originalEstimateInputInMillis;
        final Long remainingEstimateInitialValue = 60000L;

        FieldLayoutItem mockFieldLayoutItem = getMock(FieldLayoutItem.class);

        final Issue mockIssue = getMock(Issue.class);

        final ModifiedValue modifiedTimeTrackingValue = new ModifiedValue(
                new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(originalEstimateInitialValue).
                        setRemainingEstimate(remainingEstimateInitialValue).
                        build(),
                new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(originalEstimateInputInMillis).
                        setRemainingEstimate(remainingEstimateInputInMillis).
                        build());

        final DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        timeTrackingField.updateValue(mockFieldLayoutItem, mockIssue, modifiedTimeTrackingValue, issueChangeHolder);

        final List changeItems = issueChangeHolder.getChangeItems();
        assertNotNull(changeItems);
        assertEquals(changeItems.size(), 1);

        assertFalse(changeItems.contains(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ESTIMATE,
                null, null,
                null, null)));

        assertTrue(changeItems.contains(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ESTIMATE,
                remainingEstimateInitialValue.toString(), remainingEstimateInitialValue.toString(),
                remainingEstimateInputInMillis.toString(), remainingEstimateInputInMillis.toString())));
    }

    @Test
    public void testUpdateValueAddsOneChangeHistoryItemWhenOriginalEstimateHasBeenSpecifiedAsTheTargetSubField()
            throws Exception
    {
        final Long originalEstimateInputInMillis = 300000L;
        final Long remainingEstimateInputInMillis = 120000L; // Should not cause the creation of a change item

        final Long originalEstimateInitialValue = 60000L;
        final Long remainingEstimateInitialValue = 60000L; // Should not cause the creation of a change item

        FieldLayoutItem mockFieldLayoutItem = getMock(FieldLayoutItem.class);

        final Issue mockIssue = getMock(Issue.class);

        final ModifiedValue modifiedTimeTrackingValue = new ModifiedValue(
                new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setTargetSubField(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE).
                        setOriginalEstimate(originalEstimateInitialValue).
                        setRemainingEstimate(remainingEstimateInitialValue).
                        build(),
                new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setTargetSubField(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE).
                        setOriginalEstimate(originalEstimateInputInMillis).
                        setRemainingEstimate(remainingEstimateInputInMillis).
                        build());

        final DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        timeTrackingField.updateValue(mockFieldLayoutItem, mockIssue, modifiedTimeTrackingValue, issueChangeHolder);

        final List changeItems = issueChangeHolder.getChangeItems();
        assertNotNull(changeItems);
        assertEquals(changeItems.size(), 1);

        assertTrue(changeItems.contains(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ORIGINAL_ESTIMATE,
                originalEstimateInitialValue.toString(), originalEstimateInitialValue.toString(),
                originalEstimateInputInMillis.toString(), originalEstimateInputInMillis.toString())));

        assertFalse(changeItems.contains(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ESTIMATE,
                remainingEstimateInitialValue.toString(), remainingEstimateInitialValue.toString(),
                remainingEstimateInputInMillis.toString(), remainingEstimateInputInMillis.toString())));
    }

    @Test
    public void testUpdateValueAddsTwoChangeHistoryItemsWhenBothEstimatesHaveChanged() throws Exception
    {
        final Long originalEstimateInputInMillis = 300000L;
        final Long remainingEstimateInputInMillis = 120000L; // Should not cause the creation of a change item

        final Long originalEstimateInitialValue = 60000L;
        final Long remainingEstimateInitialValue = 60000L; // Should not cause the creation of a change item

        FieldLayoutItem mockFieldLayoutItem = getMock(FieldLayoutItem.class);

        final Issue mockIssue = getMock(Issue.class);

        final ModifiedValue modifiedTimeTrackingValue = new ModifiedValue(
                new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(originalEstimateInitialValue).
                        setRemainingEstimate(remainingEstimateInitialValue).
                        build(),
                new TimeTrackingSystemField.TimeTrackingValue.Builder().
                        setInLegacyMode(false).
                        setOriginalEstimate(originalEstimateInputInMillis).
                        setRemainingEstimate(remainingEstimateInputInMillis).
                        build());

        final DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        timeTrackingField.updateValue(mockFieldLayoutItem, mockIssue, modifiedTimeTrackingValue, issueChangeHolder);

        final List changeItems = issueChangeHolder.getChangeItems();
        assertNotNull(changeItems);
        assertEquals(changeItems.size(), 2);

        assertTrue(changeItems.contains(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ORIGINAL_ESTIMATE,
                originalEstimateInitialValue.toString(), originalEstimateInitialValue.toString(),
                originalEstimateInputInMillis.toString(), originalEstimateInputInMillis.toString())));

        assertTrue(changeItems.contains(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ESTIMATE,
                remainingEstimateInitialValue.toString(), remainingEstimateInitialValue.toString(),
                remainingEstimateInputInMillis.toString(), remainingEstimateInputInMillis.toString())));
    }

    @Test
    public void testIsShownReturnsFalseIfTimeTrackingIsDisabled() throws Exception
    {
        final Issue mockIssue = getMock(Issue.class);

        expectThatTimeTrackingIsDisabled();

        TimeTrackingSystemField timeTrackingField = instantiate(TimeTrackingSystemField.class);

        assertFalse(timeTrackingField.isShown(mockIssue));
    }

    private void expectThatTimeTrackingIsEnabled()
    {
        expect(mockApplicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)).andReturn(true);
    }

    private void expectThatTimeTrackingIsDisabled()
    {
        expect(mockApplicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)).andReturn(false);
    }

    private void expectThatHasWorkStartedIsCalled() throws Exception
    {
        expect(mockIssueManager.getEntitiesByIssueObject("IssueWorklog", mockIssue)).andReturn(Collections.<GenericValue>emptyList());
    }

    private void expectThatTimeTrackingLegacyModeIsTurnedOff()
    {
        expect(mockApplicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR)).andReturn(false);
    }

    private void expectThatTimeTrackingIsARequiredField(final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem)
    {
        expect(mockFieldScreenRenderLayoutItem.isRequired()).andReturn(true);
    }

    private void expectThatTimeTrackingIsNotARequiredField(final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem)
    {
        expect(mockFieldScreenRenderLayoutItem.isRequired()).andReturn(false);
    }

    private void assertContainsTimeTrackingValue(final Map fieldsValueHolder)
    {
        assertTrue(fieldsValueHolder.containsKey(IssueFieldConstants.TIMETRACKING));

        final Object timeTrackingObjectValue = fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);
        assertNotNull(timeTrackingObjectValue);
        assertTrue(timeTrackingObjectValue instanceof TimeTrackingSystemField.TimeTrackingValue);
    }
}
