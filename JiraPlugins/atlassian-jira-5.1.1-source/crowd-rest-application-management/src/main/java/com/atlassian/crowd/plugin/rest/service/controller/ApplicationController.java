package com.atlassian.crowd.plugin.rest.service.controller;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationManagerException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.manager.proxy.TrustedProxyManager;
import com.atlassian.crowd.manager.validation.XForwardedForUtil;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.DirectoryMapping;
import com.atlassian.crowd.model.application.ImmutableApplication;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.crowd.plugin.rest.entity.ApplicationEntity;
import com.atlassian.crowd.plugin.rest.entity.ApplicationEntityList;
import com.atlassian.crowd.plugin.rest.entity.RemoteAddressEntity;
import com.atlassian.crowd.plugin.rest.entity.RemoteAddressEntitySet;
import com.atlassian.crowd.plugin.rest.util.ApplicationEntityTranslator;
import com.atlassian.crowd.plugin.rest.util.ApplicationLinkUriHelper;
import com.atlassian.plugins.rest.common.Link;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

/**
 * Controller for the Application resource.
 *
 * @since 2.2
 */
public class ApplicationController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);
    
    private final ApplicationManager applicationManager;
    private final DirectoryManager directoryManager;
    private final TrustedProxyManager trustedProxyManager;

    public ApplicationController(final ApplicationManager applicationManager, final DirectoryManager directoryManager, final TrustedProxyManager trustedProxyManager)
    {
        this.applicationManager = applicationManager;
        this.directoryManager = directoryManager;
        this.trustedProxyManager = trustedProxyManager;
    }

    /**
     * Finds an application by name.
     *
     * @param name name of the application
     * @param baseUri base URI of the REST service
     * @return ApplicationEntity
     * @throws ApplicationNotFoundException if the application could not be found
     */
    public ApplicationEntity getApplicationByName(final String name, final URI baseUri) throws ApplicationNotFoundException
    {
        final Application application = applicationManager.findByName(name);
        final Link link = ApplicationLinkUriHelper.buildApplicationLink(baseUri, application.getId());
        return ApplicationEntityTranslator.toApplicationEntity(application, link);
    }

    /**
     * Finds an application by ID.
     *
     * @param id ID of the application
     * @param baseUri baseURI of the applications resource
     * @return ApplicationEntity
     * @throws ApplicationNotFoundException if the application could not be found
     */
    public ApplicationEntity getApplicationById(final long id, final URI baseUri) throws ApplicationNotFoundException
    {
        final Application application = applicationManager.findById(id);
        final Link link = ApplicationLinkUriHelper.buildApplicationLink(baseUri, application.getId());
        return ApplicationEntityTranslator.toApplicationEntity(application, link);
    }

    /**
     * Finds all applications.
     *
     * @param baseUri base URI of the REST service
     * @return ApplicationEntity
     * @throws ApplicationNotFoundException if the application could not be found
     */
    public ApplicationEntityList getAllApplications(final URI baseUri) throws ApplicationNotFoundException
    {
        final List<Application> applications = applicationManager.findAll();
        return ApplicationEntityTranslator.toApplicationEntities(applications, baseUri);
    }

    /**
     * Adds a new application with the request address.
     *
     * @param applicationEntity new application to add
     * @param request HTTP request
     * @param baseUri base URI of the REST service
     * @return link to the new application
     * @throws InvalidCredentialException if the given credentials are not valid
     * @throws DirectoryNotFoundException if the directory being mapped could not be found
     */
    public Link addApplicationWithRequestAddress(final ApplicationEntity applicationEntity, final HttpServletRequest request, final URI baseUri)
            throws InvalidCredentialException, DirectoryNotFoundException
    {
        Set<String> addresses = getRequestAddresses(request);
        if (!addresses.isEmpty())
        {
            if (applicationEntity.getRemoteAddresses() == null)
            {
                applicationEntity.setRemoteAddresses(new RemoteAddressEntitySet(Sets.<RemoteAddressEntity>newHashSet(), null));
            }

            for (String address : addresses)
            {
                applicationEntity.getRemoteAddresses().addRemoteAddress(new RemoteAddressEntity(address, null));
            }
        }
        return addApplication(applicationEntity, baseUri);
    }

    /**
     * Adds a new application.
     *
     * @param applicationEntity new application to add
     * @param baseUri base URI of the REST service
     * @return link to the new application
     * @throws InvalidCredentialException if the given credentials are not valid
     * @throws DirectoryNotFoundException if the directory being mapped could not be found
     */
    public Link addApplication(final ApplicationEntity applicationEntity, final URI baseUri)
            throws InvalidCredentialException, DirectoryNotFoundException
    {
        final Application applicationWithNoDirectoryMappings = ApplicationEntityTranslator.toApplicationWithNoDirectoryMappings(applicationEntity);
        final Application addedApplication = applicationManager.add(applicationWithNoDirectoryMappings);
        if (applicationEntity.getDirectoryMappings() != null && !applicationEntity.getDirectoryMappings().isEmpty())
        {
            final List<DirectoryMapping> directoryMappings = ApplicationEntityTranslator.toDirectoryMappings(applicationEntity.getDirectoryMappings(), addedApplication, directoryManager);
            final Application newApplication = ImmutableApplication.builder(addedApplication).setDirectoryMappings(directoryMappings).build();
            try
            {
                applicationManager.update(newApplication);
            }
            catch (ApplicationManagerException e)
            {
                throw new RuntimeException(e);
            }
            catch (ApplicationNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }
        return ApplicationLinkUriHelper.buildApplicationLink(baseUri, addedApplication.getId());
    }

    /**
     * Removes an application.
     *
     * @param applicationId ID of the application
     * @throws ApplicationManagerException if the remove operation is not permitted on the given application
     */
    public void removeApplication(final long applicationId)
            throws ApplicationManagerException
    {
        try
        {
            final Application application = applicationManager.findById(applicationId);
            applicationManager.remove(application);
        }
        catch (ApplicationNotFoundException e)
        {
            // do nothing
        }
    }

    /**
     * Updates an existing application.
     * 
     * @param applicationEntity application entity with the new details
     * @throws ApplicationNotFoundException if the application could not be found
     * @throws ApplicationManagerException if there was an error updating the application
     * @throws DirectoryNotFoundException if a directory referenced by a directory mapping could not be found
     */
    public void updateApplication(final ApplicationEntity applicationEntity)
            throws ApplicationNotFoundException, ApplicationManagerException, DirectoryNotFoundException
    {
        final Application applicationWithNoDirectoryMappings = ApplicationEntityTranslator.toApplicationWithNoDirectoryMappings(applicationEntity);
        final Application newApplication;
        if (applicationEntity.getDirectoryMappings() != null && !applicationEntity.getDirectoryMappings().isEmpty())
        {
            final List<DirectoryMapping> directoryMappings = ApplicationEntityTranslator.toDirectoryMappings(applicationEntity.getDirectoryMappings(), applicationWithNoDirectoryMappings, directoryManager);
            newApplication = ImmutableApplication.builder(applicationWithNoDirectoryMappings).setDirectoryMappings(directoryMappings).build();
        }
        else
        {
            newApplication = applicationWithNoDirectoryMappings;
        }

        applicationManager.update(newApplication);
    }

    /**
     * Adds a remote address to the list of allowed addresses for the application.
     *
     * @param applicationId ID of the application
     * @param remoteAddressEntity remote address entity to add
     * @throws ApplicationNotFoundException if the application could not be found
     */
    public void addRemoteAddress(final long applicationId, final RemoteAddressEntity remoteAddressEntity)
            throws ApplicationNotFoundException
    {
        final Application application = applicationManager.findById(applicationId);
        applicationManager.addRemoteAddress(application, ApplicationEntityTranslator.toRemoteAddress(remoteAddressEntity));
    }

    /**
     * Removes a remote address from the list of allowed addresses for the application.
     *
     * @param applicationId ID of the application
     * @param remoteAddress remote address to remove
     * @throws ApplicationNotFoundException if the application could not be found
     */
    public void removeRemoteAddress(final long applicationId, final String remoteAddress)
            throws ApplicationNotFoundException
    {
        final Application application = applicationManager.findById(applicationId);
        applicationManager.removeRemoteAddress(application, new RemoteAddress(remoteAddress));
    }

    /**
     * Retrieves the list of request addresses.
     *
     * @param request HTTP request
     * @return list of request addresses
     */
    public Set<String> getRequestAddresses(final HttpServletRequest request)
    {
        Set<String> addresses = Sets.newHashSet();
        addresses.add(request.getRemoteAddr());
        InetAddress clientAddress = XForwardedForUtil.getTrustedAddress(trustedProxyManager, request);
        addresses.add(clientAddress.getHostAddress());

        try
        {
            for (InetAddress address : InetAddress.getAllByName(clientAddress.getHostName()))
            {
                addresses.add(address.getHostAddress());
            }
        }
        catch (UnknownHostException e)
        {
            LOGGER.warn(e.getMessage());
        }
        return addresses;
    }
}
