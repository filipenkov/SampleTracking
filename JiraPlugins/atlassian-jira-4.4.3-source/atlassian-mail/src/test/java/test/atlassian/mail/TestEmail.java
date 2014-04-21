package test.atlassian.mail;

import com.atlassian.mail.Email;
import junit.framework.TestCase;

import java.util.Map;

public class TestEmail extends TestCase
{
    public void testSetters()
    {
        Email email = new Email("to");
        email.setSubject("subject").setFrom("from").setFromName("fromName").setBcc("bcc").setBody("body").setCc("cc").setEncoding("encoding").setMimeType("mimeType").setReplyTo("replyTo");

        assertEquals("from", email.getFrom());
        assertEquals("fromName", email.getFromName());
        assertEquals("to", email.getTo());
        assertEquals("subject", email.getSubject());
        assertEquals("bcc", email.getBcc());
        assertEquals("body", email.getBody());
        assertEquals("cc", email.getCc());
        assertEquals("encoding", email.getEncoding());
        assertEquals("mimeType", email.getMimeType());
        assertEquals("replyTo", email.getReplyTo());
    }

    public void testHeaders()
    {
        Email email = new Email("to");
        Map headers = email.getHeaders();

        assertEquals(headers.get("Auto-Submitted"), "auto-generated");
        assertEquals(headers.get("Precedence"), "bulk");
    }

    public void testForRequiredFields()
    {
        try
        {
            new Email(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("'To' is a required field", e.getMessage());
        }
    }
}
