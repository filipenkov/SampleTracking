package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.plugins.mail.ServiceConfiguration;
import com.atlassian.jira.plugins.mail.extensions.MessageHandlerModuleDescriptor;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginAccessor;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @since v5.0
 */
public abstract class AbstractEditHandlerDetailsWebAction extends MailWebActionSupport
{
    static final Logger log = Logger.getLogger(AbstractEditHandlerDetailsWebAction.class);

    protected final ServiceConfiguration configuration = getConfiguration();

    protected final PluginAccessor pluginAccessor;

    protected MessageHandlerModuleDescriptor descriptor;

    protected class WebWorkErrorCollector implements MessageHandlerErrorCollector
    {
        // we need such constructor - otherwise according to JLS this class will have by default protected construction
        // and inheritors of outer class will not be able to instatiate it.
        @SuppressWarnings ( { "UnusedDeclaration" })
        public WebWorkErrorCollector()
        {
        }

        @Override
        public void info(String info)
        {
            log.info(info);
        }

        @Override
        public void info(String info, Throwable e)
        {
            log.info(info, e);
        }

        @Override
        public void error(String s, Throwable throwable)
        {
            addErrorMessage(s);
        }

        @Override
        public void error(String s)
        {
            addErrorMessage(s);
        }

        @Override
        public void warning(String s)
        {
            log.warn(s);
        }

        @Override
        public void warning(String s, Throwable throwable)
        {
            log.warn(s, throwable);
        }

    }

    public AbstractEditHandlerDetailsWebAction(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;

        if (configuration != null) {
            this.descriptor = (MessageHandlerModuleDescriptor) pluginAccessor.getEnabledPluginModule(configuration.getHandlerKey());
        }
    }

    @Override
    public String doDefault() throws Exception
    {
        String result = super.doDefault();

        if (configuration == null || descriptor == null) {
            return returnCompleteWithInlineRedirect("EditServerDetails!default.jspa");
        }

        if (configuration.getServiceId() != null) {
            JiraServiceContainer serviceContainer = getService(configuration.getServiceId());
            if (serviceContainer != null) {
                copyServiceSettings(serviceContainer);
            } else {
                return returnCompleteWithInlineRedirect("IncomingMailServers.jspa");
            }
        }

        return result;
    }

    protected abstract void copyServiceSettings(JiraServiceContainer serviceContainer)
            throws ObjectConfigurationException;

    protected Map<String, String[]> getServiceParams() throws Exception
    {
        return MapBuilder.<String, String[]>newBuilder()
                .addAll(configuration.toServiceParams(pluginAccessor))
                .addAll(getAdditionalServiceParams())
                .add(AbstractMessageHandlingService.KEY_HANDLER_PARAMS, new String[] { ServiceUtils.toParameterString(getHandlerParams()) })
                .toMutableMap();
    }

    protected abstract Map<String, String> getHandlerParams();
    protected Map<String, String[]> getAdditionalServiceParams() throws Exception {
        return Collections.emptyMap();
    }

    @Nonnull
    public String getHandlerName() {
        return configuration != null ? pluginAccessor.getEnabledPluginModule(configuration.getHandlerKey()).getName() : "";
    }

    public boolean isEditing() {
        return configuration != null && configuration.getServiceId() != null;
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (configuration == null) {
            return returnCompleteWithInlineRedirect("EditServerDetails!default.jspa");
        }

        if ((isEditing() && !canEditService(configuration.getServiceId()))
                || (!isEditing() && !canAddService(configuration.getServiceClass()))) {
            return "securitybreach";
        }

        try
        {
            if (!isEditing()) {
                getServiceManager().addService(configuration.getServiceName(),
                    configuration.getServiceClass(), (configuration.getDelay() * 60000), getServiceParams()).getId();
            } else {
                final JiraServiceContainer service = getServiceManager().getServiceWithId(configuration.getServiceId());
                if (!service.getName().equals(configuration.getServiceName())
                        || !service.getServiceClass().equals(configuration.getServiceClass())) {
                    if (!canAddService(configuration.getServiceClass())) {
                        return "securitybreach";
                    }
                    // need to remove and add the service again to change it's name
                    final Long serviceId = getServiceManager().addService(configuration.getServiceName(),
                        configuration.getServiceClass(), (configuration.getDelay() * 60000),
                            getServiceParams()).getId();
                    getServiceManager().removeService(configuration.getServiceId());
                    configuration.setServiceId(serviceId);
                    setConfiguration(configuration);
                } else {
                    getServiceManager().editService(configuration.getServiceId(), configuration.getDelay() * 60000, getServiceParams());
                }
            }

            if (getHasErrorMessages()) {
                return INPUT;
            }

            return returnCompleteWithInlineRedirect(ViewMailServers.INCOMING_MAIL_ACTION);
        }
        catch (Exception e)
        {
            log.error(getText("jmp.editHandlerDetails.cant.add.service", configuration.getServiceName()), e);
            addErrorMessage(getText("admin.errors.error.adding.service")+ " " + e.toString() + ".");
        }

        return INPUT;
    }

}
