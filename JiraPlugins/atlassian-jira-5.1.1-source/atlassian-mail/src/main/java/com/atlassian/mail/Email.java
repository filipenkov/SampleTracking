package com.atlassian.mail;

import org.apache.commons.lang.StringUtils;

import javax.mail.Multipart;
import java.util.HashMap;
import java.util.Map;

public class Email
{
    // mandatory fields
    private String to;
    private String subject;

    // optional fields
    private String from;
    private String fromName;
    private String cc;
    private String bcc;
    private String replyTo;
    private String inReplyTo;
    private String body;
    private String mimeType;
    private String encoding;
    private Multipart multipart;
    private String messageId;
    private Map headers;

    private void init(String to)
    {
        this.to = to;

        // set defaults
        this.subject = "";
        this.body = ""; // needs to be instantiated to empty string else send() will throw a NullPointer
        this.mimeType = "text/plain";
        this.encoding = "UTF-8";
        this.headers = new HashMap();
        loadDefaultHeaders();
    }

    /**
     * <b>Note</b>: By default the message has the "Precedence" header set to "bulk". Use {@link #removeHeader(java.lang.String)}
     * to remove
     *
     * @param to
     */
    public Email(String to)
    {
        if (StringUtils.isBlank(to))
            throw new IllegalArgumentException("'To' is a required field");

        init(to);
    }

    /**
     * <b>Note</b>: By default the message has the "Precedence" header set to "bulk". Use {@link #removeHeader(java.lang.String)}
     * to remove
     *
     * @param to
     */
    public Email(String to, String cc, String bcc)
    {
        if (StringUtils.isBlank(to) && StringUtils.isBlank(cc) && StringUtils.isBlank(bcc))
            throw new IllegalArgumentException("One of 'To', 'CC' or 'BCC' is required");

        init(to);
        this.cc = cc;
        this.bcc = bcc;
    }

    protected void loadDefaultHeaders()
    {
        // Set the "Precedence" header to "bulk". All the mails coming from atlassian products likely need this header.
        // This header should stop mail clients generating automatic messages to "answer" mail from atlassian products.
        // For example, "away on holiday" messages. JRA-2622
        headers.put("Precedence", "bulk");
        headers.put("Auto-Submitted", "auto-generated"); // see JRA-15325
    }

    public Email setFrom(String from)
    {
        this.from = from;
        return this;
    }

    public Email setFromName(String fromName)
    {
        this.fromName = fromName;
        return this;
    }

    public Email setTo(String to)
    {
        this.to = to;
        return this;
    }

    public Email setSubject(String subject)
    {
        this.subject = subject;
        return this;
    }

    public Email setCc(String cc)
    {
        this.cc = cc;
        return this;
    }

    public Email setBcc(String bcc)
    {
        this.bcc = bcc;
        return this;
    }

    public Email setReplyTo(String replyTo)
    {
        this.replyTo = replyTo;
        return this;
    }

    public Email setInReplyTo(String inReplyTo)
    {
        this.inReplyTo = inReplyTo;
        return this;
    }

    public Email setBody(String body)
    {
        this.body = body;
        return this;
    }

    public Email setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
        return this;
    }

    public Email setEncoding(String encoding)
    {
        this.encoding = encoding;
        return this;
    }

    public Email setMultipart(Multipart multipart)
    {
        this.multipart = multipart;
        return this;
    }

    public String getFrom()
    {
        return from;
    }

    public String getFromName()
    {
        return fromName;
    }

    public String getTo()
    {
        return to;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getCc()
    {
        return cc;
    }

    public String getBcc()
    {
        return bcc;
    }

    public String getReplyTo()
    {
        return replyTo;
    }

    public String getInReplyTo()
    {
        return inReplyTo;
    }

    public String getBody()
    {
        return body;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public Multipart getMultipart()
    {
        return multipart;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }

    /**
     * Body is NOT included in comparing two Email objects
     *
     * @param o
     * @return
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;

        final Email email = (Email) o;

        if (bcc != null ? !bcc.equals(email.bcc) : email.bcc != null) return false;
        if (cc != null ? !cc.equals(email.cc) : email.cc != null) return false;
        if (encoding != null ? !encoding.equals(email.encoding) : email.encoding != null) return false;
        if (from != null ? !from.equals(email.from) : email.from != null) return false;
        if (fromName != null ? !fromName.equals(email.fromName) : email.fromName != null) return false;
        if (inReplyTo != null ? !inReplyTo.equals(email.inReplyTo) : email.inReplyTo != null) return false;
        if (messageId != null ? !messageId.equals(email.messageId) : email.messageId != null) return false;
        if (mimeType != null ? !mimeType.equals(email.mimeType) : email.mimeType != null) return false;
        if (multipart != null ? !multipart.equals(email.multipart) : email.multipart != null) return false;
        if (replyTo != null ? !replyTo.equals(email.replyTo) : email.replyTo != null) return false;
        if (subject != null ? !subject.equals(email.subject) : email.subject != null) return false;
        if (!to.equals(email.to)) return false;

        return true;
    }

    /**
     * Body is NOT included in calculating the hashCode for the object.
     *
     * @return
     */
    public int hashCode()
    {
        int result;
        result = to.hashCode();
        result = 29 * result + (subject != null ? subject.hashCode() : 0);
        result = 29 * result + (from != null ? from.hashCode() : 0);
        result = 29 * result + (fromName != null ? fromName.hashCode() : 0);
        result = 29 * result + (cc != null ? cc.hashCode() : 0);
        result = 29 * result + (bcc != null ? bcc.hashCode() : 0);
        result = 29 * result + (replyTo != null ? replyTo.hashCode() : 0);
        result = 29 * result + (inReplyTo != null ? inReplyTo.hashCode() : 0);
        result = 29 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 29 * result + (encoding != null ? encoding.hashCode() : 0);
        result = 29 * result + (multipart != null ? multipart.hashCode() : 0);
        result = 29 * result + (messageId != null ? messageId.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return "To='" + to + "' Subject='" + subject + "' From='" + from + "' FromName='" + fromName + "' Cc='" + cc +
                "' Bcc='" + bcc + "' ReplyTo='" + replyTo + "' InReplyTo='" + inReplyTo + "' MimeType='" + mimeType +
                "' Encoding='" + encoding + "' Multipart='" + multipart + "' MessageId='" + messageId + "'";
    }

    public void addHeader(String headerName, String headerValue)
    {
        headers.put(headerName, headerValue);
    }

    /**
     * @param headerName
     * @return the value of the removed header
     */
    public String removeHeader(String headerName)
    {
        if (headers.containsKey(headerName))
        {
            return (String) headers.remove(headerName);
        }
        else
        {
            return null;
        }
    }

    public Map getHeaders()
    {
        return headers;
    }
}
