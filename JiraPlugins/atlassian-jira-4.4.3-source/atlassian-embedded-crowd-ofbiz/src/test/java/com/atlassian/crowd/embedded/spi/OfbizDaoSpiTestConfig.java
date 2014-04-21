package com.atlassian.crowd.embedded.spi;

public enum OfbizDaoSpiTestConfig implements AbstractDaoSpiTest.DaoSpiTestConfig
{
    INSTANCE;

    public String getConfigPath()
    {
       return "ofBizSpiTestContext.xml";
    }

    public String[] getTableNames()
    {
        return new String[] {
                "cwd_application_address",
                "cwd_application",
                "cwd_membership",
                "cwd_group_attribute",
                "cwd_group",
                "cwd_user_attribute",
                "cwd_user_credential_record",
                "cwd_user",
                "cwd_directory_attribute",
                "cwd_directory_operation",
                "cwd_directory"
        };
    }
}
