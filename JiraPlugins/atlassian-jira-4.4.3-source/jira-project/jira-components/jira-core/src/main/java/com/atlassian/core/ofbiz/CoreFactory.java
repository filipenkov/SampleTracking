package com.atlassian.core.ofbiz;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.ofbiz.association.DefaultAssociationManager;
import com.atlassian.core.action.ActionDispatcher;
import com.atlassian.core.action.DefaultActionDispatcher;

import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.startup.SystemTenantProvider;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.MultiTenantCreator;
import com.atlassian.multitenant.Tenant;
import org.ofbiz.core.entity.GenericDelegator;

/**
 * This is the Factory for "atlassian-core".
 * <p/>
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 * <p/>
 * TODO: Offer alternatives and deprecate this Factory.
 */
public class CoreFactory
{
    private static ActionDispatcher actionDispatcher;
    private static AssociationManager associationManager;
    private static GenericDelegator genericDelegator;

    public static void globalRefresh()
    {
        genericDelegator = null;
        actionDispatcher = null;
        associationManager = null;
    }

    public static GenericDelegator getGenericDelegator()
    {
        if (genericDelegator == null)
        {
            if (MultiTenantContext.isEnabled())
            {
                genericDelegator = MultiTenantContext.getFactory().createEnhancedComponent(
                        new MultiTenantCreator<GenericDelegator>()
                        {
                            public GenericDelegator create(final Tenant tenant)
                            {
                                return GenericDelegator.getGenericDelegator(tenant.getConfig(DatabaseConfig.class).getDelegatorName());
                            }
                        }, GenericDelegator.class);
            }
            else
            {
                genericDelegator = GenericDelegator.getGenericDelegator(SystemTenantProvider.SYSTEM_TENANT_DELEGATOR_NAME);
            }
        }
        return genericDelegator;
    }


    public static ActionDispatcher getActionDispatcher()
    {
        if (actionDispatcher == null)
        {
            actionDispatcher = new DefaultActionDispatcher();
        }
        return actionDispatcher;
    }

    public static void setActionDispatcher(final ActionDispatcher newActionDispatcher)
    {
        CoreFactory.actionDispatcher = newActionDispatcher;
    }

    public static AssociationManager getAssociationManager()
    {
        if (associationManager == null)
        {
            associationManager = new DefaultAssociationManager(getGenericDelegator());
        }
        return associationManager;
    }

    public static void setAssociationManager(final AssociationManager associationManager)
    {
        CoreFactory.associationManager = associationManager;
    }
}
