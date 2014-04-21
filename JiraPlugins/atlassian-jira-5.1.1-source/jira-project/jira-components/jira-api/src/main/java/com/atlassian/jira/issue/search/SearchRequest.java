package com.atlassian.jira.issue.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.util.NotNull;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.order.OrderByImpl;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A class representing a Search Request. Sometimes also referred to as a "Saved search", "Saved Filter", "Search Filter".
 *
 * This class binds the {@link com.atlassian.query.Query}, which is used to perform the actual search, and
 * the saved information (such as name, description), and any permissions that may be associated with the saved search
 * together.
 */
@PublicApi
public class SearchRequest implements SharedEntity
{
    public static final TypeDescriptor<SearchRequest> ENTITY_TYPE = TypeDescriptor.Factory.get().create("SearchRequest");

    private Long id;
    private String name;
    private String description;
    private Long favouriteCount;
    private String ownerUserName;
    private Query query;
    private boolean modified = false;
    private boolean loaded = false;
    // Whether to use the Search requests specific columns
    private boolean useColumns;
    // Calculated properties
    private SharePermissions sharePermissions = SharePermissions.PRIVATE; // default to private

    /**
     * A no-arg constructor that will build a SearchRequest with an empty {@link com.atlassian.query.Query}, this
     * will be a search that will find all issues with the default system sorting . You can then use the setter methods
     * to set the attributes you wish this SearchRequest to contain.
     */
    public SearchRequest()
    {
        this.query = new QueryImpl(null, new OrderByImpl(), null);
        setModified(false);
        setUseColumns(true);
    }

    /**
     * Creates a SearchRequest with the specified {@link com.atlassian.query.Query} and no other attributes.
     * This can be used to create a programtic SearchRequest that can be used to perform a search but is not ready to
     * be saved.
     *
     * @param query provides the details of the search that will be performed with this SearchRequest.
     */
    public SearchRequest(final Query query)
    {
        this.query = query;
        setModified(false);
        setUseColumns(true);
    }

    /**
     * Used to create a SearchRequest that copies all the information from the old search request. This includes
     * the name, description, author, id, favCount and the SearchQuery.
     *
     * @param oldRequest defines all the attributes that this SearchRequest will contain.
     */
    public SearchRequest(final SearchRequest oldRequest)
    {
        this(oldRequest.getQuery(), oldRequest.getOwnerUserName(), oldRequest.getName(), oldRequest.getDescription(), oldRequest.getId(), oldRequest.getFavouriteCount());
        setUseColumns(oldRequest.useColumns());
        setPermissions(oldRequest.getPermissions());
        setModified(oldRequest.isModified());
    }

    /**
     * Build a SearchRequest with the provided attributes, this can be used if you want to create a SearchRequest that
     * can be persisted.
     *
     * @param query defines what this SearchRequest will search for.
     * @param ownerUserName the owner, user who initially create the request.
     * @param name the name associated with this SearchRequest, can be set even if this is not persistent yet.
     * @param description the description associated with this SearchRequest, can be set even if this is not persistent yet.
     * filter.
     */
    public SearchRequest(final Query query, final String ownerUserName, final String name, final String description)
    {
        this();
        this.ownerUserName = getUserKey(ownerUserName);
        this.name = name;
        this.description = description;
        this.query = query;
    }

    /**
     * Build a SearchRequest with the provided attributes.
     *
     * @param query defines what this SearchRequest will search for.
     * @param ownerUserName the owner, user who initially create the request.
     * @param name the name associated with this SearchRequest, can be set even if this is not persistent yet.
     * @param description the description associated with this SearchRequest, can be set even if this is not persistent yet.
     * @param id the persistent id of the SearchRequest, null if the SearchRequest is not persistent.
     * @param favouriteCount the number of users that have this filter as a favortie, will only be set if this is a persistent
     * filter.
     */
    public SearchRequest(final Query query, final String ownerUserName, final String name, final String description, final Long id, long favouriteCount)
    {
        this();
        this.ownerUserName = getUserKey(ownerUserName);
        this.name = name;
        this.description = description;
        this.query = query;
        this.id = id;
        this.favouriteCount = favouriteCount;
    }

    /**
     * Gets the SearchQuery that defines the search that will be performed for this SearchRequest.
     *
     * @return the SearchQuery that defines the search that will be performed for this SearchRequest, not null.
     */
    @NotNull
    public Query getQuery()
    {
        return query;
    }

    public void setQuery(final Query query)
    {
        notNull("query", query);
        setModified(true);
        this.query = query;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        setModified(true);
        this.name = name;
    }

    public Long getId()
    {
        return id;
    }

    public SharePermissions getPermissions()
    {
        return sharePermissions;
    }

    public void setPermissions(final SharePermissions sharePermissions)
    {
        notNull("permissions", sharePermissions);
        this.sharePermissions = sharePermissions;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        setModified(true);
        this.description = description;
    }

    protected void setFavouriteCount(final Long favouriteCount)
    {
        setModified(true);
        this.favouriteCount = favouriteCount;
    }

    public Long getFavouriteCount()
    {
        if (favouriteCount == null)
        {
            favouriteCount = 0L;
        }
        return favouriteCount;
    }

    public String getOwnerUserName()
    {
        return ownerUserName;
    }

    /**
     * Set the owner of the SearchRequest.
     *
     * @param ownerUserName the user name of the user who is the search requests owner.
     */
    public void setOwnerUserName(final String ownerUserName)
    {
        setModified(true);
        this.ownerUserName = getUserKey(ownerUserName);
    }

    public boolean isModified()
    {
        return modified;
    }

    public void setModified(final boolean modified)
    {
        this.modified = modified;
    }

    public boolean isLoaded()
    {
        return this.id != null;
    }

    /**
     * @return true if this SearchRequest should be displayed using the saved column layout, false otherwise
     */
    public boolean useColumns()
    {
        return useColumns;
    }

    public void setUseColumns(final boolean useColumns)
    {
        this.useColumns = useColumns;
    }

    // /CLOVER:OFF
    @Override
    public String toString()
    {
        final StringBuilder buff = new StringBuilder();
        buff.append("Search Request: name: ");
        buff.append(getName());
        buff.append("\n");

        if (query != null && !StringUtils.isBlank(query.toString()))
        {
            buff.append("query = ").append(query.toString());
        }

        return buff.toString();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final SearchRequest request = (SearchRequest) o;

        if (loaded != request.loaded)
        {
            return false;
        }
        if (description != null ? !description.equals(request.description) : request.description != null)
        {
            return false;
        }
        if (id != null ? !id.equals(request.id) : request.id != null)
        {
            return false;
        }
        if (name != null ? !name.equals(request.name) : request.name != null)
        {
            return false;
        }
        if (ownerUserName != null ? !ownerUserName.equals(request.ownerUserName) : request.ownerUserName != null)
        {
            return false;
        }
        if (!query.equals(request.query))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (ownerUserName != null ? ownerUserName.hashCode() : 0);
        result = 31 * result + query.hashCode();
        result = 31 * result + (loaded ? 1 : 0);
        return result;
    }

    @SuppressWarnings("unchecked")
    public final TypeDescriptor<SearchRequest> getEntityType()
    {
        return SearchRequest.ENTITY_TYPE;
    }

    @Nullable
    private String getUserKey(@Nullable String username) {
        return username == null ? null : IdentifierUtils.toLowerCase(username);
    }
}