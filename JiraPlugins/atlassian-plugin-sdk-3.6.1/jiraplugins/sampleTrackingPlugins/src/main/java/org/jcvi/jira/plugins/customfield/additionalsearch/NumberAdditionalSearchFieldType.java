package org.jcvi.jira.plugins.customfield.additionalsearch;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Field;
import org.jcvi.jira.plugins.utils.typemapper.TypeMapper;

/**
 * This version of the Search Indexer should be used for Number fields. It
 * requires the field to return a Double or collection&lt;Double&gt; from
 * CustomField.getValues.
 * If a number in a Text field is being used then use TextToNumberAdditionalSearchIndexer
 */
public class NumberAdditionalSearchFieldType extends AbstractAdditionalSearchFieldType<Double> {
    private final TypeMapper<Double,String> luceneTypeMapper;
    private final TypeMapper<Object, Double> copyTypeMapper;

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
     * @param doubleConverter       Injected, used to convert Doubles into
     *                              values for the Lucene index
     */
    public NumberAdditionalSearchFieldType(CustomFieldValuePersister persist,
                                        GenericConfigManager configManager,
                                        FieldVisibilityManager fieldVisibility,
                                        CustomFieldManager fieldManager,
                                        final DoubleConverter doubleConverter) {
        super(persist,configManager,fieldVisibility,fieldManager,Double.class);
        luceneTypeMapper = new TypeMapper<Double, String>() {
            @Override
            public String convert(Double value) {
                //this is how NumberCustomFieldIndexer does the conversion
                return doubleConverter.getStringForLucene(value);
            }
        };
        copyTypeMapper = new TypeMapper<Object, Double>() {
                            @Override
                            public Double convert(Object value) {
                                //shouldn't ever be null but it doesn't hurt to check
                                if (value != null) {
                                    //already in the correct format
                                    if (value instanceof Double) {
                                        return (Double)value;
                                    }
                                    //convert any other number into a double
                                    //this should always be safe as double is the
                                    //largest number format.
                                    //todo: is that true for long -> double? do
                                    //we lose precision?
                                    if (value instanceof Number) {
                                        return ((Number)value).doubleValue();
                                    }
                                    //try to convert strings into numbers
                                    if (value instanceof String) {
                                        try {
                                            return doubleConverter.getDouble((String)value);
                                        } catch (FieldValidationException ve) {
                                            //not much that can be done
                                        }
                                    }
                                }
                                return null;
                            }
                        };
    }

    @Override
    protected PersistenceFieldType getDatabaseType() {
        return PersistenceFieldType.TYPE_DECIMAL;
    }

    @Override
    protected Field.Index getLuceneIndexForIndexer() {
        return Field.Index.NOT_ANALYZED; //do not split the input at the decimal!
    }

    @Override
    protected TypeMapper<Double, String> getLuceneTypeMapper() {
        return luceneTypeMapper;
    }

    @Override
    protected TypeMapper<Object, Double> getCopiedFieldValueMapper() {
        return copyTypeMapper;
    }
}
