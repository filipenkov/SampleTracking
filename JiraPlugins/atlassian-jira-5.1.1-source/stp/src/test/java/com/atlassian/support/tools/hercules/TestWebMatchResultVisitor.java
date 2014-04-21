package com.atlassian.support.tools.hercules;

import junit.framework.TestCase;

public class TestWebMatchResultVisitor extends TestCase
{
	public void testGetPercentRead()
	{
		WebMatchResultVisitor visitor = new WebMatchResultVisitor("/tmp/foo.txt");
		visitor.setTotalSize(10000);
		visitor.setProgress(125);
		assertEquals("125/10000 doesn't return 1 percent", 1, visitor.getPercentRead());
		visitor.setProgress(150);
		assertEquals("150/10000 doesn't return 2 percent", 2, visitor.getPercentRead());
		visitor.setProgress(175);
		assertEquals("175/10000 doesn't return 2 percent", 2, visitor.getPercentRead());
		visitor.setProgress(20000);
		assertEquals("20000/10000 doesn't return 100 percent", 100, visitor.getPercentRead());
	}
}
