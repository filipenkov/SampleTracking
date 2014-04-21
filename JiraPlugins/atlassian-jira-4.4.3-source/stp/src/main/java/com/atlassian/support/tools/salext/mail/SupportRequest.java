package com.atlassian.support.tools.salext.mail;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class SupportRequest implements Serializable
{
	public static final int DEFAULT_PRIORITY = 3;

	private final Map<String, String> headers = new HashMap<String, String>();
	private final List<SupportRequestAttachment> attachments = new ArrayList<SupportRequestAttachment>();
	
	private String description;
	private String subject;
	private String timeZone;
	private String toAddress;
	private String fromAddress;
	private String body;
	private int priority;

	
	public SupportRequest()
	{
		this.description = "";
		this.fromAddress = "";
		this.subject = "";
		this.timeZone = "";
		this.priority = DEFAULT_PRIORITY;
	}
	
	public String saveForMail(SupportApplicationInfo info)
	{
		Properties prop = new Properties();
		prop.setProperty("description", description);
		prop.setProperty("contactEmail", fromAddress);
		prop.setProperty("subject", subject);
		prop.setProperty("timeZone", timeZone);
		prop.setProperty("priority", String.valueOf(priority));
		
		final String sen = info.getApplicationSEN();
		if(sen != null)
			prop.setProperty("sen", sen);
		
		final String serverID = info.getApplicationServerID();
		if(serverID != null)
			prop.setProperty("serverID", serverID);
		
		final String version = info.getApplicationVersion();
		if(version != null)
			prop.setProperty("version", version);
		
		StringWriter writer = new StringWriter();
		try
		{
			prop.store(writer, null);
		}
		catch(IOException e)
		{
			// this should never happen unless we run out of memory or somesuch
			e.printStackTrace();
		}
		finally
		{
			IOUtils.closeQuietly(writer);
		}
		return writer.toString();
	}

	public Iterable<Entry<String, String>> getHeaders()
	{
		return headers.entrySet();
	}
	
	public Iterable<SupportRequestAttachment> getAttachments()
	{
		return attachments;
	}
	
	public void addHeader(String name, String value)
	{
		headers.put(name, value);
	}
	
	public void addAttachment(SupportRequestAttachment attachment)
	{
		attachments.add(attachment);
	}
	
	public String getDescription()
	{
		return description;
	}

	public String getFromAddress()
	{
		return fromAddress;
	}

	public String getToAddress()
	{
		return toAddress;
	}
	
	public String getSubject()
	{
		return subject;
	}

	public String getTimeZone()
	{
		return timeZone;
	}

	public int getPriority()
	{
		return priority;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public void setTimeZone(String timeZone)
	{
		this.timeZone = timeZone;
	}

	public void setToAddress(String toAddress)
	{
		this.toAddress = toAddress;
	}

	public void setFromAddress(String fromAddress)
	{
		this.fromAddress = fromAddress;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public void setBody(String body)
	{
		this.body = body;
	}
	
	public String getBody()
	{
		return body;
	}
}
