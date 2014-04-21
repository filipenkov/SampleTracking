package com.atlassian.support.tools.salext;

import junit.framework.TestCase;

public class TestNullSafeProperties extends TestCase
{
	public void testNullSafeProperties()
	{
		NullSafeProperties nsp = new NullSafeProperties();
		nsp.put("valid", "valid");
		assertTrue("Null Safe Properties stripped a non-null value.", nsp.size() == 1);

		NullSafeProperties nsp2 = new NullSafeProperties();
		nsp2.put(null, "valid");
		assertTrue("Null Safe Properties didn't strip a null key.", nsp2.size() == 0);

		NullSafeProperties nsp3 = new NullSafeProperties();
		nsp3.put("valid", null);
		assertTrue("Null Safe Properties didn't strip a null value.", nsp3.size() == 0);
	}
}
