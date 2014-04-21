package com.atlassian.jira.configurator.gui;

import com.atlassian.jira.configurator.Configurator;
import com.atlassian.jira.configurator.config.DatabaseType;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.SettingsLoader;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.configurator.db.DatabaseConfigPanel;
import com.atlassian.jira.configurator.db.HsqlConfigPanel;
import com.atlassian.jira.configurator.db.MySqlConfigPanel;
import com.atlassian.jira.configurator.db.OracleConfigPanel;
import com.atlassian.jira.configurator.db.PostgresConfigPanel;
import com.atlassian.jira.configurator.db.SqlServerConfigPanel;
import com.atlassian.jira.exception.ParseException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * The main frame for the GUI.
 */
public class ConfiguratorFrame extends JFrame
{
    private final JTextField tfJiraHome = new JTextField(32);
    private final JTextField tfConnectionPoolSize = new JTextField(10);
    private final JTextField tfHttpPort = new JTextField(10);
    private final JTextField tfControlPort = new JTextField(10);
    private JComboBox ddDatabaseType;
    private JPanel cardPanel;

    private CardLayout jdbcTypeCardLayout;
    private Map<DatabaseType, DatabaseConfigPanel> configPanelMap = null;
    final ConnectionTestDialog connectionTestDialog;
    private String previousJiraHome;

    public ConfiguratorFrame()
    {
        init();

        // We need to construct the ConnectionTestDialog after the ConfiguratorFrame has finished initialising because
        // the Dialog width is set based on the Frame width.
        connectionTestDialog = new ConnectionTestDialog(ConfiguratorFrame.this);
    }

    private void init()
    {
        setIconImage(new ImageIcon("../atlassian-jira/images/intro-gadget.png").getImage());
        setLayout(new BorderLayout());
        add(newTabbedPanel(), BorderLayout.CENTER);
        add(newButtonPanel(), BorderLayout.SOUTH);
        // Override the Window Close event so we can show a Confirm Dialog.
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(final WindowEvent e)
            {
                closeApplication();
            }
        });
        // Let the Window resize itself to the right size for this platform.
        this.pack();
    }

    /**
     * Closes the application after checking for unsaved changes.
     * If there are unsaved changes, then a confirm dialog is shown.
     */
    private void closeApplication()
    {
        // Check if there are unsaved changes
        try
        {
            if (Configurator.settingsEqual(gatherNewSettings()))
            {
                // No changes - just exit without a prompt
                System.exit(0);
            }
        }
        catch (ValidationException e)
        {
            // Presumably the user changed the settings to make them invalid. We will show the "Conform Exit" prompt.
        }
        // Unsaved changes - confirm the close operation.
        int option = JOptionPane.showConfirmDialog(ConfiguratorFrame.this, "Are you sure you wish to close without saving?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }
    }

    private Component newTabbedPanel()
    {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                checkForJiraHomeChange();
            }
        });
        tabbedPane.addTab("JIRA Home", newJiraHomePanel());
        tabbedPane.addTab("Database", newDatabasePanel());
        tabbedPane.addTab("Web Server", newWebServerPanel());
        return tabbedPane;
    }

    private JComponent newJiraHomePanel()
    {
        ConfigPanelBuilder panelBuilder = new ConfigPanelBuilder();
        panelBuilder.add("JIRA Home Directory", getJiraHomePicker());
        JPanel panel = panelBuilder.getPanel();
        // set 4 pixel border
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));
        
        return panel;
    }

    private JComponent getJiraHomePicker()
    {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        tfJiraHome.setToolTipText("Set the 'JIRA Home' directory (used to store data for this JIRA installation)");
        // Let TextField take up most room
        panel.add(tfJiraHome, BorderLayout.CENTER);
        // Add a button on the end
        final JButton btnJiraHome = new JButton("Browse");
        btnJiraHome.setToolTipText("Opens a dialog box to select the JIRA Home");
        btnJiraHome.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                browseForJiraHome();
            }
        });
        panel.add(btnJiraHome, BorderLayout.EAST);

        return panel;
    }

    private void browseForJiraHome()
    {
        JFileChooser chooser = new JFileChooser(tfJiraHome.getText());
        chooser.setDialogTitle("JIRA Home directory");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileFilter()
        {
            public boolean accept(final File f)
            {
                return f.isDirectory();
            }

            public String getDescription()
            {
                return "Directory";
            }
        });
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // Show the file chooser with custom text for Accept button
        int returnVal = chooser.showDialog(this, "OK");
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            tfJiraHome.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private JComponent newWebServerPanel()
    {
        ConfigPanelBuilder panelBuilder = new ConfigPanelBuilder();
        panelBuilder.add("HTTP Port", tfHttpPort);
        panelBuilder.add("Control Port", tfControlPort);

        JPanel panel = panelBuilder.getPanel();
        // set 4 pixel border
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));

        return panel;
    }

    private JComponent newDatabasePanel()
    {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));

        // North Panel
        JPanel pnlNorth = new JPanel(new VerticalFlowLayout());
        panel.add(pnlNorth, BorderLayout.NORTH);

        // DB Type drop down
        createDataBaseDropDown();
        ConfigPanelBuilder panelBuilder = new ConfigPanelBuilder();
        panelBuilder.add("Database type", ddDatabaseType);
        pnlNorth.add(panelBuilder.getPanel());

        // Card panel with DB details
        createCardPanel();
        pnlNorth.add(cardPanel);

        // Connection Pool
        panelBuilder = new ConfigPanelBuilder();
        panelBuilder.add("Pool Size", tfConnectionPoolSize);
        tfConnectionPoolSize.setToolTipText("The maximum number of database connections in the Connection Pool");
        panelBuilder.setTitle("Connection Pool");
        pnlNorth.add(panelBuilder.getPanel());

        // on the bottom put the test button
        panel.add(newTestConnectionButton(), BorderLayout.SOUTH);

        return panel;
    }

    private JComponent newTestConnectionButton()
    {
        JButton button = new JButton("Test Connection");
        button.setToolTipText("Try to connect to the database with these settings");
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                // Reset the Connection test message
                connectionTestDialog.setText("Attempting to connect to the " + getSelectedDatabaseConfigPanel().getDisplayName() + " database server...");
                // Don't allow the dialog to clase until the test is complete
                connectionTestDialog.enableCloseButton(false);

                // Because we are going to show a modal dialog, our test has to run in a separate thread.
                new Thread(new Runnable()
                {
                    public void run()
                    {
                        testConnection();
                    }
                }).start();

                // Show the modal dialog
                connectionTestDialog.setVisible(true);
            }
        });
        return button;
    }

    private void testConnection()
    {
        DatabaseConfigPanel jdbcPanel = getSelectedDatabaseConfigPanel();
        String errorMessage = null;

        try
        {
            jdbcPanel.validate();
            jdbcPanel.testConnection(tfJiraHome.getText());
        }
        catch (UnsupportedClassVersionError ex)
        {
            errorMessage = "UnsupportedClassVersionError occurred. It is likely your JDBC drivers use a newer version of Java than you are running now.";
        }
        catch (ClassNotFoundException ex)
        {
            errorMessage = "Driver class '" + jdbcPanel.getClassName() + "' not found. Ensure the DB driver is installed in the 'lib' directory.";
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
        catch (ValidationException ex)
        {
            errorMessage = ex.getMessage();
        }
        catch (RuntimeException ex)
        {
            errorMessage = "An unexpected error occurred: " + ex.getMessage();
            System.err.println(errorMessage);
            ex.printStackTrace(System.err);
        }

        // Not really supposed to do Swing stuff in a different thread :(
        // make a Runnable to invoke on the Swing thread.
        class SwingRunner implements Runnable
        {
            private String errorMessage;

            SwingRunner(String errorMessage)
            {
                this.errorMessage = errorMessage;
            }
            public void run()
            {
                if (errorMessage == null)
                    connectionTestDialog.addText("Connection successful.");
                else
                    connectionTestDialog.addText(errorMessage);
                connectionTestDialog.enableCloseButton(true);
            }
        }

        SwingUtilities.invokeLater(new SwingRunner(errorMessage));
    }

    private DatabaseConfigPanel getSelectedDatabaseConfigPanel()
    {
        return getDatabaseConfigPanel(getSelectedDatabaseType());
    }

    private DatabaseType getSelectedDatabaseType()
    {
        return (DatabaseType) ddDatabaseType.getSelectedItem();
    }

    private void createCardPanel()
    {
        cardPanel = new JPanel();
        jdbcTypeCardLayout = new CardLayout();
        cardPanel.setLayout(jdbcTypeCardLayout);
        cardPanel.setBorder(new TitledBorder("Connection Details"));

        for (DatabaseType databaseType : DatabaseType.values())
        {
            cardPanel.add(getDatabaseConfigPanel(databaseType).getPanel(), databaseType.getDisplayName());
        }
    }

    private DatabaseConfigPanel getDatabaseConfigPanel(final DatabaseType databaseType)
    {
        if (configPanelMap == null)
        {
            configPanelMap = new HashMap<DatabaseType, DatabaseConfigPanel>(5);
            configPanelMap.put(DatabaseType.HSQL, new HsqlConfigPanel());
            configPanelMap.put(DatabaseType.SQL_SERVER, new SqlServerConfigPanel());
            configPanelMap.put(DatabaseType.MY_SQL, new MySqlConfigPanel());
            configPanelMap.put(DatabaseType.ORACLE, new OracleConfigPanel());
            configPanelMap.put(DatabaseType.POSTGRES, new PostgresConfigPanel());
        }
        return configPanelMap.get(databaseType);
    }


    private void createDataBaseDropDown()
    {
        ddDatabaseType = new JComboBox(DatabaseType.values());
        ddDatabaseType.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e)
            {
                DatabaseType databaseType = getSelectedDatabaseType();
                if (databaseType == null)
                {
                    return;
                }
                // Show the specific config panel for this JDBC connection type
                jdbcTypeCardLayout.show(cardPanel, databaseType.getDisplayName());
            }
        });
    }


    private JPanel newButtonPanel()
    {
        JPanel panel = new JPanel();
        panel.add(newSaveButton());
        panel.add(newCloseButton());
        return panel;
    }

    private Component newSaveButton()
    {
        JButton button = new JButton("Save");
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                try
                {
                    checkForJiraHomeChange();
                    Configurator.saveSettings(gatherNewSettings());
                    showInformationDialog("Settings saved successfully.");
                }
                catch (ValidationException ex)
                {
                    showErrorDialog(ex);
                }
                catch (IOException ex)
                {
                    showErrorDialog(ex);
                }
                catch (RuntimeException ex)
                {
                    ex.printStackTrace();
                    showErrorDialog("Unexpected error during save:\n" + ex.getMessage());
                }
            }
        });
        return button;
    }

    private Settings gatherNewSettings() throws ValidationException
    {
        Settings newSettings = new Settings();
        // jira.home
        final String jiraHome = tfJiraHome.getText();
        newSettings.setJiraHome(jiraHome);
        // DB Pool
        setPoolSize(newSettings);
        // DB Connection
        getSelectedDatabaseConfigPanel().saveSettings(newSettings, jiraHome);
        // Web Server
        saveWebServerSettings(newSettings);
        return newSettings;
    }

    private void setPoolSize(final Settings newSettings) throws ValidationException
    {
        // Check for default
        String newPoolSize = tfConnectionPoolSize.getText().trim();
        if (newPoolSize.length() == 0)
        {
            newSettings.setDbPoolSize("20");
            return;
        }
        // Check it is valid
        int poolSize;
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

    private void saveWebServerSettings(Settings newSettings) throws ValidationException
    {
        // HTTP Port
        String port = tfHttpPort.getText().trim();
        validatePort("HTTP Port", port);
        newSettings.setHttpPort(port);

        // HTTP Port
        port = tfControlPort.getText().trim();
        validatePort("Control Port", port);
        newSettings.setControlPort(port);

    }

    private void validatePort(String fieldName, String port) throws ValidationException
    {

        if (port.trim().length() == 0)
        {
            throw new ValidationException(fieldName + " is a mandatory field.");
        }
        // Check it is valid
        int portNumber;
        try
        {
            portNumber = Integer.parseInt(port);
            if (portNumber < 0 || portNumber > 65535)
            {
                throw new ValidationException(fieldName + " must be a number between 0 and 65535.");
            }
        }
        catch (NumberFormatException e)
        {
            throw new ValidationException(fieldName + " must be a valid number between 0 and 65535.");
        }
    }

    private Component newCloseButton()
    {
        JButton button = new JButton("Close");
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                closeApplication();
            }
        });
        return button;
    }

    public void setSettings(final Settings settings) throws ParseException
    {
        // JIRA home
        tfJiraHome.setText(settings.getJiraHome());
        previousJiraHome = settings.getJiraHome();
        // DB Pool
        tfConnectionPoolSize.setText(settings.getDbPoolSize());
        // Set the drop down according to the current Database type
        ddDatabaseType.setSelectedItem(settings.getDatabaseType());
        getSelectedDatabaseConfigPanel().setSettings(settings);
        // Web Server
        tfHttpPort.setText(settings.getHttpPort());
        tfControlPort.setText(settings.getControlPort());
    }

    private void showErrorDialog(final Exception ex)
    {
        showErrorDialog(ex.getMessage());
    }

    private void showErrorDialog(final String message)
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInformationDialog(final String message)
    {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void checkForJiraHomeChange()
    {
        String newJiraHome = tfJiraHome.getText().trim();
        if (!newJiraHome.equals(previousJiraHome))
        {
            // Check if this folder actually exists and contains a dbconfig file
            File configFile = new File(newJiraHome, "dbconfig.xml");
            if (configFile.exists())
            {
                // Offer to reload the DB config from the file
                int option = JOptionPane.showConfirmDialog(ConfiguratorFrame.this, "Would you like to reload the DB configuration from the new JIRA Home?", "Reload DB Config", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (option == JOptionPane.YES_OPTION)
                {
                    try
                    {
                        final Settings settings = SettingsLoader.reloadDbConfig(newJiraHome);
                        // Set the drop down according to the current Database type
                        ddDatabaseType.setSelectedItem(settings.getDatabaseType());
                        getSelectedDatabaseConfigPanel().setSettings(settings);
                        // DB Pool
                        tfConnectionPoolSize.setText(settings.getDbPoolSize());
                    }
                    catch (IOException ex)
                    {
                        showErrorDialog(ex);
                    }
                    catch (ParseException ex)
                    {
                        showErrorDialog(ex);
                    }
                }
            }
            previousJiraHome = newJiraHome.trim();
        }
    }
}
