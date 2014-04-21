package org.jcvi.jira.plugins.customfield.velocity;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Field;
import org.apache.velocity.exception.VelocityException;
import org.jcvi.jira.plugins.config.ConfigManagerStore;
import org.jcvi.jira.plugins.customfield.shared.config.CFConfigItem;
import org.jcvi.jira.plugins.customfield.shared.CFIndexer;
import org.jcvi.jira.plugins.utils.PageImporter;
import org.jcvi.jira.plugins.utils.typemapper.TypeMapperUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Created by IntelliJ IDEA.
 * User: pedworth
 * <p>The velocity indexer intercepts calls on the field to get the value to
 * store in the index and instead uses a velocity template to generate
 * the value.</p>
 * <p>The original value from the field is available to the template as
 * $value (Collection&lt;String&gt;>)</p>
 */
public class VelocityFieldIndexer extends CFIndexer<String> {
    private static final Logger log = Logger.getLogger(VelocityFieldIndexer.class);
    private final CFConfigItem config;
    private final GenericConfigManager genericConfigManager;

    public VelocityFieldIndexer(GenericConfigManager configManager,
                                FieldVisibilityManager fieldVisibilityManager,
                                CustomField customField,
                                CFConfigItem configItem) {
        super(fieldVisibilityManager,       //pass through
              notNull("field", customField),//pass through
              String.class,                 //String transport objects
              //The modifications are done in getValues, the mapper doesn't
              //need to do anything
              new TypeMapperUtils.NopMapper<String>());
        this.genericConfigManager = configManager;
        this.config               = configItem;
    }

    protected Collection<String> getValues(Issue issue) {
        //get the original values
        Collection<String> values = super.getValues(issue);

        //get the velocity configuration
        Map<String,String> configuration = new HashMap<String,String>();
        String template = getConfigManagerStore(issue).retrieveStoredValue(TemplateType.SEARCH);

        //create the context
        Map<String,Object> context =
                VelocityContextFactory.getContext(configuration,
                                                     issue,
                                                     customField,
                                                     null);//null authenticationContext
        VelocityContextFactory.setContextValue(context, values);

        Collection<String> toReturn = new ArrayList<String>(1);
        //render the template
        try {
            toReturn.add(PageImporter.insertTemplate(context, template));
        } catch (VelocityException ve) {
            //outside the for loop, give up after one try to save spamming the
            //log
            log.error("Failed running velocity in VelocityFieldIndexer",ve);
        }
        return toReturn;
    }

    @Override
    protected Field.Index getLuceneIndexType() {
        return Field.Index.ANALYZED;  //the result of the velocity template
        //should be treated as a block of text and split into words when storing
        //it in Lucene
    }

    protected ConfigManagerStore getConfigManagerStore(Issue issue) {
        return new ConfigManagerStore(genericConfigManager,
                                      customField.getRelevantConfig(issue),
                                      config);
    }
}
