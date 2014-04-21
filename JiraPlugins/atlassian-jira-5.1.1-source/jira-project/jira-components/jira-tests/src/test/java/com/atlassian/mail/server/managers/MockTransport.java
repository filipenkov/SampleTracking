package com.atlassian.mail.server.managers;

import org.codehaus.xfire.transport.DefaultTransportManager;

import javax.mail.Message;
import javax.mail.Transport;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.event.TransportListener;
import java.io.IOException;

public class MockTransport extends Transport
{

    private boolean sendCalled = false;
    private Message expectedMessage;
    private Message sentMessage;
    private static MockTransport defaultTransport;

    public MockTransport(Session session, URLName urlName)
    {
        super(session, urlName);
        defaultTransport = this;
    }

    public void setExpectedMessage(Message aMessage)
    {
        expectedMessage = aMessage;
    }

    public static void send(Message aMessage)
    {
        if (defaultTransport != null)
        {
            defaultTransport.sendCalled = true;
            defaultTransport.sentMessage = aMessage;
        }
    }

    public static void send(Message msg, Address[] address)
    {

    }

    public void sendMessage(Message msg, Address[] address)
    {

    }

    public void addTransportListener(TransportListener transportListener)
    {

    }

    public void removeTransportListener(TransportListener transportListener)
    {

    }

    public void verify()
    {
        if (!sendCalled)
        {
            junit.framework.Assert.fail("Send function wasn't called");
        }
        else
        {
            if (expectedMessage != null)
            {
                try
                {
                    if (!expectedMessage.getContent().equals(sentMessage.getContent()))
                        junit.framework.Assert.fail("Expected content was\n" + expectedMessage.getContent() + "\n" +
                                "Sent Content was\n" + sentMessage.getContent());

                    if (!expectedMessage.getContentType().equals(sentMessage.getContentType()))
                        junit.framework.Assert.fail("Expected content type was\n" + expectedMessage.getContentType() + "\n" +
                                "Sent Content type was\n" + sentMessage.getContentType());

                    if (!expectedMessage.getSubject().equals(sentMessage.getSubject()))
                        junit.framework.Assert.fail("Expected subject was\n" + expectedMessage.getSubject() + "\n" +
                                "Sent subject was\n" + sentMessage.getSubject());
                }
                catch (IOException e)
                {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                }
                catch (MessagingException e)
                {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                }
            }
        }
    }
}
