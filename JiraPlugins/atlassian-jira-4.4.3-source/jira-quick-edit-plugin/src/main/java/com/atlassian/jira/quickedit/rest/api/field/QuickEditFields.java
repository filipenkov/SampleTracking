package com.atlassian.jira.quickedit.rest.api.field;

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
 * Returns a list of ordered {@link QuickEditField}s to be used to render the quick edit & create forms.  The fields are
 * in the same order as on the normal edit screen.
 * <p/>
 * This object also contains a list of field ids that specify which fields should be visible in the quick edit dialog.
 * <p/>
 * Use the {@link Builder} to construct an instance of QuickEditFields.
 *
 * @since v5.0
 */
@XmlRootElement
public class QuickEditFields
{
    @XmlElement (name = "fields")
    private List<QuickEditField> fields = new ArrayList<QuickEditField>();
    @XmlElement (name = "userPreferences")
    private UserPreferences userPreferences;

    @XmlElement (name = "sortedTabs")
    private List<TabWithLabels> sortedTabs = new ArrayList<TabWithLabels>();

    @XmlElement (name = "issueKey")
    private String issueKey;

    private QuickEditFields() {}

    private QuickEditFields(final List<QuickEditField> fields, final UserPreferences userPreferences, final Collection<TabWithLabels> sortedTabs, final String createdIssueKey)
    {
        this.fields.addAll(fields);
        this.userPreferences = userPreferences;
        this.sortedTabs.addAll(sortedTabs);
        this.issueKey = createdIssueKey;
    }

    public List<QuickEditField> getFields()
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
     * Once constructed simply add fields by calling the {@link #addField(QuickEditField)} method. This should be called
     * for all fields visible for the given project & issue type. Finally call the {@link
     * #build(com.atlassian.jira.quickedit.rest.api.UserPreferences)} method with the list of fields the user has chosen
     * to display in the quick edit/create dialog to construct the actual object.
     */
    public static class Builder
    {
        private List<QuickEditField> fields = new ArrayList<QuickEditField>();
        private String createdIssueKey;

        public Builder addField(final QuickEditField field)
        {
            fields.add(field);
            return this;
        }

        public Builder createdIssue(final String key)
        {
            createdIssueKey = key;
            return this;
        }


        public QuickEditFields build(final UserPreferences prefs)
        {
            final SortedMap<Integer, TabWithLabels> positionToTabMap = new TreeMap<Integer, TabWithLabels>();
            //need to build up the sorted list of tabs
            for (QuickEditField field : fields)
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
            return new QuickEditFields(fields, prefs, positionToTabMap.values(), createdIssueKey);
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
