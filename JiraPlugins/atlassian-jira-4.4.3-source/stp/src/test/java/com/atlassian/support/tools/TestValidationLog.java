package com.atlassian.support.tools;

import junit.framework.TestCase;

import com.atlassian.support.tools.action.ActionError;
import com.atlassian.support.tools.action.ActionWarning;
import com.atlassian.support.tools.mock.MockApplicationInfo;

public class TestValidationLog extends TestCase
{
	public void testCreation()
	{
		ActionError testError = new ActionError("Empty bag of chips",
				"not enough chips left to satisfy the team's needs.");
		ActionWarning testWarning = new ActionWarning("Out of celery", "The team may not be getting enough fiber.");

		// Sanity check the hasErrors and hasWarnings methods
		ValidationLog testValidationLog = new ValidationLog(new MockApplicationInfo());
		assertFalse("hasErrors() does not return false before adding an error.", testValidationLog.hasErrors());
		assertFalse("hasWarnings() does not return false before adding a warning.", testValidationLog.hasWarnings());

		// Make sure that all list methods return empty lists rather than null
		assertFalse("getErrors() on an empty log equals null rather than an empty list.",
				testValidationLog.getErrors() == null);
		assertFalse("getWarnings() on an empty log equals null rather than an empty list.",
				testValidationLog.getWarnings() == null);
		assertFalse("getFieldErrors() with a bogus key on an empty log equals null rather than an empty list.",
				testValidationLog.getFieldErrors("bogus") == null);
		assertFalse("getFieldWarnings() with a bogus key on an empty log equals null rather than an empty list.",
				testValidationLog.getFieldWarnings("bogus") == null);

		testValidationLog.addError(testError);
		assertTrue("hasErrors() does not return true after adding an error.", testValidationLog.hasErrors());
		testValidationLog.addWarning(testWarning);
		assertTrue("hasWarnings() does not return true after adding a warning.", testValidationLog.hasWarnings());

		// Make sure error logging works by roundtripping an error
		ValidationLog testValidationLog1 = new ValidationLog(new MockApplicationInfo());
		testValidationLog1.addError(testError);
		ActionError storedError = testValidationLog1.getErrors().get(0);
		assertEquals("Stored error does not equal original error", testError, storedError);

		// Make sure warning logging works by roundtripping a warning
		ValidationLog testValidationLog2 = new ValidationLog(new MockApplicationInfo());
		testValidationLog2.addWarning(testWarning);
		ActionWarning storedWarning = testValidationLog2.getWarnings().get(0);
		assertEquals("Stored warning does not equal original error", testWarning, storedWarning);

		// Make sure a field error based on an ActionError is stored
		// appropriately
		ValidationLog testValidationLog3 = new ValidationLog(new MockApplicationInfo());
		testValidationLog3.addFieldError("chips", testError);
		ActionError storedFieldError = testValidationLog3.getFieldErrors("chips").get(0);
		assertEquals("Stored field error does not equal original error", testError, storedFieldError);
		assertTrue("Field error was not added to the overall list of errors.", testValidationLog3.hasErrors());
		ActionError storedFieldError2 = testValidationLog3.getErrors().get(0);
		assertEquals("Stored field error in main list of errors does not equal original error", testError,
				storedFieldError2);

		// Make sure a field warning based on an ActionWarning is stored
		// appropriately
		ValidationLog testValidationLog4 = new ValidationLog(new MockApplicationInfo());
		testValidationLog4.addFieldWarning("celery", testWarning);
		ActionWarning storedFieldWarning = testValidationLog4.getFieldWarnings("celery").get(0);
		assertEquals("Stored field warning does not equal original error", testWarning, storedFieldWarning);
		assertTrue("Field warning was not added to the overall list of warnings.", testValidationLog4.hasWarnings());
		ActionWarning storedFieldWarning2 = testValidationLog4.getWarnings().get(0);
		assertEquals("Warning stored in overall list of warnings does not equal original warning", testWarning,
				storedFieldWarning2);

		// Make sure a field error based on only a body is stored appropriately
		ValidationLog testValidationLog5 = new ValidationLog(new MockApplicationInfo());
		testValidationLog5.addFieldError("chips", testError.getBody());
		ActionError storedFieldError3 = testValidationLog5.getFieldErrors("chips").get(0);
		assertEquals("Stored field error body does not equal original error body", testError.getBody(),
				storedFieldError3.getBody());
		ActionError storedFieldError4 = testValidationLog5.getErrors().get(0);
		assertEquals("Stored field error in main list of errors does not equal original error", testError.getBody(),
				storedFieldError4.getBody());

		// Make sure a field warning based on an ActionWarning is stored
		// appropriately
		ValidationLog testValidationLog6 = new ValidationLog(new MockApplicationInfo());
		testValidationLog6.addFieldWarning("celery", testWarning.getBody());
		ActionWarning storedFieldWarning3 = testValidationLog6.getFieldWarnings("celery").get(0);
		assertEquals("Stored field warning body does not equal original warning body", testWarning.getBody(),
				storedFieldWarning3.getBody());
		ActionWarning storedFieldWarning4 = testValidationLog6.getWarnings().get(0);
		assertEquals("Body of warning stored in overall list of warnings does not equal original warning body",
				testWarning.getBody(), storedFieldWarning4.getBody());
	}
}
