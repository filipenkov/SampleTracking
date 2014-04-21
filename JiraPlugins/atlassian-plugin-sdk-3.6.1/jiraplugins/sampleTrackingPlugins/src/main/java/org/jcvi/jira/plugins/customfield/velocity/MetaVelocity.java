package org.jcvi.jira.plugins.customfield.velocity;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.fields.CustomField;
import org.apache.velocity.exception.VelocityException;
import org.jcvi.jira.plugins.config.ConfigManagerStore;
import org.jcvi.jira.plugins.customfield.shared.config.CFConfigItem;
import org.jcvi.jira.plugins.utils.PageImporter;

import java.util.Map;

/**
 * A class to run Velocity on a template that is called from within another
 * velocity instance and passed the environment of that instance
 * User: pedworth
 * Date: 8/26/11
 * Time: 11:02 AM
 */
public class MetaVelocity {
    private final ConfigManagerStore config;

    public MetaVelocity(GenericConfigManager genericConfigManager,
                        Issue issue,
                        CustomField field,
                        CFConfigItem configType) {
        this.config = new ConfigManagerStore(genericConfigManager,
                field.getRelevantConfig(issue),
                configType);
    }

    //uses values stored in the object for the config and issue
    //Called from Velocity
    @SuppressWarnings({"UnusedDeclaration"})
    public String insertTemplate(String templateKey,
                                 Map<String, Object> context)
            throws VelocityException {
        TemplateType templateField = TemplateType.valueOf(templateKey);
        if (templateField != null) {
            String template = config.retrieveStoredValue(templateField);
            return PageImporter.insertTemplate(context, template);
        }
        return null;
    }
}
