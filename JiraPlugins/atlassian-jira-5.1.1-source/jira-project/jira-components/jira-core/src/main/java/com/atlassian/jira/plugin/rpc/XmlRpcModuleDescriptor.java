package com.atlassian.jira.plugin.rpc;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.module.ModuleFactory;

public class XmlRpcModuleDescriptor extends RpcModuleDescriptor
{
    public XmlRpcModuleDescriptor(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

}