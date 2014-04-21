package org.jcvi.jira.plugins.customfield.shared;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Field;
import org.jcvi.jira.plugins.utils.typemapper.TypeMapperUtils;

/**
 * An empty implementation for String based fields
 */
public class StringIndexer extends CFIndexer<String> {
    /**
     * @param fieldVisibilityManager Passed via Injection to the
     *                               CustomFieldTypes constructor
     * @param customField            Passed to the getRelatedIndexer(s) methods
     */
    public StringIndexer(FieldVisibilityManager fieldVisibilityManager,
                         CustomField customField) {
        super(fieldVisibilityManager, customField,
                String.class, new TypeMapperUtils.NopMapper<String>());
    }

    @Override
    protected Field.Index getLuceneIndexType() {
        return Field.Index.ANALYZED;  //split into words
    }
}
