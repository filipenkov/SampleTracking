package com.atlassian.crowd.embedded.ofbiz;

import static org.ofbiz.core.entity.TransactionUtil.beginLocalTransaction;
import static org.ofbiz.core.entity.TransactionUtil.rollbackLocalTransaction;

import org.hsqldb.jdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.config.EntityConfigUtil;

public abstract class AbstractTransactionalOfBizTestCase
{
    static
    {
        new jdbcDriver(); // Will only work if in a static block
    }

    private GenericDelegator delegator;

    @Before
    public void setUpOfBiz() throws Exception
    {
        delegator = GenericDelegator.getGenericDelegator("default");
        beginLocalTransaction(getDataSourceName(), -1);
    }

    @After
    public void tearDownOfBiz() throws Exception
    {
        rollbackLocalTransaction(true);
        delegator = null;
    }

    protected final GenericDelegator getGenericDelegator()
    {
        return delegator;
    }

    protected String getDataSourceName()
    {
        return "defaultDS";
    }
}
