package com.atlassian.jira.rest.v2;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.rest.bind.DateTimeAdapter;
import com.atlassian.jira.util.BuildUtilsInfo;

import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
* @since v4.2
*/
@XmlRootElement
class ServerInfoBean
{
    @XmlElement
    private String baseUrl;

    @XmlElement
    private String version;

    @XmlElement
    private Integer buildNumber;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    private Date buildDate;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    private Date serverTime;

    @XmlElement
    private String scmInfo;

    @XmlElement
    private String buildPartnerName;

    @XmlElement
    private String serverTitle;

    public ServerInfoBean() {}

    public ServerInfoBean(final ApplicationProperties properties, final BuildUtilsInfo buildUtils, final boolean canUse)
    {
        baseUrl = properties.getString(APKeys.JIRA_BASEURL);
        version = buildUtils.getVersion();
        buildNumber = Integer.valueOf(buildUtils.getCurrentBuildNumber());
        buildDate = buildUtils.getCurrentBuildDate();
        scmInfo = buildUtils.getSvnRevision();
        buildPartnerName = buildUtils.getBuildPartnerName();
        serverTitle = properties.getString(APKeys.JIRA_TITLE);
        if (canUse)
        {
            serverTime = new Date();
        }
    }

    final static ServerInfoBean DOC_EXAMPLE = new ServerInfoBean();
    static {
        DOC_EXAMPLE.baseUrl = "http://localhost:8080/jira";
        DOC_EXAMPLE.version = "4.2-SNAPSHOT";
        DOC_EXAMPLE.buildNumber = 582;
        DOC_EXAMPLE.buildDate = new Date();
        DOC_EXAMPLE.serverTime = new Date();
        DOC_EXAMPLE.scmInfo = "482389";
        DOC_EXAMPLE.buildPartnerName = "Example Partner Co.";
        DOC_EXAMPLE.serverTitle = "My Shiny New JIRA Server";
    }
}
