package com.atlassian.jira.rest.v2;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.rest.bind.DateTimeAdapter;
import com.atlassian.jira.util.BuildUtilsInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

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
    private int[] versionNumbers;

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
        versionNumbers = buildUtils.getVersionNumbers();
        buildNumber = Integer.valueOf(buildUtils.getCurrentBuildNumber());
        buildDate = buildUtils.getCurrentBuildDate();
        scmInfo = buildUtils.getCommitId();
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
        DOC_EXAMPLE.version = "5.0-SNAPSHOT";
        DOC_EXAMPLE.versionNumbers = new int[] {5, 0, 0};
        DOC_EXAMPLE.buildNumber = 582;
        DOC_EXAMPLE.buildDate = new Date();
        DOC_EXAMPLE.serverTime = new Date();
        DOC_EXAMPLE.scmInfo = "482389";
        DOC_EXAMPLE.buildPartnerName = "Example Partner Co.";
        DOC_EXAMPLE.serverTitle = "My Shiny New JIRA Server";
    }
}
