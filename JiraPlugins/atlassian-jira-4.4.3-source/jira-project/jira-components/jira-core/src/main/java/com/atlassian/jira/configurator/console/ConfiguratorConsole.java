package com.atlassian.jira.configurator.console;

import com.atlassian.jira.configurator.Configurator;
import com.atlassian.jira.configurator.config.DatabaseType;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.configurator.db.ConfigField;
import com.atlassian.jira.configurator.db.DatabaseConfigConsole;
import com.atlassian.jira.configurator.db.DatabaseConfigConsoleImpl;
import com.atlassian.jira.configurator.db.MySqlDatabaseConfig;
import com.atlassian.jira.configurator.db.OracleDatabaseConfig;
import com.atlassian.jira.configurator.db.PostgresDatabaseConfig;
import com.atlassian.jira.configurator.db.SqlServerDatabaseConfig;
import com.atlassian.jira.exception.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.SQLException;

/**
 * Runs the Configurator in the console (no GUI).
 *
 * @since 4.1
 */
public class ConfiguratorConsole
{
    private BufferedReader in;
    private PrintStream out;
    private final DatabaseConfigConsole MYSQL_CONFIG = new DatabaseConfigConsoleImpl(new MySqlDatabaseConfig());
    private final DatabaseConfigConsole ORACLE_CONFIG = new DatabaseConfigConsoleImpl(new OracleDatabaseConfig());
    private final DatabaseConfigConsole POSTGRES_CONFIG = new DatabaseConfigConsoleImpl(new PostgresDatabaseConfig());
    private final DatabaseConfigConsole SQL_SERVER_CONFIG = new DatabaseConfigConsoleImpl(new SqlServerDatabaseConfig());

    private DatabaseType selectedDatabaseType;
    private String jiraHome;
    private String connectionPoolSize;

    public void setSettings(Settings settings)
    {
        jiraHome = settings.getJiraHome();
        // DB Pool
        connectionPoolSize = settings.getDbPoolSize();
        // the current Database type
        selectedDatabaseType = settings.getDatabaseType();
        try
        {
            getSelectedDatabaseConfig().setSettings(settings);
        }
        catch (ParseException e)
        {
            out.println("Unable to fully parse the current JDBC settings. Some settings may be blank.");
            out.println("Parse Exception: " + e.getMessage());
        }
    }

    public void start()
    {
        // can't use System.console() because it is Java 6
        // System.in is a byte stream with no character stream features. Wrap System.in in InputStreamReader.
        in = new BufferedReader(new InputStreamReader(System.in));
        out = System.out;
        showWelcomeMessage();
        try
        {
            showMainMenu();
        }
        catch (IOException e)
        {
            out.println();
            out.println("A fatal IOException has occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showMainMenu() throws IOException
    {
        while (true)
        {
            out.println();
            out.println("--- Main Menu ---");
            out.println("[H] Configure JIRA Home");
            out.println("[D] Configure Database connection");
            out.println("[S] Save and Exit");
            out.println("[X] Exit without Saving");
            char ch = firstCharUpper();
            switch (ch)
            {
                case 'D':
                    showDatabaseSettings();
                    break;
                case 'X':
                    System.exit(0);
                case 'S':
                    saveSettings();
                default:
                    out.println("Unknown command '" + ch + "'");
            }
        }
    }

    private void saveSettings()
    {
        try
        {
            Configurator.saveSettings(gatherNewSettings());
            out.println("Settings saved successfully.");
            System.exit(0);
        }
        catch (ValidationException ex)
        {
            printErrorMessage(ex);
        }
        catch (IOException ex)
        {
            printErrorMessage(ex);
        }        
    }

    private Settings gatherNewSettings() throws ValidationException
    {
        Settings newSettings = new Settings();
        // jira.home
        newSettings.setJiraHome(jiraHome);
        // DB Pool
        setPoolSize(newSettings);
        // DB Connection
        getSelectedDatabaseConfig().saveSettings(newSettings);
        return newSettings;
    }

    private void setPoolSize(final Settings newSettings) throws ValidationException
    {
        // Check for default
        String newPoolSize = connectionPoolSize.trim();
        if (newPoolSize.length() == 0)
        {
            newSettings.setDbPoolSize("20");
            return;
        }
        // Check it is valid
        int poolSize = 0;
        try
        {
            poolSize = Integer.parseInt(newPoolSize);
            newSettings.setDbPoolSize(newPoolSize);
        }
        catch (NumberFormatException e)
        {
            throw new ValidationException("Connection Pool size must be a valid integer.");
        }
        if (poolSize < 1)
        {
            throw new ValidationException("Connection Pool size must be greater than zero.");
        }
    }
    
    private void showWelcomeMessage()
    {
        out.println("----------------------");
        out.println("JIRA Configurator v1.0");
        out.println("----------------------");
    }

    private void showDatabaseSettings() throws IOException
    {
        while (true)
        {
            out.println("Database Type : " + selectedDatabaseType.getDisplayName());
            out.println("Instance      : " + getSelectedDatabaseConfig().getInstanceName());
            out.println("Connect As    : " + getSelectedDatabaseConfig().getUsername() + " / " + getSelectedDatabaseConfig().getPassword());
            out.println("Select:");
            final char defaultCommand = selectedDatabaseType.getDisplayName().charAt(0);
            showMenuItem('H', "HSQL (not for production use)", defaultCommand);
            showMenuItem('M', "MySQL", defaultCommand);
            showMenuItem('O', "Oracle", defaultCommand);
            showMenuItem('P', "PostgreSQL", defaultCommand);
            showMenuItem('S', "SQL Server (MS-SQL)", defaultCommand);
            showMenuItem('X', "Return to Main Menu", defaultCommand);
            char ch = Character.toUpperCase(readFirstChar());
            switch (ch)
            {
                case '\n':
                case '\r':
                    // Don't change the selected DB
                    doDatabaseConfig();
                    return;
                case 'M':
                    selectedDatabaseType = DatabaseType.MY_SQL;
                    doDatabaseConfig();
                    return;
                case 'O':
                    selectedDatabaseType = DatabaseType.ORACLE;
                    doDatabaseConfig();
                    return;
                case 'X':
                    return;
                default:
                    out.println("Unknown command '" + ch + "'");
            }
        }
    }

    private void doDatabaseConfig() throws IOException
    {
        DatabaseConfigConsole databaseConfig = getSelectedDatabaseConfig();
        while(true)
        {
            out.println(databaseConfig.getDatabaseType() + " Database Configuration.");
            for (ConfigField configField : databaseConfig.getFields())
            {
                out.print(configField.getLabel() + ' ' + '(' + configField.getValue() + "): ");
                String newValue = readLine();
                if (newValue.length() > 0)
                {
                    configField.setValue(newValue.trim());
                }
            }
            out.println("Test Connection? (Y)");
            if (readYesNoWithDefault(true))
            {
                out.println("Attempting to connect to " + getSelectedDatabaseConfig().getInstanceName());
                String error = testConnection(databaseConfig);
                if (error == null)
                {
                    out.println("Connection successful!");
                    return;
                }
                out.println("Connection failed: " + error);
                out.println();
            }
            else
            {
                return;
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

    private void showMenuItem(char key, String label, char defaultCommandKey)
    {
        if (key == defaultCommandKey)
        {
            out.print("*");
        }
        else
        {
            out.print(" ");
        }
        out.println(" [" + key + "] " + label);
    }

    private char firstCharUpper() throws IOException
    {
        return Character.toUpperCase(readFirstChar());
    }

    private char readFirstChar() throws IOException
    {
        // Unfortunately the Standard Input is buffered. See eg http://www.gamedev.net/community/forums/topic.asp?topic_id=422415
        char firstChar = (char) in.read();
        // Burn all the rest of the input so far.
        while (in.ready())
            in.read();
        // and return just the first character
        return firstChar;
    }

    private String readLine() throws IOException
    {
        return in.readLine();
    }

    private boolean readYesNoWithDefault(boolean defaultValue) throws IOException
    {
        String response = readLine();
        if (response.length() == 0)
        {
            return defaultValue;
        }
        char ch = Character.toUpperCase(response.charAt(0));
        switch (ch)
        {
            case 'Y':
            case 'y':
                return true;
            case 'N':
            case 'n':
                return false;
            default:
                return defaultValue;
        }
    }

    private void printErrorMessage(Exception ex)
    {
        out.println(ex.getMessage());
    }

    private DatabaseConfigConsole getSelectedDatabaseConfig()
    {
        switch (selectedDatabaseType)
        {
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
