package com.atlassian.jira.rest.v2.issue.version;

import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.bind.DateAdapter;
import com.atlassian.jira.rest.v2.common.SimpleLinkBean;
import com.atlassian.jira.rest.v2.issue.Examples;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
* @since v4.2
*/
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name="version")
public class VersionBean
{

    /**
     * A version bean instance used for auto-generated documentation.
     */
    static final VersionBean DOC_EXAMPLE;
    static final VersionBean DOC_EXAMPLE_2;
    static final List<VersionBean> DOC_EXAMPLE_LIST = new ArrayList<VersionBean>();
    static final VersionBean DOC_CREATE_EXAMPLE;
    static
    {
        final DateFormat exampleDateFormat = new SimpleDateFormat("d/MMM/yyyy", Locale.ENGLISH);

        VersionBean version = new VersionBean();
        version.self = Examples.restURI("version/10000");
        version.id = "10000";
        version.name = "New Version 1";
        version.description = "An excellent version";
        version.archived = false;
        version.released = true;
        version.overdue = true;
        version.releaseDate = new Date(1278385482288L);
        version.userReleaseDate = exampleDateFormat.format(version.releaseDate);

        DOC_EXAMPLE = version;

        version = new VersionBean();
        version.self = Examples.restURI("version/10010");
        version.id = "10010";
        version.name = "Next Version";
        version.description = "Minor Bugfix version";
        version.archived = false;
        version.released = false;
        version.overdue = false;

        DOC_EXAMPLE_2 = version;
        DOC_EXAMPLE_LIST.add(DOC_EXAMPLE);
        DOC_EXAMPLE_LIST.add(DOC_EXAMPLE_2);

        version = new VersionBean();
        version.project = "PXA";
        version.name = "New Version 1";
        version.description = "An excellent version";
        version.archived = false;
        version.released = true;
        version.releaseDate = new Date(1278385482288L);
        version.userReleaseDate = exampleDateFormat.format(version.releaseDate);

        DOC_CREATE_EXAMPLE = version;

    }

    @XmlAttribute
    private String expand;

    @XmlElement
    private URI self;

    @XmlElement
    private String id;

    @XmlElement
    private String description;

    @XmlElement
    private String name;

    @XmlElement
    private Boolean archived;

    @XmlElement
    private Boolean released;

    private Date releaseDate;

    /** This field is used to trap the fact the Release Date has been set, even though it may have been set to null. */
    @XmlTransient
    private boolean releaseDateSet = false;

    @XmlElement
    private Boolean overdue;

    @XmlElement
    private String userReleaseDate;

    @XmlElement
    private String project;

    @XmlElement
    private URI moveUnfixedIssuesTo;

    @XmlElement
    private ArrayList<SimpleLinkBean> operations;

    public String getProject()
    {
        return project;
    }

    public String getUserReleaseDate()
    {
        return userReleaseDate;
    }

    public Boolean getOverdue()
    {
        return overdue;
    }

    @XmlElement
    @XmlJavaTypeAdapter (DateAdapter.class)
    public Date getReleaseDate()
    {
        return releaseDate;
    }

    @XmlTransient
    public boolean isReleaseDateSet()
    {
        return releaseDateSet;
    }

    public Boolean isReleased()
    {
        return released;
    }

    public Boolean isArchived()
    {
        return archived;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getExpand()
    {
        return expand;
    }
    public void setExpand(String expand)
    {
        this.expand = expand;
    }

    public URI getSelf()
    {
        return self;
    }

    public URI getMoveUnfixedIssuesTo()
    {
        return moveUnfixedIssuesTo;
    }

    public void setSelf(URI self)
    {
        this.self = self;
    }

    public void setId(Long id)
    {
        this.id = id == null ? null : id.toString();
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setArchived(Boolean archived)
    {
        this.archived = archived;
    }

    public void setReleased(Boolean released)
    {
        this.released = released;
    }

    public void setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
        this.releaseDateSet = true;
    }

    public void setOverdue(Boolean overdue)
    {
        this.overdue = overdue;
    }

    public void setUserReleaseDate(String userReleaseDate)
    {
        this.userReleaseDate = userReleaseDate;
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    public void setMoveUnfixedIssuesTo(URI moveUnfixedIssuesTo)
    {
        this.moveUnfixedIssuesTo = moveUnfixedIssuesTo;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    //Needed so that JAXB works.
    public VersionBean() {}

    VersionBean(final Version version, Boolean overdue, String userReleaseDate, final URI self)
    {
        this(version, overdue, userReleaseDate, self, null);
    }
    VersionBean(final Version version, Boolean overdue, String userReleaseDate, final URI self, ArrayList<SimpleLinkBean> operations)
    {
        this.self = self;
        this.id = version.getId().toString();
        this.name = version.getName();
        this.description = StringUtils.stripToNull(version.getDescription());
        this.releaseDate = version.getReleaseDate();
        this.archived = version.isArchived();
        this.released = version.isReleased();
        this.overdue = overdue;
        this.userReleaseDate = userReleaseDate;
        this.operations = operations;
    }

    private VersionBean(Long id, String project, URI self, String name, String description, boolean archived, boolean released, Date releaseDate, boolean releaseDateSet, String userReleaseDate, Boolean overdue, ArrayList<SimpleLinkBean> operations)
    {
        this.id = id == null ? null : id.toString();
        this.self = self;
        this.description = description;
        this.name = name;
        this.archived = archived;
        this.released = released;
        this.releaseDate = releaseDate;
        this.releaseDateSet = releaseDateSet;
        this.overdue = overdue;
        this.userReleaseDate = userReleaseDate;
        this.project = project;
        this.operations = operations;
    }

    public static class Builder
    {
        private URI self;
        private Long id;
        private String description;
        private String name;
        private boolean archived;
        private boolean released;
        private Date releaseDate;
        private boolean releaseDateSet;
        private Boolean overdue;
        private String userReleaseDate;
        private String project;
        private ArrayList<SimpleLinkBean> operations;

        public URI getSelf()
        {
            return self;
        }

        public Builder setSelf(URI self)
        {
            this.self = self;
            return this;
        }

        public Builder setVersion(Version version)
        {
            this.id = version.getId();
            this.name = version.getName();
            this.description = StringUtils.stripToNull(version.getDescription());
            this.releaseDate = version.getReleaseDate();
            this.archived = version.isArchived();
            this.released = version.isReleased();
            return this;
        }

        public Long getId()
        {
            return id;
        }

        public Builder setId(Long id)
        {
            this.id = id;
            return this;
        }

        public String getDescription()
        {
            return description;
        }

        public Builder setDescription(String description)
        {
            this.description = description;
            return this;
        }

        public String getName()
        {
            return name;
        }

        public Builder setName(String name)
        {
            this.name = name;
            return this;
        }

        public boolean isArchived()
        {
            return archived;
        }

        public Builder setArchived(boolean archived)
        {
            this.archived = archived;
            return this;
        }

        public boolean isReleased()
        {
            return released;
        }

        public Builder setReleased(boolean released)
        {
            this.released = released;
            return this;
        }

        public Date getReleaseDate()
        {
            return releaseDate;
        }

        public Builder setReleaseDate(Date releaseDate)
        {
            this.releaseDate = releaseDate;
            this.releaseDateSet = true;
            return this;
        }

        public Boolean getOverdue()
        {
            return overdue;
        }

        public Builder setOverdue(Boolean overdue)
        {
            this.overdue = overdue;
            return this;
        }

        public String getUserReleaseDate()
        {
            return userReleaseDate;
        }

        public Builder setUserReleaseDate(String userReleaseDate)
        {
            this.userReleaseDate = userReleaseDate;
            return this;
        }

        public String getProject()
        {
            return project;
        }

        public Builder setProject(String project)
        {
            this.project = project;
            return this;
        }

        public ArrayList<SimpleLinkBean> getOperations()
        {
            return operations;
        }

        public Builder setOperations(ArrayList<SimpleLinkBean> operations)
        {
            this.operations = operations;
            return this;
        }

        public VersionBean build()
        {
            return new VersionBean(id, project, self, name, description, archived, released, releaseDate, releaseDateSet,
                    userReleaseDate, overdue, operations);
        }
    }

}
