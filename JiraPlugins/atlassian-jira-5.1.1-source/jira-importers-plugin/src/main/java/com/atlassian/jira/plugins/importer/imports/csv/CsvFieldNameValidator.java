/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * This class represents a validator that is used for field name validation for
 * CSV import data. This validator checks for presence of plus sign character
 * ('+') which causes exception in webwork (see JRA-12887). This validator also
 * checks for open round and square brackets without corresponding closing
 * brackets. The errors are indicated via return set that contains
 * {@link com.atlassian.jira.plugins.importer.imports.csv.CsvFieldNameValidator.Error} objects
 * if any errors are found.
 */
public class CsvFieldNameValidator {

	/**
	 * Simple class for representing errors reported by this validator. Each
	 * error constant has a unique key that can be used as an com.atlassian.jira.plugins.importer.i18n message
	 * resource key for messages defined in JiraWebActionSupport.properties.
	 */
	public static class Error implements Comparable {
		/**
		 * This error represents an invalid plus sign character
		 */
		public static final Error PLUS_SIGN = new Error("admin.errors.name.plus");

		/**
		 * This error represents an error when bracket mismatch is found
		 */
		public static final Error BRACKET_MISMATCH = new Error("admin.errors.name.bracket.mismatch");

		private final String key;

		private Error(String key) {
			this.key = key;
		}

		/**
		 * Returns com.atlassian.jira.plugins.importer.i18n message resource key
		 *
		 * @return i18n message resource key
		 */
		public String getKey() {
			return key;
		}

		/**
		 * Returns com.atlassian.jira.plugins.importer.i18n message resource key
		 *
		 * @return i18n message resource key
		 */
		public String toString() {
			return key;
		}

		/*
				 * Implements the comparison by keys.
				 *
				 * @param o the Object to be compared
				 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater
				 *         than the specified object.
				 */

		public int compareTo(Object o) {
			if (o instanceof Error) {
				return key.compareTo(((Error) o).getKey());
			}
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}

	/**
	 * Runs the result of the check over the given field name.
	 * Returns a set of {@link com.atlassian.jira.plugins.importer.imports.csv.CsvFieldNameValidator.Error} objects if any errors found.
	 *
	 * @param fieldName field name
	 * @return a set of {@link com.atlassian.jira.plugins.importer.imports.csv.CsvFieldNameValidator.Error} objects, never null
	 */
	public Set<Error> check(@Nullable String fieldName) {
		Set<Error> reasons = new TreeSet<Error>();

		if (!containsMatchingBrackets(fieldName)) {
			reasons.add(Error.BRACKET_MISMATCH);
		}

		// Plus sign makes webwork to crash. See JRA-12887
		if (StringUtils.contains(fieldName, '+')) {
			reasons.add(Error.PLUS_SIGN);
		}

		return reasons;
	}

	/**
	 * Check the given string for matching number of opening and closing round or square brackets.
	 * The brackets also need to be properly nested.
	 *
	 * @param value string to check, can be null
	 * @return true if string does not contain any brackets, or if brackets are matching and correctly nested
	 */
	protected boolean containsMatchingBrackets(@Nullable String value) {
		if (value == null) {
			return true;
		}
		final Stack<Character> bracketStack = new Stack<Character>();
		for (int i = 0; i < value.length(); i++) {
			final char c = value.charAt(i);
			switch (c) {
				case '(':
				case '[':
					bracketStack.push(Character.valueOf(c));
					break;

				case ')':
					if (!bracketStack.isEmpty() && '(' == bracketStack.peek().charValue()) {
						bracketStack.pop();
						break;
					}
					return false;

				case ']':
					if (!bracketStack.isEmpty() && '[' == bracketStack.peek().charValue()) {
						bracketStack.pop();
						break;
					}
					return false;
			}
		}
		return bracketStack.isEmpty();
	}
}
