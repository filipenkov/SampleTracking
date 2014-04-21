package com.atlassian.crowd.embedded.spi;

public class OfBizUserDaoSpiTest extends UserDaoSpiTest
{
    @Override
    protected DaoSpiTestConfig getDaoSpiTestConfig()
    {
        return OfbizDaoSpiTestConfig.INSTANCE;
    }
}
