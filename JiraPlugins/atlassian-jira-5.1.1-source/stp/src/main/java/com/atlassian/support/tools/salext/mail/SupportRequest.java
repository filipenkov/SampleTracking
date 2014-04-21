package com.atlassian.support.tools.salext.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;

import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class SupportRequest implements Serializable
{
	public static final int DEFAULT_PRIORITY = 3;

	private final Map<String, String> headers = new HashMap<String, String>();
	private final List<SupportRequestAttachment> attachments = new ArrayList<SupportRequestAttachment>();
	
	private String description;
	private String subject;
	private String toAddress;
	private String fromAddress;
	private String body;
	private int priority;

	
	public SupportRequest()
	{
		this.description = "";
		this.fromAddress = "";
		this.subject = "";
		this.priority = DEFAULT_PRIORITY;
	}
	
	public String saveForMail(SupportApplicationInfo info)
	{
		Properties prop = new Properties();
		prop.setProperty("description", description);
		prop.setProperty("contactEmail", fromAddress);
		prop.setProperty("subject", subject);

		String rawTimezone = System.getProperty("user.timezone");
        TimeZone timeZone = TimeZone.getTimeZone(rawTimezone); // returns GMT if it can't parse (yuck!)
        int offsetMS = timeZone.getRawOffset() + (timeZone.inDaylightTime(new Date()) ? timeZone.getDSTSavings() : 0);
        int offsetHour = offsetMS/1000/60/60; // Note that this rounds timezones *down* to the nearest hour
		
		String timezoneStringInGMT = "GMT"+(offsetHour >= 0 ? "+" : "") + offsetHour;
		prop.setProperty("timeZone", timezoneStringInGMT);
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
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			prop.store(baos, null);
		}
		catch(IOException e)
		{
			// this should never happen unless we run out of memory or somesuch
			e.printStackTrace();
		}
		finally
		{
			IOUtils.closeQuietly(baos);
		}
		return baos.toString();
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
