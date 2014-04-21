package com.atlassian.crowd.embedded.spi;

public class OfbizMembershipDaoSearchSpiTest extends MembershipDaoSearchSpiTest
{
    @Override
    protected DaoSpiTestConfig getDaoSpiTestConfig()
    {
        return OfbizDaoSpiTestConfig.INSTANCE;
    }
}
