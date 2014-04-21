package com.atlassian.jira.rest.v2.admin;

import com.atlassian.jira.bc.admin.ApplicationPropertiesService;
import com.atlassian.jira.bc.admin.ApplicationProperty;
import com.atlassian.jira.bc.admin.ApplicationPropertyMetadata;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.atlassian.validation.Validated;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

@Path ("application-properties")
@Produces ( { MediaType.APPLICATION_JSON })
@WebSudoRequired
public class ApplicationPropertiesResource
{
    private static final Logger log = Logger.getLogger(ApplicationPropertiesResource.class);

    private final JiraAuthenticationContext authenticationContext;
    private PermissionManager permissionManager;
    private ApplicationPropertiesService applicationPropertiesService;

    public ApplicationPropertiesResource(
            final JiraAuthenticationContext authenticationContext,
            final PermissionManager permissionManager,
            final ApplicationPropertiesService applicationPropertiesService)
    {
        this.authenticationContext = Assertions.notNull("authenticationContext", authenticationContext);
        this.permissionManager = Assertions.notNull("permissionManager", permissionManager);
        this.applicationPropertiesService = Assertions.notNull("applicationPropertiesService", applicationPropertiesService);
    }

    @GET
    public Response getProperty(@QueryParam ("key") String key)
    {
        if (!permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, authenticationContext.getLoggedInUser()))
        {
            log.debug("No permission to get properties (must be a system administrator)");
            return noPermissionResponse();
        }
        else
        {
            if (key != null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Got request for property value with key " + key);
                }
                ApplicationProperty property = applicationPropertiesService.getApplicationProperty(key);
                return Response.ok(new Property(property)).cacheControl(NO_CACHE).build();
            }
            else
            {
                log.debug("Got request for all editable property values");
                List<ApplicationProperty> editableApplicationProperties = applicationPropertiesService.getEditableApplicationProperties();
                List<Property> props = new ArrayList<Property>();
                for (ApplicationProperty editableApplicationProperty : editableApplicationProperties)
                {
                    props.add(new Property(editableApplicationProperty));
                }
                return Response.ok(props).cacheControl(NO_CACHE).build();
            }
        }
    }

    @PUT
    @Path("/{id}")
    public Response setPropertyViaRestfulTable(@PathParam ("id") final String key, final ApplicationPropertyBean applicationPropertyBean)
    {
        final String value = applicationPropertyBean.getValue();

        return setProperty(key, value);
    }

    public Response setProperty(final String key, final String value)
    {
        if (!permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, authenticationContext.getLoggedInUser()))
        {
            log.debug("No permission to set a property (must be a system administrator)");
            return noPermissionResponse();
        }
        else
        {
            if (key != null)
            {
                try
                {
                    Validated<ApplicationProperty> validatedApplicationProperty = applicationPropertiesService.setApplicationProperty(key, value);
                    if (validatedApplicationProperty.getResult().isValid())
                    {
                        return Response.ok(new Property(validatedApplicationProperty.getValue())).
                                cacheControl(NO_CACHE).build();
                    }
                    else
                    {
                        final SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
                        simpleErrorCollection.addError("value",
                                validatedApplicationProperty.getResult().getErrorMessage());

                        return Response.status(Response.Status.BAD_REQUEST).
                                entity(ErrorCollection.of(simpleErrorCollection)).
                                cacheControl(NO_CACHE).build();
                    }

                }
                catch (Exception e)
                {
                    log.info("Error setting Application Property", e);
                    return Response.serverError().cacheControl(NO_CACHE).build();
                }
            }
            else
            {
                return Response.status(Response.Status.BAD_REQUEST).
                        entity(ErrorCollection.of("No property key passed with the request!")).
                        cacheControl(NO_CACHE).build();
            }
        }
    }

    private Response noPermissionResponse()
    {
        return Response.status(Response.Status.FORBIDDEN).entity(ErrorCollection.of("No permission")).cacheControl(NO_CACHE).build();
    }

    @XmlRootElement
    public static class Property
    {
        @XmlElement
        private String id;
        @XmlElement
        private String key;
        @XmlElement
        private String value;
        @XmlElement
        private String name;
        @XmlElement
        private String desc;
        @XmlElement
        private String type;
        @XmlElement
        private String defaultValue;

        @XmlElement
        private Collection<String> allowedValues;

        public Property(ApplicationProperty applicationProperty)
        {
            ApplicationPropertyMetadata metadata = applicationProperty.getMetadata();
            this.id = metadata.getKey();
            this.key = metadata.getKey();
            this.value = applicationProperty.getCurrentValue();
            this.name = metadata.getName();
            this.desc = metadata.getDescription();
            if (!metadata.getDefaultValue().equals(value))
            {
                // save transport of values that are default by leaving it unspecified in that case
                this.defaultValue = metadata.getDefaultValue();
            }
            if(metadata.getType().equals("enum"))
            {
                this.allowedValues = metadata.getEnumerator().getEnumeration();
            }
        }

        public String getKey()
        {
            return key;
        }

        public String getValue()
        {
            return value;
        }

        @Override
        public String toString()
        {
            return "Property{"
                    + "key='" + key + '\''
                    + ", value='" + value + '\'' +
                    ", name='" + name + '\''
                    + ", desc='" + desc + '\''
                    + ", type='" + type + '\''
                    + ", defaultValue='" + defaultValue + '\''
                    + '}';
        }
    }
}
