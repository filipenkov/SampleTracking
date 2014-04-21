package com.atlassian.crowd.model.alias;

import com.atlassian.crowd.model.application.Application;

import java.io.Serializable;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

public class Alias implements Serializable
{
    private Long id;
    private Application application;

    /**
     * This represents the username for the underlying {@link com.atlassian.crowd.model.user.User}
     */
    private String name;

    /**
     * This represents a lowercase version username for the underlying {@link com.atlassian.crowd.model.user.User}
     */
    private String lowerName;

    /**
     * This represents the mixed-case alias for the underlying {@link com.atlassian.crowd.model.user.User}
     */
    private String alias;

    /**
     * This represents a lowercase version alias for the underlying {@link com.atlassian.crowd.model.user.User}
     */
    private String lowerAlias;

    private Alias()
    {
    }

    // generally used for imports
    public Alias(final Long id, final Application application, final String name, final String alias)
    {
        this(application, name, alias);
        this.id = id;
    }

    public Alias(Application application, final String name, final String alias)
    {
        this.application = application;
        setName(name);
        setAlias(alias);
    }

    public Long getId()
    {
        return id;
    }

    private void setId(final Long id)
    {
        this.id = id;
    }

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(final String alias)
    {
        this.alias = alias;
        this.lowerAlias = toLowerCase(alias);
    }

    public Application getApplication()
    {
        return application;
    }

    private void setApplication(final Application application)
    {
        this.application = application;
    }

    public String getName()
    {
        return name;
    }

    private void setName(final String name)
    {
        this.name = name;
        this.lowerName = toLowerCase(name);
    }

    public String getLowerAlias()
    {
        return lowerAlias;
    }

    private void setLowerAlias(final String lowerAlias)
    {
        this.lowerAlias = lowerAlias;
    }

    public String getLowerName()
    {
        return lowerName;
    }

    private void setLowerName(final String lowerName)
    {
        this.lowerName = lowerName;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Alias)) return false;

        Alias alias = (Alias) o;

        if (getApplication().getId() != null ? !getApplication().getId().equals(alias.getApplication().getId()) : alias.getApplication().getId() != null) return false;
        if (getLowerName() != null ? !getLowerName().equals(alias.getLowerName()) : alias.getLowerName() != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = getLowerName() != null ? getLowerName().hashCode() : 0;
        result = 31 * result + (getApplication().getId() != null ? getApplication().getId().hashCode() : 0);
        return result;
    }
}
