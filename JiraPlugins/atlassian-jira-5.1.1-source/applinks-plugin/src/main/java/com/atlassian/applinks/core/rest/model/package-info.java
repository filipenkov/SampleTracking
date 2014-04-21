@XmlAccessorType(XmlAccessType.FIELD)
@XmlJavaTypeAdapters({@XmlJavaTypeAdapter(value = ApplicationIdAdapter.class, type = ApplicationId.class),
        @XmlJavaTypeAdapter(value = VersionAdapter.class, type = Version.class)})
package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.core.rest.model.adapter.ApplicationIdAdapter;
import com.atlassian.applinks.core.rest.model.adapter.VersionAdapter;
import org.osgi.framework.Version;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

