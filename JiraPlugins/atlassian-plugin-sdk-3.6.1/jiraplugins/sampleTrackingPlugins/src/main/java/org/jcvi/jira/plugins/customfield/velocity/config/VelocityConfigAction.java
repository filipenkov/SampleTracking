package org.jcvi.jira.plugins.customfield.velocity.config;

import org.jcvi.jira.plugins.customfield.shared.config.CFConfigAction;
import org.jcvi.jira.plugins.customfield.shared.config.CFConfigItem;
import org.jcvi.jira.plugins.customfield.velocity.TemplateType;

/**
 * Created by IntelliJ IDEA.
 * User: pedworth
 * Date: 8/18/11
 */
//has to be suppressed as this class is dynamically loaded
@SuppressWarnings({"UnusedDeclaration"})
public class VelocityConfigAction extends CFConfigAction {

    @Override
    public TemplateType[] getConfigurableParameters() {
        return TemplateType.values();
    }

    @Override
    public CFConfigItem getConfigItem() {
        return new VelocityConfigItem(getDescriptor(),
                                      getGenericConfigManager());
    }

    @Override
    protected String getSessionKey() {
        return this.getClass().getName();
    }
}
