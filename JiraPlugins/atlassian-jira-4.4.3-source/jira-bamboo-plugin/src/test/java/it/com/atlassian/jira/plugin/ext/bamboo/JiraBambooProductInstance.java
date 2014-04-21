package it.com.atlassian.jira.plugin.ext.bamboo;

import com.atlassian.pageobjects.ProductInstance;
import org.apache.log4j.Logger;

final class JiraBambooProductInstance implements ProductInstance
{
    private static final Logger log = Logger.getLogger(JiraBambooProductInstance.class);
    // ------------------------------------------------------------------------------------------------------- Constants

    public static final JiraBambooProductInstance INSTANCE = new JiraBambooProductInstance();
    private static final int PORT = 1990;
    private static final String CONTEXT = "/jbam";

    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors

    private JiraBambooProductInstance()
    {
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods

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
        return "JIRA";
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
