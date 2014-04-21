package com.atlassian.jira.functest.framework.navigator;

import com.meterware.httpunit.WebForm;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;

/**
 * A parameter that can be used work with User/Group selector navigator components.
 *
 * @since v4.0
 */
public class UserGroupPicker implements NavigatorCondition
{
    public static final String OPTION_ANY_USER = "";
    public static final String OPTION_SPECIFIC_USER = "specificuser";
    public static final String OPTION_CURRENT_USER = "issue_current_user";
    public static final String OPTION_SPECIFIC_GROUP = "specificgroup";

    private String paramName;
    private PickerOption pickerOption;

    public UserGroupPicker(UserGroupPicker picker)
    {
        this.paramName = picker.paramName;
        this.pickerOption = picker.pickerOption;
    }

    public UserGroupPicker(String paramName)
    {
        setParamName(paramName);
    }

    public UserGroupPicker setParamName(final String paramName)
    {
        this.paramName = paramName;
        return this;
    }

    public String getParamName()
    {
        return paramName;
    }

    public String getSelectParamName()
    {
        return this.paramName + "Select";
    }

    public PickerOption getPickerOption()
    {
        return pickerOption;
    }

    public UserGroupPicker setPickerOption(final PickerOption pickerOption)
    {
        this.pickerOption = pickerOption;
        return this;
    }

    public UserGroupPicker setAnyUser()
    {
        setPickerOption(new PickerOption(OPTION_ANY_USER));
        return this;
    }
    
    public boolean isAnyUser()
    {
        return isOptionSet(OPTION_ANY_USER);
    }
    
    public UserGroupPicker setCurrentUser()
    {
        setPickerOption(new PickerOption(OPTION_CURRENT_USER));
        return this;
    }

    public boolean isCurrentUser()
    {
        return isOptionSet(OPTION_CURRENT_USER);
    }

    public UserGroupPicker setUser(String user)
    {
        return setPickerOption(new PickerOption(OPTION_SPECIFIC_USER, user));
    }

    public String getUser()
    {
        return getValueIfOptionSet(OPTION_SPECIFIC_USER);
    }

    public UserGroupPicker setGroup(String group)
    {
        return setPickerOption(new PickerOption(OPTION_SPECIFIC_GROUP, group));
    }

    public String getGroup()
    {
        return getValueIfOptionSet(OPTION_SPECIFIC_GROUP);
    }

    String getValueIfOptionSet(final String option)
    {
        if (isOptionSet(option))
        {
            return getPickerOption().getValue();
        }
        else
        {
            return null;
        }
    }

    boolean isOptionSet(final String optionValue)
    {
        return getPickerOption() != null && optionValue.equals(getPickerOption().getOption());
    }

    public void setForm(final WebTester tester)
    {
        if (getParamName() == null || getPickerOption() == null || getPickerOption().getOption() == null)
        {
            throw new IllegalStateException();
        }

        tester.setFormElement(getSelectParamName(), getPickerOption().getOption());
        if (getPickerOption().getValue() != null)
        {
            tester.setFormElement(getParamName(), getPickerOption().getValue());
        }
    }

    public void parseCondition(final WebTester tester)
    {
        final WebForm form = tester.getDialog().getForm();
        setPickerOption(getPickerOption(form));
    }

    public void assertSettings(final WebTester tester)
    {
        final WebForm form = tester.getDialog().getForm();
        final PickerOption currentPicker = getPickerOption(form);

        Assert.assertEquals("User/Group picker not set correctly: " + paramName, pickerOption, currentPicker);
    }

    private PickerOption getPickerOption(final WebForm form)
    {
        PickerOption option = null;

        final String selectValue = StringUtils.trimToNull(form.getParameterValue(getSelectParamName()));
        if (selectValue != null)
        {
            final String value = StringUtils.trimToNull(form.getParameterValue(getParamName()));
            option = new PickerOption(selectValue, value);
        }
        return option;
    }

    public NavigatorCondition copyCondition()
    {
        return new UserGroupPicker(this);
    }

    public NavigatorCondition copyConditionForParse()
    {
        return new UserGroupPicker(this.paramName);
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder("User/Group Picker");
        if (paramName != null)
        {
            buffer.append('(').append(paramName).append(')');
        }

        if (pickerOption != null)
        {
            buffer.append(" = ").append(pickerOption);
        }

        return buffer.toString();
    }

    public static class PickerOption
    {
        private final String option;
        private final String value;

        public PickerOption(PickerOption pickerOption)
        {
            this.option = pickerOption.option;
            this.value = pickerOption.value;
        }
        
        public PickerOption(String option, String value)
        {
            this.option = option;
            this.value = value;
        }

        public String getOption()
        {
            return option;
        }

        public String getValue()
        {
            return value;
        }

        public PickerOption(String option)
        {
            this(option, null);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final PickerOption that = (PickerOption) o;

            if (option != null ? !option.equals(that.option) : that.option != null)
            {
                return false;
            }
            if (value != null ? !value.equals(that.value) : that.value != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = option != null ? option.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder("[");
            builder.append(option);
            if (value != null)
            {
                builder.append(", ").append(value);
            }
            builder.append("]");
            return builder.toString();
        }
    }
}
