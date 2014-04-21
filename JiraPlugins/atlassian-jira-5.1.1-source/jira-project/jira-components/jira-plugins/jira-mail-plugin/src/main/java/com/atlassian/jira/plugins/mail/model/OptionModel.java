/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.model;

import org.apache.commons.lang.builder.EqualsBuilder;

public class OptionModel
{
    public String name;
	public String id;

    public OptionModel() {

    }

	public OptionModel(String name, String id) {
		this.name = name;
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj); // for testing
	}
}
