package org.jcvi.jira.plugins.workflow;

import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.spi.Step;
import org.apache.log4j.Logger;
import org.jcvi.jira.plugins.utils.jdbc.NamedParameterSQL;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * This is a very JCVI specific post function. It will be used to update an
 * ExtentAttribute in the GLK to reflect the current status of the Issue
 */
public class SQLPostFunction extends AbstractPostFunction {
    private static final Logger log = Logger.getLogger(SQLPostFunction.class);

    public static final String NEW_STATUS = "new-status";

    public static final String JNDI_ENVIRONMENT = "java:comp/env";
    //Configuration parameters
    public static final String CONNECTION_SECTION = "database";
      public static final String SUBSECTION_JDBC = "jdbc";
        public static final String JDBC_FIELD_CLASS = "class";
        public static final String JDBC_FIELD_URI      = "uri";
        public static final String JDBC_FIELD_USER     = "user";
        public static final String JDBC_FIELD_PASSWORD = "password";
      public static final String SUBSECTION_JNDI = "jndi";
        public static final String JNDI_FIELD_URI = "uri";

    public static final String SQL_SECTION = "sqlSource";
      public static final String SUBSECTION_RESOURCE = "file";
        public static final String RESOURCE_FIELD_NAME = "resource";
      public static final String SUBSECTION_TEXT = "text";
        public static final String TEXT_FIELD_VALUE = "value";

    public static final String CATALOG_SECTION = "catalog";
      public static final String SUBSECTION_CATALOG  = "true";
        public static final String CATALOG_FILED_FIELD = "field";

    @Override
    public void execute(JIRAWorkflowState<WorkflowFunctionModuleDescriptor> state)
            throws WorkflowException {
        log.debug("execute() called");
        SQLPostFunctionConfig config = new SQLPostFunctionConfig(state);
        //load the config data
        String sqlString = config.getSQL();
        if (sqlString == null) {
            log.error("Failed to update DB because no SQL was specified");
            return;
        }
        String catalogFieldName = config.getCatalogFieldName();

        try {
            Connection con = config.createConnection();
            if (con == null) {
                log.error("Could not get a Database Connection");
            } else {
                if (catalogFieldName != null) {
                    String catalogFieldValue = getFieldValue(state,catalogFieldName);
                    if (catalogFieldValue != null) {
                        con.setCatalog(catalogFieldValue);
                    }
                }
                NamedParameterSQL sql = new NamedParameterSQL(sqlString);
                log.debug("parsed SQL = "+sql.getPreparedStatementSQL());
                PreparedStatement ps = con.prepareStatement(sql.getPreparedStatementSQL());

                for(int i = 1; i <= sql.getNumberOfParameters(); i++) {
                    ps.setString(i,getFieldValue(state, sql.getParameterAt(i)));
                }

                int updated = ps.executeUpdate();
                //todo: add an option to error if less than M / more than N updates occurred?
                log.info("SQL ran successfully, " + updated + " records were updated");
            }
        } catch (SQLException sqle) {
            log.debug("SQL="+sqlString);
            log.error("SQL Failed with: "+sqle);
        }
    }

private static final String DB_PROPS_FILE		= "database.properties";
private static final String DB_DRIVER_KEY 		= "database.driver";
private static final String DB_URI_KEY 			= "database.uri";
private static final String DB_USER_KEY 		= "database.user";
private static final String DB_PASSWORD_KEY 	= "database.password";

    private static class SQLPostFunctionConfig {
        private final JIRAWorkflowState<WorkflowFunctionModuleDescriptor> state;
        
        private Properties dbPropsFromFile = null;
        
        /**
         * Use the DAO to access the configuration information from the
         * workflow.
         *
         * @param dao information about the current transition
         *
         */
        public SQLPostFunctionConfig(JIRAWorkflowState<WorkflowFunctionModuleDescriptor> dao) {
            this.state = dao;
        }

        /**
         * Uses the configuration of the plug-in to create a connection
         * to the database.
         * @return The Connection object
         * @throws SQLException
         */
        public Connection createConnection() throws SQLException{
            String section = CONNECTION_SECTION;
            String subSection = getArgument(section,null,null);

            if (SUBSECTION_JDBC.equals(subSection)) {
            	
            	if (null == dbPropsFromFile)
            	{
            		log.info("Loading db props from "+DB_PROPS_FILE);
            		
            		dbPropsFromFile = new Properties();
            		InputStream inputStream = SQLPostFunctionConfig.class.getClassLoader()
            			.getResourceAsStream(DB_PROPS_FILE);

            			if (inputStream == null) 
            			{
            				throw new SQLException("SQL property file '" + DB_PROPS_FILE+ "' not found in the classpath");
            			}
            			
            			try
            			{
            				dbPropsFromFile.load(inputStream);
            			}
            			catch (IOException ioe)
            			{
            				throw new SQLException("I/O Exception reading SQL property file '" + DB_PROPS_FILE);
            			}
            	}
            	//read the parameters
            	String dbClass  = dbPropsFromFile.getProperty(DB_DRIVER_KEY);
                String uri      = dbPropsFromFile.getProperty(DB_URI_KEY);
                String user     = dbPropsFromFile.getProperty(DB_USER_KEY);
                String password = dbPropsFromFile.getProperty(DB_PASSWORD_KEY);
                
                log.info("DB props: "+dbClass+" / "+uri+" / "+user+" / "+password);
            	
                //read the parameters
//                String dbClass  = getArgument(section,subSection, JDBC_FIELD_CLASS);
//                String uri      = getArgument(section,subSection,JDBC_FIELD_URI);
//                String user     = getArgument(section,subSection,JDBC_FIELD_USER);
//                String password = getArgument(section,subSection,JDBC_FIELD_PASSWORD);

                //check required properties
                if (uri == null || dbClass == null) {
                    throw new SQLException(
                            "Connecting using JDBC requires the properties " +
                                    "'"+ JDBC_FIELD_URI +"' and "+
                                    "'"+ JDBC_FIELD_CLASS +"'");
                }

                try { //load / check the class
                    Class.forName(dbClass);
                } catch (ClassNotFoundException cnfe) {
                    throw new SQLException("driver class: '"+dbClass+"' not found");
                }

                Properties props = new Properties();
                //only add user/password if they exist
                if (user != null) {
                    props.put("user",user);
                }
                if (password != null) {
                    props.put("password",password);
                }

                return DriverManager.getConnection(uri,props);
            } else if (SUBSECTION_JNDI.equals(subSection)) {
                //read the parameters
                String uri = getArgument(section,subSection, JNDI_FIELD_URI);

                //check required properties
                if (uri == null) {
                    throw new SQLException(
                            "Connecting using JNDI requires the property " +
                                    "'"+ JNDI_FIELD_URI+"'");
                }

                //see if the uri is valid
                try {
                    Context initCtx = new InitialContext();
                    Context envCtx = (Context)initCtx.lookup(JNDI_ENVIRONMENT);
                    DataSource ds = (DataSource)envCtx.lookup(uri);
                    //Return a new Connection
                    return ds.getConnection();
                } catch (NamingException ne) {
                    throw new SQLException(
                            "Could not find JNDI data source. "+ne.getMessage());
                }
            }
            //Todo add JNDI that uses alternative directories
//            Hashtable env = new Hashtable();
//            env.put(...
//            Context initialContext = new InitialContext(env);
//
//          The env can hold various parameters e.g.
//            Context.INITIAL_CONTEXT_FACTORY
//            Context.PROVIDER_URL
//          but there are a lot of others that are required for some remote
//          stores or local factory implementations and not others
            return null;
        }

        /**
         * Uses the configuration from the workflow to decide where to get
         * the SQL from. The actual SQL is loaded from Resources defined in
         * atlassian-plugin.xml
         * @return  The SQL to use, with field references unresolved.
         */
        public String getSQL() {
            String section = SQL_SECTION;
            String subSection = getArgument(section,null,null);
            if (SUBSECTION_RESOURCE.equals(subSection)) {
                String resourceName = getArgument(section,subSection,RESOURCE_FIELD_NAME);
                if (resourceName == null) {
                    log.error("No resource name specified for the SQL");
                } else {
                    try {
                        return state.readResource("SQL", resourceName);
                    } catch (IOException ioe) {
                        log.error("failed to load sql from resource: "+resourceName);
                    }
                }
            }
            if (SUBSECTION_TEXT.equals(subSection)) {
                return getArgument(section,subSection,TEXT_FIELD_VALUE);
            }
            return null;
        }

        /**
         * Find the field that is to be used to set the catalog, if any
         * @return  null if the catalog is not to be set, otherwise the
         * value to set the catalog to
         */
        public String getCatalogFieldName() {
            String section = CATALOG_SECTION;
            String subSection = getArgument(section,null,null);
            if (SUBSECTION_CATALOG.equals(subSection)) {
                return getArgument(section,subSection,CATALOG_FILED_FIELD);
            }
            return null;
        }

        //creates the structured names used in the config
        private String getArgument(String section, String subSection, String field) {
            String name = section;
            if (subSection != null) {
                name += "-"+subSection;
                if (field != null) {
                    name += "-"+field;
                }
            }
            return state.getArgument(name);
        }
    }

    /**
     * Carries out the lookup of a value referenced in the SQL
     * The value could come from the Issue or the Transient Variables.
     * Either way it is accessed via the JIRAWorkflowState
     * @param fieldName The name from the SQL
     * @return  A value to populate the prepared statement with
     */
    protected static String getFieldValue(JIRAWorkflowState<WorkflowFunctionModuleDescriptor> state,
            String fieldName) {
        //intercept the special case of new-status
        if (NEW_STATUS.equals(fieldName)) {
            Object nextStepObj = state.getTransientVariable(TransientVariableTypes.CREATED_STEP);
            if (nextStepObj != null && nextStepObj instanceof Step) {
                return ((Step)nextStepObj).getStatus();
            } else {
                log.warn("Could not find createdStep in the transient variables");
                return null;
            }
        }

        //Try the Issue
        String value = state.getIssueFieldValue(fieldName);
        if (value != null) {
            return value;
        }

        //last try, is it a transient variable?
        TransientVariableTypes transientVariable = TransientVariableTypes.getByName(fieldName);
        if (transientVariable != null) {
            //it matches the name of a transientVariable
            Object valueObj = state.getTransientVariable(transientVariable);
            if (valueObj != null) {
                return valueObj.toString();
            }
        }

        //we are out of things to try
        log.error("Couldn't find a value for '"+fieldName+"'");
        return null;
    }
}