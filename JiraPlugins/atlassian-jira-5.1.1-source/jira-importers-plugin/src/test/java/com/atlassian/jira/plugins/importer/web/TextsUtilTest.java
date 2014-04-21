/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class TextsUtilTest {
	@Test
	public void testConvertToNiceHtmlString() throws Exception {
		Assert.assertEquals("fds", TextsUtil.convertToNiceHtmlString("fds"));
		Assert.assertEquals("<p>fds</p><p>abc</p>", TextsUtil.convertToNiceHtmlString("fds\nabc"));
	}

	@Test
	public void testConvertToNiceHtmlStringFromIterable() throws Exception {
		Assert.assertEquals("fds", TextsUtil.convertToNiceHtmlString(Collections.singletonList("fds")));
		Assert.assertEquals("<p>fds</p><p>abc</p>", TextsUtil.convertToNiceHtmlString(Arrays.asList("fds","abc")));
	}

	@Test
	public void testBuildErrorMessage() {
		ErrorCollection errorCollection = new SimpleErrorCollection();
		errorCollection.addError("a", "error-a");
		errorCollection.addError("b", "error-b");
		errorCollection.addErrorMessage("c");
		Assert.assertEquals("c\nerror-a\nerror-b", TextsUtil.buildErrorMessage(errorCollection));
		errorCollection.getErrors().clear();
		Assert.assertEquals("c", TextsUtil.buildErrorMessage(errorCollection));
		errorCollection.getErrorMessages().clear();
		Assert.assertEquals("", TextsUtil.buildErrorMessage(errorCollection));
	}
}
