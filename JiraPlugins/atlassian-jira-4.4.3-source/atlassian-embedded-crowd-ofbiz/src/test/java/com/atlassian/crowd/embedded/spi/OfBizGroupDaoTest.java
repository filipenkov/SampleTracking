package com.atlassian.crowd.embedded.spi;

public class OfBizGroupDaoTest extends GroupDaoSpiTest
{
    @Override
    protected DaoSpiTestConfig getDaoSpiTestConfig()
    {
        return OfbizDaoSpiTestConfig.INSTANCE;
    }
}
