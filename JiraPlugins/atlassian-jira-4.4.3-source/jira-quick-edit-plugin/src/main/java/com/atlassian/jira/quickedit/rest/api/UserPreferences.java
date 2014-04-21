package com.atlassian.jira.quickedit.rest.api;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides user preferences around quick edit & create.  Determines if welcome screen needs to be shown and if we
 * should use quick-edit vs complete edit.
 *
 * @since v5.0
 */
@XmlRootElement
public class UserPreferences
{
    @XmlElement (name = "showWelcomeScreen")
    private boolean showWelcomeScreen;
    @XmlElement (name = "useQuickForm")
    private boolean useQuickForm;
    @XmlElement (name = "fields")
    private List<String> fields = new ArrayList<String>();

    private UserPreferences() {}

    private UserPreferences(final boolean showWelcomeScreen, final boolean useQuickForm, final List<String> fields)
    {
        this.showWelcomeScreen = showWelcomeScreen;
        this.useQuickForm = useQuickForm;
        this.fields = fields;
    }

    public boolean isShowWelcomeScreen()
    {
        return showWelcomeScreen;
    }

    public boolean isUseQuickForm()
    {
        return useQuickForm;
    }

    public List<String> getFields()
    {
        return fields;
    }

    public static class Builder
    {
        private boolean showWelcomeScreen = true;
        private boolean useQuickForm = true;
        private List<String> fields;

        public Builder() {}


        public Builder showWelcomeScreen(final boolean showWelcomeScreen)
        {
            this.showWelcomeScreen = showWelcomeScreen;
            return this;
        }

        public Builder useQuickForm(final boolean useQuickForm)
        {
            this.useQuickForm = useQuickForm;
            return this;
        }

        public Builder fields(final List<String> fields)
        {
            this.fields = fields;
            return this;
        }

        public UserPreferences build()
        {
            return new UserPreferences(showWelcomeScreen, useQuickForm, fields);
        }

    }
}
