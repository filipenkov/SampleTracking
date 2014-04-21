package com.atlassian.renderer.wysiwyg;

import org.w3c.tidy.TidyMessageListener;
import org.w3c.tidy.TidyMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomd
 * Date: 15/07/2005
 * Time: 09:39:33
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTidyMessageListener implements TidyMessageListener
{
    private List messages = new ArrayList();

    public List getMessages()
    {
        return messages;
    }

    public void messageReceived(TidyMessage tidyMessage)
    {
        messages.add(tidyMessage);
    }
}
