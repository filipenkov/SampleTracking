package com.atlassian.crowd.embedded.spi;

public class OfBizUserDaoSearchSpiTest extends UserDaoSearchSpiTest
{
    @Override
    protected DaoSpiTestConfig getDaoSpiTestConfig()
    {
        return OfbizDaoSpiTestConfig.INSTANCE;
    }
}
