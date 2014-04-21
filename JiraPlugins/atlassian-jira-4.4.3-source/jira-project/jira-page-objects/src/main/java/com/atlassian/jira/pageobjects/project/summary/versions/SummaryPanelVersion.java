package com.atlassian.jira.pageobjects.project.summary.versions;

import com.atlassian.jira.pageobjects.project.versions.EditVersionForm;
import com.atlassian.jira.pageobjects.project.versions.Version;
import com.atlassian.jira.pageobjects.project.versions.operations.VersionOperationDropdown;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
* @since v4.4
*/
public class SummaryPanelVersion implements Version
{
    private String name;
    private Date releaseDate;
    private boolean overdue;
    private boolean archieved;
    private boolean released;
    private String description;

    public static List<SummaryPanelVersion> toSimple(Iterable<? extends Version> versions)
    {
        List<SummaryPanelVersion> summaryPanelVersions = new ArrayList<SummaryPanelVersion>();
        for (Version version : versions)
        {
            if (version instanceof SummaryPanelVersion)
            {
                summaryPanelVersions.add((SummaryPanelVersion) version);
            }
            else
            {
                summaryPanelVersions.add(new SummaryPanelVersion(version));
            }
        }
        return summaryPanelVersions;
    }

    public SummaryPanelVersion(Version version)
    {
        this.name = version.getName();
        this.description = version.getDescription();
        this.overdue = version.isOverdue();
        this.archieved = version.isArchived();
        this.released = version.isReleased();
        this.releaseDate = version.getReleaseDate();
    }


    public SummaryPanelVersion(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public Date getReleaseDate()
    {
        return releaseDate;
    }

    @Override
    public EditVersionForm edit(String name)
    {
        return null;
    }

    @Override
    public boolean isOverdue()
    {
        return overdue;
    }

    @Override
    public boolean isArchived()
    {
        return archieved;
    }

    @Override
    public boolean isReleased()
    {
        return released;
    }

    public SummaryPanelVersion setOverdue(boolean overdue)
    {
        this.overdue = overdue;
        return this;
    }

    public SummaryPanelVersion setArchieved(boolean archieved)
    {
        this.archieved = archieved;
        return this;
    }

    public SummaryPanelVersion setReleased(boolean released)
    {
        this.released = released;
        return this;
    }

    public SummaryPanelVersion setName(String name)
    {
        this.name = name;
        return this;
    }

    public SummaryPanelVersion setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
        return this;
    }

    public SummaryPanelVersion setReleaseDate(String releaseDate)
    {
        DateFormat format = createDateFormat();
        try
        {
            this.releaseDate = format.parse(releaseDate);
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("Unparseable releaseDate '" + releaseDate + "'.", e);
        }
        return this;
    }

    public SummaryPanelVersion setReleaseDate(int day, int month, int year)
    {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.set(year, getMonth(month), day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        this.releaseDate = calendar.getTime();
        return this;
    }

    public SummaryPanelVersion setDescription(String description)
    {
        this.description = description;
        return this;
    }

    @Override
    public VersionOperationDropdown openOperationsCog()
    {
        throw new UnsupportedOperationException("I was too lazy to work out how to implement this here.");
    }

    @Override
    public TimedQuery<Boolean> hasFinishedVersionOperation()
    {
        throw new UnsupportedOperationException("I was too lazy to work out how to implement this here.");
    }

    private DateFormat createDateFormat()
    {
        return new SimpleDateFormat("dd/MMM/yy");
    }

    public static int getMonth(int month)
    {
        //This is a hack that I know works. If sun ever changes this I will eat your shorts.
        return month - 1;
    }

    @Override
    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
