/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 10, 2004
 * Time: 12:29:39 PM
 */
package com.atlassian.jira.project.version;

import com.atlassian.jira.ofbiz.OfBizValueWrapper;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

import java.util.Date;

public interface Version extends OfBizValueWrapper
{
    /**
     * Returns Project as a GenericValue.
     * @return Project as a GenericValue.
     *
     * @deprecated Please use {@link #getProjectObject()}. Since v4.0
     */
    GenericValue getProject();

    /**
     * Returns project this verion relates to.
     *
     * @return project domain object
     * @since v3.10
     */
    Project getProjectObject();

    Long getId();

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    Long getSequence();

    void setSequence(Long sequence);

    boolean isArchived();

    void setArchived(boolean archived);

    boolean isReleased();

    void setReleased(boolean released);

    Date getReleaseDate();

    void setReleaseDate(Date releasedate);
}