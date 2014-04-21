package com.atlassian.jira.ofbiz;

import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.TenantReference;
import org.ofbiz.core.entity.ConnectionFactory;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.EntityConfigUtil;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Copyright All Rights Reserved.
 * Created: christo 12/10/2006 15:50:59
 */
public class DefaultOfBizConnectionFactory implements OfBizConnectionFactory
{
    private final TenantReference tenantReference = MultiTenantContext.getTenantReference();
    private final EntityConfigUtil entityConfigUtil = EntityConfigUtil.getInstance();

    public Connection getConnection() throws SQLException, DataAccessException
    {
        try
        {
            return ConnectionFactory.getConnection(getDatabaseConfig().getDatasourceName());
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public DatasourceInfo getDatasourceInfo()
    {
        return entityConfigUtil.getDatasourceInfo(getDatabaseConfig().getDatasourceName());
    }

    @Override
    public String getDelegatorName()
    {
        return getDatabaseConfig().getDelegatorName();
    }

    private DatabaseConfig getDatabaseConfig()
    {
        return tenantReference.get().getConfig(DatabaseConfig.class);
    }
}
