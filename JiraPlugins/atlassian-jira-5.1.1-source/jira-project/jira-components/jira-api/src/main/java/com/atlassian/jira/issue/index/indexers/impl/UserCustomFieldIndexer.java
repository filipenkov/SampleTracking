package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A simple version custom field indexer for the SingleUserPicker custom field.
 * The multiuser indexer is defined on the
 * {@link com.atlassian.jira.issue.customfields.impl.MultiUserCFType#getRelatedIndexers(com.atlassian.jira.issue.fields.CustomField)} method
 *
 * @since v4.0
 */
public class UserCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    private final CustomField customField;
    private final UserConverter userConverter;

    public UserCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField, UserConverter userConverter)
    {
        super(fieldVisibilityManager, notNull("customField", customField));
        this.userConverter = userConverter;
        this.customField = customField;
    }

    public void addDocumentFieldsSearchable(final Document doc, final Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.NO);
    }

    private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType)
    {
        final Object value = customField.getValue(issue);
        if (value instanceof User)
        {
            // When indexing user names in the issue search index (Lucene) we store the names in lower case.
            // This overcomes an historical issue that arises if customers migrate from using one user directory, e.g. internal,
            // to another, e.g. LDAP. which hold the users in different case.  So if we search for issues assigned to a user 'fred' we
            // will find issues assiged to the 'old' 'fred' internal user, as well as the new 'Fred' LDAP user.
            String userId = CaseFolding.foldUsername(userConverter.getString((User) value));
            doc.add(new Field(getDocumentFieldId(), userId, Field.Store.YES, indexType));
        }
    }
}
