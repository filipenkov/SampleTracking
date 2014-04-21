package com.atlassian.crowd.embedded.spi;

/**
 *
 */
public class OfBizMembershipDaoSpiTest extends MembershipDaoSpiTest
{
    @Override
    protected DaoSpiTestConfig getDaoSpiTestConfig()
    {
        return OfbizDaoSpiTestConfig.INSTANCE;
    }
}
