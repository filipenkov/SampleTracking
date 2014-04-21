package com.atlassian.jira.quickedit.rest.api.field;

import com.atlassian.jira.issue.fields.rest.json.beans.FieldHtmlBean;
import com.atlassian.jira.quickedit.rest.api.UserPreferences;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Returns a list of ordered {@link FieldHtmlBean}s to be used to render the quick edit & create forms.  The fields are
 * in the same order as on the normal edit screen.
 * <p/>
 * This object also contains a list of field ids that specify which fields should be visible in the quick edit dialog.
 * <p/>
 * Use the {@link Builder} to construct an instance of FieldHtmlBeans.
 *
 * @since v5.0
 */
@XmlRootElement
public class QuickEditFields
{
    @XmlElement (name = "fields")
    private List<FieldHtmlBean> fields = new ArrayList<FieldHtmlBean>();
    @XmlElement (name = "userPreferences")
    private UserPreferences userPreferences;

    @XmlElement (name = "sortedTabs")
    private List<TabWithLabels> sortedTabs = new ArrayList<TabWithLabels>();

    @XmlElement (name = "issueKey")
    private String issueKey;

    @XmlElement (name = "atl_token")
    private String atlToken;

    private QuickEditFields() {}

    private QuickEditFields(final List<FieldHtmlBean> fields, final UserPreferences userPreferences, final Collection<TabWithLabels> sortedTabs, final String createdIssueKey, final String atlToken)
    {
        this.atlToken = atlToken;
        this.fields.addAll(fields);
        this.userPreferences = userPreferences;
        this.sortedTabs.addAll(sortedTabs);
        this.issueKey = createdIssueKey;
    }

    public List<FieldHtmlBean> getFields()
    {
        return fields;
    }

    public UserPreferences getUserPreferences()
    {
        return userPreferences;
    }

    public List<TabWithLabels> getSortedTabs()
    {
        return sortedTabs;
    }

    /**
     * Builder used to construct an immutable {@link QuickEditFields} object.
     * <p/>
     * Once constructed simply add fields by calling the {@link #addField(FieldHtmlBean)} method. This should be called
     * for all fields visible for the given project & issue type. Finally call the {@link
     * #build(com.atlassian.jira.quickedit.rest.api.UserPreferences, String)} method with the list of fields the user has chosen
     * to display in the quick edit/create dialog to construct the actual object.
     */
    public static class Builder
    {
        private List<FieldHtmlBean> fields = new ArrayList<FieldHtmlBean>();
        private String createdIssueKey;

        public Builder addFields(final List<FieldHtmlBean> fields)
        {
            for (FieldHtmlBean field : fields)
            {
                addField(field);
            }
            return this;
        }

        public Builder addField(final FieldHtmlBean field)
        {
            fields.add(field);
            return this;
        }

        public Builder createdIssue(final String key)
        {
            createdIssueKey = key;
            return this;
        }


        public QuickEditFields build(final UserPreferences prefs, final String atlToken)
        {
            final SortedMap<Integer, TabWithLabels> positionToTabMap = new TreeMap<Integer, TabWithLabels>();
            //need to build up the sorted list of tabs
            for (FieldHtmlBean field : fields)
            {
                if (field.getTab() != null)
                {
                    int position = field.getTab().getPosition();
                    if (!positionToTabMap.containsKey(position))
                    {
                        positionToTabMap.put(position, new TabWithLabels(field.getTab().getLabel()));
                    }

                    positionToTabMap.get(position).add(new TabWithLabels.Field(field.getLabel(), field.getId()));
                }
            }
            return new QuickEditFields(fields, prefs, positionToTabMap.values(), createdIssueKey, atlToken);
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("fields", fields).
                append("userPreferences", userPreferences).
                append("sortedTabs", sortedTabs).
                toString();
    }
}
