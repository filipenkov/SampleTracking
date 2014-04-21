package com.atlassian.jira.configurator.db;

import com.atlassian.jira.config.database.JdbcDatasource;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.configurator.gui.ConfigPanelBuilder;
import com.atlassian.jira.exception.ParseException;

import javax.swing.*;

public class OracleConfigPanel extends DatabaseConfigPanel
{
    private JTextField tfHostname = new JTextField(TEXT_FIELD_COLUMNS);
    // Set the default Port Number. Blank is not valid for the Oracle JDBC drivers included in JIRA.
    private JTextField tfPort = new JTextField("1521", TEXT_FIELD_COLUMNS);
    private JTextField tfSid = new JTextField(TEXT_FIELD_COLUMNS);
    private JTextField tfUsername = new JTextField(TEXT_FIELD_COLUMNS);
    private JTextField tfPassword = new JPasswordField(TEXT_FIELD_COLUMNS);
    private JPanel configPanel;

    @Override
    public String getDisplayName()
    {
        return "Oracle";
    }

    @Override
    public String getClassName()
    {
        return "oracle.jdbc.OracleDriver";
    }

    @Override
    public String getUrl(String jiraHome)
    {
        // http://www.oracle.com/technology/tech/java/sqlj_jdbc/htdocs/jdbc_faq.html#05_03
        //    jdbc:oracle:thin:@[host]:[port]:SID
        String url = "jdbc:oracle:thin:@" + tfHostname.getText().trim();
        url += ':' + tfPort.getText().trim();
        return url + ":" + tfSid.getText();
    }

    @Override
    public String getUsername()
    {
        return tfUsername.getText();
    }

    @Override
    public String getPassword()
    {
        return tfPassword.getText();
    }

    @Override
    public String getSchemaName()
    {
        // Oracle should use the default schema for the logged in user
        return null;
    }

    @Override
    public JPanel getPanel()
    {
        if (configPanel == null)
        {
            ConfigPanelBuilder panelBuilder = new ConfigPanelBuilder();
            panelBuilder.add("Hostname", tfHostname);
            tfHostname.setToolTipText("The hostname or IP address of the Oracle server");
            panelBuilder.add("Port", tfPort);
            tfPort.setToolTipText("The port number that Oracle is running on. (Default is 1521)");
            panelBuilder.add("SID", tfSid);
            tfSid.setToolTipText("System ID of the Oracle server database instance. (eg 'ORCL'. For Express Edition use 'XE')");
            panelBuilder.add("Username", tfUsername);
            tfUsername.setToolTipText("The username used to login");
            panelBuilder.add("Password", tfPassword);
            tfPassword.setToolTipText("The password used to login");
            configPanel = panelBuilder.getPanel();
        }
        return configPanel;
    }

    @Override
    public void setSettings(final Settings settings) throws ParseException
    {
        final JdbcDatasource.Builder datasourceBuilder = settings.getJdbcDatasourceBuilder();
        tfUsername.setText(datasourceBuilder.getUsername());
        tfPassword.setText(datasourceBuilder.getPassword());

        // parse the URL.
        OracleConnectionProperties connectionProperties = parseUrl(datasourceBuilder.getJdbcUrl());

        tfHostname.setText(connectionProperties.host);
        tfPort.setText(connectionProperties.port);
        tfSid.setText(connectionProperties.sid);
    }

    OracleConnectionProperties parseUrl(final String jdbcUrl) throws ParseException
    {
        OracleConnectionProperties oracleConnectionProperties = new OracleConnectionProperties();
        // http://www.herongyang.com/jdbc/Oracle-JDBC-Driver-Connection-URL.html
        //    jdbc:oracle:thin:[user/password]@[host][:port]:SID
        //    jdbc:oracle:thin:[user/password]@//[host][:port]/SID

        if (!jdbcUrl.startsWith("jdbc:oracle:thin:"))
        {
            throw new ParseException("Unable to parse the Oracle JDBC URL '" + jdbcUrl +
                                         "'. It should start with protocol prefix 'jdbc:oracle:thin:'.");
        }
        // Strip off the protocol prefix
        String stripped = jdbcUrl.substring("jdbc:oracle:thin:".length());
        // Get the text after the @
        String[] split = stripped.split("@", 2);
        if (split.length == 1)
        {
            throw new ParseException("Unable to parse the Oracle JDBC URL '" + jdbcUrl +
                                         "'. Expected to find a '@' before the host name.");
        }
        String props = split[1];
        // Now there are two slightly different formats. For now just handle the first one.
        if (props.startsWith("//"))
        {
            // Strip off the //
            props = props.substring(2);
            String[] hostPort_Sid = props.split("/", 2);
            if (hostPort_Sid.length == 1)
            {
                throw new ParseException("Unable to parse the Oracle JDBC URL '" + jdbcUrl +
                                             "'. Missing '/' before the SID.");
            }
            oracleConnectionProperties.sid = hostPort_Sid[1];
            String[] hostPort = hostPort_Sid[0].split(":");
            oracleConnectionProperties.host = hostPort[0];
            if (hostPort.length == 1)
            {
                oracleConnectionProperties.port = "";
            }
            else
            {
                oracleConnectionProperties.port = hostPort[1];
            }
        }
        else
        {
            String[] host_Port_Sid = props.split(":", 3);
            if (host_Port_Sid.length == 1)
            {
                throw new ParseException("Unable to parse the Oracle JDBC URL '" + jdbcUrl +
                                             "'. Missing ':' before the SID.");
            }
            if (host_Port_Sid.length == 2)
            {
                // port must be missing
                oracleConnectionProperties.host = host_Port_Sid[0];
                oracleConnectionProperties.port = "";
                oracleConnectionProperties.sid = host_Port_Sid[1];
            }
            else
            {
                oracleConnectionProperties.host = host_Port_Sid[0];
                oracleConnectionProperties.port = host_Port_Sid[1];
                oracleConnectionProperties.sid = host_Port_Sid[2];
            }
        }

        return oracleConnectionProperties;
    }

    @Override
    public void validate() throws ValidationException
    {
        // Hostname can be empty, defaults to 127.0.0.1
        // Oracle JDBC drivers don't seem to allow a blank Port Number.
        if (tfPort.getText().trim().length() == 0)
        {
            throw new ValidationException("Please supply a Port Number to connect to. (Default Oracle port is 1521).");
        }
        validatePortNumber(tfPort.getText());
    }

    class OracleConnectionProperties
    {
        String host;
        String port;
        String sid;
    }
}
