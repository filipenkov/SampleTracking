package org.jcvi.jira.plugins.customfield.shared;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.jcvi.jira.plugins.utils.typemapper.TypeMapper;
import org.jcvi.jira.plugins.utils.typemapper.TypeMapperUtils;

import java.util.Collection;

import org.jcvi.jira.plugins.utils.typemapper.TypeUtils;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Created by IntelliJ IDEA.
 * User: pedworth
 * <p>This is a basic implementation of the FieldIndexer interface, it simply gets the
 * values from the field, converts them into a collection of Strings and then adds
 * each string to the index.</p>
 * <p>Extensions will probably want to override convertValues. Any Strings in the returned
 * Collection will be added to the index. (super.convertValues can be used to get the
 * fields current value as a collection of strings)</p>
 */
public abstract class CFIndexer<TRANSPORT_TYPE> extends AbstractCustomFieldIndexer {
    private static final Logger log = Logger.getLogger(CFIndexer.class);
    private final TypeMapper<TRANSPORT_TYPE, String> mapper;
    //needed to check that the object from field.getValue is the type expected
    private final Class<TRANSPORT_TYPE> transportType;

    //-------------------------------------------------------------------------
    //                  Overrride
    //-------------------------------------------------------------------------

    /**
     * <p>Override to alter where the values stored in the index come from. The
     * default implementation uses CustomField.getValue(Issue).</p>
     * @param issue     The issue to extract the values from
     * @return A collection of objects of type TRANSPORT_TYPE. If no values
     * are to be added either null or an empty collection can be returned.
     */
    protected Collection<TRANSPORT_TYPE> getValues(Issue issue) throws FieldValidationException {
        Object values = customField.getValue(issue);
        if (values == null) {
            return null;
        }
        //put the values in a collection, or if they already are
        //cast them to a collection
        Collection<Object> valuesCollection = TypeUtils.toCollection(values);
        return TypeUtils.filterType(valuesCollection,transportType);
    }

    /**
     * Three values appear to be used for most indexers:
     * <ul>
     *     <li>
     *         NOT_ANALYSED:    Used when the input should be treated as a single
     *         value. e.g. don't break up into words or lists
     *     </li><li>
     *         ANALYSED:        Used when the input should be treated as a
     *         collection of values. e.g. a text area that should be treated
     *         as a series of words
     *     </li><li>
     *         NO               Don't index this item.
     *     </li>
     * </ul>
     * <p>This method should only return UN_TOKENIZED OR TOKENIZED never null or
     * NO The other values from Field.Index may also be valid but I don't know
     * exactly how they map.</p>
     * <p>TOKENIZED and UN_TOKENIZED have been renamed to ANALYZED and
     * NOT_ANALYZED respectively. At the moment using the old ones to match
     * Atlassian's code seems sensible.</p>
     * @return A valid Field.Index type
     */
    protected abstract Field.Index getLuceneIndexType();

    //-------------------------------------------------------------------------
    //                  Constructors
    //-------------------------------------------------------------------------

    /**
     * @param fieldVisibilityManager Passed via Injection to the
     *                               CustomFieldTypes constructor
     * @param customField            Passed to the getRelatedIndexer(s) methods
     * @param transportTypeClass     An object matching the generic type used
     *                               by the implementation of this class
     * @param typeMapper             A TypeMapper to convert the TRANSPORT_TYPE
     *                               object into a String to store in lucene.
     *
     */
    public CFIndexer(FieldVisibilityManager fieldVisibilityManager,
                     CustomField customField,
                     Class<TRANSPORT_TYPE> transportTypeClass,
                     TypeMapper<TRANSPORT_TYPE,String> typeMapper) {
        super(fieldVisibilityManager, notNull("field", customField));
        this.mapper = typeMapper;
        this.transportType = transportTypeClass;
    }



    //-------------------------------------------------------------------------
    //                  Implementation
    //-------------------------------------------------------------------------
    @Override
    public void addDocumentFieldsSearchable(Document doc, Issue issue) {
        //add the field as either
        //Field.Index.ANALYSED (split into chunks) or
        //Field.Index.NOT_ANALYSED (processed as a single unit)
        addDocumentFields(doc, issue, getLuceneIndexType());
    }
    @Override
    public void addDocumentFieldsNotSearchable(Document doc, Issue issue) {
        addDocumentFields(doc, issue, Field.Index.NO);
    }

    private void addDocumentFields(Document doc,
                                   Issue issue,
                                   Field.Index indexType) {
        String key = "";
        //if (issue != null) {
        //    key += issue.getKey();
        //}
        //key += ":";
        key += customField.getName();
        //key += " ";
//Optional Override getValues
        try {
            Collection<TRANSPORT_TYPE> transportObjects = getValues(issue);
            log.debug(key+"getValues returned " + transportObjects + " to index");
            if (transportObjects == null || transportObjects.size() == 0) {
                //no objects to add
                return;
            }
            //convert the transport values into Strings before indexing them
            Collection<String> stringValues =
    //Optional Override mapper
                  TypeMapperUtils.mapUnorderedCollection(mapper, transportObjects);
            if (stringValues == null) {
                //nothing maps to values for the index
                return;
            }

            //add a field for each value
            for(String value : stringValues) {
                if (value == null) {
                    //if the value didn't map to anything don't add it
                    continue;
                }
                log.debug(key+"Adding value " + value + " to index of " + issue.getSummary());
                doc.add(new Field(getDocumentFieldId(),
                        value,
                        Field.Store.YES,
                        indexType));
            }
        } catch (FieldValidationException fve) {
            log.error("Could not index issue owing to: "+fve.getMessage());
        }
    }
}
