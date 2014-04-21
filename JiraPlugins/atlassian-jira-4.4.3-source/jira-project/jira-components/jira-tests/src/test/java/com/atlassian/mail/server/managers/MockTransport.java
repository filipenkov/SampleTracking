package com.atlassian.mail.server.managers;

import alt.javax.mail.Message;
import alt.javax.mail.Transport;
import com.mockobjects.mail.MockService;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.event.TransportListener;
import java.io.IOException;

public class MockTransport extends MockService implements Transport
{

    private boolean sendCalled = false;
    private Message expectedMessage;
    private Message sentMessage;

    public void setExpectedMessage(Message aMessage)
    {
        expectedMessage = aMessage;
    }

    public void send(Message aMessage)
    {
        sendCalled = true;
        sentMessage = aMessage;
    }

    public void send(Message msg, Address[] address)
    {
        notImplemented();
    }

    public void sendMessage(Message msg, Address[] address)
    {
        notImplemented();
    }

    public void addTransportListener(TransportListener transportListener)
    {
        notImplemented();
    }

    public void removeTransportListener(TransportListener transportListener)
    {
        notImplemented();
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
                    if (!expectedMessage.getRealMessage().getContent().equals(sentMessage.getRealMessage().getContent()))
                        junit.framework.Assert.fail("Expected content was\n" + expectedMessage.getRealMessage().getContent() + "\n" +
                                "Sent Content was\n" + sentMessage.getRealMessage().getContent());

                    if (!expectedMessage.getRealMessage().getContentType().equals(sentMessage.getRealMessage().getContentType()))
                        junit.framework.Assert.fail("Expected content type was\n" + expectedMessage.getRealMessage().getContentType() + "\n" +
                                "Sent Content type was\n" + sentMessage.getRealMessage().getContentType());

                    if (!expectedMessage.getRealMessage().getSubject().equals(sentMessage.getRealMessage().getSubject()))
                        junit.framework.Assert.fail("Expected subject was\n" + expectedMessage.getRealMessage().getSubject() + "\n" +
                                "Sent subject was\n" + sentMessage.getRealMessage().getSubject());
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
