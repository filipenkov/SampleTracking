package com.atlassian.jira.util;

/**
 * Default Implementation for Sniffing User Agents.
 * Some code was taken from http://nerds.palmdrive.net/useragent/code.html
 *
 * @since v4.0
 */
public class UserAgentUtilImpl implements UserAgentUtil
{

    public UserAgent getUserAgentInfo(String userAgent)
    {
        return new UserAgent(getBrowser(userAgent), getOS(userAgent));
    }

    private String getVersionNumber(String useragent, int pos)
    {
        if (pos < 0)
        {
            return "";
        }
        if (useragent == null)
        {
            return "";
        }
        
        StringBuffer res = new StringBuffer();
        int status = 0;

        while (pos < useragent.length())
        {
            char c = useragent.charAt(pos);
            switch (status)
            {
                case 0: // No valid digits encountered yet
                    if (c == ' ' || c == '/')
                    {
                        break;
                    }
                    if (c == ';' || c == ')')
                    {
                        return "";
                    }
                    status = 1;
                case 1: // Version number in progress
                    if (c == ';' || c == '/' || c == ')' || c == '(' || c == '[')
                    {
                        return res.toString().trim();
                    }
                    if (c == ' ')
                    {
                        status = 2;
                    }
                    res.append(c);
                    break;
                case 2: // Space encountered - Might need to end the parsing
                    if ((Character.isLetter(c) &&
                            Character.isLowerCase(c)) ||
                            Character.isDigit(c))
                    {
                        res.append(c);
                        status = 1;
                    }
                    else
                    {
                        return res.toString().trim();
                    }
                    break;
            }
            pos++;
        }
        return res.toString().trim();
    }

    private OperatingSystem getOS(String userAgent)
    {
        if (userAgent == null)
        {
            return new OperatingSystem(OperatingSystem.OperatingSystemFamily.UNKNOWN);
        }

        for (OperatingSystem.OperatingSystemFamily osFamily : OperatingSystem.OperatingSystemFamily.values())
        {
            if (userAgent.contains(osFamily.getUserAgentString()))
            {
                return new OperatingSystem(osFamily);
            }
        }

        return new OperatingSystem(OperatingSystem.OperatingSystemFamily.UNKNOWN);

    }


    private Browser getBrowser(String userAgent)
    {
        if (userAgent == null)
        {
            return new Browser(BrowserFamily.UKNOWN, BrowserMajorVersion.UNKNOWN, "0");
        }
        
        for (BrowserFamily browserFamily : BrowserFamily.values())
        {
            if (userAgent.contains(browserFamily.getUserAgentString()))
            {
                for (BrowserMajorVersion majorVersion : BrowserMajorVersion.values())
                {
                    if (majorVersion.getBrowserFamily().equals(browserFamily))
                    {
                        int pos;
                        if ((pos = userAgent.indexOf(majorVersion.getUserAgentString())) > -1)
                        {
                            return new Browser(browserFamily, majorVersion, majorVersion.getMinorVersionPrefix() + getVersionNumber(userAgent, pos + majorVersion.getVersionPos()));
                        }
                    }
                }
                int pos = userAgent.indexOf(browserFamily.getUserAgentString());
                return new Browser(browserFamily, BrowserMajorVersion.UNKNOWN, browserFamily.getUserAgentString() + getVersionNumber(userAgent, pos + browserFamily.getUserAgentString().length()));
            }
        }

        return new Browser(BrowserFamily.UKNOWN, BrowserMajorVersion.UNKNOWN, "0");

    }

}
