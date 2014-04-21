/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.model;

import com.atlassian.jira.util.ErrorCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.Map;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class ValidationResultModel
{
    private Collection<String> globalErrors;
    private Map<String, String> fieldErrors;

    public ValidationResultModel(ErrorCollection errors) {
        this.globalErrors = ImmutableList.copyOf(errors.getErrorMessages());
        this.fieldErrors = ImmutableMap.copyOf(errors.getErrors());
    }
}
