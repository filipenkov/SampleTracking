package test.mock.mail;

import javax.mail.*;
import javax.activation.DataHandler;
import java.util.Enumeration;
import java.util.Date;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * A dummy Message implementation for testing.
 */
public class MockMessage extends Message
{

    public void addFrom(Address[] addresses) throws MessagingException
    {
    }

    public void addHeader(String string, String string1) throws MessagingException
    {
    }

    public void addRecipients(RecipientType recipientType, Address[] addresses) throws MessagingException
    {
    }

    public Enumeration getAllHeaders() throws MessagingException
    {
        return null;
    }

    public Object getContent() throws IOException, MessagingException
    {
        return null;
    }

    public String getContentType() throws MessagingException
    {
        return null;
    }

    public DataHandler getDataHandler() throws MessagingException
    {
        return null;
    }

    public String getDescription() throws MessagingException
    {
        return null;
    }

    public String getDisposition() throws MessagingException
    {
        return null;
    }

    public String getFileName() throws MessagingException
    {
        return null;
    }

    public Flags getFlags() throws MessagingException
    {
        return null;
    }

    public Address[] getFrom() throws MessagingException
    {
        return new Address[0];
    }

    public String[] getHeader(String string) throws MessagingException
    {
        return new String[0];
    }

    public InputStream getInputStream() throws IOException, MessagingException
    {
        return null;
    }

    public int getLineCount() throws MessagingException
    {
        return 0;
    }

    public Enumeration getMatchingHeaders(String[] strings) throws MessagingException
    {
        return null;
    }

    public Enumeration getNonMatchingHeaders(String[] strings) throws MessagingException
    {
        return null;
    }

    public Date getReceivedDate() throws MessagingException
    {
        return null;
    }

    public Address[] getRecipients(RecipientType recipientType) throws MessagingException
    {
        return new Address[0];
    }

    public Date getSentDate() throws MessagingException
    {
        return null;
    }

    public int getSize() throws MessagingException
    {
        return 0;
    }

    public String getSubject() throws MessagingException
    {
        return null;
    }

    public boolean isMimeType(String string) throws MessagingException
    {
        return false;
    }

    public void removeHeader(String string) throws MessagingException
    {
    }

    public Message reply(boolean b) throws MessagingException
    {
        return null;
    }

    public void saveChanges() throws MessagingException
    {
    }

    public void setContent(Multipart multipart) throws MessagingException
    {
    }

    public void setContent(Object object, String string) throws MessagingException
    {
    }

    public void setDataHandler(DataHandler dataHandler) throws MessagingException
    {
    }

    public void setDescription(String string) throws MessagingException
    {
    }

    public void setDisposition(String string) throws MessagingException
    {
    }

    public void setFileName(String string) throws MessagingException
    {
    }

    public void setFlags(Flags flags, boolean b) throws MessagingException
    {
    }

    public void setFrom() throws MessagingException
    {
    }

    public void setFrom(Address address) throws MessagingException
    {
    }

    public void setHeader(String string, String string1) throws MessagingException
    {
    }

    public void setRecipients(RecipientType recipientType, Address[] addresses) throws MessagingException
    {
    }

    public void setSentDate(Date date) throws MessagingException
    {
    }

    public void setSubject(String string) throws MessagingException
    {
    }

    public void setText(String string) throws MessagingException
    {
    }

    public void writeTo(OutputStream outputStream) throws IOException, MessagingException
    {
    }
}
