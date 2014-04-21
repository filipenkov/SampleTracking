package org.jcvi.jira.plugins.searcher.shared;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.ExactTextSearcher;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.util.concurrent.Nullable;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jcvi.jira.plugins.statisticsmapper.shared.StringCFStatisticsMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: pedworth
 * Date: 11/9/11
 * An implementation of a CustomFieldSearcher.
 * <h1>CustomFieldSearcher overview</h1>
 * <h2>CustomFieldSearcher Interface (extends IssueSearcher)</h2>
 * <p>Extends the Issue Searcher interface to store and retrieve a
 * configuration object for the CustomFieldSearcher instance. The
 * configuration comes from the atlassian-plugin file.
 * The methods added, to handle the configuration, are:<p>
 * <p><b>public void init(CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor)</b></p>
 * <p><b>public CustomFieldSearcherModuleDescriptor getDescriptorFromImplementationClass()</b></p>
 * <p>Note there are two init methods (one in CustomFieldSearcher and
 * one in IssueSearcher) both of these are called when the searcher is
 * created.</p>
 * <p>The CustomFieldSearcher interface also adds a single method to get a
 * CustomFieldSearcherClauseHandler object. This object wraps the
 * implementation of the searches conversion to and from the HTML
 * parameters and the Lucene query. It also provides information used
 * by the parser to convert the input parameters into the correct type
 * and to filter the operations used with it. It is accessed via:</p>
 * <p><b>CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler()</b></p>
 *
 * <h2>IssueSearcher&lt;T=CustomField&gt; interface</h2>
 * <p><b>void init(CustomField field)</b></p>

 * <p>Normally this method carries out the actual setup and all of the other
 * methods simply return the object created here. The field passed in
 * <strong>may</strong></p> be null</p>

 * <p><b>SearcherInformation&lt;CustomField&gt; getSearchInformation()</b></p>
 * <p>Provides very similar information to the CustomFieldType interface. Such
 * as GUI Name, internal ID and FieldIndexers.</p>
 *
 * <p><b>SearchInputTransformer getSearchInputTransformer()</b></p>
 * <p>Provides implementations for converting request parameters to field
 * holder values and field holder values to
 * {@link com.atlassian.query.clause.Clause} search representations.</p>
 *
 * <p><b>SearchRenderer getSearchRenderer()</b></p>
 * <p>Controls the rendering of the edit and view html in the
 * Issue Navigator.</p>
 *
 * <h1>Implementation</h1>
 * <p><b>extends ExactTextSearcher</b></p>
 * <p>ExactTextSearcher provides default implementations of all of the
 * methods and most of the Objects.</p>
 * <p><b>ExactTextSearcher extends AbstractInitializationCustomFieldSearcher</b></p>
 * <p>This provides the implementation of the configuration handling
 * methods of CustomFieldSearcher</p>
 * <h4>Overriding</h4>
 * <p>The atlassian interface requires a lot of methods to be implemented
 * across several classes. The following methods provide a shorter list
 * of requirements. They also group all of the customization into
 * one Class. The methods are listed below, OPT means that they only
 * need Overriding if something other than the default is needed.
 * MUST indicates that they are required to make a concrete class.</p>
 * <ul>
 *     <li>OPT: {@link #getFieldIndexer}</li>
 *     <li>OPT: {@link #getFieldIndexers}</li>
 * </ul> *
 * <p>Modify the SearcherInformation object returned by getSearchInformation</p>
 * <p/>
 * <ul>
 *     <li>MUST: {@link #getClauseVisitor(com.atlassian.jira.issue.fields.CustomField)}</li>
 * </ul>
 * <p>Must be implemented to set the behaviour of the {@link SearchInputTransformer}</p>
 * <p/>
 * <ul>
 *     <li>OPT: {@link #getSearchInputTransformer()}</li>
 * </ul>
 * <p>The Object from the {@link #getClauseVisitor(com.atlassian.jira.issue.fields.CustomField)}
 * method is passed to this method. Implementations should return a
 * {@link SearchInputTransformer} that extends {@link CFSearchInputTransformer}
 *
 * <h1>Use of field in reports</h1>
 * The class implements CustomFieldStattable and so can be used in reports.
 * It returns StringCFStatisticsMapper by default. This should work for most
 * text based fields.
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class CFTextSearch extends ExactTextSearcher implements CustomFieldSearcher, CustomFieldStattable {
    protected static final String PROPERTIES_TYPE = "properties";
    protected static final String CONFIG_FILE = "configuration";

    private volatile CustomFieldInputHelper customFieldInputHelper = null;
    private final JqlOperandResolver operandResolver;
    private final FieldVisibilityManager fieldVisibilityManager;

    private SearcherInformation<CustomField> info = null;
    private SearchInputTransformer inputTransformer = null;
    private SearchRenderer searchRenderer = null;

    private final Object configurationLock = new Object();
    private Properties configuration = null;

   /**
    * <p>The parameters are provided by the dependency injection system and the
    * System defaults should normally be used.</p>
    * @param jqlOperandResolver     Converts the reference or value in the
    *                               search string into the type defined by ?
    * @param fieldInputHelper       provides methods to lookup a fields name
    *                               and resolve that into a uid. It is used to
    *                               construct {@link com.atlassian.jira.issue.customfields.searchers.transformer.ExactTextCustomFieldSearchInputTransformer}
    * @param fieldVisibilityManager provides methods to test if the field
    *                               should be rendered on the current page. It
    *                               could be used to create a custom
    *                               {@link FieldIndexer} or {@link com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer}
    **/
    //loaded by the plugin system
    @SuppressWarnings({"UnusedDeclaration"})
    public CFTextSearch(JqlOperandResolver jqlOperandResolver,
                        CustomFieldInputHelper fieldInputHelper,
                        FieldVisibilityManager fieldVisibilityManager) {
        super(jqlOperandResolver, fieldInputHelper, fieldVisibilityManager);
        this.customFieldInputHelper = fieldInputHelper;
        this.operandResolver = jqlOperandResolver;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    protected JqlOperandResolver getOperandResolver() {
        return operandResolver;
    }

    //---------------------------------------------------------------
    //             Must be Overridden
    //---------------------------------------------------------------
    /**
     * <h4>Must be Overridden</h4>
     * Note: This method is ignored if {@link #getSearchInputTransformer}
     * is overridden.
     * <h4>Description</h4>
     * <p>This handles conversion of the values from the HTML form into
     * a Lucene query.</p>
     * <p>Combined with {@link #getClauseVisitor(com.atlassian.jira.issue.fields.CustomField)}
     * this implements the behaviour of the {@link SearchInputTransformer}</p>
     * @see CFSearchInputTransformer
     * @return A {@link CFSearchClauseFactory}
     */
    protected abstract CFSearchClauseFactory getSearchClauseFactory();

    /**
     * <h4>Must be Overridden</h4>
     * Note: This method is ignored if {@link #getSearchInputTransformer}
     * is overridden.
     * <h4>Description</h4>
     * <p>The ClauseVisitor traverses the Query object at the Clause level.
     * Normally it searches for clauses related to the current field and then
     * stores them for further processing.</p>
     * <p>{@link CFSimpleClauseVisitor} can be used as the basis of an
     * implementation of this method.</p>
     * @see    CFSimpleClauseVisitor
     * @param  field          The customField being implemented, passed to the
     *                        init method
     * @return a ClauseVisitor that will be used in the SearchInputTransformer
     */
    protected abstract CFSimpleClauseVisitor getClauseVisitor(CustomField field);

    //---------------------------------------------------------------
    //             Can be Overridden
    //---------------------------------------------------------------
    /**
     * <h4>Can Override</h4>
     * <h4>Description</h4>
     * <p>Override to return a single custom indexer. If both this and
     * getFieldIndexers are overridden then their contents are combined.</p>
     * <p>Indexers are more commonly returned from CustomFields, if
     * both are being implemented</p>
     * <p>If neither are implemented then {@link org.jcvi.jira.plugins.customfield.shared.CFIndexer}
     * is used</p>
     * @param customField   The field that the indexer is for
     * @return A FieldIndexer to pass to jira via SearcherInformation. (Nullable)
     */
    @Nullable
    protected FieldIndexer getFieldIndexer(CustomField customField) {
        return null;
    }

    /**
     * <h4>Can Override</h4>
     * <h4>Description</h4>
     * <p>Override to return multiple custom indexers. If both this and getFieldIndexer are
     * overridden then their contents are combined.</p>
     * <p>Indexers are more commonly returned from CustomFields, if
     * both are being implemented</p>
     * @param customField   The field that the indexer is for
     * @return A FieldIndexer to pass to jira via SearcherInformation. (Nullable)
     */
    @Nullable
    protected List<FieldIndexer> getFieldIndexers(CustomField customField) {
        return null;
    }

    /**
     * <h4>Not Normally Overridden</h4>
     * <p>Restriction: Call super if overridden</p>
     * <p>getSearchInformation and getSearchInputTransformer rely on this
     * method to initialize the objects that they return.</p>
     * <h4>Override instead</h4>
     * <ul>
     *  <li>{@link #getFieldIndexer}</li>
     *  <li>{@link #getFieldIndexers}</li>
     *  <li>{@link #getClauseVisitor(com.atlassian.jira.issue.fields.CustomField)}</li>
     *  <li>{@link #createInputTransformer}</li>
     *  <li></li>
     * </ul>
     * <h4>Description</h4>
     * Called after object creation, but before any other method call is made.
     * @param field     The field that this instance of CFTextSearch,
     *                  or a sub-class there of, is associated with
     */
    @Override
    public void init(CustomField field) {
        super.init(field);

        List<FieldIndexer> indexers = new ArrayList<FieldIndexer>();
        FieldIndexer singleIndexer = getFieldIndexer(field);
        if (singleIndexer != null) {
            indexers.add(singleIndexer);
        }
        List<FieldIndexer> multipleIndexers = getFieldIndexers(field);
        if (multipleIndexers != null) {
            indexers.addAll(multipleIndexers);
        }
        if (indexers.isEmpty()) {
            String message="At least one valid indexer is needed";
            log.error(message);
            throw new IllegalStateException(message);
        }

        this.info = new CustomFieldSearcherInformation(field.getId(),
                field.getNameKey(),
                indexers, new AtomicReference<CustomField>(field));

        this.inputTransformer = createInputTransformer(field,
                customFieldInputHelper);

        /**
         * <p>Methods on this object are called to populate the velocity
         * environment for the search's part of the Issue Navigator
         * search side bar.</p>
         * <p>The values added to the environment for each template are listed
         * below:
         * <table>
         *     <hr><td>Template Name</td><td>Velocity Parameter</td><td>Method called</td></hr>
         *     <tr><td>search</td><td>value</td><td>getStringValue</td></tr>
         *     <tr><td rowspan=2>view</td><td>value</td><td>getStringValue</td></tr>
         *     <tr><td>valueObject</td><td>getValue</td></tr>
         * </table>
         * </p>
         * <p>The 'search' template is used to generate the form element when
         * editing a search.</p>
         * <p>The 'view' template is used when a search is being described in
         * the 'Summary' section.</p>
         */
        CustomFieldValueProvider customFieldValueProvider =
            new CustomFieldValueProvider() {
                /**
                 * <p>This method is called to populate the
                 * <em>value</em> variable in the velocity environment for the
                 * search and view templates.</p>
                 * <p>The object returned is of type <em>CustomFieldParam</em>.
                 * This object is used to extract the values to use both
                 * in the search and when rendering the searcher.</p>
                 * <p>The values are extracted from the FieldValuesHolder object</p>
                 * <h4>Multi-input Searches</h4>
                 * <p>The wrapper object is used to group together multiple
                 * sections of the searches input (e.g. a date range search has
                 * a before and an after section).</p>
                 * <p>Single section searches should use
                 * <code>name='$!customField.Id'</code> in their forms. The
                 * values for the field can be stored under a 'NullKey' and
                 * can be accessed using the get...ForNullKey methods</p>
                 * <p>Multiple section searches should include an extra suffix
                 * for each input to indicate the which section the input is for.
                 * <code>name='${!customField.Id}:SECTION_NAME'</code>. The
                 * values can be accessed using the get...ForKey methods. The
                 * key is the suffix after the separator, e.g. SECTION_NAME</p>
                 * <h4>Multi-value Searchers</h4>
                 * <p>A single section can have multiple values, either by
                 * including multiple inputs with the same name in the form or
                 * by using inputs that accept multiple values (e.g. select with
                 * multiple="multiple").</p>
                 * <p>Single Values are accessed using the getFirstValue...
                 * methods. The interface only specifies that they should return
                 * an Object but normally they should return a String.</p>
                 * <p>Multiple Values are accessed using the getValues...
                 * methods the collection returned can then be iterated over
                 * using <code>#foreach ($value in $values)</code>. Again the
                 * values in the collection officially could be any Object but
                 * implementations should stick to returning Strings.</p>
                 * <p>Summary of the methods available on a CustomFieldParam
                 * object:
                 * <table>
                 *     <hr><td>             </td><td>Single Value           </td><td>Collection of Values</td></hr>
                 *     <tr><td>Default value</td><td>getFirstValueForNullKey</td><td>getValuesForNullKey </td></tr>
                 *     <tr><td>Named value  </td><td>getFirstValueForKey    </td><td>getValuesForKey</td></tr>
                 * </table>
                 * @param customField        The field being set
                 * @param fieldValuesHolder  The values from the form
                 * @return  A CustomFieldParam object NOT A STRING
                 */
                @Override
                public Object getStringValue(CustomField customField,
                                          FieldValuesHolder fieldValuesHolder) {
                    CFTextSearch.logFieldValuesHolder(log, Level.DEBUG,
                            "CFTextSearch:CustomFieldValueProvider.getStringValue",customField,fieldValuesHolder);
                    // Get this fields values from the fieldValuesHolder
                    return customField.getCustomFieldValues(fieldValuesHolder);
                }

                /**
                 * <p>This method is only called for the 'view' template. It
                 * populates the 'valueObject' variable</p>
                 * <p>This implementation just returns the same as
                 * {@link #getStringValue}</p>
                 */
                public Object getValue(CustomField customField,
                                       FieldValuesHolder fieldValuesHolder) {
                    CFTextSearch.logFieldValuesHolder(log, Level.DEBUG,
                            "CFTextSearch:CustomFieldValueProvider.getValue",customField,fieldValuesHolder);
                    return getStringValue(customField,fieldValuesHolder);
                }
            };
        this.searchRenderer = new CustomFieldRenderer(field.getClauseNames(),
                                                      getDescriptor(),
                                                      field,
                                                      customFieldValueProvider,
                                                      fieldVisibilityManager);
    }

   /**
    * <h4>Not Normally Overridden</h4>
    * Note: the Object creation is carried out in the {@link #init(com.atlassian.jira.issue.fields.CustomField)}
    * method
    * <h4>Override Instead</h4>
    * <ul>
    *  <li>{@link #getFieldIndexer}</li>
    *  <li>{@link #getFieldIndexers}</li>
    * </ul>
    * <h4>Description</h4>
    * <p>
    * <p>This method returns an object that is used to describe the searcher.
    * The actual object returned is created in the init method.
    * The returned object is:</p>
    * <h3>SearcherInformation</h3>
    * <p>Provides more information on the Searcher. It is a little mixed though
    * as to what information is passed:
    *
    * <p><b>getNameKey()</b> provides the i18n key used in the GUI to represent
    * the searcher.<I>Default: matches the field's name</I></p>
    *
    * <p><B>getId()</b> and <b>getField()</b> are used internally by JIRA for
    * keeping track of the searchers and their associated fields. getfield()
    * can return null, which is important if init is called with a null field
    * <I>Default: matches the field's id</I></p>
    *
    * <p><b>getRelatedIndexers()</b>returns implementations of
    * {@link com.atlassian.jira.issue.index.indexers.FieldIndexer}.
    * These are the same as the FieldIndexers returned by CustomFieldType
    * objects. The returned value must not return null, however returning
    * an empty list is fine.<I>Default: the indexers to use are gathered
    * from {@link #getFieldIndexer} and {@link #getFieldIndexers}</I></p>
    *
    * <p>The final method <b>getSearcherGroupType()</b> isn't even used for
    * customFields.
    * <I>Default: com.atlassian.jira.issue.search.searchers.SearcherGroupType#CUSTOM</I></p>
    */
    @Override
    public SearcherInformation<CustomField> getSearchInformation() {
        return info;
    }

    /**
     * <h4>Not Normally Overridden</h4>
     * <h4>Override Instead</h4>
     * <ul>
     *  <li>{@link #getSearchClauseFactory()} </li>
     *  <li>{@link #getClauseVisitor(com.atlassian.jira.issue.fields.CustomField)} </li>
     * </ul>
     * <h4>Description</h4>
     * <p>The input transformer handles mapping from ParamValues to Query and
     * back.</p>
     * <p>The default implementation is {@link CFSearchInputTransformer}
     * </p>
     * @see CFSearchInputTransformer
     * @param field            The customField being implemented, passed to the
     *                         init method
     * @param inputHelper      An injected helper class, passed to constructor
     * @return A SearchInputTransformer that will be passed back when
     *                         getSearchInputTransformer is called.
     */
    protected SearchInputTransformer createInputTransformer(
                                                final CustomField field,
                                                CustomFieldInputHelper inputHelper) {
        return new CFSearchInputTransformer(field,
                                            inputHelper) {

            @Override
            protected CFSimpleClauseVisitor createClauseVisitor() {
                return getClauseVisitor(field);
            }

            @Override
            protected CFSearchClauseFactory createSearchClauseFactory() {
                return getSearchClauseFactory();
            }
        };
    }

    public StatisticsMapper getStatisticsMapper(CustomField customField) {
        return new StringCFStatisticsMapper(customField,
                                            true); //use exact text matching
    }
    //---------------------------------------------------------------
    //              Don't Override
    //---------------------------------------------------------------
    /**
     * <h4>Do Not Override</h4>
     * Note: the Object creation is carried out in the {@link #init(com.atlassian.jira.issue.fields.CustomField)}
     * method
     * <h4>Override Instead</h4>
     * <ul>
     *  <li>{@link #createInputTransformer}</li>
     * </ul>
     * <h4>Description</h4>

     * <h4>Override createInputTransformer instead</h4>
     * The input transformer handles mapping from ParamValues to Query and back.
     * This method returns the SearchInputTransformer that is created during
     * init.
     * @return The result of the call during init to {@link #createInputTransformer}
     */
    @Override
    public SearchInputTransformer getSearchInputTransformer() {
        return inputTransformer;
    }

    /**
     * SearchRenderer Carries out the lookup of the velocity template and
     * setting up its environment. The default implementation,
     * CustomFieldRenderer, is used but with a custom
     * CustomFieldValueProvider that handles multiple values. The parent's
     * (ExactTextSearch) implementation only returns a single value.
     * @return  An object containing instructions of how to display the
     * search field on the Issue Navigator screen
     */
    @Override
    public SearchRenderer getSearchRenderer() {
        return searchRenderer;
    }

    protected Properties getConfiguration() {
        synchronized (configurationLock) {
            if (configuration == null) {
                configuration = loadConfigurationFromDescriptor();
            }
            return configuration;
        }
    }

    private Properties loadConfigurationFromDescriptor() {
        Properties config = new Properties();

        CustomFieldSearcherModuleDescriptor descriptor = getDescriptor();
        if (descriptor == null) {
            log.error("No Descriptor found");
            return null;
        }
        ResourceDescriptor configResource  =
                descriptor.getResourceDescriptor(PROPERTIES_TYPE, CONFIG_FILE);
        if (configResource != null) {
            //file based properties
            String location = configResource.getLocation();
            if (location != null) {
                InputStream configStream =
                        this.getClass().getResourceAsStream(location);
                if (configStream == null) {
                    log.error("Unable to load configuration from: "+location);
                } else {
                    try {
                        config.load(configStream);
                    } catch (IOException ioe) {
                        log.error("Unable to read configuration from: "+location,ioe);
                    }
                }
            }

            //directly included properties
            String content = configResource.getContent();
            if (content != null && !content.trim().isEmpty()) {
                Reader resourceContent = new StringReader(content);
                try {
                    config.load(resourceContent);
                } catch (IOException ioe) {
                    log.error("Failed loading properties from a StringReader!",ioe);
                }
            }

            //check that something was defined
            if (location == null &&
                    (content == null || content.trim().isEmpty())) {
                log.error("No Resource (of type '"+PROPERTIES_TYPE+"') " +
                          "defined for "+CONFIG_FILE);
            }
        }
        Map<String,String> params = descriptor.getParams();
        for(String property: config.stringPropertyNames()) {
            params.put(property,config.getProperty(property));
        }
        return config;
    }
    //---------------------------------------------------------------
    //              Implemented by ExactTextSearcher
    //---------------------------------------------------------------
    //The parent implementation returns a CustomFieldRenderer which gets
    //the html from the descriptor CustomFieldSearcherModuleDescriptorImpl
    //which gets the Velocity template from the configuration in
    //atlassian-plugin.xml
    //It uses "search" for getSearchHtml and "view' for getViewHtml.
    //It also provides the stats html using: "label"
//    public SearchRenderer getSearchRenderer();
    //only handled if super.init is called
//    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler() {

    //---------------------------------------------------------------
    //      Implemented by AbstractInitializationCustomFieldSearcher
    //---------------------------------------------------------------
//    public void init(CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor) {
//    public CustomFieldSearcherModuleDescriptor getDescriptorFromImplementationClass() {

    public static void logFieldValuesHolder(Logger logToUse,
                                            Level logLevel,
                                            String callIdentifier,
                                            CustomField field,
                                            FieldValuesHolder holder) {
        if (logToUse.isEnabledFor(logLevel)) {
            String message = callIdentifier + "\n"+
                             "Field="+field.getId()+"\n"+
                             "FieldValuesHolder contains:\n";
            for(Object key : holder.keySet()) {
                message += key.toString()+"="+holder.get(key)+"\n";
            }
            logToUse.log(logLevel,message);
        }
    }
}
