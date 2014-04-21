package com.atlassian.support.tools;

import junit.framework.TestCase;

public class TestStage extends TestCase
{
	public void testStageLookup()
	{
		String start = Stage.START.toString();
		String execute = Stage.EXECUTE.toString();

		assertEquals("Lookup results for 'start' didn't return the correct results.", Stage.START, Stage.lookup(start));
		assertEquals("Lookup results for 'execute' didn't return the correct results.", Stage.EXECUTE,
				Stage.lookup(execute));

		assertFalse("Lookup results for 'start' incorrectly matched the 'execute' stage.",
				Stage.START.equals(Stage.lookup(execute)));
		assertFalse("Lookup results for 'execute' incorrectly matched the 'start' stage.",
				Stage.EXECUTE.equals(Stage.lookup(start)));
	}
}
