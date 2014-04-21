/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jul 28, 2004
 * Time: 11:01:00 AM
 */
package com.atlassian.jira.plugin.issueoperation;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.RequiresRestart;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

/**
 * An issue operation plugin adds extra operations to JIRA's View Issue page.
 *
 * @deprecated as of JIRA 4.1 this plugin module is no longer used in favour of web items. It is scheduled to be
 * removed from JIRA in future releases. Please do not implement this module any more and migrate existing ones to
 * web items
 * @see <a href="http://confluence.atlassian.com/x/EBUC">Issue operations module documentation</a>
 */
@Deprecated
public class IssueOperationModuleDescriptor extends JiraResourcedModuleDescriptor<PluggableIssueOperation> implements OrderableModuleDescriptor
{
    private int order;

    public IssueOperationModuleDescriptor(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        assertResourceExists("velocity", "view");

        order = ModuleDescriptorXMLUtils.getOrder(element);
    }

    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(PluggableIssueOperation.class);
    }

    public PluggableIssueOperation getOperation()
    {
        return getModule();
    }

    public int getOrder()
    {
        return order;
    }
}
