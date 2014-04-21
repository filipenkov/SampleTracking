package org.jcvi.jira.plugins.customfield.additionalsearch;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Field;
import org.jcvi.jira.plugins.utils.typemapper.TypeMapper;
import org.jcvi.jira.plugins.utils.typemapper.TypeMapperUtils;

/**
 * This version of the Search Indexer should be used for Text fields
 */
public class TextAdditionalSearchFieldType extends AbstractAdditionalSearchFieldType<String> {
    private final TypeMapper<String,String> luceneTypeMapper;
    private final TypeMapper<Object, String> copyTypeMapper;

    /**
     *
     * @param persist               Injected manager used to access the backing store
     * @param configManager         Injected manager used to access the
     *                              customField's configuration information
     *                              (in combination with CustomField and
     *                              Issue objects)
     * @param fieldVisibility       Injected manager used to determine if a
     *                              value needs to be stored/indexed for a
     *                              particular issue
     * @param fieldManager          Injected manager used to access the copied
     *                              field
     */
    public TextAdditionalSearchFieldType(CustomFieldValuePersister persist,
                                        GenericConfigManager configManager,
                                        FieldVisibilityManager fieldVisibility,
                                        CustomFieldManager fieldManager) {
        super(persist,configManager,fieldVisibility,fieldManager,String.class);
        luceneTypeMapper = new TypeMapperUtils.NopMapper<String>();
        copyTypeMapper =
              //No mapping is needed between transport
              //and storage, it's all Strings
              new TypeMapper<Object, String>() {
                  @Override
                  public String convert(Object value) {
                      if (value != null) {
                          return value.toString();
                      }
                      return null; //would an empty string be better?
                  }
              };
    }

    @Override
    protected PersistenceFieldType getDatabaseType() {
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }

    @Override
    protected Field.Index getLuceneIndexForIndexer() {
        return Field.Index.NOT_ANALYZED; //split into words when storing
    }

    @Override
    protected TypeMapper<String, String> getLuceneTypeMapper() {
        return luceneTypeMapper;
    }

    @Override
    protected TypeMapper<Object, String> getCopiedFieldValueMapper() {
        return copyTypeMapper;
    }
}
