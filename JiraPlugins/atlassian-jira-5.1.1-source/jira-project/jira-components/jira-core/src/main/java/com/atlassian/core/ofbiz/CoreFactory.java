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
 * @deprecated See individual methods for individual replacements. Since v5.0.
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

    /**
     * Old way to get a GenericDelegator.
     * <p>
     * Normally you would use {@link com.atlassian.jira.ofbiz.OfBizDelegator} instead.
     * Get OfBizDelegator injected or use {@link com.atlassian.jira.component.ComponentAccessor#getOfBizDelegator()}.
     * <p>
     * If you really want the raw Entity Engine "delegator", get the instance of DelegatorInterface from Pico.
     * You can call <tt>ComponentAccessor#getComponent(DelegatorInterface.class)</tt> if you need static access.
     *
     * @return GenericDelegator
     *
     * @deprecated Use {@link com.atlassian.jira.ofbiz.OfBizDelegator} or get {@link org.ofbiz.core.entity.DelegatorInterface} from Pico instead. Since v5.0.
     */
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

    /**
     * ActionDispatcher is used for running "non-web actions".
     * The use of non-web actions is dperecated, and the few remaining instances are being replaced.
     *
     * @return ActionDispatcher
     *
     * @deprecated See individual BackEnd Action or constants in {@link com.atlassian.jira.action.ActionNames} for particular replacement. Normally a Service or Manager class in Pico Dependency Injection container. Since v5.0.
     */
    public static ActionDispatcher getActionDispatcher()
    {
        if (actionDispatcher == null)
        {
            actionDispatcher = new DefaultActionDispatcher();
        }
        return actionDispatcher;
    }

    /**
     * Presumably, this was intended soley for injecting a MockActionDispatcher for Unit Tests?
     *
     * @param newActionDispatcher ActionDispatcher
     *
     * @deprecated Because use of {@link #getActionDispatcher()} is deprecated. Since v5.0.
     */
    public static void setActionDispatcher(final ActionDispatcher newActionDispatcher)
    {
        CoreFactory.actionDispatcher = newActionDispatcher;
    }

    /**
     * Old way to get AssociationManager
     *
     * @return AssociationManager
     *
     * @deprecated Get AssociationManager from dependency injection instead. Since v5.0.
     */
    public static AssociationManager getAssociationManager()
    {
        if (associationManager == null)
        {
            associationManager = new DefaultAssociationManager(getGenericDelegator());
        }
        return associationManager;
    }

    /**
     * Presumably, this was intended to be useful for Unit Tests?
     *
     * @param associationManager AssociationManager
     *
     * @deprecated Get AssociationManager from dependency injection instead. Since v5.0.
     */
    public static void setAssociationManager(final AssociationManager associationManager)
    {
        CoreFactory.associationManager = associationManager;
    }
}
