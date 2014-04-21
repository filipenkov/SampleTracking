package com.atlassian.support.tools.action;

import junit.framework.TestCase;

public class TestDefaultMessage extends TestCase
{

	public void testConstructor()
	{
		String name = "name";
		String body = "body";

		DefaultMessage testMessage = new DefaultMessage(name, body);
		assertEquals("The default message name was not stored correctly", name, testMessage.getName());
		assertEquals("The default message body was not stored correctly", body, testMessage.getBody());
	}

}
