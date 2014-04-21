/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue.navigator;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ParameterStore;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Iterator;

public class DateRangePicker extends JiraWebActionSupport
{
    private static final int SECONDS_IN_DAY = 24 * 60 * 60;
    private static final String TYPE_OVERDUE = "TYPE_OVERDUE";
    private static final String END_DATE_DAYS_AGO = "END_DATE_DAYS_AGO";
    private static final String TYPE_DUEINNEXT = "TYPE_DUEINNEXT";
    private static final String TYPE_GENERIC = "TYPE_GENERIC";
    private static final String AND_OVERDUE = "AND";
    private static final String AND_NOT_OVERDUE = "AND_NOT";
    private static final String START_DATE_IN_PAST = "START_DATE_IN_PAST";
    private static final String START_DATE_DAYS_AGO = "START_DATE_DAYS_AGO";

    private static final String FIELD_NAME_FIELD_ID = "fieldId";
    private static final String FIELD_NAME_FORM_NAME = "formName";
    private static final String FIELD_NAME_NEXT_FIELD_NAME = "nextFieldName";
    private static final String FIELD_NAME_PREVIOUS_FIELD_NAME = "previousFieldName";

    private static final String KEY_FIELD_INVALID = "popups.daterange.field.invalid";
    private static final String KEY_FIELD_NOT_SET = "popups.daterange.field.not.set";

    private String formName;
    private String fieldId;
    private String fieldName;
    private String previousFieldName;
    private String nextFieldName;
    private String previousFieldValue;
    private String nextFieldValue;
    private String andOverdue;
    private String dueInNext;
    private String selectedType = TYPE_GENERIC;
    private String endDateDaysAgo;

    private String startDateDaysAgo;

    private final JiraAuthenticationContext authenticationContext;
    private final FieldManager fieldManager;

    public DateRangePicker(JiraAuthenticationContext authenticationContext, FieldManager fieldManager)
    {
        this.authenticationContext = authenticationContext;
        this.fieldManager = fieldManager;
    }

    /**
     * Performs the validation that checks that required fields are set with valid values.
     */
    protected void doValidation()
    {
        super.doValidation();
        if (isSetAndNotEmpty(FIELD_NAME_FIELD_ID, fieldId))
        {
            String fieldId = getFieldId();
            if (!isCustomField() && fieldManager.getField(fieldId) == null)
            {
                addErrorMessage(getText(KEY_FIELD_INVALID, fieldId));
            }
        }
        isSetAndNotEmpty(FIELD_NAME_FORM_NAME, formName);
        isSetAndNotEmpty(FIELD_NAME_NEXT_FIELD_NAME, nextFieldName);
        isSetAndNotEmpty(FIELD_NAME_PREVIOUS_FIELD_NAME, previousFieldName);
    }

    /**
     * Returns true if the value passed is not null and not empty string. Otherwise adds an error and returns false.
     *
     * @param name  field name
     * @param value field value
     * @return true if not null and not empty string, false otherwise
     */
    private boolean isSetAndNotEmpty(String name, String value)
    {
        boolean setAndNotEmpty = value != null && value.length() > 0;
        if (!setAndNotEmpty)
        {
            addErrorMessage(getText(KEY_FIELD_NOT_SET, name));
        }
        return setAndNotEmpty;
    }

    protected String doExecute() throws Exception
    {
        // Set the field name
        if (isCustomField())
        {
            CustomField customField = fieldManager.getCustomField(getFieldId());
            setFieldName(customField.getName());
        }
        else
        {
            Field field = fieldManager.getField(getFieldId());
            setFieldName(field.getName());
        }

        // If the picker was called with no values
        if (StringUtils.isEmpty(getPreviousFieldValue()) && StringUtils.isEmpty(getNextFieldValue()))
        {
            // Nothing to do just draw the picker
            return getResult();
        }

        long nextVal;
        long previousVal;
        long nextValDays;
        try
        {
            // try to see if the passed in values are valid
            nextVal = DateUtils.getDurationWithNegative(getNextFieldValue());
            nextValDays = nextVal / SECONDS_IN_DAY;
            previousVal = DateUtils.getDurationWithNegative(getPreviousFieldValue());
        }
        catch (InvalidDurationException e)
        {
            // Invalid values were passed. Ignore them.
            previousFieldValue = "";
            nextFieldValue = "";
            log.debug("Invalid parameters passed from the caller page. Ignoring them...");
            return getResult();
        }

        // Checks for the time period drop down
        if (!isDueDate() && StringUtils.isNotEmpty(getPreviousFieldValue()) && isSelectListValue(previousVal))
        {
            selectedType = START_DATE_IN_PAST;
            previousFieldValue = DateUtils.getDurationStringWithNegative(previousVal);
            return getResult();
        }

        if (!isDueDate() && StringUtils.isNotEmpty(getPreviousFieldValue()) && previousVal < 0 && isDayValue(previousVal))
        {
            selectedType = START_DATE_DAYS_AGO;
            startDateDaysAgo = String.valueOf(-previousVal / SECONDS_IN_DAY);
            return getResult();
        }

        // Note bits below here has funny logic... Leaving as "legacy" until someoone actually rasises an issue
        // No 'from' bound
        if (StringUtils.isEmpty(getPreviousFieldValue()))
        {
            // If the 'to' bound is set
            if (StringUtils.isNotEmpty(getNextFieldValue()))
            {
                // The 'to' bound is 0 (now)
                if (nextVal == 0 && isDueDate())
                {
                    setNextFieldValue("");
                    selectedType = TYPE_OVERDUE;
                    return getResult();
                }

                // The 'to' bound is negative (before now)
                else if (nextVal < 0)
                {
                    // The 'to' bound can be expressed as days exactly
                    if (isDayValue(nextVal))
                    {
                        setNextFieldValue("");
                        selectedType = END_DATE_DAYS_AGO;
                        this.endDateDaysAgo = "" + -nextValDays;
                        return getResult();
                    }
                }
            }
        }

        // The 'to' bound is positive (after now)
        if (nextVal > 0)
        {
            // The 'to' bound can be expressed as days exactly
            if (isDayValue(nextVal))
            {
                // No 'from' bound
                if (StringUtils.isEmpty(getPreviousFieldValue()))
                {
                    setNextFieldValue("");
                    setAndOverdue(AND_OVERDUE);
                    selectedType = TYPE_DUEINNEXT;
                    this.dueInNext = "" + nextValDays;
                    return getResult();
                }

                // The 'from' bound is 0 (now)
                else if (previousVal == 0)
                {
                    setNextFieldValue("");
                    setPreviousFieldValue("");
                    setAndOverdue(AND_NOT_OVERDUE);
                    selectedType = TYPE_DUEINNEXT;
                    this.dueInNext = "" + nextValDays;
                    return getResult();
                }
            }
        }

        // Default case
        selectedType = TYPE_GENERIC;
        return getResult();
    }

    public boolean isCustomField()
    {
        return fieldManager.isCustomField(getFieldId());
    }

    private boolean isSelectListValue(long previousVal)
    {
        final Collection timePeriods = getTimePeriods();
        for (Iterator iterator = timePeriods.iterator(); iterator.hasNext();)
        {
            Option option = (Option) iterator.next();
            try
            {
                final long optionValue = DateUtils.getDurationWithNegative(option.getId());
                if (optionValue == previousVal)
                {
                    return true;
                }
            }
            catch (InvalidDurationException e)
            {
                throw new IllegalArgumentException("Option " + option.getId() + " is not a valid period. Fix up the ParameterStire code");
            }

        }

        return false;
    }

    private boolean isDayValue(long nextVal)
    {
        return nextVal % SECONDS_IN_DAY == 0;
    }

    public String getResult()
    {
        if (isDueDate())
        {
            return "duedate";
        }
        else
        {
            return "generic";
        }
    }

    private boolean isDueDate()
    {
        return DocumentConstants.ISSUE_DUEDATE.equals(fieldId);
    }

    public String getFieldId()
    {
        return fieldId;
    }

    public void setFieldId(String fieldId)
    {
        this.fieldId = fieldId;
    }

    public String getEndDateDaysAgo()
    {
        return endDateDaysAgo;
    }

    public String getDueInNext()
    {
        return dueInNext;
    }

    /**
     * Get the name of the calling form
     *
     * @return form name
     */
    public String getFormName()
    {
        return formName;
    }

    /**
     * Set the name of the calling form
     *
     * @param formName form name
     */
    public void setFormName(String formName)
    {
        this.formName = formName;
    }

    public String getPreviousFieldName()
    {
        return previousFieldName;
    }

    public void setPreviousFieldName(String previousFieldName)
    {
        this.previousFieldName = previousFieldName;
    }

    public String getNextFieldName()
    {
        return nextFieldName;
    }

    public void setNextFieldName(String nextFieldName)
    {
        this.nextFieldName = nextFieldName;
    }

    public String getPreviousFieldValue()
    {
        return previousFieldValue;
    }

    public void setPreviousFieldValue(String previousFieldValue)
    {
        this.previousFieldValue = previousFieldValue;
    }

    public String getNextFieldValue()
    {
        return nextFieldValue;
    }

    public void setNextFieldValue(String nextFieldValue)
    {
        this.nextFieldValue = nextFieldValue;
    }

    public String getSelectedType()
    {
        return selectedType;
    }

    public String getAndOverdue()
    {
        return andOverdue;
    }

    public void setAndOverdue(String andOverdue)
    {
        this.andOverdue = andOverdue;
    }

    public String getStartDateDaysAgo()
    {
        return startDateDaysAgo;
    }

    public void setStartDateDaysAgo(String startDateDaysAgo)
    {
        this.startDateDaysAgo = startDateDaysAgo;
    }

    public Collection getTimePeriods()
    {
        ParameterStore parameterStore = new ParameterStore(authenticationContext.getLoggedInUser());
        return parameterStore.getTimePeriods();
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }
}
