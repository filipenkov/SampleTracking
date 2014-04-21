@XmlAccessorType(XmlAccessType.FIELD)
@XmlJavaTypeAdapters({@XmlJavaTypeAdapter(ApplicationIdAdapter.class), @XmlJavaTypeAdapter(VersionAdapter.class)})
package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.core.rest.model.adapter.ApplicationIdAdapter;
import com.atlassian.applinks.core.rest.model.adapter.VersionAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

