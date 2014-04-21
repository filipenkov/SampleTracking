package com.atlassian.jira.plugin.issuenav.viewissue.webpanel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Represents all the webpanels on a view issue page.
 *
 * @since v5.1
 */
@XmlRootElement
public class IssueWebPanelsBean
{

    @XmlElement
    private List<WebPanelBean> leftPanels;
    @XmlElement
    private List<WebPanelBean> rightPanels;
    @XmlElement
    private List<WebPanelBean> infoPanels;

    IssueWebPanelsBean() { }

    public IssueWebPanelsBean(List<WebPanelBean> leftPanels, List<WebPanelBean> rightPanels, List<WebPanelBean> infoPanels)
    {
        this.leftPanels = leftPanels;
        this.rightPanels = rightPanels;
        this.infoPanels = infoPanels;
    }

    public List<WebPanelBean> getLeftPanels()
    {
        return leftPanels;
    }

    public List<WebPanelBean> getRightPanels()
    {
        return rightPanels;
    }

    public List<WebPanelBean> getInfoPanels()
    {
        return infoPanels;
    }
}
