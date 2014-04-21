package com.atlassian.jira.quickedit.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.quickedit.rest.api.UserPreferences;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the UserPreferencesStore interface that persists edit & create fields for a user in the user's
 * propertyset as a comma separated list of field ids.
 *
 * @since v5.0
 */
public class UserPreferencesStoreImpl implements UserPreferencesStore
{
    private static final List<String> EDIT_FIELD_DEFAULTS = new ArrayList<String>();

    static
    {
        //from stats Spuddy did based on JDOG usage.
        EDIT_FIELD_DEFAULTS.add(IssueFieldConstants.FIX_FOR_VERSIONS);
        EDIT_FIELD_DEFAULTS.add(IssueFieldConstants.ASSIGNEE);
        EDIT_FIELD_DEFAULTS.add(IssueFieldConstants.LABELS);
        EDIT_FIELD_DEFAULTS.add(IssueFieldConstants.COMPONENTS);
        EDIT_FIELD_DEFAULTS.add(IssueFieldConstants.PRIORITY);
        EDIT_FIELD_DEFAULTS.add(IssueFieldConstants.COMMENT);
    }

    private static final List<String> CREATE_FIELD_DEFAULTS = new ArrayList<String>();

    static
    {
        CREATE_FIELD_DEFAULTS.add(IssueFieldConstants.SUMMARY);
        CREATE_FIELD_DEFAULTS.add(IssueFieldConstants.DESCRIPTION);
        CREATE_FIELD_DEFAULTS.add(IssueFieldConstants.PRIORITY);
        CREATE_FIELD_DEFAULTS.add(IssueFieldConstants.AFFECTED_VERSIONS);
        CREATE_FIELD_DEFAULTS.add(IssueFieldConstants.COMPONENTS);
    }

    private static final String FIELD_SEPARATOR = ",";
    private final UserPropertyManager userPropertyManager;

    public UserPreferencesStoreImpl(final UserPropertyManager userPropertyManager)
    {
        this.userPropertyManager = userPropertyManager;
    }

    @Override
    public UserPreferences getEditUserPreferences(final User loggedInUser)
    {
        final PropertySet ps = userPropertyManager.getPropertySet(loggedInUser);
        final UserPreferences.Builder builder = new UserPreferences.Builder();
        builder.showWelcomeScreen(getBooleanWithDefault(ps, SHOW_WELCOME_SCREEN_KEY, true)).
                useQuickForm(getBooleanWithDefault(ps, USE_QUICK_EDIT_KEY, true)).
                fields(getEditFields(loggedInUser));

        return builder.build();
    }

    @Override
    public void storeEditUserPreferences(final User loggedInUser, final UserPreferences prefs)
    {
        Assertions.notNull("prefs", prefs);

        final PropertySet ps = userPropertyManager.getPropertySet(loggedInUser);
        ps.setBoolean(SHOW_WELCOME_SCREEN_KEY, prefs.isShowWelcomeScreen());
        ps.setBoolean(USE_QUICK_EDIT_KEY, prefs.isUseQuickForm());
        setFieldsForUser(loggedInUser, QUICK_EDIT_FIELDS, prefs.getFields());
    }

    @Override
    public UserPreferences getCreateUserPreferences(final User loggedInUser)
    {
        final PropertySet ps = userPropertyManager.getPropertySet(loggedInUser);
        final UserPreferences.Builder builder = new UserPreferences.Builder();
        builder.showWelcomeScreen(getBooleanWithDefault(ps, SHOW_WELCOME_SCREEN_KEY, true)).
                useQuickForm(getBooleanWithDefault(ps, USE_QUICK_CREATE_KEY, true)).
                fields(getCreateFields(loggedInUser));

        return builder.build();
    }

    @Override
    public void storeCreateUserPreferences(final User loggedInUser, final UserPreferences prefs)
    {
        Assertions.notNull("prefs", prefs);

        final PropertySet ps = userPropertyManager.getPropertySet(loggedInUser);
        ps.setBoolean(SHOW_WELCOME_SCREEN_KEY, prefs.isShowWelcomeScreen());
        ps.setBoolean(USE_QUICK_CREATE_KEY, prefs.isUseQuickForm());
        setFieldsForUser(loggedInUser, QUICK_CREATE_FIELDS, prefs.getFields());
    }

    private List<String> getEditFields(final User loggedInUser)
    {
        List<String> fieldsForUser = getFieldsForUser(loggedInUser, QUICK_EDIT_FIELDS);
        if (fieldsForUser.isEmpty())
        {
            return Collections.unmodifiableList(EDIT_FIELD_DEFAULTS);
        }
        return fieldsForUser;
    }

    private List<String> getCreateFields(final User loggedInUser)
    {
        List<String> fieldsForUser = getFieldsForUser(loggedInUser, QUICK_CREATE_FIELDS);
        if (fieldsForUser.isEmpty())
        {
            return Collections.unmodifiableList(CREATE_FIELD_DEFAULTS);
        }
        return fieldsForUser;
    }

    private void setFieldsForUser(final User loggedInUser, final String key, final List<String> fields)
    {
        final StringBuilder values = new StringBuilder();
        if (fields != null)
        {
            Iterator<String> iterator = fields.iterator();
            while (iterator.hasNext())
            {
                values.append(iterator.next());
                if (iterator.hasNext())
                {
                    values.append(FIELD_SEPARATOR);
                }
            }
        }

        final PropertySet userProperties = userPropertyManager.getPropertySet(loggedInUser);
        userProperties.setText(key, values.toString());
    }

    private List<String> getFieldsForUser(final User loggedInUser, final String key)
    {
        final PropertySet userProperties = userPropertyManager.getPropertySet(loggedInUser);
        final String userFields = userProperties.getText(key);
        final List<String> ret = new ArrayList<String>();
        if (StringUtils.isNotBlank(userFields))
        {
            ret.addAll(Arrays.asList(userFields.split(FIELD_SEPARATOR)));
        }
        return Collections.unmodifiableList(ret);
    }

    private boolean getBooleanWithDefault(final PropertySet ps, final String key, final boolean defaultValue)
    {
        if (ps.exists(key))
        {
            return ps.getBoolean(key);
        }
        return defaultValue;
    }

}
