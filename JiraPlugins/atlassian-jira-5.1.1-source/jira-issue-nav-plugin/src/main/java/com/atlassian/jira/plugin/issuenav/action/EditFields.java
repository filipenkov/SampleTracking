package com.atlassian.jira.plugin.issuenav.action;

import com.atlassian.jira.issue.fields.rest.json.beans.FieldHtmlBean;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Returns a list of ordered {@link FieldHtmlBean}s to be used to render the quick edit & create forms.  The fields are
 * in the same order as on the normal edit screen.
 * <p/>
 * This object also contains a list of field ids that specify which fields should be visible in the quick edit dialog.
 * <p/>
 *
 * @since v5.0.3
 */
@XmlRootElement
public class EditFields
{
    @XmlElement (name = "fields")
    private List<FieldHtmlBean> fields = new ArrayList<FieldHtmlBean>();

    @XmlElement (name = "atl_token")
    private String atlToken;

    @XmlElement (name = "errorCollection")
    private ErrorCollection errorCollection;

    protected EditFields() {}

    public EditFields(final String atlToken, final ErrorCollection errorCollection)
    {
        this(Collections.<FieldHtmlBean>emptyList(), atlToken, errorCollection);
    }

    public EditFields(final List<FieldHtmlBean> fields, final String atlToken, final ErrorCollection errorCollection)
    {
        this.atlToken = atlToken;
        this.errorCollection = errorCollection;
        this.fields.addAll(fields);
    }

    public List<FieldHtmlBean> getFields()
    {
        return fields;
    }

    public ErrorCollection getErrorCollection()
    {
        return errorCollection;
    }

    public String getAtlToken()
    {
        return atlToken;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("fields", fields).
                append("errors", errorCollection).
                toString();
    }
}
