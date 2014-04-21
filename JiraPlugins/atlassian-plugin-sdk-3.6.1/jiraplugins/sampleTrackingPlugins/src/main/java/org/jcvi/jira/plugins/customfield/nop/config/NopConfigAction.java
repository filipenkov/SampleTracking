package org.jcvi.jira.plugins.customfield.nop.config;

import org.jcvi.jira.plugins.customfield.nop.NopCustomField;
import org.jcvi.jira.plugins.customfield.shared.config.CFConfigAction;
import org.jcvi.jira.plugins.customfield.shared.config.CFConfigItem;

/**
 * User: pedworth
 * Date: 11/3/11
 * <p>The simplest implementation of CFConfigAction</p>
 */
public class NopConfigAction extends CFConfigAction {
    @Override
    public NopConfigurationParameters[] getConfigurableParameters() {
        //The implementation is simple but because of the static call it can't
        //be defined in the generic class
        return NopConfigurationParameters.values();
    }

    @Override
    public CFConfigItem getConfigItem() {
        return new NopConfigItem(getDescriptor(), getGenericConfigManager());
    }

    @Override
    protected String getSessionKey() {
        //again simple, but the static reference stops it being part of CFConfigAction
        return NopCustomField.class.toString();
    }
}
