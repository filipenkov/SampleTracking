package com.atlassian.jira.configurator.console;

import com.atlassian.jira.configurator.Configurator;
import com.atlassian.jira.configurator.config.ConnectionPoolField;
import com.atlassian.jira.configurator.config.DatabaseType;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.SettingsLoader;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.configurator.config.Validator;
import com.atlassian.jira.configurator.db.ConfigField;
import com.atlassian.jira.configurator.db.DatabaseConfigConsole;
import com.atlassian.jira.configurator.db.DatabaseConfigConsoleImpl;
import com.atlassian.jira.configurator.db.HsqlDatabaseConfig;
import com.atlassian.jira.configurator.db.MySqlDatabaseConfig;
import com.atlassian.jira.configurator.db.OracleDatabaseConfig;
import com.atlassian.jira.configurator.db.PostgresDatabaseConfig;
import com.atlassian.jira.configurator.db.SqlServerDatabaseConfig;
import com.atlassian.jira.exception.ParseException;
import org.ofbiz.core.entity.config.ConnectionPoolInfo;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Runs the Configurator in the console (no GUI).
 *
 * @since 4.1
 */
public class ConfiguratorConsole
{
    final ConsoleProvider console = ConsoleProvider.Factory.getInstance();

    private final DatabaseConfigConsole HSQL_CONFIG = new HsqlDatabaseConfig();
    private final DatabaseConfigConsole MYSQL_CONFIG = new DatabaseConfigConsoleImpl(new MySqlDatabaseConfig());
    private final DatabaseConfigConsole ORACLE_CONFIG = new DatabaseConfigConsoleImpl(new OracleDatabaseConfig());
    private final DatabaseConfigConsole POSTGRES_CONFIG = new DatabaseConfigConsoleImpl(new PostgresDatabaseConfig());
    private final DatabaseConfigConsole SQL_SERVER_CONFIG = new DatabaseConfigConsoleImpl(new SqlServerDatabaseConfig());

    private Settings settings = new Settings();
    private DatabaseType selectedDatabaseType = DatabaseType.HSQL;

    public void setSettings(Settings settings)
    {
        this.settings = settings;
        selectedDatabaseType = settings.initDatabaseType(false);
        try
        {
            getSelectedDatabaseConfig().setSettings(settings);
        }
        catch (ParseException e)
        {
            console.printErrorMessage("Unable to fully parse the current JDBC settings. Some settings may be blank.");
            console.printErrorMessage("Parse Exception: " + e.getMessage());
        }
    }

    public void start()
    {
        showWelcomeMessage();
        try
        {
            showMainMenu();
            System.exit(0);
        }
        catch (IOException e)
        {
            console.println();
            console.printErrorMessage("A fatal IOException has occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private int showMainMenu() throws IOException
    {
        do
        {
            console.println();
            console.println("--- Main Menu ---");
            showMenuItem('H', "Configure JIRA Home");
            showMenuItem('D', "Database Selection");
            showMenuItem('A', "Advanced Settings");
            showMenuItem('S', "Save and Exit");
            showMenuItem('X', "Exit without Saving");
            console.println();
        }
        while (processMainMenu());
        return 0;
    }

    private boolean processMainMenu() throws IOException
    {
        while (true)
        {
            final char ch = readMenuChoice("Main Menu");
            switch (ch)
            {
                case '\r':
                case '\n':
                    continue;  // reprompt
                case '?':
                    return true;  // redisplay menu
                case 'X':
                    return false;  // done
                case 'H':
                    showJiraHomeSettings();
                    return true;
                case 'D':
                    showDatabaseSettings();
                    return true;
                case 'S':
                    return !saveSettings();
                case 'A':
                    showAdvancedSettings();
                    return true;
                default:
                    console.println("Unknown command '" + ch + "'");
                    return true;
            }
        }
    }

    private void showJiraHomeSettings() throws IOException
    {
        console.println("Current JIRA Home: " + settings.getJiraHome());
        while (acceptNewJiraHome())
        {
            // repeat until it succeeds or they cancel
        }
    }

    private boolean acceptNewJiraHome() throws IOException
    {
        String newJiraHome = console.readLine("New JIRA Home");
        if (newJiraHome.length() == 0 || newJiraHome.equals(settings.getJiraHome()))
        {
            console.println("The JIRA Home directory was not changed.");
            return false;
        }

        final File file = new File(newJiraHome).getAbsoluteFile();
        if (!file.isDirectory())
        {
            console.printErrorMessage(file.exists()
                    ? "The specified path already exists and it is not a directory."
                    : "That directory does not exist.");
            return true;
        }

        settings.setJiraHome(file.getAbsolutePath());
        if (new File(file, "dbconfig.xml").canRead())
        {
            console.println("The specified directory has a dbconfig.xml in it.");
            if (console.readYesNo("Reload database configuration", true))
            {
                try
                {
                    settings = SettingsLoader.reloadDbConfig(newJiraHome);
                    console.println("Settings successfully reloaded.");
                }
                catch (IOException ioe)
                {
                    console.printErrorMessage(ioe);
                    console.printErrorMessage("Some of your settings may not have been loaded.");
                }
            }
        }
        return false;
    }

    private boolean saveSettings()
    {
        try
        {
            Configurator.saveSettings(gatherNewSettings());
            console.println("Settings saved successfully.");
            return true;
        }
        catch (ValidationException ex)
        {
            console.printErrorMessage(ex);
        }
        catch (IOException ex)
        {
            console.printErrorMessage(ex);
        }
        return false;
    }

    private Settings gatherNewSettings() throws ValidationException
    {
        getSelectedDatabaseConfig().saveSettings(settings);
        return settings;
    }

    private void showWelcomeMessage()
    {
        console.println("----------------------");
        console.println("JIRA Configurator v1.1");
        console.println("----------------------");
    }

    private void showDatabaseSettings() throws IOException
    {
        do
        {
            console.println();
            console.println("--- Database Selection ---");
            console.println("  Database Type : " + selectedDatabaseType.getDisplayName());
            console.println("  Instance      : " + getSelectedDatabaseConfig().getInstanceName());
            final String pass = getSelectedDatabaseConfig().getPassword();
            console.println("  Connect As    : " + getSelectedDatabaseConfig().getUsername() + " / " +
                    ((pass != null && pass.length() > 0) ? "*****" : "(no password)"));
            console.println();

            final char defaultCommand = selectedDatabaseType.getDisplayName().charAt(0);
            showMenuItem('H', "HSQL (not for production use)", defaultCommand);
            showMenuItem('M', "MySQL", defaultCommand);
            showMenuItem('O', "Oracle", defaultCommand);
            showMenuItem('P', "PostgreSQL", defaultCommand);
            showMenuItem('S', "SQL Server (MS-SQL)", defaultCommand);
            console.println();
            showMenuItem('X', "Return to Main Menu");
            console.println();
        }
        while (processDatabaseSettings());
    }

    private boolean processDatabaseSettings() throws IOException
    {
        while (true)
        {
            final char defaultCommand = selectedDatabaseType.getDisplayName().charAt(0);
            final char ch = readMenuChoice("Database Selection [" + defaultCommand + ']');
            switch (ch)
            {
                case '\r':
                    continue;  // reprompt
                case '?':
                    return true;  // redisplay menu
                case 'X':
                    return false;  // done
                case '\n':
                    // Don't change the selected DB
                    doDatabaseConfig();
                    return true;
                case 'H':
                    selectedDatabaseType = DatabaseType.HSQL;
                    doDatabaseConfig();
                    return true;
                case 'M':
                    selectedDatabaseType = DatabaseType.MY_SQL;
                    doDatabaseConfig();
                    return true;
                case 'O':
                    selectedDatabaseType = DatabaseType.ORACLE;
                    doDatabaseConfig();
                    return true;
                case 'P':
                    selectedDatabaseType = DatabaseType.POSTGRES;
                    doDatabaseConfig();
                    return true;
                default:
                    console.println("Unknown command '" + ch + "'");
                    return true;
            }
        }
    }

    private void doDatabaseConfig() throws IOException
    {
        final DatabaseConfigConsole databaseConfig = getSelectedDatabaseConfig();
        console.println(databaseConfig.getDatabaseType() + " Database Configuration.");

        final ConfigField[] fields = databaseConfig.getFields();
        if (fields == null)
        {
            console.println("The built-in " + selectedDatabaseType.getDisplayName() + " database is auto-configured.");
            return;
        }

        for (ConfigField configField : databaseConfig.getFields())
        {
            if (configField.isPassword())
            {
                final String oldValue = (configField.getValue().length() > 0) ? "*****" : "";
                final String newValue = console.readPassword(configField.getLabel() + " (" + oldValue + ')');
                configField.setValue(newValue);
            }
            else
            {
                final String oldValue = configField.getValue();
                final String newValue = console.readLine(configField.getLabel() + " (" + oldValue + ')');
                if (newValue.length() > 0)
                {
                    configField.setValue(newValue);
                }
            }
        }

        if (console.readYesNo("Test Connection", true))
        {
            console.println("Attempting to connect to " + getSelectedDatabaseConfig().getInstanceName());
            String error = testConnection(databaseConfig);
            if (error == null)
            {
                console.println("Connection successful!");
            }
            else
            {
                console.printErrorMessage("Connection failed: " + error);
            }
        }
    }

    private String menuItemAndValue(ConnectionPoolField field, Object value)
    {
        final String label = field.label();
        final StringBuilder sb = new StringBuilder(128).append(label);
        while (sb.length() < 40)
        {
            sb.append(' ');
        }
        sb.append(": ");
        if (value != null)
        {
            sb.append(value);
        }
        else
        {
            sb.append("(default)");
        }
        return (value != null) ? (label + " (" + value + ")") : label;
    }

    private <T> T readValue(ConnectionPoolField field, T currentValue, Validator<T> validator) throws IOException
    {
        try
        {
            console.println(field.label());
            console.println("  " + field.description());
            console.println("  Default: " + field.defaultValue());
            console.flush();
            T newValue = validator.apply(field.label(), console.readLine("New value"));
            if (newValue != null)
            {
                console.println(field.label() + " = " + newValue);
            }
            else
            {
                console.println(field.label() + " restored to default setting.");
            }
            console.println();
            return newValue;
        }
        catch (ValidationException ve)
        {
            console.printErrorMessage(ve);
            console.println();
            return currentValue;
        }
    }

    private void showAdvancedSettings() throws IOException
    {
        final ConnectionPoolInfo.Builder poolInfo = settings.getConnectionPoolInfoBuilder();
        do {
            console.println();
            console.println("--- Advanced Settings ---");
            console.println();
            console.println("Scalability and Performance:");
            showMenuItem('1', menuItemAndValue(ConnectionPoolField.MAX_SIZE, poolInfo.getPoolMaxSize()));
            showMenuItem('2', menuItemAndValue(ConnectionPoolField.MAX_IDLE, poolInfo.getPoolMaxIdle()));
            showMenuItem('3', menuItemAndValue(ConnectionPoolField.MIN_SIZE, poolInfo.getPoolMinSize()));
            showMenuItem('4', menuItemAndValue(ConnectionPoolField.INITIAL_SIZE, poolInfo.getPoolInitialSize()));
            showMenuItem('5', menuItemAndValue(ConnectionPoolField.MAX_WAIT, poolInfo.getPoolMaxWait()));
            showMenuItem('6', menuItemAndValue(ConnectionPoolField.POOL_STATEMENTS, poolInfo.getPoolPreparedStatements()));
            showMenuItem('7', menuItemAndValue(ConnectionPoolField.MAX_OPEN_STATEMENTS, poolInfo.getMaxOpenPreparedStatements()));
            console.println();
            console.println("Eviction Policy:");
            showMenuItem('A', menuItemAndValue(ConnectionPoolField.VALIDATION_QUERY, poolInfo.getValidationQuery()));
            showMenuItem('B', menuItemAndValue(ConnectionPoolField.VALIDATION_QUERY_TIMEOUT, poolInfo.getValidationQueryTimeout()));
            showMenuItem('C', menuItemAndValue(ConnectionPoolField.TEST_ON_BORROW, poolInfo.getTestOnBorrow()));
            showMenuItem('D', menuItemAndValue(ConnectionPoolField.TEST_ON_RETURN, poolInfo.getTestOnReturn()));
            showMenuItem('E', menuItemAndValue(ConnectionPoolField.TEST_WHILE_IDLE, poolInfo.getTestWhileIdle()));
            showMenuItem('F', menuItemAndValue(ConnectionPoolField.TIME_BETWEEN_EVICTION_RUNS, poolInfo.getTimeBetweenEvictionRunsMillis()));
            showMenuItem('G', menuItemAndValue(ConnectionPoolField.MIN_EVICTABLE_IDLE_TIME, poolInfo.getMinEvictableTimeMillis()));
            showMenuItem('H', menuItemAndValue(ConnectionPoolField.REMOVE_ABANDONED, poolInfo.getRemoveAbandoned()));
            showMenuItem('I', menuItemAndValue(ConnectionPoolField.REMOVE_ABANDONED_TIMEOUT, poolInfo.getRemoveAbandonedTimeout()));
            console.println();
            showMenuItem('X', "Exit the Advanced Settings menu");
            showMenuItem('?', "Redisplay this menu");
            console.println();
        } while (acceptAdvancedSettings());
    }

    private boolean acceptAdvancedSettings() throws IOException
    {
        final ConnectionPoolInfo.Builder poolInfo = settings.getConnectionPoolInfoBuilder();
        while (true)
        {
            char ch = readMenuChoice("Advanced Settings");
            switch (ch)
            {
                case '\n':
                case '\r':
                    continue;  // reprompt
                case '?':
                    return true;  // redisplay menu
                case 'X':
                    return false;  // done
                case '1':
                    poolInfo.setPoolMaxSize(readValue(ConnectionPoolField.MAX_SIZE, poolInfo.getPoolMaxSize(), Validator.INTEGER_POSITIVE));
                    continue;
                case '2':
                    poolInfo.setPoolMaxIdle(readValue(ConnectionPoolField.MAX_IDLE, poolInfo.getPoolMaxIdle(), Validator.INTEGER_ALLOW_MINUS_1));
                    continue;
                case '3':
                    poolInfo.setPoolMinSize(readValue(ConnectionPoolField.MIN_SIZE, poolInfo.getPoolMinSize(), Validator.INTEGER_POSITIVE_OR_ZERO));
                    continue;
                case '4':
                    poolInfo.setPoolInitialSize(readValue(ConnectionPoolField.INITIAL_SIZE, poolInfo.getPoolInitialSize(), Validator.INTEGER_POSITIVE_OR_ZERO));
                    continue;
                case '5':
                    poolInfo.setPoolMaxWait(readValue(ConnectionPoolField.MAX_WAIT, poolInfo.getPoolMaxWait(), Validator.LONG_ALLOW_MINUS_1));
                    continue;
                case '6':
                    poolInfo.setPoolPreparedStatements(readValue(ConnectionPoolField.POOL_STATEMENTS, poolInfo.getPoolPreparedStatements(), Validator.BOOLEAN));
                    continue;
                case '7':
                    poolInfo.setMaxOpenPreparedStatements(readValue(ConnectionPoolField.MAX_OPEN_STATEMENTS, poolInfo.getMaxOpenPreparedStatements(), Validator.INTEGER_ALLOW_MINUS_1));
                    continue;
                case 'A':
                    poolInfo.setValidationQuery(readValue(ConnectionPoolField.VALIDATION_QUERY, poolInfo.getValidationQuery(), Validator.TRIMMED_STRING));
                    continue;
                case 'B':
                    poolInfo.setValidationQueryTimeout(readValue(ConnectionPoolField.VALIDATION_QUERY_TIMEOUT, poolInfo.getValidationQueryTimeout(), Validator.INTEGER_ALLOW_MINUS_1));
                    continue;
                case 'C':
                    poolInfo.setTestOnBorrow(readValue(ConnectionPoolField.TEST_ON_BORROW, poolInfo.getTestOnBorrow(), Validator.BOOLEAN));
                    continue;
                case 'D':
                    poolInfo.setTestOnReturn(readValue(ConnectionPoolField.TEST_ON_RETURN, poolInfo.getTestOnReturn(), Validator.BOOLEAN));
                    continue;
                case 'E':
                    poolInfo.setTestWhileIdle(readValue(ConnectionPoolField.TEST_WHILE_IDLE, poolInfo.getTestWhileIdle(), Validator.BOOLEAN));
                    continue;
                case 'F':
                    poolInfo.setTimeBetweenEvictionRunsMillis(readValue(ConnectionPoolField.TIME_BETWEEN_EVICTION_RUNS, poolInfo.getTimeBetweenEvictionRunsMillis(), Validator.LONG_ALLOW_MINUS_1));
                    continue;
                case 'G':
                    poolInfo.setMinEvictableTimeMillis(readValue(ConnectionPoolField.MIN_EVICTABLE_IDLE_TIME, poolInfo.getMinEvictableTimeMillis(), Validator.LONG_POSITIVE));
                    continue;
                case 'H':
                    poolInfo.setRemoveAbandoned(readValue(ConnectionPoolField.REMOVE_ABANDONED, poolInfo.getRemoveAbandoned(), Validator.BOOLEAN));
                    continue;
                case 'I':
                    poolInfo.setRemoveAbandonedTimeout(readValue(ConnectionPoolField.REMOVE_ABANDONED_TIMEOUT, poolInfo.getRemoveAbandonedTimeout(), Validator.INTEGER_POSITIVE));
                    continue;
                default:
                    console.println("Unknown command '" + ch + "'");
                    return true;
            }
        }
    }

    private String testConnection(DatabaseConfigConsole databaseConfig)
    {
        String errorMessage;
        try
        {
//            databaseConfig.validate();
            databaseConfig.testConnection();
            return null;
        }
        catch (UnsupportedClassVersionError ex)
        {
            errorMessage = "UnsupportedClassVersionError occurred. It is likely your JDBC drivers use a newer version of Java than you are running now.";
        }
        catch (ClassNotFoundException ex)
        {
            errorMessage = "Driver class '" + databaseConfig.getClassName() + "' not found. Ensure the DB driver is installed in the 'lib' directory.";
        }
        catch (SQLException ex)
        {
            String message = ex.getMessage();
            if (message.contains("Stack Trace"))
            {
                // postgres wants to throw the stack trace in the error message (even for host not found - dumb)
                // try to make this more user friendly
                if (message.contains("UnknownHostException"))
                {
                    message = "Unknown host.";
                }
                else if (message.contains("Check that the hostname and port are correct"))
                {
                    message = "Check that the hostname and port are correct.";
                }
                // other message unknown - show in full.
            }
            errorMessage = "Could not connect to the DB: " + message;
        }
//        catch (ValidationException ex)
//        {
//            errorMessage = ex.getMessage();
//        }
        catch (RuntimeException ex)
        {
            errorMessage = "An unexpected error occurred: " + ex.getMessage();
            System.err.println(errorMessage);
            ex.printStackTrace(System.err);
        }
        catch (ValidationException e)
        {
            errorMessage = e.getMessage();
        }
        return errorMessage;
    }

    private void showMenuItem(char key, String label)
    {
        console.println("  [" + key + "] " + label);
    }

    private void showDefaultMenuItem(char key, String label)
    {
        console.println("* [" + key + "] " + label);
    }

    private void showMenuItem(char key, String label, char defaultKey)
    {
        if (key == defaultKey)
        {
            showDefaultMenuItem(key, label);
        }
        else
        {
            showMenuItem(key, label);
        }
    }

    private char readMenuChoice(String menuName) throws IOException
    {
        return Character.toUpperCase(console.readFirstChar(menuName));
    }

    private DatabaseConfigConsole getSelectedDatabaseConfig()
    {
        switch (selectedDatabaseType)
        {
            case HSQL:
                return HSQL_CONFIG;
            case ORACLE:
                return ORACLE_CONFIG;
            case MY_SQL:
                return MYSQL_CONFIG;
            case POSTGRES:
                return POSTGRES_CONFIG;
            case SQL_SERVER:
                return SQL_SERVER_CONFIG;
        }
        throw new IllegalStateException("Unknown database type " + selectedDatabaseType);
    }
}


