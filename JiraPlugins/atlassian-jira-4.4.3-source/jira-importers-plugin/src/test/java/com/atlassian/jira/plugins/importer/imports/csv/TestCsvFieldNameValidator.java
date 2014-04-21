/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.plugins.importer.imports.csv.CsvFieldNameValidator;
import junit.framework.TestCase;

import java.util.Set;

public class TestCsvFieldNameValidator extends TestCase {
	public void testCheck() {
		CsvFieldNameValidator validator = new CsvFieldNameValidator();
		assertTrue(validator.check(null).isEmpty());
		assertTrue(validator.check("").isEmpty());
		assertTrue(validator.check("abc").isEmpty());
		assertTrue(validator.check("def123").isEmpty());
		assertTrue(validator.check("ABC").isEmpty());
		assertTrue(validator.check("abc123ABC").isEmpty());
		assertTrue(validator.check("any(round)brackets").isEmpty());
		assertTrue(validator.check("some(square)brackets").isEmpty());

		// missing closing round bracket
		Set invalidChars = validator.check("opening(round");
		assertNotNull(invalidChars);
		assertEquals(1, invalidChars.size());
		assertTrue(invalidChars.contains(CsvFieldNameValidator.Error.BRACKET_MISMATCH));

		// missing closing square bracket
		invalidChars = validator.check("opening[round");
		assertNotNull(invalidChars);
		assertEquals(1, invalidChars.size());
		assertTrue(invalidChars.contains(CsvFieldNameValidator.Error.BRACKET_MISMATCH));

		// plus sign
		invalidChars = validator.check("a+b");
		assertNotNull(invalidChars);
		assertEquals(1, invalidChars.size());
		assertTrue(invalidChars.contains(CsvFieldNameValidator.Error.PLUS_SIGN));

		// all of them
		invalidChars = validator.check("round(plus+[square");
		assertNotNull(invalidChars);
		assertEquals(2, invalidChars.size());
		assertTrue(invalidChars.contains(CsvFieldNameValidator.Error.BRACKET_MISMATCH));
		assertTrue(invalidChars.contains(CsvFieldNameValidator.Error.PLUS_SIGN));
	}

	public void testBracketMatching() {
		CsvFieldNameValidator validator = new CsvFieldNameValidator();

		// no brackets
		assertTrue(validator.containsMatchingBrackets(null));
		assertTrue(validator.containsMatchingBrackets(""));
		assertTrue(validator.containsMatchingBrackets(" "));
		assertTrue(validator.containsMatchingBrackets("abc"));
		assertTrue(validator.containsMatchingBrackets("JIRA"));

		// square brackets
		assertTrue(validator.containsMatchingBrackets("[JIRA]"));
		assertTrue(validator.containsMatchingBrackets("J[I]RA"));
		assertTrue(validator.containsMatchingBrackets("JI[R]A"));
		assertTrue(validator.containsMatchingBrackets("J[IR]A"));
		assertTrue(validator.containsMatchingBrackets("[J[I]R]A"));
		assertTrue(validator.containsMatchingBrackets("[[J[I]R]A]"));
		assertTrue(validator.containsMatchingBrackets("[[[JIRA]]]"));

		// round brackets
		assertTrue(validator.containsMatchingBrackets("(JIRA)"));
		assertTrue(validator.containsMatchingBrackets("J(I)RA"));
		assertTrue(validator.containsMatchingBrackets("JI(R)A"));
		assertTrue(validator.containsMatchingBrackets("J(IR)A"));
		assertTrue(validator.containsMatchingBrackets("(J(I)R)A"));
		assertTrue(validator.containsMatchingBrackets("((J(I)R)A)"));
		assertTrue(validator.containsMatchingBrackets("(((JIRA)))"));

		// mixed brackets
		assertTrue(validator.containsMatchingBrackets("[(JIRA)]"));
		assertTrue(validator.containsMatchingBrackets("([JIRA])"));
		assertTrue(validator.containsMatchingBrackets("[J(I)R]A"));
		assertTrue(validator.containsMatchingBrackets("(J[I]R)A"));
		assertTrue(validator.containsMatchingBrackets("[([J[I]RA])]"));
		assertTrue(validator.containsMatchingBrackets("[J](I)[(R)(A)]"));
		assertTrue(validator.containsMatchingBrackets("[](JI[]RA)[]"));

		// invalid combinations - unclosed
		assertFalse(validator.containsMatchingBrackets("(JIRA"));
		assertFalse(validator.containsMatchingBrackets("[JIRA"));
		assertFalse(validator.containsMatchingBrackets("J(IRA"));
		assertFalse(validator.containsMatchingBrackets("JI[RA"));
		assertFalse(validator.containsMatchingBrackets("[J]I[RA"));
		assertFalse(validator.containsMatchingBrackets("(JI)RA("));
		assertFalse(validator.containsMatchingBrackets("[[JI]RA"));
		assertFalse(validator.containsMatchingBrackets("J((IRA)"));

		// invalid combinations - unopen
		assertFalse(validator.containsMatchingBrackets("JIRA)"));
		assertFalse(validator.containsMatchingBrackets("JIR]A"));
		assertFalse(validator.containsMatchingBrackets("J[I]R]A"));
		assertFalse(validator.containsMatchingBrackets("J(I)R)A"));
		assertFalse(validator.containsMatchingBrackets("[(JIR)A)]"));
		assertFalse(validator.containsMatchingBrackets("J[IRA]]"));
		assertFalse(validator.containsMatchingBrackets("J(IRA))"));

		// invalid combinations - interleaved
		assertFalse(validator.containsMatchingBrackets("[J(I]R)A"));
		assertFalse(validator.containsMatchingBrackets("J(I[R)A]"));
		assertFalse(validator.containsMatchingBrackets("[J(I[R)A]]"));
	}

}
