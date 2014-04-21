package com.atlassian.jira.web.action.setup;

import com.atlassian.config.ConfigurationException;
import com.atlassian.config.bootstrap.BootstrapException;
import com.atlassian.config.bootstrap.DefaultAtlassianBootstrapManager;
import com.atlassian.config.db.DatabaseDetails;
import com.atlassian.config.db.DatabaseList;
import com.atlassian.core.util.PairType;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.database.Datasource;
import com.atlassian.jira.config.database.JdbcDatasource;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.configurator.config.DatabaseType;
import com.atlassian.jira.startup.StartupCheck;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Action for configuring a database connection for JIRA and testing that it works. Part of the setup wizard.
 *
 * @since v4.4
 */
@SuppressWarnings ( { "UnusedDeclaration" })
public class SetupDatabase extends AbstractSetupAction
{
    private static final int DEFAULT_POOL_SIZE = 15;
    private boolean testingConnection; // The hidden field populated when the button is clicked.

    private final BuildUtilsInfo buildUtilsInfo;
    private final DatabaseConfigurationManager databaseConfigurationManager;
    private final IndexLanguageToLocaleMapper languageToLocaleMapper;

    private String language;
    private boolean changingLanguage;

    // Database information
    private String databaseOption = "INTERNAL"; // default to internal
    private String databaseType;
    private String schemaName;
    private static final Map<String, String> SCHEMA_NAMES = ImmutableMap.of("postgres72", "public", "mssql", "dbo");
    private String jdbcHostname;
    private String jdbcPort;
    private String jdbcDatabase;
    private String jdbcSid;
    private String jdbcUsername;
    private String jdbcPassword;
    private Integer poolSize = DEFAULT_POOL_SIZE;

    // from atlassian config (cf. supportedDatabases.properties)
    private DatabaseList databaseList = new DatabaseList();

    private boolean isTestConnectionSuccessful;
    private static final Map<String, DatabaseType> databaseTypeMap = MapBuilder.<String, DatabaseType>newBuilder()
            .add("oracle10g", DatabaseType.ORACLE)
            .add("postgres72", DatabaseType.POSTGRES)
            .add("mysql", DatabaseType.MY_SQL)
            .add("mssql", DatabaseType.SQL_SERVER)
            .toMap();

    public SetupDatabase(final FileFactory fileFactory, final IndexLanguageToLocaleMapper languageToLocaleMapper,
            BuildUtilsInfo buildUtilsInfo, final DatabaseConfigurationManager databaseConfigurationManager,
            final ApplicationProperties applicationProperties)
    {
        super(fileFactory);
        this.languageToLocaleMapper = languageToLocaleMapper;
        this.buildUtilsInfo = buildUtilsInfo;
        this.databaseConfigurationManager = databaseConfigurationManager;
    }

    public String doInput() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        return INPUT;
    }

    public String doDefault()
    {

        if (isDatabaseSetup())
        {
            return forceRedirect("Setup!input.jspa?title=Your+Company+JIRA&mode=public");
        }
        return INPUT;
    }

    @Override
    protected void doValidation()
    {
        // Don't validate if we're already setup or just changing languages
        if (setupAlready() || isChangingLanguage())
        {
            return;
        }

        // if using external db
        if (isExternalDatabase())
        {
            // Make sure a database type is selected!
            if ("".equals(databaseType))
            {
                addError("databaseType", getText("setupdb.error.selectDatabaseType"));
            }

            if (StringUtils.isEmpty(jdbcHostname))
            {
                addError("jdbcHostname", getText("setupdb.error.requireJdbcHostname"));
            }

            if (StringUtils.isEmpty(jdbcPort))
            {
                addError("jdbcPort", getText("setupdb.error.requireJdbcPort"));
            }

            if (isOracleDatabaseType())
            {
                if (StringUtils.isEmpty(jdbcSid))
                {
                    addError("jdbcSid", getText("setupdb.error.requireSID"));
                }
            }
            else
            {
                if (StringUtils.isEmpty(jdbcDatabase))
                {
                    addError("jdbcDatabase", getText("setupdb.error.requireDatabase"));
                }
            }

            if (StringUtils.isEmpty(jdbcUsername))
            {
                addError("jdbcUsername", getText("setupdb.error.requireJdbcUsername"));
            }
        }

        testConnection();

        validateDatabaseIsEmpty();

        super.doValidation();
    }

    private boolean isOracleDatabaseType()
    {
        return "oracle10g".equals(databaseType);
    }

    private boolean isExternalDatabase()
    {
        return "EXTERNAL".equals(databaseOption);
    }

    private void testConnection()
    {
        // Only test the connection if all required fields are entered
        if (!hasAnyErrors())
        {
            try
            {
                final DatabaseConfig databaseConfiguration = createDatabaseConfiguration();
                final StartupCheck startupCheck = databaseConfiguration.testConnection(new DefaultAtlassianBootstrapManager());
                if (startupCheck != null) {
                    addErrorMessage(startupCheck.getFaultDescription());
                }
            }
            catch (BootstrapException e)
            {
                addErrorMessage(getText("setupdb.error.connectionFailed"));
                Throwable cause = e.getCause();
                addErrorMessage(cause.getLocalizedMessage());
                if (cause.getCause() != null && !cause.getLocalizedMessage().equals(cause.getCause().getLocalizedMessage()))
                {
                    addErrorMessage(cause.getCause().getLocalizedMessage());
                }
            }
            catch (IllegalArgumentException e)
            {
                addErrorMessage(getText("setupdb.error.invalidDriver") + e.getCause().getLocalizedMessage());
            }
            isTestConnectionSuccessful = isTestingConnection() && !hasAnyErrors();
        }
    }

    private void validateDatabaseIsEmpty()
    {
        // Only test if database if empty if all previous checks have succeeded
        if (!hasAnyErrors())
        {
            boolean isDatabaseEmpty = false;
            final DatabaseConfig databaseConfiguration = createDatabaseConfiguration();
            try
            {
                isDatabaseEmpty = databaseConfiguration.isDatabaseEmpty(new DefaultAtlassianBootstrapManager());
            }
            catch (BootstrapException e)
            {
                // This really shouldn't happen since we have just successfully tested database connection
                addErrorMessage(getText("setupdb.error.connectionFailed"));
                Throwable cause = e.getCause();
                addErrorMessage(cause.getLocalizedMessage());
                if (cause.getCause() != null && !cause.getLocalizedMessage().equals(cause.getCause().getLocalizedMessage()))
                {
                    addErrorMessage(cause.getCause().getLocalizedMessage());
                }
            }

            if (!isDatabaseEmpty)
            {
                addErrorMessage(getText("setupdb.error.nonemptyDatabase"));
            }

            isTestConnectionSuccessful = isTestingConnection() && !hasAnyErrors();
        }

    }

    @Override
    protected String doExecute() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        if (isDatabaseSetup())
        {
            return forceRedirect("Setup!input.jspa?title=Your+Company+JIRA&mode=public");
        }

        if (isChangingLanguage())
        {
            setLanguage();
            return INPUT;
        }
        else if (isTestingConnection())
        {
            return hasAnyErrors() ? ERROR : INPUT;
        }
        else
        {
            DatabaseConfig databaseConfiguration = createDatabaseConfiguration();
            databaseConfigurationManager.setDatabaseConfiguration(databaseConfiguration);
            // now do the post db setup work
            databaseConfigurationManager.activateDatabase();
            // now that the database and everything is up, we can set the language as selected
            setLanguage();

            return forceRedirect("Setup!input.jspa?title=Your+Company+JIRA&mode=public");
        }
    }

    private DatabaseConfig createDatabaseConfiguration()
    {

        if (isExternalDatabase())
        {
            final DatabaseDetails databaseDetails = getDatabaseDetails(databaseType);
            poolSize = databaseDetails.getPoolSize();
            final String instanceName = isOracleDatabaseType() ? jdbcSid : jdbcDatabase;

            final DatabaseType type = getDatabaseTypeEnum();
            // Set validationQuery for MySQL
            final String validationQuery = type == DatabaseType.MY_SQL ? "select 1" : null;
            // Set Eviction settings for HSQL
            Long minEvictableTimeMillis = null;
            Long timeBetweenEvictionRunsMillis = null;
            if (type == DatabaseType.HSQL)
            {
                minEvictableTimeMillis = 4000L;
                timeBetweenEvictionRunsMillis = 5000L;
            }
            Datasource datasource = new JdbcDatasource(type, jdbcHostname, jdbcPort, instanceName, jdbcUsername, jdbcPassword, poolSize, validationQuery, minEvictableTimeMillis, timeBetweenEvictionRunsMillis);
            return new DatabaseConfig(databaseType, schemaName, datasource);
        }
        // we are doing internal
        return databaseConfigurationManager.getInternalDatabaseConfiguration();
    }

    public boolean isDatabaseConnectionTestWorked()
    {
        return isTestConnectionSuccessful;
    }

    public boolean isDatabaseSetup()
    {
        return databaseConfigurationManager.isDatabaseSetup();
    }

    public Collection<Locale> getInstalledLocales()
    {
        return ComponentAccessor.getComponentOfType(LocaleManager.class).getInstalledLocales();
    }

    public List<PairType> getExternalDatabases()
    {
        PairType defaultSelect = new PairType("", getText("setupdb.database.selectType"));

        List<PairType> selectList = new ArrayList<PairType>();
        selectList.add(defaultSelect);
        selectList.addAll(databaseList.getDatabases());

        return selectList;
    }

    public DatabaseDetails getDatabaseDetails(String database)
    {
        try
        {
            return DatabaseDetails.getDefaults(database);
        }
        catch (ConfigurationException e)
        {
            log.debug(e);
            return null;
        }
    }

    public String getSchemaName(String database)
    {
        return SCHEMA_NAMES.get(database);
    }

    private void setLanguage()
    {
        final ApplicationProperties applicationProperties = getApplicationProperties();
        // if user selects a non-default language, we need to change the default locale
        if (TextUtils.stringSet(getLanguage()))
        {
            applicationProperties.setString(APKeys.JIRA_I18N_DEFAULT_LOCALE, getLanguage());
        }
        // set the indexing language for the selected locale
        applicationProperties.setString(APKeys.JIRA_I18N_LANGUAGE_INPUT, languageToLocaleMapper.getLanguageForLocale(getLocale().toString()));
    }

    private DatabaseType getDatabaseTypeEnum()
    {
        final DatabaseType type = databaseTypeMap.get(databaseType);
        if (type == null)
        {
            throw new IllegalStateException("Unknown database type '" + databaseType + "'");
        }
        return type;
    }

    public String getDatabaseType()
    {
        return databaseType;
    }

    public void setDatabaseType(String databaseType)
    {
        this.databaseType = databaseType;
    }

    public String getDatabaseOption()
    {
        return databaseOption;
    }

    public void setDatabaseOption(String databaseOption)
    {
        this.databaseOption = databaseOption;
    }

    @Override
    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public BuildUtilsInfo getBuildUtilsInfo()
    {
        return buildUtilsInfo;
    }

    public int modulo(int index, int modulus)
    {
        return index % modulus;
    }

    public boolean isTestingConnection()
    {
        return testingConnection;
    }

    public void setTestingConnection(boolean testingConnection)
    {
        this.testingConnection = testingConnection;
    }

    public String getJdbcHostname()
    {
        return jdbcHostname;
    }

    public void setJdbcHostname(String jdbcHostname)
    {
        this.jdbcHostname = jdbcHostname;
    }

    public String getJdbcPassword()
    {
        return jdbcPassword;
    }

    public void setJdbcPassword(String jdbcPassword)
    {
        this.jdbcPassword = jdbcPassword;
    }

    public String getJdbcPort()
    {
        return jdbcPort;
    }

    public void setJdbcPort(String jdbcPort)
    {
        this.jdbcPort = jdbcPort;
    }

    public String getJdbcDatabase()
    {
        return jdbcDatabase;
    }

    public void setJdbcDatabase(String jdbcDatabase)
    {
        this.jdbcDatabase = jdbcDatabase;
    }

    public String getJdbcSid()
    {
        return jdbcSid;
    }

    public void setJdbcSid(String jdbcSid)
    {
        this.jdbcSid = jdbcSid;
    }

    public String getJdbcUsername()
    {
        return jdbcUsername;
    }

    public void setJdbcUsername(String jdbcUsername)
    {
        this.jdbcUsername = jdbcUsername;
    }

    public String getSchemaName()
    {
        return schemaName;
    }

    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    public boolean isChangingLanguage()
    {
        return changingLanguage;
    }

    public void setChangingLanguage(boolean changingLanguage)
    {
        this.changingLanguage = changingLanguage;
    }
}
