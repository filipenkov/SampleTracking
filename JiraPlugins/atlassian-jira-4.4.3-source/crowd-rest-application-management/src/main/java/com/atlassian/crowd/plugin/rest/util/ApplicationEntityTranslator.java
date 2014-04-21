package com.atlassian.crowd.plugin.rest.util;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.application.DirectoryMapping;
import com.atlassian.crowd.model.application.ImmutableApplication;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.crowd.plugin.rest.entity.ApplicationEntity;
import com.atlassian.crowd.plugin.rest.entity.ApplicationEntityList;
import com.atlassian.crowd.plugin.rest.entity.AttributeEntity;
import com.atlassian.crowd.plugin.rest.entity.AttributeEntityList;
import com.atlassian.crowd.plugin.rest.entity.DirectoryMappingEntity;
import com.atlassian.crowd.plugin.rest.entity.DirectoryMappingEntityList;
import com.atlassian.crowd.plugin.rest.entity.PasswordEntity;
import com.atlassian.crowd.plugin.rest.entity.RemoteAddressEntity;
import com.atlassian.crowd.plugin.rest.entity.RemoteAddressEntitySet;
import com.atlassian.plugins.rest.common.Link;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.Validate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.crowd.plugin.rest.util.ApplicationEntityTranslator.PasswordMode.EXCLUDE_PASSWORD;

/**
 * Translates between application related REST entities and <tt>com.atlassian.crowd.model</tt> classes.
 *
 * @since 2.2
 */
public class ApplicationEntityTranslator
{
    public enum PasswordMode { EXCLUDE_PASSWORD, INCLUDE_PASSWORD }

    private ApplicationEntityTranslator()
    {
        // do not allow instantiation
    }

    /**
     * Translates from an ApplicationEntity to an Application with no directory mappings.
     *
     * @param applicationEntity ApplicationEntity to convert
     * @return Application if applicationEntity is not null, otherwise null
     */
    public static Application toApplicationWithNoDirectoryMappings(final ApplicationEntity applicationEntity)
    {
        if (applicationEntity == null)
        {
            return null;
        }

        final ApplicationType applicationType = toApplicationType(applicationEntity.getType());

        final Map<String, String> attributes = Maps.newHashMap();
        if (applicationEntity.getAttributes() != null)
        {
            for (AttributeEntity attributeEntity : applicationEntity.getAttributes())
            {
                attributes.put(attributeEntity.getName(), attributeEntity.getValue());
            }
        }

        return ImmutableApplication.builder(applicationEntity.getName(), applicationType)
                    .setDescription(applicationEntity.getDescription())
                    .setActive(getBoolean(applicationEntity.isActive()))
                    .setPasswordCredential(toPasswordCredential(applicationEntity.getPassword()))
                    .setRemoteAddresses(toRemoteAddresses(applicationEntity.getRemoteAddresses()))
                    .setLowercaseOutput(getBoolean(applicationEntity.isLowercaseOutput()))
                    .setAliasingEnabled(getBoolean(applicationEntity.isAliasingEnabled()))
                    .setAttributes(attributes)
                    .build();
    }

    /**
     * Translates from an Application to an ApplicationEntity.
     *
     * @param application Application to convert
     * @param link Link to the Application
     * @return ApplicationEntity if application is not null, otherwise null
     */
    public static ApplicationEntity toApplicationEntity(final Application application, final Link link)
    {
        return toApplicationEntity(application, link, EXCLUDE_PASSWORD);
    }

    /**
     * Translates from an Application to an ApplicationEntity.
     *
     * @param application Application to convert
     * @param link Link to the Application
     * @param passwordMode whether to include the password in the application entity
     * @return ApplicationEntity if application is not null, otherwise null
     */
    public static ApplicationEntity toApplicationEntity(final Application application, final Link link, final PasswordMode passwordMode)
    {
        if (application == null)
        {
            return null;
        }

        final URI applicationUri = link.getHref();
        final URI passwordUri = ApplicationLinkUriHelper.buildPasswordUri(applicationUri);
        final PasswordEntity passwordEntity;
        final PasswordCredential credential = application.getCredential();
        switch (passwordMode)
        {
            case INCLUDE_PASSWORD:
                passwordEntity = (credential == null ? null : new PasswordEntity(credential.getCredential(), Link.edit(passwordUri)));
                break;
            case EXCLUDE_PASSWORD:
                passwordEntity = null;
                break;
            default:
                throw new AssertionError("Should not reach here");
        }
        final ApplicationEntity applicationEntity = new ApplicationEntity(application.getId(), application.getName(), application.getType().name(), application.getDescription(), application.isActive(), passwordEntity, application.isLowerCaseOutput(), application.isAliasingEnabled(), link);

        final RemoteAddressEntitySet remoteAddressEntities = toRemoteAddressEntities(application.getRemoteAddresses(), applicationUri);
        applicationEntity.setRemoteAddresses(remoteAddressEntities);

        final AttributeEntityList attributeEntities = toAttributeEntities(application.getAttributes());
        applicationEntity.setAttributes(attributeEntities);

        final DirectoryMappingEntityList directoryMappingEntities = toDirectoryMappingEntities(application.getDirectoryMappings(), applicationUri);
        applicationEntity.setDirectoryMappings(directoryMappingEntities);

        return applicationEntity;
    }

    /**
     * Translates from a {@link PasswordEntity} to a {@link PasswordCredential}.
     *
     * @param passwordEntity password entity to translate
     * @return PasswordCredential or null if the input was null
     */
    public static PasswordCredential toPasswordCredential(final PasswordEntity passwordEntity)
    {
        if (passwordEntity == null)
        {
            return null;
        }

        return PasswordCredential.unencrypted(passwordEntity.getValue());
    }

    /**
     * Translates from a {@link DirectoryMappingEntityList} to a list of {@link DirectoryMapping}.
     *
     * @param directoryMappingEntities list of directory mapping entities
     * @param application the application being mapped
     * @param directoryManager the DirectoryManager
     * @return list of directory mappings or null if the input was null
     * @throws DirectoryNotFoundException if the directory being mapped could not be found
     */
    public static List<DirectoryMapping> toDirectoryMappings(final DirectoryMappingEntityList directoryMappingEntities, final Application application, final DirectoryManager directoryManager)
            throws DirectoryNotFoundException
    {
        if (directoryMappingEntities == null)
        {
            return Collections.emptyList();
        }

        ImmutableList.Builder<DirectoryMapping> builder = ImmutableList.builder();
        for (DirectoryMappingEntity directoryMappingEntity : directoryMappingEntities)
        {
            final Directory directory = directoryManager.findDirectoryById(directoryMappingEntity.getDirectoryId());
            builder.add(toDirectoryMapping(directoryMappingEntity, application, directory));
        }
        return builder.build();
    }

    /**
     * Translates from a {@link DirectoryMappingEntity} to a {@link DirectoryMapping}.
     *
     * @param directoryMappingEntity directory mapping entity
     * @param application the application to map
     * @param directory the directory to map
     * @return directory mapping
     */
    public static DirectoryMapping toDirectoryMapping(final DirectoryMappingEntity directoryMappingEntity, final Application application, final Directory directory)
    {
        if (directoryMappingEntity == null)
        {
            return null;
        }

        return new DirectoryMapping(application, directory, directoryMappingEntity.isAuthenticateAll(), toOperationTypes(directoryMappingEntity.getAllowedOperations()));
    }

    /**
     * Translates from a list of {@link DirectoryMapping} to a {@link DirectoryMappingEntityList}.
     *
     * @param directoryMappings list of directory mappings
     * @param applicationUri URI for the application resource
     * @return list of directory mapping entities or null if the input was null
     */
    public static DirectoryMappingEntityList toDirectoryMappingEntities(final List<DirectoryMapping> directoryMappings, final URI applicationUri)
    {
        if (directoryMappings == null)
        {
            return null;
        }

        final URI directoryMappingsUri = ApplicationLinkUriHelper.buildDirectoryMappingsUri(applicationUri);
        final List<DirectoryMappingEntity> directoryMappingEntities = Lists.newArrayListWithExpectedSize(directoryMappings.size());
        for (DirectoryMapping directoryMapping : directoryMappings)
        {
            DirectoryMappingEntity directoryMappingEntity = toDirectoryMappingEntity(directoryMapping, directoryMappingsUri);
            directoryMappingEntities.add(directoryMappingEntity);
        }
        return new DirectoryMappingEntityList(directoryMappingEntities, Link.self(directoryMappingsUri));
    }

    /**
     * Translates from a {@link DirectoryMapping} to a {@link DirectoryMappingEntity}.
     *
     * @param directoryMapping directory mapping
     * @param directoryMappingsUri URI to the application directory mappings resource
     * @return directory mapping entity
     */
    public static DirectoryMappingEntity toDirectoryMappingEntity(final DirectoryMapping directoryMapping, final URI directoryMappingsUri)
    {
        if (directoryMapping == null)
        {
            return null;
        }

        final URI directoryMappingUri = ApplicationLinkUriHelper.buildDirectoryMappingUri(directoryMappingsUri, directoryMapping.getDirectory().getId());
        return new DirectoryMappingEntity(directoryMapping.getDirectory().getId(), directoryMapping.isAllowAllToAuthenticate(), toOperationTypeStrings(directoryMapping.getAllowedOperations()), Link.self(directoryMappingUri));
    }

    /**
     * Translates from a {@link RemoteAddressEntitySet} to a set of {@link RemoteAddress}.
     *
     * @param remoteAddressEntities set of remote address entities
     * @return set of remote addresses, or null if the input was null
     */
    public static Set<RemoteAddress> toRemoteAddresses(final RemoteAddressEntitySet remoteAddressEntities)
    {
        if (remoteAddressEntities == null)
        {
            return Collections.emptySet();
        }

        ImmutableSet.Builder<RemoteAddress> builder = ImmutableSet.builder();
        for (RemoteAddressEntity remoteAddressEntity : remoteAddressEntities)
        {
            builder.add(toRemoteAddress(remoteAddressEntity));
        }
        return builder.build();
    }

    /**
     * Translates from a collection of {@link Application}s to a list of {@link ApplicationEntity}s.
     *
     * @param applications collection of applications to translate
     * @param baseUri base URI of the REST service
     * @return list of application entities
     */
    public static ApplicationEntityList toApplicationEntities(final Collection<Application> applications, final URI baseUri)
    {
        final URI applicationsUri = ApplicationLinkUriHelper.buildApplicationsUri(baseUri);
        List<ApplicationEntity> applicationEntities = Lists.newArrayListWithExpectedSize(applications.size());
        for (Application application : applications)
        {
            Link applicationLink = ApplicationLinkUriHelper.buildApplicationLink(baseUri, application.getId());
            applicationEntities.add(toApplicationEntity(application, applicationLink));
        }
        return new ApplicationEntityList(applicationEntities, Link.self(applicationsUri));
    }

    /**
     * Translates from a {@link RemoteAddressEntity} to a {@link RemoteAddress}.
     *
     * @param remoteAddressEntity remote address entity
     * @return RemoteAddress or null if the input was null
     */
    public static RemoteAddress toRemoteAddress(final RemoteAddressEntity remoteAddressEntity)
    {
        if (remoteAddressEntity == null)
        {
            return null;
        }

        return new RemoteAddress(remoteAddressEntity.getValue());
    }

    /**
     * Translate from a set of {@link RemoteAddress}es to {@link RemoteAddressEntitySet}.
     *
     * @param remoteAddresses Remote addresses
     * @param baseUri base URI of the application
     * @return RemoteAddressEntitySet
     */
    public static RemoteAddressEntitySet toRemoteAddressEntities(final Set<RemoteAddress> remoteAddresses, final URI baseUri)
    {
        if (remoteAddresses == null)
        {
            return null;
        }

        URI remoteAddressesUri = ApplicationLinkUriHelper.buildRemoteAddressesUri(baseUri);
        Link link = Link.self(remoteAddressesUri);
        Set<RemoteAddressEntity> remoteAddressEntities = Sets.newHashSetWithExpectedSize(remoteAddresses.size());
        for (RemoteAddress remoteAddress : remoteAddresses)
        {
            remoteAddressEntities.add(toRemoteAddressEntity(remoteAddress, remoteAddressesUri));
        }

        return new RemoteAddressEntitySet(remoteAddressEntities, link);
    }

    /**
     * Translate from a <code>RemoteAddress</code> to a <code>RemoteAddressEntity</code>.
     *
     * @param remoteAddress Remote address
     * @param baseUri base URI of the application remote addresses
     * @return RemoteAddressEntity
     */
    public static RemoteAddressEntity toRemoteAddressEntity(final RemoteAddress remoteAddress, final URI baseUri)
    {
        if (remoteAddress == null)
        {
            return null;
        }

        URI uri = ApplicationLinkUriHelper.buildRemoteAddressUri(baseUri, remoteAddress.getAddress());
        Link link = Link.self(uri);

        return new RemoteAddressEntity(remoteAddress.getAddress(), link);
    }

    /**
     * Translates from a set of operation type strings to a set of {@link OperationType}.
     *
     * @param types operation types in string
     * @return set of OperationTypes
     */
    public static Set<OperationType> toOperationTypes(Set<String> types)
    {
        Set<OperationType> operationTypes = Sets.newHashSetWithExpectedSize(types.size());
        for (String type : types)
        {
            operationTypes.add(toOperationType(type));
        }
        return EnumSet.copyOf(operationTypes);
    }

    /**
     * Translates from an operation type string to an {@link OperationType}.
     *
     * @param type operation type in string
     * @return OperationType
     */
    public static OperationType toOperationType(final String type)
    {
        return OperationType.valueOf(type.toUpperCase());
    }

    /**
     * Translates from a set of {@link OperationType}s to a set of operation type strings.
     *
     * @param types a set of OperationTypes
     * @return a set of operation type strings
     */
    public static Set<String> toOperationTypeStrings(final Set<OperationType> types)
    {
        final ImmutableSet.Builder<String> operationTypesBuilder = ImmutableSet.builder();
        for (OperationType type : types)
        {
            operationTypesBuilder.add(toOperationTypeString(type));
        }
        return operationTypesBuilder.build();
    }

    /**
     * Translates from an {@link OperationType} to an operation type string.
     *
     * @param type OperationType
     * @return operation type in string
     */
    public static String toOperationTypeString(final OperationType type)
    {
        return type.name();
    }

    /**
     * Translates from an application type string to an {@link ApplicationType}.
     *
     * @param type application type in string
     * @return ApplicationType
     */
    public static ApplicationType toApplicationType(final String type)
    {
        Validate.notNull(type, "Application type cannot be null");
        return ApplicationType.valueOf(type.toUpperCase());
    }

    /**
     * Translates attributes to {@link com.atlassian.crowd.plugin.rest.entity.AttributeEntityList}.
     *
     * @param attributes Attributes of an entity
     * @return AttributeEntityList if attributes is not null, otherwise null
     */
    public static AttributeEntityList toAttributeEntities(final Map<String, String> attributes)
    {
        if (attributes == null)
        {
            return null;
        }

        Set<String> keys = attributes.keySet();
        List<AttributeEntity> attributeList = new ArrayList<AttributeEntity>(keys.size());
        for (Map.Entry<String, String> entry : attributes.entrySet())
        {
            attributeList.add(new AttributeEntity(entry.getKey(), entry.getValue(), null));
        }
        return new AttributeEntityList(attributeList, null);
    }

    /**
     * Returns the boolean if not null, otherwise returns <tt>false</tt>.
     * @param b boolean value
     * @return <tt>b</tt> if not null, otherwise returns <tt>false</tt>
     */
    public static Boolean getBoolean(final Boolean b)
    {
        return b != null ? b : Boolean.FALSE;
    }
}
