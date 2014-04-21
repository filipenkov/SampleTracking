package com.atlassian.plugins.rest.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

/**
 * Status entity for those responses that don't have any other entity body.
 */
@XmlRootElement
public class Status
{
    /**
     * This is the plugin that exposes the REST api
     */
    @XmlElement
    private final Plugin plugin;

    /**
     * The HTTP reponse code, 200 for ok, 404 for not found, etc.
     * See <a href="http://www.w3.org/Protocols/rfc2616/rfc2616.html">RFC 2616</a> for more information on those response codes.
     */
    @XmlElement(name = "status-code")
    private final Integer code;

    /**
     * The plugin specific response code. This is used to differenciate possible error code for example.
     */
    @XmlElement(name = "sub-code")
    private final Integer subCode;

    /**
     * A humane readable message for the given status.
     */
    @XmlElement
    private final String message;

    /**
     * <p>the eTag.</p>
     * <p>See <a href="http://www.w3.org/Protocols/rfc2616/rfc2616.html">RFC 2616</a> for more information about ETag.
     */
    @XmlElement(name = "etag")
    private final String eTag;

    /**
     * Resource that have been updated during this request.
     */
    @XmlElementWrapper(name = "resources-created")
    @XmlElement(name = "link")
    private final Collection<Link> resourcesCreated;

    /**
     * Resource that have been updated during this request.
     */
    @XmlElementWrapper(name = "resources-updated")
    @XmlElement(name = "link")
    private final Collection<Link> resourcesUpdated;

    // For JAXB's usage
    private Status()
    {
        this.plugin = null;
        this.code = -1;
        this.subCode = -1;
        this.message = null;
        this.eTag = null;
        this.resourcesCreated = null;
        this.resourcesUpdated = null;
    }

    private Status(Plugin plugin, Integer code, Integer subCode, String message, String eTag, Collection<Link> resourcesCreated, Collection<Link> resourcesUpdated)
    {
        this.plugin = plugin;
        this.code = code;
        this.subCode = subCode;
        this.message = message;
        this.eTag = eTag;
        this.resourcesCreated = resourcesCreated;
        this.resourcesUpdated = resourcesUpdated;
    }

    public static StatusResponseBuilder ok()
    {
        return new StatusResponseBuilder(Response.Status.OK);
    }

    public static StatusResponseBuilder notFound()
    {
        return new StatusResponseBuilder(Response.Status.NOT_FOUND);
    }

    public static StatusResponseBuilder error()
    {
        // errors are not cached
        return new StatusResponseBuilder(Response.Status.INTERNAL_SERVER_ERROR).noCache().noStore();
    }

    public static StatusResponseBuilder badRequest()
    {
        // errors are not cached
        return new StatusResponseBuilder(Response.Status.BAD_REQUEST).noCache().noStore();
    }

    public static StatusResponseBuilder forbidden()
    {
        return new StatusResponseBuilder(Response.Status.FORBIDDEN);
    }

    public static StatusResponseBuilder unauthorized()
    {
        return new StatusResponseBuilder(Response.Status.UNAUTHORIZED);
    }

    public static StatusResponseBuilder created(Link link)
    {
        return new StatusResponseBuilder(Response.Status.CREATED).created(Preconditions.checkNotNull(link));
    }

    public Plugin getPlugin()
    {
        return plugin;
    }

    public int getCode()
    {
        return code;
    }

    public int getSubCode()
    {
        return subCode;
    }

    public String getMessage()
    {
        return message;
    }

    public String getETag()
    {
        return eTag;
    }

    public Collection<Link> getResourcesCreated()
    {
        return Collections.unmodifiableCollection(resourcesCreated);
    }

    public Collection<Link> getResourcesUpdated()
    {
        return Collections.unmodifiableCollection(resourcesUpdated);
    }

    @XmlRootElement
    public static class Plugin
    {
        @XmlAttribute
        private final String key;

        @XmlAttribute
        private final String version;

        // For JAXB's usage
        private Plugin()
        {
            this.key = null;
            this.version = null;
        }

        public Plugin(String key, String version)
        {
            this.key = Preconditions.checkNotNull(key);
            this.version = Preconditions.checkNotNull(version);
        }

        public String getKey()
        {
            return key;
        }

        public String getVersion()
        {
            return version;
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(3, 5).append(key).append(version).toHashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (obj == this)
            {
                return true;
            }
            if (obj.getClass() != getClass())
            {
                return false;
            }
            final Plugin plugin = (Plugin) obj;
            return new EqualsBuilder().append(key, plugin.key).append(version, plugin.version).isEquals();
        }
    }

    public static class StatusResponseBuilder
    {
        private final CacheControl cacheControl;
        private final Response.Status status;
        private String eTag;
        private Plugin plugin;
        private String message;
        private List<Link> created;
        private List<Link> updated;

        private StatusResponseBuilder(Response.Status status)
        {
            this(status, new CacheControl());
        }

        private StatusResponseBuilder(Response.Status status, CacheControl cacheControl)
        {
            this.status = Preconditions.checkNotNull(status);
            this.cacheControl = Preconditions.checkNotNull(cacheControl);
        }

        public StatusResponseBuilder plugin(String key, String version)
        {
            plugin = new Plugin(key, version);
            return this;
        }

        public StatusResponseBuilder message(String message)
        {
            this.message = message;
            return this;
        }

        public StatusResponseBuilder tag(String eTag)
        {
            this.eTag = eTag;
            return this;
        }

        public StatusResponseBuilder noCache()
        {
            cacheControl.setNoCache(true);
            return this;
        }

        public StatusResponseBuilder noStore()
        {
            cacheControl.setNoStore(true);
            return this;
        }

        public Status build()
        {
            return new Status(plugin, status.getStatusCode(), null, message, eTag, created, updated);
        }

        public Response response()
        {
            return responseBuilder().build();
        }

        public Response.ResponseBuilder responseBuilder()
        {
            final Response.ResponseBuilder builder =
                    Response.status(status).cacheControl(cacheControl).tag(eTag).entity(build()).type(APPLICATION_XML);

            final List<Link> c = getCreated();
            final List<Link> u = getUpdated();
            if (c.size() == 1 && u.isEmpty())
            {
                builder.location(c.get(0).getHref());
            }
            else if (u.size() == 1 && c.isEmpty())
            {
                builder.location(u.get(0).getHref());
            }
            return builder;
        }

        public StatusResponseBuilder created(final Link link)
        {
            getCreated().add(link);
            return this;
        }

        public StatusResponseBuilder updated(final Link link)
        {
            getUpdated().add(link);
            return this;
        }

        private List<Link> getCreated()
        {
            if (created == null)
            {
                created = Lists.newLinkedList();
            }
            return created;
        }

        private List<Link> getUpdated()
        {
            if (updated == null)
            {
                updated = Lists.newLinkedList();
            }
            return updated;
        }
    }

    /**
     * These are the media types that a Status can be represented as.
     */
    private static final List<Variant> POSSIBLE_VARIANTS = Variant.mediaTypes(
            MediaType.APPLICATION_XML_TYPE,
            MediaType.APPLICATION_JSON_TYPE).add().build();

    public static MediaType variantFor(Request request)
    {
        return request.selectVariant(POSSIBLE_VARIANTS).getMediaType();
    }
}
