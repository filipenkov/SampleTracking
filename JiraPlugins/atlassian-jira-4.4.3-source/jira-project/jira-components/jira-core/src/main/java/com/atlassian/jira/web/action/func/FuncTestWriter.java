package com.atlassian.jira.web.action.func;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.log4j.Logger;

import java.util.Collection;

public class FuncTestWriter extends JiraWebActionSupport implements HtmlEvent
{
    // ------------------------------------------------------------------------------------------------------- Constants
    private static final Logger log = Logger.getLogger(FuncTestWriter.class);

    // ------------------------------------------------------------------------------------------------- Type Properties
    private String elementId;
    private String eventType;
    private String tagName;
    private String innerHtml;
    private String responseText;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final EventTypeManager eventTypeManager;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public FuncTestWriter()
    {
        eventTypeManager = new EventTypeManagerImpl();
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    public String doAddEvent() throws Exception
    {
        EventType eventType = eventTypeManager.getEventType(getTagName(), getEventType());
        String responseText = eventType.getResponseText(this);
        setResponseText(responseText);
        return SUCCESS;
    }
    // --------------------------------------------------------------------------------------------- View Helper Methods
    public Collection getAllEvents()
    {
        return eventTypeManager.getAllEventTypes();
    }
    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public String getElementId()
    {
        return elementId;
    }

    public void setElementId(String elementId)
    {
        this.elementId = elementId;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getTagName()
    {
        return tagName;
    }

    public void setTagName(String tagName)
    {
        this.tagName = tagName;
    }

    public String getInnerHtml()
    {
        return innerHtml;
    }

    public void setInnerHtml(String innerHtml)
    {
        this.innerHtml = innerHtml;
    }

    public String getResponseText()
    {
        return responseText;
    }

    public void setResponseText(String responseText)
    {
        this.responseText = responseText;
    }
}
