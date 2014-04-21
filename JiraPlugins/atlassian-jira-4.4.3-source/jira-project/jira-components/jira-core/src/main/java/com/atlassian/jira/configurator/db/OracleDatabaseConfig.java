package com.atlassian.jira.configurator.db;

import com.atlassian.jira.exception.ParseException;

public class OracleDatabaseConfig implements DatabaseConfig
{

    public String getDatabaseType()
    {
        return "Oracle";
    }

    public String getInstanceFieldName()
    {
        return "SID";
    }

    public String getClassName()
    {
        return "oracle.jdbc.OracleDriver";
    }

    public String getUrl(String hostname, String port, String instance)
    {
        // http://www.oracle.com/technology/tech/java/sqlj_jdbc/htdocs/jdbc_faq.html#05_03
        //    jdbc:oracle:thin:@[host]:[port]:SID
        String url = "jdbc:oracle:thin:@" + hostname.trim();
        url += ':' + port.trim();
        return url + ":" + instance.trim();
    }

    public DatabaseInstance parseUrl(final String jdbcUrl) throws ParseException
    {
        DatabaseInstance oracleConnectionProperties = new DatabaseInstance();
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
            oracleConnectionProperties.setInstance(hostPort_Sid[1]);
            String[] hostPort = hostPort_Sid[0].split(":");
            oracleConnectionProperties.setHostname(hostPort[0]);
            if (hostPort.length == 1)
            {
                oracleConnectionProperties.setPort("");
            }
            else
            {
                oracleConnectionProperties.setPort(hostPort[1]);
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
                oracleConnectionProperties.setHostname(host_Port_Sid[0]);
                oracleConnectionProperties.setPort("");
                oracleConnectionProperties.setInstance(host_Port_Sid[1]);
            }
            else
            {
                oracleConnectionProperties.setHostname(host_Port_Sid[0]);
                oracleConnectionProperties.setPort(host_Port_Sid[1]);
                oracleConnectionProperties.setInstance(host_Port_Sid[2]);
            }
        }

        return oracleConnectionProperties;
    }

}
