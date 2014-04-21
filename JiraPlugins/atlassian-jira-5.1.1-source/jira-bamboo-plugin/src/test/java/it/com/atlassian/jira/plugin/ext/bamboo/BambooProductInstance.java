package it.com.atlassian.jira.plugin.ext.bamboo;

import com.atlassian.pageobjects.ProductInstance;
import org.apache.log4j.Logger;

public class BambooProductInstance implements ProductInstance
{

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logger.getLogger(BambooProductInstance.class);
    // ------------------------------------------------------------------------------------------------------- Constants

    public static final BambooProductInstance INSTANCE = new BambooProductInstance();
    private static final int PORT = 19901;
    private static final String CONTEXT = "/jbam";

    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors

    private BambooProductInstance()
    {
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods

    public String getBaseUrl()
    {
        return "http://localhost:" + getHttpPort() + CONTEXT;
    }

    public int getHttpPort()
    {
        return PORT;
    }

    public String getContextPath()
    {
        return CONTEXT;
    }

    public String getInstanceId()
    {
        return "BAMBOO";
    }

    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
