package com.atlassian.crowd.directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapperImpl;
import com.atlassian.crowd.directory.ldap.LdapTemplateWithClassLoaderWrapper;
import com.atlassian.crowd.directory.ldap.mapper.GroupContextMapper;
import com.atlassian.crowd.directory.ldap.mapper.UserContextMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.atlassian.crowd.directory.ldap.mapper.entity.LDAPGroupAttributesMapper;
import com.atlassian.crowd.directory.ldap.mapper.entity.LDAPUserAttributesMapper;
import com.atlassian.crowd.directory.ldap.name.Converter;
import com.atlassian.crowd.directory.ldap.name.Encoder;
import com.atlassian.crowd.directory.ldap.name.GenericConverter;
import com.atlassian.crowd.directory.ldap.name.GenericEncoder;
import com.atlassian.crowd.directory.ldap.name.SearchDN;
import com.atlassian.crowd.directory.ldap.util.DNStandardiser;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotSupportedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.LDAPDirectoryEntity;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.LDAPGroupWithAttributes;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.search.Entity;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.ldap.LDAPQuery;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.search.ldap.NullResultException;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.crowd.util.UserUtils;
import com.atlassian.event.api.EventPublisher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.ContextMapperCallbackHandler;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AggregateDirContextProcessor;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.transaction.compensating.manager.ContextSourceTransactionManager;
import org.springframework.ldap.transaction.compensating.manager.TransactionAwareContextSourceProxy;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static com.atlassian.crowd.search.util.SearchResultsUtil.constrainResults;
import static com.atlassian.crowd.search.util.SearchResultsUtil.convertEntitiesToNames;

/**
 * This class implements a remote LDAP directory using Spring LdapTemplate.
 * <p>
 * Warning: CWD-2494: When read timeout is enabled, operations can fail
 * randomly with "javax.naming.NamingException: LDAP response read timed out..."
 * error message without waiting for the timeout to pass.
 */
public abstract class SpringLDAPConnector implements LDAPDirectory
{
    public static final int DEFAULT_PAGE_SIZE = 999;

    private static final Logger logger = Logger.getLogger(SpringLDAPConnector.class);

    // configuration parameters (initialisation)
    private volatile long directoryId;
    protected volatile AttributeValuesHolder attributes;

    // derived configuration
    protected volatile LdapTemplateWithClassLoaderWrapper ldapTemplate;
    protected volatile ContextSource contextSource;
    protected volatile Converter nameConverter;
    protected volatile SearchDN searchDN;
    protected volatile LDAPPropertiesMapper ldapPropertiesMapper;
    protected volatile ContextSourceTransactionManager contextSourceTransactionManager;

    // spring injected dependencies
    protected final LDAPQueryTranslater ldapQueryTranslater;
    protected final EventPublisher eventPublisher;
    private final InstanceFactory instanceFactory;

    public SpringLDAPConnector(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory)
    {
        this.ldapQueryTranslater = ldapQueryTranslater;
        this.eventPublisher = eventPublisher;
        this.instanceFactory = instanceFactory;
    }

    public long getDirectoryId()
    {
        return this.directoryId;
    }

    /**
     * Called by the {@link com.atlassian.crowd.directory.loader.DirectoryInstanceLoader} after
     * constructing an InternalDirectory.
     *
     * @param id The unique <code>id</code> of the Directory stored in the database.
     */
    public void setDirectoryId(final long id)
    {
        this.directoryId = id;
    }

    /**
     * Called by the {@link com.atlassian.crowd.directory.loader.DirectoryInstanceLoader} after
     * constructing an InternalDirectory.
     *
     * @param attributes attributes map.
     */
    public void setAttributes(final Map<String, String> attributes)
    {
        this.attributes = new AttributeValuesHolder(attributes);

        // configure our LDAP helper - now getting this via Spring
        Object object = instanceFactory.getInstance(LDAPPropertiesMapperImpl.class);
        ldapPropertiesMapper = (LDAPPropertiesMapper) object;
        ldapPropertiesMapper.setAttributes(attributes);

        // create a spring connection context object
        contextSource = createContextSource(ldapPropertiesMapper, getBaseEnvironmentProperties());
        contextSourceTransactionManager = new ContextSourceTransactionManager();
        contextSourceTransactionManager.setContextSource(contextSource);

        ldapTemplate = new LdapTemplateWithClassLoaderWrapper(new LdapTemplate(contextSource));

        // Ignore PartialResultExceptions when not following referrals
        if (!ldapPropertiesMapper.isReferral())
        {
            ldapTemplate.setIgnorePartialResultException(true);
        }

        nameConverter = new GenericConverter(getEncoder());
        searchDN = new SearchDN(ldapPropertiesMapper, nameConverter);
    }

    private static ContextSource createContextSource(LDAPPropertiesMapper ldapPropertiesMapper, Map<String, String> envProperties)
    {
        LdapContextSource targetContextSource = new LdapContextSource();

        targetContextSource.setUrl(ldapPropertiesMapper.getConnectionURL());
        targetContextSource.setUserDn(ldapPropertiesMapper.getUsername());
        targetContextSource.setPassword(ldapPropertiesMapper.getPassword());

        // let spring know of our connection attributes
        targetContextSource.setBaseEnvironmentProperties(envProperties);

        // create a pool for when doing multiple calls.
        targetContextSource.setPooled(true);

        try
        {
            // we need to tell the context source to configure up our ldap server
            targetContextSource.afterPropertiesSet();
        } catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        return new TransactionAwareContextSourceProxy(targetContextSource);
    }

    /**
     * Exposed so that delegated directories can get a handle on the underlying LDAP context.
     *
     * @return ContextSource.
     */
    public ContextSource getContextSource()
    {
        return contextSource;
    }

    public LDAPPropertiesMapper getLdapPropertiesMapper()
    {
        return ldapPropertiesMapper;
    }

    public Set<String> getValues(final String name)
    {
        return attributes.getValues(name);
    }

    public String getValue(final String name)
    {
        return attributes.getValue(name);
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    public long getAttributeAsLong(final String name, long defaultValue)
    {
        return attributes.getAttributeAsLong(name, defaultValue);
    }

    public boolean getAttributeAsBoolean(final String name, boolean defaultValue)
    {
        return attributes.getAttributeAsBoolean(name, defaultValue);
    }

    public Set<String> getKeys()
    {
        return attributes.getKeys();
    }

    public SearchDN getSearchDN()
    {
        return searchDN;
    }

    protected SearchControls getSubTreeSearchControl()
    {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        // TODO: potential performance benefit by setting this to false so that only the name and class of the object is returned instead of the whole object
        searchControls.setReturningObjFlag(true);

        return searchControls;
    }

    /**
     * Returns an Encoder that escapes LDAP special characters for use in object names and in DNs.
     *
     * @return an Encoder that escapes LDAP special characters for use in object names and in DNs.
     */
    protected Encoder getEncoder()
    {
        return new GenericEncoder();
    }

    /**
     * Returns the properties used to set up the Ldap ContextSource.
     *
     * @return the properties used to set up the Ldap ContextSource.
     */
    protected Map<String, String> getBaseEnvironmentProperties()
    {
        return ldapPropertiesMapper.getEnvironment();
    }

    /**
     * Performs a paged results search on an LDAP directory server searching using the LDAP paged results control
     * option to fetch results in chunks rather than all at once.
     *
     * @param baseDN              The DN to beging the search from.
     * @param filter              The search filter.
     * @param contextMapper       Maps from LDAP search results into objects such as <code>Group</code>s.
     * @param searchControls      The LDAP search scope type.
     * @param ldapRequestControls Any LDAP request controls (set to <code>null</code> if you do not need <b>additional</b> request controls for the search).
     * @param maxResults          maximum number of results to return. Set to <code>-1</code> if no result limiting is desired (WARNING: doing so is obviously a hazard).
     * @return The search results.
     * @throws OperationFailedException Search failed due to a communication error to the remote directory
     */
    protected CollectingNameClassPairCallbackHandler pageSearchResults(Name baseDN, String filter, ContextMapper contextMapper, SearchControls searchControls, DirContextProcessor ldapRequestControls, int maxResults)
            throws OperationFailedException
    {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
        try
        {
            // Use a transaction in order to use the same connection for multiple LDAP requests
            TransactionStatus status = contextSourceTransactionManager.getTransaction(transactionDefinition);
            try
            {
                int pagingSize = ldapPropertiesMapper.getPagedResultsSize();

                PagedResultsDirContextProcessor pagedResultsControl = new PagedResultsDirContextProcessor(pagingSize);

                if (logger.isDebugEnabled())
                {
                    logger.debug("Paged results are enabled with a paging size of: " + pagingSize);
                }

                // specify that we are using an object that will callback to the server doing chunks of processing users
                CollectingNameClassPairCallbackHandler handler = new ContextMapperCallbackHandler(contextMapper);

                // the cookie to use when maintaining our index location
                byte[] cookie = null;

                do
                {
                    // setup the LDAP request control(s)
                    AggregateDirContextProcessor aggregateDirContextProcessor = new AggregateDirContextProcessor();
                    aggregateDirContextProcessor.addDirContextProcessor(pagedResultsControl);
                    if (ldapRequestControls != null)
                    {
                        aggregateDirContextProcessor.addDirContextProcessor(ldapRequestControls);
                    }

                    // perform the search
                    ldapTemplate.search(baseDN, filter, searchControls, handler, aggregateDirContextProcessor);

                    if (logger.isDebugEnabled())
                    {
                        int resultSize = pagedResultsControl.getPageSize();

                        logger.debug("Iterating a search result size of: " + resultSize);
                    }

                    // get the marker for the paged results list
                    pagedResultsControl = new PagedResultsDirContextProcessor(pagingSize, pagedResultsControl.getCookie());

                    // set our index pointer to the item we are currently at on the list
                    // if it is not null
                    if (pagedResultsControl.getCookie() != null)
                    {
                        cookie = pagedResultsControl.getCookie().getCookie();
                    }
                }
                // while there are more elements keep looping AND if we don't have maxResults
                while ((cookie != null) && (cookie.length != 0) && (handler.getList().size() < maxResults || maxResults == EntityQuery.ALL_RESULTS));

                // return the results
                return handler;
            } finally
            {
                contextSourceTransactionManager.commit(status);
            }
        } catch (TransactionException e)
        {
            throw new OperationFailedException(e);
        } catch (NamingException e)
        {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Executes a search with paging if paged results is supported.
     *
     * @param baseDN        base DN of search.
     * @param filter        encoded LDAP search filter.
     * @param contextMapper directory context to object mapper.
     * @param startIndex    index to start at. Set to <code>0</code> to start from the first result.
     * @param maxResults    maximum number of results to return. Set to <code>-1</code> if no result limiting is desired (WARNING: doing so is obviously a hazard).
     * @return list of entities of type corresponding to the contextMapper's output.
     * @throws OperationFailedException a Communication error occurred when trying to talk to a remote directory
     */
    protected List searchEntities(final Name baseDN, final String filter, final ContextMapper contextMapper, final int startIndex, final int maxResults)
            throws OperationFailedException
    {
        return searchEntitiesWithRequestControls(baseDN, filter, contextMapper, getSubTreeSearchControl(), null, startIndex, maxResults);
    }

    @SuppressWarnings("unchecked")
    protected List searchEntitiesWithRequestControls(final Name baseDN, final String filter, final ContextMapper contextMapper, final SearchControls searchControls, final DirContextProcessor ldapRequestControls, final int startIndex, final int maxResults)
            throws OperationFailedException
    {
        List results;

        // Set the time limit for the search (if not specified in properties, will be default of 0 - no time limit)
        searchControls.setTimeLimit(ldapPropertiesMapper.getSearchTimeLimit());

        // if the directory supports paged results, use them
        if (ldapPropertiesMapper.isPagedResultsControl())
        {
            CollectingNameClassPairCallbackHandler handler = pageSearchResults(baseDN, filter, contextMapper, searchControls, ldapRequestControls, startIndex + maxResults);

            results = handler.getList();
        } else
        {
            try
            {
                // otherwise fetch all the results at once
                if (ldapRequestControls != null)
                {
                    results = ldapTemplate.search(baseDN, filter, searchControls, contextMapper, ldapRequestControls);
                } else
                {
                    results = ldapTemplate.search(baseDN, filter, searchControls, contextMapper);
                }
            } catch (NamingException ex)
            {
                throw new OperationFailedException(ex);
            }
        }

        if (contextMapper instanceof GroupContextMapper)
        {
            // Need to postprocessGroups here (for Microsoft AD) in case a group has a large number of members
            // See: https://studio.atlassian.com/browse/EMBCWD-622
            results = postprocessGroups(results);
        }

        return constrainResults(results, startIndex, maxResults);
    }

    private String spaceIfBlank(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return " ";
        } else
        {
            return value;
        }
    }

    /*
     * This method is not suitable for generic attribute updates as it only supports single
     * attribute-value mappings (ie. suitable for field values as opposed to custom attributes).
     */
    private ModificationItem createModificationItem(String directoryAttributeName, String oldValue, String newValue)
    {
        // do some manual dirty checking
        ModificationItem modificationItem;

        if (oldValue == null && newValue == null)
        {
            // no modification
            modificationItem = null;
        } else if (oldValue == null)
        {
            // need to create the item
            modificationItem = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(directoryAttributeName, spaceIfBlank(newValue)));
        }
        /*
        // NOTE: WE CURRENTLY HAVE NO NEED TO REMOVE ITEMS BECAUSE ALL THAT EXIST, EXIST PERMANENTLY OR GET REPLACED BY A SPACE
        else if (newValue == null)
        {
            // need to remove the item
            modificationItem = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attributeName));
        }
        */
        else if (!oldValue.equals(newValue))
        {
            // need to update the item
            modificationItem = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(directoryAttributeName, spaceIfBlank(newValue)));
        } else
        {
            // attribute hasn't changed
            modificationItem = null;
        }

        return modificationItem;
    }

    /////////////////// CONTEXT MAPPERS ///////////////////

    /**
     * Returns a ContextMapper that can transform a Context into a User.
     *
     * @return a ContextMapper that can transform a Context into a User.
     */
    public ContextMapper getUserContextMapper()
    {
        return new UserContextMapper(this.getDirectoryId(), ldapPropertiesMapper, getCustomUserAttributeMappers());
    }

    /**
     * @return a collection of custom attribbute mappers. By default just return an empty list.
     */
    protected List<AttributeMapper> getCustomUserAttributeMappers()
    {
        return new ArrayList<AttributeMapper>();
    }

    /**
     * Returns a ContextMapper ready to translate LDAP objects into Groups and fetches all member objects.
     *
     * @param groupType the GroupType
     * @return a ContextMapper ready to translate LDAP objects into Groups and fetches all member objects
     */
    public ContextMapper getGroupContextMapper(GroupType groupType)
    {
        return new GroupContextMapper(getDirectoryId(), groupType, ldapPropertiesMapper, getCustomGroupAttributeMappers());
    }

    /**
     * As a minimum, this SHOULD provide an attribute mapper that maps the group members attribute (if available).
     *
     * @return collection of custom attribute mappers (cannot be <tt>null</tt> but can be an empty list).
     */
    protected List<AttributeMapper> getCustomGroupAttributeMappers()
    {
        return new ArrayList<AttributeMapper>();
    }

    /////////////////// USER OPERATIONS ///////////////////

    public LDAPUserWithAttributes findUserByName(String name) throws UserNotFoundException, OperationFailedException
    {
        Validate.notNull(name, "name argument cannot be null");
        // equivalent call to findUserWithAttributes
        return findUserWithAttributesByName(name);

        // TODO: potential performance benefit: request only the first-class attributes
    }

    public LDAPUserWithAttributes findUserWithAttributesByName(final String name) throws UserNotFoundException, OperationFailedException
    {
        Validate.notNull(name, "name argument cannot be null");

        EntityQuery query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(name)).returningAtMost(1);

        final List<LDAPUserWithAttributes> users;

        users = searchUserObjects(query);

        if (users.isEmpty())
        {
            throw new UserNotFoundException(name);
        }

        // return the first object
        return users.get(0);
    }

    @SuppressWarnings("unchecked")
    protected List<LDAPUserWithAttributes> searchUserObjects(EntityQuery query) throws OperationFailedException, IllegalArgumentException
    {
        if (query == null)
        {
            throw new IllegalArgumentException("user search can only evaluate non-null EntityQueries for Entity.USER");
        }

        if (query.getEntityDescriptor().getEntityType() != Entity.USER)
        {
            throw new IllegalArgumentException("user search can only evaluate EntityQueries for Entity.USER");
        }

        Name baseDN = searchDN.getUser();

        List<LDAPUserWithAttributes> results;

        try
        {
            LDAPQuery ldapQuery = ldapQueryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
            String filter = ldapQuery.encode();
            logger.debug("Performing user search: baseDN = " + baseDN + " - filter = " + filter);

            results = searchEntities(baseDN, filter, getUserContextMapper(), query.getStartIndex(), query.getMaxResults());

        } catch (NullResultException e)
        {
            results = Collections.emptyList();
        }

        return results;
    }

    public void removeUser(String name) throws UserNotFoundException, OperationFailedException
    {
        Validate.notEmpty(name, "name argument cannot be null or empty");

        LDAPUserWithAttributes user = findUserByName(name);

        // remove the dn
        try
        {
            ldapTemplate.unbind(asLdapUserName(user.getDn(), name));
        } catch (NamingException ex)
        {
            throw new OperationFailedException(ex);
        }
    }

    public void updateUserCredential(String name, PasswordCredential credential) throws InvalidCredentialException, UserNotFoundException, OperationFailedException
    {
        Validate.notEmpty(name, "name argument cannot be null or empty");
        Validate.notNull(credential, "credential argument cannot be null");

        ModificationItem[] mods = new ModificationItem[1];

        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapPropertiesMapper.getUserPasswordAttribute(), encodePassword(credential.getCredential())));

        Name userDn = asLdapUserName(findUserByName(name).getDn(), name);

        try
        {
            ldapTemplate.modifyAttributes(userDn, mods);
        } catch (NamingException ex)
        {
            throw new OperationFailedException(ex);
        }
    }

    public User renameUser(final String oldName, final String newName) throws UserNotFoundException, InvalidUserException, OperationFailedException
    {
        // TODO: support this, aka CWD-1466
        throw new OperationNotSupportedException("User renaming is not yet supported for LDAP directories");
    }

    public void storeUserAttributes(final String username, final Map<String, Set<String>> attributes) throws UserNotFoundException, OperationFailedException
    {
        throw new OperationNotSupportedException("Custom user attributes are not yet supported for LDAP directories");
    }

    public void removeUserAttributes(final String username, final String attributeName) throws UserNotFoundException, OperationFailedException
    {
        throw new OperationNotSupportedException("Custom user attributes are not yet supported for LDAP directories");
    }

    /**
     * Translates the <code>User</code> into LDAP attributes, in preparation for creating a new user.
     *
     * @param user       The user object to translate into LDAP attributes
     * @param credential raw password.
     * @return An Attributes object populated with directory-specific information.
     * @throws InvalidCredentialException The password, if supplied, was invalid in some manner.
     * @throws NamingException            If the <code>User</code> could not be translated to an <code>Attributes</code>
     */
    protected Attributes getNewUserAttributes(User user, PasswordCredential credential) throws InvalidCredentialException, NamingException
    {
        // get the basic attributes
        LDAPUserAttributesMapper mapper = new LDAPUserAttributesMapper(getDirectoryId(), ldapPropertiesMapper);
        Attributes attributes = mapper.mapAttributesFromUser(user);

        if (credential != null && credential.getCredential() != null)
        {
            String unencodedPassword = credential.getCredential();
            attributes.put(ldapPropertiesMapper.getUserPasswordAttribute(), encodePassword(unencodedPassword));
        }

        // add directory-specific attributes to the user
        getNewUserDirectorySpecificAttributes(user, attributes);

        return attributes;
    }

    /**
     * Populates attributes object with directory-specific attributes.
     * <p/>
     * Overrider of this method can take advantage of the default group attributes mapping logic
     * in {#getNewUserAttributes(User)}.
     * <p/>
     * Note that the attribute values supplied here will be used raw. This entails that overrider is responsible
     * for supplying values in a format supported by the directory.
     * In some directory implementations, for example, a blank string ("") is considered illegal. Overrider thus
     * would have to make sure the method does not generate a value as such.
     *
     * @param user       (potential) source of information that needs to be added.
     * @param attributes attributes to add directory-specific information to.
     */
    protected void getNewUserDirectorySpecificAttributes(User user, Attributes attributes)
    {
        // default is a no-op
    }

    /**
     * Adds a user to LDAP.
     * <p/>
     * If the displayName on the user is blank, then the
     *
     * @param user       template of the user to add.
     * @param credential password.
     * @return LDAP user retrieved from LDAP after successfully adding the user to LDAP.
     * @throws InvalidUserException       if the user to create was deemed invalid by the LDAP server or already exists.
     * @throws InvalidCredentialException if the password credential was deemed invalid by the password encoder.
     * @throws OperationFailedException   if we were unable to add the user to LDAP.
     */
    public LDAPUserWithAttributes addUser(UserTemplate user, PasswordCredential credential)
            throws InvalidUserException, InvalidCredentialException, OperationFailedException
    {
        Validate.notNull(user, "user cannot be null");
        Validate.notNull(user.getName(), "user.name cannot be null");
        try
        {
            // build the DN for the new user
            Name dn = nameConverter.getName(ldapPropertiesMapper.getUserNameRdnAttribute(), user.getName(), searchDN.getUser());

            Attributes attrs = getNewUserAttributes(user, credential);

            // create the user
            ldapTemplate.bind(dn, null, attrs);

            return findEntityByDN(getStandardisedDN(dn), LDAPUserWithAttributes.class);
        } catch (NamingException e)
        {
            throw new InvalidUserException(user, e.getMessage(), e);
        } catch (InvalidNameException e)
        {
            throw new InvalidUserException(user, e.getMessage(), e);
        } catch (GroupNotFoundException e)
        {
            throw new AssertionError("Should not throw a GroupNotFoundException");
        } catch (UserNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    /**
     * A default install of many directory servers (inc. Sun DSEE 6.2 and Apache DS 1.0.2) requires the following to be
     * set before user creation is allowed:
     * objectClass -> inetorgperson
     * cn          ->
     * sn          ->
     * If a call is being made from an external system (eg JIRA), the user is created with the bare minimum of
     * attributes, then later updated. We need to make sure to add <code>sn</code> if it's not present in the
     * information provided.
     *
     * @param attrs          The LDAP user attributes to be checked and potentially updated.
     * @param defaultSnValue default lastname/surname value
     */
    protected void addDefaultSnToUserAttributes(Attributes attrs, String defaultSnValue)
    {
        addDefaultValueToUserAttributesForAttribute(ldapPropertiesMapper.getUserLastNameAttribute(), attrs, defaultSnValue);
    }

    protected void addDefaultValueToUserAttributesForAttribute(String attributeName, Attributes attrs, String defaultValue)
    {
        if (attrs == null)
        {
            return;
        }

        Attribute userAttribute = attrs.get(attributeName);
        if (userAttribute == null)
        {
            attrs.put(new BasicAttribute(attributeName, defaultValue));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends LDAPDirectoryEntity> T findEntityByDN(String dn, Class<T> entityClass)
            throws UserNotFoundException, GroupNotFoundException, OperationFailedException
    {
        dn = standardiseDN(dn);
        if (User.class.isAssignableFrom(entityClass))
        {
            return findEntityByDN(dn, getStandardisedDN(searchDN.getUser()), ldapPropertiesMapper.getUserFilter(), getUserContextMapper(), entityClass);
        } else if (Group.class.isAssignableFrom(entityClass))
        {
            // are we looking for a group of type GROUP or LEGACY_ROLE?
            String groupBaseDN = getStandardisedDN(searchDN.getGroup());
            String roleBaseDN = getStandardisedDN(searchDN.getRole());
            LDAPDirectoryEntity groupEntity;
            if (dn.endsWith(groupBaseDN))
            {
                groupEntity = findEntityByDN(dn, groupBaseDN, ldapPropertiesMapper.getGroupFilter(), getGroupContextMapper(GroupType.GROUP), entityClass);
            } else if (dn.endsWith(roleBaseDN))
            {
                if (ldapPropertiesMapper.isRolesDisabled())
                {
                    throw new GroupNotFoundException("DN: " + dn);
                }
                groupEntity = findEntityByDN(dn, roleBaseDN, ldapPropertiesMapper.getRoleFilter(), getGroupContextMapper(GroupType.LEGACY_ROLE), entityClass);
            } else
            {
                // default to group (but this will throw an ONFE eventually anyway)
                groupEntity = findEntityByDN(dn, groupBaseDN, ldapPropertiesMapper.getGroupFilter(), getGroupContextMapper(GroupType.GROUP), entityClass);
            }
            return (T) postprocessGroups(Collections.singletonList((LDAPGroupWithAttributes) groupEntity)).get(0);
        } else
        {
            throw new IllegalArgumentException("Class " + entityClass.getCanonicalName() + " is not assignable from " + User.class.getCanonicalName() + " or " + Group.class.getCanonicalName());
        }
    }

    protected <T extends LDAPDirectoryEntity> RuntimeException typedEntityNotFoundException(String name, Class<T> entityClass)
            throws UserNotFoundException, GroupNotFoundException
    {
        if (User.class.isAssignableFrom(entityClass))
        {
            throw new UserNotFoundException(name);
        } else if (Group.class.isAssignableFrom(entityClass))
        {
            throw new GroupNotFoundException(name);
        } else
        {
            throw new IllegalArgumentException("Class " + entityClass.getCanonicalName() + " is not assignable from " + User.class.getCanonicalName() + " or " + Group.class.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends LDAPDirectoryEntity> T findEntityByDN(String dn, String baseDN, String filter, ContextMapper contextMapper, Class<T> entityClass)
            throws UserNotFoundException, GroupNotFoundException, OperationFailedException
    {
        if (StringUtils.isBlank(dn))
        {
            throw typedEntityNotFoundException("Blank DN", entityClass);
        }

        if (dn.endsWith(baseDN))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Executing search at DN: <" + dn + "> with filter: <" + filter + ">");
            }

            List<T> entities = null;

            try
            {
                SearchControls searchControls = new SearchControls();
                searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
                searchControls.setTimeLimit(ldapPropertiesMapper.getSearchTimeLimit());
                searchControls.setReturningObjFlag(true); // because a contextmapper is used, we need to return an object
                // this is totally messed up..i wasted a whole day trying to work out why passing a String DN doesn't work - just FORGET ABOUT IT and use javax.naming.Name
                entities = ldapTemplate.search(asLdapName(dn, "DN: " + dn, entityClass), filter, searchControls, contextMapper);
            } catch (NameNotFoundException e)
            {
                // SpringLDAP likes to chuck this exception, essentially an ONFE

                if (logger.isDebugEnabled())
                {
                    logger.debug(e);
                }

                // entities == null so we'll get a nice ONFE just below
            } catch (NamingException ex)
            {
                throw new OperationFailedException(ex);
            }

            if (entities != null && !entities.isEmpty())
            {
                return entities.get(0);
            } else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Entity DN <" + dn + "> does not exist or does not match filter <" + filter + ">");
                }
                throw typedEntityNotFoundException("DN: " + dn, entityClass);
            }
        } else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Entity DN <" + dn + "> is outside the entity base DN subtree scope <" + baseDN + ">");
            }
            throw typedEntityNotFoundException("DN: " + dn, entityClass);
        }
    }

    public User updateUser(UserTemplate user) throws UserNotFoundException, OperationFailedException
    {
        Validate.notNull(user, "user cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(user.getName()), "user cannot have blank user name");

        // pre-populate user names (first name, last name, display name may need to be constructed)
        User populatedUser = UserUtils.populateNames(user);

        // Get the current user, if this user is not found a ONFE will be thrown
        LDAPUserWithAttributes currentUser = findUserByName(user.getName());

        // get ldap helper object type
        String ldapUserObjectType = ldapPropertiesMapper.getUserObjectClass();

        List<ModificationItem> modificationItems = new ArrayList<ModificationItem>();

        // TODO: This should probably be removed, but it is really redundant
        // if ldap object type is of inetOrgPerson or AD User object, we can update 'certain' attributes
        if ("inetOrgPerson".equalsIgnoreCase(ldapUserObjectType) || "user".equalsIgnoreCase(ldapUserObjectType))
        {
            // sn
            ModificationItem snMod = createModificationItem(ldapPropertiesMapper.getUserLastNameAttribute(), currentUser.getLastName(), spaceIfBlank(populatedUser.getLastName()));
            if (snMod != null)
            {
                modificationItems.add(snMod);
            }

            // mail
            ModificationItem mailMod = createModificationItem(ldapPropertiesMapper.getUserEmailAttribute(), currentUser.getEmailAddress(), spaceIfBlank(populatedUser.getEmailAddress()));
            if (mailMod != null)
            {
                modificationItems.add(mailMod);
            }

            // giveName
            ModificationItem givenNameMod = createModificationItem(ldapPropertiesMapper.getUserFirstNameAttribute(), currentUser.getFirstName(), spaceIfBlank(populatedUser.getFirstName()));
            if (givenNameMod != null)
            {
                modificationItems.add(givenNameMod);
            }

            // displayName
            ModificationItem displayNameMod = createModificationItem(ldapPropertiesMapper.getUserDisplayNameAttribute(), currentUser.getDisplayName(), spaceIfBlank(populatedUser.getDisplayName()));
            if (displayNameMod != null)
            {
                modificationItems.add(displayNameMod);
            }
        }

        // Perform the update if there are modification items
        if (!modificationItems.isEmpty())
        {
            try
            {
                ldapTemplate.modifyAttributes(asLdapUserName(currentUser.getDn(), user.getName()), modificationItems.toArray(new ModificationItem[modificationItems.size()]));
            } catch (NamingException ex)
            {
                throw new OperationFailedException(ex);
            }
        }

        // Return the user 'fresh' from the LDAP directory
        try
        {
            return findEntityByDN(currentUser.getDn(), LDAPUserWithAttributes.class);
        } catch (GroupNotFoundException e)
        {
            throw new AssertionError("Should not throw a GroupNotFoundException");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> searchUsers(EntityQuery<T> query) throws OperationFailedException
    {
        List<LDAPUserWithAttributes> users = searchUserObjects(query);

        if (query.getReturnType() == String.class) // as names
        {
            return (List<T>) convertEntitiesToNames(users);
        } else
        {
            return (List<T>) users;
        }
    }

    public User authenticate(String name, PasswordCredential credential)
            throws InvalidAuthenticationException, UserNotFoundException, OperationFailedException
    {
        // connection object
        LdapContextSource ctxSource = new LdapContextSource();

        // connection url
        ctxSource.setUrl(ldapPropertiesMapper.getConnectionURL());

        // username/password
        LDAPUserWithAttributes user = findUserByName(name);
        ctxSource.setUserDn(user.getDn());

        // Do not allow the password credential to be blank
        // We should possibly follow http://opensource.atlassian.com/projects/spring/browse/LDAP-39
        // so we can simplify this call down. Currently if you use a blank password the below call
        // to getReadWriteContext will succeed: http://jira.atlassian.com/browse/CWD-316
        if (credential == null || StringUtils.isBlank(credential.getCredential()))
        {
            throw new InvalidAuthenticationException("You cannot authenticate with a blank password");
        }

        ctxSource.setPassword(credential.getCredential());

        // additional ldap properties
        ctxSource.setBaseEnvironmentProperties(getBaseEnvironmentProperties());

        ctxSource.setPooled(false);

        try
        {
            ctxSource.afterPropertiesSet();

            // Perform the authentication call by getting the context with the above attributes
            ctxSource.getReadWriteContext();
        } catch (NamingException e)
        {
            throw new InvalidAuthenticationException(name, e);
        } catch (Exception e)
        {
            throw new InvalidAuthenticationException(name, e);
        }

        return user;
    }


    /////////////////// GROUP OPERATIONS ///////////////////

    public LDAPGroupWithAttributes findGroupByName(String name) throws GroupNotFoundException, OperationFailedException
    {
        Validate.notNull(name, "name argument cannot be null");
        // equivalent call to findGroupWithAttributes
        return findGroupWithAttributesByName(name);

        // TODO: potential performance benefit: request only the first-class attributes
    }

    public LDAPGroupWithAttributes findGroupWithAttributesByName(final String name) throws GroupNotFoundException, OperationFailedException
    {
        Validate.notNull(name, "name argument cannot be null");

        EntityQuery query = QueryBuilder.queryFor(Group.class, EntityDescriptor.group(null)).with(Restriction.on(GroupTermKeys.NAME).exactlyMatching(name)).returningAtMost(1);

        List<LDAPGroupWithAttributes> groups = searchGroupObjects(query);

        if (groups.isEmpty())
        {
            throw new GroupNotFoundException(name);
        }

        // return the first object (group gets priority over role)
        return groups.get(0);
    }

    protected LDAPGroupWithAttributes findGroupByNameAndType(final String name, GroupType groupType) throws GroupNotFoundException, OperationFailedException
    {
        Validate.notNull(name, "name argument cannot be null");

        EntityQuery query = QueryBuilder.queryFor(Group.class, EntityDescriptor.group(groupType)).with(Restriction.on(GroupTermKeys.NAME).exactlyMatching(name)).returningAtMost(1);

        List<LDAPGroupWithAttributes> groups = searchGroupObjects(query);

        if (groups.isEmpty())
        {
            throw new GroupNotFoundException(name);
        }

        // return the first object
        return groups.get(0);
    }

    /**
     * This method expects that the query contains a non-null groupType in the entityDescriptor.
     *
     * @param query search query.
     * @return list of results.
     * @throws OperationFailedException represents a Communication error when trying to talk to a remote directory
     */
    @SuppressWarnings("unchecked")
    protected List<LDAPGroupWithAttributes> searchGroupObjectsOfSpecifiedGroupType(EntityQuery query) throws OperationFailedException
    {
        GroupType groupType = query.getEntityDescriptor().getGroupType();

        Name baseDN;
        if (GroupType.GROUP.equals(groupType))
        {
            baseDN = searchDN.getGroup();
        } else if (GroupType.LEGACY_ROLE.equals(groupType))
        {
            baseDN = searchDN.getRole();
        } else
        {
            throw new IllegalArgumentException("Cannot search for groups of type: " + groupType);
        }

        List<LDAPGroupWithAttributes> results;

        try
        {
            LDAPQuery ldapQuery = ldapQueryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
            String filter = ldapQuery.encode();
            logger.debug("Performing group search: baseDN = " + baseDN + " - filter = " + filter);

            results = searchEntities(baseDN, filter, getGroupContextMapper(groupType), query.getStartIndex(), query.getMaxResults());
        } catch (NullResultException e)
        {
            results = Collections.emptyList();
        }

        return results;
    }

    protected List<LDAPGroupWithAttributes> searchGroupObjects(EntityQuery query) throws OperationFailedException
    {
        Validate.notNull(query, "query argument cannot be null");

        if (query.getEntityDescriptor().getEntityType() != Entity.GROUP)
        {
            throw new IllegalArgumentException("group search can only evaluate EntityQueries for Entity.GROUP");
        }

        GroupType groupType = query.getEntityDescriptor().getGroupType();

        if (groupType == null)
        {
            List<LDAPGroupWithAttributes> results = new ArrayList<LDAPGroupWithAttributes>();

            int groupStartIndex = query.getStartIndex();
            int groupMaxResults = query.getMaxResults();
            if (!ldapPropertiesMapper.isRolesDisabled())
            {
                // if roles are enabled then we need to search for ALL results in order to get the aggregated indexing right (moral: disable roles)
                groupStartIndex = 0;
                groupMaxResults = EntityQuery.ALL_RESULTS;
            }

            // search for groups
            GroupQuery<Group> groupQuery = new GroupQuery<Group>(Group.class, GroupType.GROUP, query.getSearchRestriction(), groupStartIndex, groupMaxResults);
            results.addAll(searchGroupObjectsOfSpecifiedGroupType(groupQuery));

            // search for roles if enabled
            if (!ldapPropertiesMapper.isRolesDisabled())
            {
                EntityQuery<Group> roleQuery = new GroupQuery<Group>(Group.class, GroupType.LEGACY_ROLE, query.getSearchRestriction(), groupStartIndex, groupMaxResults);
                results.addAll(searchGroupObjectsOfSpecifiedGroupType(roleQuery));
            }

            return constrainResults(results, query.getStartIndex(), query.getMaxResults());
        } else
        {
            // group or role was specified in the query, so we can pass the query straight through
            return searchGroupObjectsOfSpecifiedGroupType(query);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> searchGroups(EntityQuery<T> query) throws OperationFailedException
    {
        Validate.notNull(query, "query argument cannot be null");

        final List<LDAPGroupWithAttributes> groups = searchGroupObjects(query);

        if (query.getReturnType() == String.class) // as names
        {
            return (List<T>) convertEntitiesToNames(groups);
        } else
        {
            return (List<T>) groups;
        }
    }

    /**
     * Perform any post-processing on groups.
     *
     * @param groups to post-process
     * @return list of groups that have been processed if required
     */
    protected List<LDAPGroupWithAttributes> postprocessGroups(List<LDAPGroupWithAttributes> groups)
    {
        return groups;
    }


    protected Attributes getNewGroupAttributes(Group group) throws NamingException
    {
        // get the basic attributes
        LDAPGroupAttributesMapper mapper = new LDAPGroupAttributesMapper(getDirectoryId(), group.getType(), ldapPropertiesMapper);
        Attributes attributes = mapper.mapAttributesFromGroup(group);

        // extension point: add directory-specific attributes to the group
        getNewGroupDirectorySpecificAttributes(group, attributes);

        // add member if required
        String defaultContainerMemberDN = getInitialGroupMemberDN();
        if (defaultContainerMemberDN != null)
        {
            // OpenLDAP fails if defaultContainerMemberDN is a space character, so don't pad it
            attributes.put(new BasicAttribute(ldapPropertiesMapper.getGroupMemberAttribute(), defaultContainerMemberDN));
        }

        return attributes;
    }

    /**
     * Populates attributes object with directory-specific attributes.
     * <p/>
     * Overrider of this method can take advantage of the default group attributes mapping logic
     * in {#getNewGroupAttributes(Group)}.
     * <p/>
     * Note that the attribute values supplied here will be used raw. This entails that overrider is responsible
     * for supplying values in a format supported by the directory.
     * In some directory implementations, for example, a blank string ("") is considered illegal. Overrider thus
     * would have to make sure the method does not generate a value as such.
     *
     * @param group      (potential) source of information that needs to be added.
     * @param attributes attributes to add directory-specific information to.
     */
    protected void getNewGroupDirectorySpecificAttributes(final Group group, final Attributes attributes)
    {
        // default no-op
    }

    /**
     * Returns the default container member DN.
     * <p/>
     * If this method returns null or blank string, no member DN will be added.
     *
     * @return empty member.
     */
    protected String getInitialGroupMemberDN()
    {
        // empty member
        return "";
    }

    public Group addGroup(GroupTemplate group) throws InvalidGroupException, OperationFailedException
    {
        Validate.notNull(group, "group cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(group.getName()), "group cannot have blank group name");

        if (groupExists(group))
        {
            throw new InvalidGroupException(group, "Group already exists");
        }

        Name baseDN;
        String nameAttribute;
        if (group.getType() == GroupType.GROUP)
        {
            baseDN = searchDN.getGroup();
            nameAttribute = ldapPropertiesMapper.getGroupNameAttribute();
        } else if (group.getType() == GroupType.LEGACY_ROLE)
        {
            baseDN = searchDN.getRole();
            nameAttribute = ldapPropertiesMapper.getRoleNameAttribute();
        } else
        {
            throw new InvalidGroupException(group, "group.type must be GroupType.GROUP or GroupType.LEGACY_ROLE");
        }

        try
        {
            Name dn = nameConverter.getName(nameAttribute, group.getName(), baseDN);

            Attributes groupAttributes = getNewGroupAttributes(group);

            // create the group
            ldapTemplate.bind(dn, null, groupAttributes);

            return findEntityByDN(getStandardisedDN(dn), LDAPGroupWithAttributes.class);
        } catch (UserNotFoundException e)
        {
            throw new AssertionError("Should not throw UserNotFoundException");
        } catch (GroupNotFoundException e)
        {
            throw new OperationFailedException(e);
        } catch (NamingException e)
        {
            throw new InvalidGroupException(group, e.getMessage(), e);
        } catch (InvalidNameException e)
        {
            throw new InvalidGroupException(group, e.getMessage(), e);
        }
    }

    public Group updateGroup(GroupTemplate group) throws GroupNotFoundException, OperationFailedException
    {
        Validate.notNull(group, "group cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(group.getName()), "group cannot have blank group name");

        // Get the current group, if this group is not found a ONFE will be thrown
        LDAPGroupWithAttributes currentGroup = findGroupByName(group.getName());

        if (currentGroup.getType() != group.getType())
        {
            throw new OperationNotSupportedException("Cannot modify the GroupType for an LDAP group");
        }

        List<ModificationItem> modificationItems = new ArrayList<ModificationItem>();

        // description
        String descriptionAttribute;
        if (group.getType() == GroupType.GROUP)
        {
            descriptionAttribute = ldapPropertiesMapper.getGroupDescriptionAttribute();
        } else
        {
            descriptionAttribute = ldapPropertiesMapper.getRoleDescriptionAttribute();
        }

        ModificationItem descriptionMod = createModificationItem(descriptionAttribute, currentGroup.getDescription(), group.getDescription());
        if (descriptionMod != null)
        {
            modificationItems.add(descriptionMod);
        }

        // Perform the update if there are modification items
        if (!modificationItems.isEmpty())
        {
            try
            {
                ldapTemplate.modifyAttributes(asLdapGroupName(currentGroup.getDn(), group.getName()), modificationItems.toArray(new ModificationItem[modificationItems.size()]));
            } catch (NamingException ex)
            {
                throw new OperationFailedException(ex);
            }
        }

        // Return the group 'fresh' from the LDAP directory
        try
        {
            return findEntityByDN(currentGroup.getDn(), LDAPGroupWithAttributes.class);
        } catch (UserNotFoundException e)
        {
            throw new AssertionError("Should not throw UserNotFoundException.");
        }
    }

    public void removeGroup(String name) throws GroupNotFoundException, OperationFailedException
    {
        Validate.notEmpty(name, "name argument cannot be null or empty");

        LDAPGroupWithAttributes group = findGroupByName(name);

        // remove the dn
        try
        {
            ldapTemplate.unbind(asLdapGroupName(group.getDn(), name));
        } catch (NamingException ex)
        {
            throw new OperationFailedException(ex);
        }
    }

    public Group renameGroup(final String oldName, final String newName) throws GroupNotFoundException, InvalidGroupException, OperationFailedException
    {
        // TODO: support this, aka CWD-1466
        throw new OperationNotSupportedException("Group renaming is not yet supported for LDAP directories");
    }

    public void storeGroupAttributes(final String groupName, final Map<String, Set<String>> attributes) throws GroupNotFoundException, OperationFailedException
    {
        throw new OperationNotSupportedException("Custom group attributes are not yet supported for LDAP directories");
    }

    public void removeGroupAttributes(final String groupName, final String attributeName) throws GroupNotFoundException, OperationFailedException
    {
        throw new OperationNotSupportedException("Custom group attributes are not yet supported for LDAP directories");
    }

    public <T> List<T> searchGroupRelationships(final MembershipQuery<T> query) throws OperationFailedException
    {
        Validate.notNull(query, "query argument cannot be null");

        if (query.getEntityToMatch().getEntityType() == Entity.GROUP && query.getEntityToReturn().getEntityType() == Entity.GROUP && query.getEntityToMatch().getEntityType() != query.getEntityToReturn().getEntityType())
        {
            throw new IllegalArgumentException("Cannot search for group relationships of mismatching GroupTypes: attempted to match <" + query.getEntityToMatch().getEntityType() + "> and return <" + query.getEntityToReturn().getEntityType() + ">");
        }

        // welcome to the mess that is roles

        List<T> results;

        if (query.getEntityToMatch().getEntityType() == Entity.GROUP && query.getEntityToReturn().getEntityType() == Entity.USER)
        {
            GroupType groupType = query.getEntityToMatch().getGroupType();

            if (groupType == null)
            {
                // if groupType is null then we are searching for either the group or the role (try group first, then role)
                MembershipQuery<T> groupQuery = QueryBuilder.createMembershipQuery(query.getMaxResults(), query.getStartIndex(), query.isFindChildren(), query.getEntityToReturn(), query.getReturnType(), query.getEntityToMatch(), query.getEntityNameToMatch());
                results = searchGroupRelationshipsWithGroupTypeSpecified(groupQuery);

                // only search for roles if (a) roles are enabled and (b) no results were found while searching for the group
                if (!ldapPropertiesMapper.isRolesDisabled() && results.isEmpty())
                {
                    MembershipQuery<T> roleQuery = QueryBuilder.createMembershipQuery(query.getMaxResults(), query.getStartIndex(), query.isFindChildren(), query.getEntityToReturn(), query.getReturnType(), EntityDescriptor.group(GroupType.LEGACY_ROLE), query.getEntityNameToMatch());
                    results = searchGroupRelationshipsWithGroupTypeSpecified(roleQuery);
                }
            } else
            {
                // groupType has been specified, so safe to execute directly
                results = searchGroupRelationshipsWithGroupTypeSpecified(query);
            }
        } else if (query.getEntityToMatch().getEntityType() == Entity.USER && query.getEntityToReturn().getEntityType() == Entity.GROUP)
        {
            GroupType groupType = query.getEntityToReturn().getGroupType();

            if (groupType == null)
            {
                // if groupType is null then we are searching for either the group or the role (try group first, then role)
                MembershipQuery<T> groupQuery = QueryBuilder.createMembershipQuery(query.getMaxResults(), query.getStartIndex(), query.isFindChildren(), EntityDescriptor.group(GroupType.GROUP), query.getReturnType(), query.getEntityToMatch(), query.getEntityNameToMatch());
                results = searchGroupRelationshipsWithGroupTypeSpecified(groupQuery);

                // only search for roles if (a) roles are enabled and (b) no results were found while searching for the group
                if (!ldapPropertiesMapper.isRolesDisabled() && results.isEmpty())
                {
                    MembershipQuery<T> roleQuery = QueryBuilder.createMembershipQuery(query.getMaxResults(), query.getStartIndex(), query.isFindChildren(), EntityDescriptor.group(GroupType.LEGACY_ROLE), query.getReturnType(), query.getEntityToMatch(), query.getEntityNameToMatch());
                    results = searchGroupRelationshipsWithGroupTypeSpecified(roleQuery);
                }
            } else
            {
                // groupType has been specified, so safe to execute directly
                results = searchGroupRelationshipsWithGroupTypeSpecified(query);
            }
        } else if (query.getEntityToMatch().getEntityType() == Entity.GROUP && query.getEntityToReturn().getEntityType() == Entity.GROUP)
        {
            GroupType groupTypeToMatch = query.getEntityToMatch().getGroupType();
            GroupType groupTypeToReturn = query.getEntityToReturn().getGroupType();

            if (groupTypeToMatch != groupTypeToReturn)
            {
                throw new IllegalArgumentException("Cannot search for group relationships of mismatching GroupTypes: attempted to match <" + groupTypeToMatch + "> and return <" + groupTypeToReturn + ">");
            }

            if (groupTypeToReturn == null)
            {
                // if groupType is null then we are searching for either the group or the role (try group first, then role)
                final MembershipQuery<T> groupQuery = QueryBuilder.createMembershipQuery(query.getMaxResults(), query.getStartIndex(), query.isFindChildren(), EntityDescriptor.group(GroupType.GROUP), query.getReturnType(), EntityDescriptor.group(GroupType.GROUP), query.getEntityNameToMatch());
                results = searchGroupRelationshipsWithGroupTypeSpecified(groupQuery);

                // only search for roles if (a) roles are enabled and (b) no results were found while searching for the group
                if (!ldapPropertiesMapper.isRolesDisabled() && results.isEmpty())
                {
                    MembershipQuery<T> roleQuery = QueryBuilder.createMembershipQuery(query.getMaxResults(), query.getStartIndex(), query.isFindChildren(), EntityDescriptor.group(GroupType.LEGACY_ROLE), query.getReturnType(), EntityDescriptor.group(GroupType.LEGACY_ROLE), query.getEntityNameToMatch());
                    results = searchGroupRelationshipsWithGroupTypeSpecified(roleQuery);
                }
            } else
            {
                // groupType has been specified, so safe to execute directly
                results = searchGroupRelationshipsWithGroupTypeSpecified(query);
            }
        } else
        {
            throw new IllegalArgumentException("Cannot search for relationships between a USER and another USER");
        }

        return results;
    }

    /**
     * Execute the search for group relationships given that a group of type GROUP or LEGACY_ROLE has
     * been specified in the EntityDescriptor for the group(s).
     *
     * @param query membership query with all GroupType's not null.
     * @return list of members or memberships depending on the query.
     * @throws OperationFailedException if the operation failed due to a communication error with the remote directory,
     *                                  or if the query is invalid
     */
    protected abstract <T> List<T> searchGroupRelationshipsWithGroupTypeSpecified(MembershipQuery<T> query) throws OperationFailedException;

    /////////////////// MISCELLANEOUS OPERATIONS /////////////////

    /**
     * Given an plain-text password, encodes/encrypts it according to the settings required by the particular directory
     * connector. Return type should be either String or byte[].
     *
     * @param unencodedPassword The password to be transformed
     * @return An encoded password, suitable for passing to the directory.
     * @throws InvalidCredentialException If the password could not be converted.
     */
    protected abstract Object encodePassword(String unencodedPassword) throws InvalidCredentialException;

    public boolean supportsNestedGroups()
    {
        return !ldapPropertiesMapper.isNestedGroupsDisabled();
    }

    public boolean isRolesDisabled()
    {
        return ldapPropertiesMapper.isRolesDisabled();
    }

    public void testConnection() throws OperationFailedException
    {
        try
        {
            LdapContext ldapContext = (LdapContext) contextSource.getReadOnlyContext();
            ldapContext.getConnectControls();
        } catch (Exception e)
        {
            throw new OperationFailedException(e.getMessage());
        }
    }

    protected String getStandardisedDN(Name dn) throws OperationFailedException
    {
        try
        {
            return DNStandardiser.standardise(new DistinguishedName(dn), !ldapPropertiesMapper.isRelaxedDnStandardisation());
        }
        catch (NamingException e)
        {
            throw new OperationFailedException("Failed to parse distinguished name", e);
        }
    }

    final String standardiseDN(String dn)
    {
        return DNStandardiser.standardise(dn, !ldapPropertiesMapper.isRelaxedDnStandardisation());
    }

    /**
     * This method is required to wrap DN's into LdapNames as spring-ldap
     * doesn't correctly handle operations with String dn arguments.
     * <p/>
     * This mainly affects the escaping of slashes in DNs.
     * <p/>
     * The resulting javax.naming.Name is not designed to be used for
     * caching or comparisons, rather, it is to be used for direct
     * calls into spring-ldap's ldapTemplate.
     *
     * @param dn          string version of DN.
     * @param entityName  used if NotFoundException needs to be thrown.
     * @param entityClass in case there is a problem converting the dn into an LdapName a NotFoundException of this type (group/user) will be thrown.
     *                    Must implement User or Group, otherwise an IllegalArgumentException will be thrown.
     * @return LdapName for use with spring-ldap.
     * @throws UserNotFoundException  unable to construct LdapName for User.
     * @throws GroupNotFoundException unable to construct LdapName for Group.
     */
    protected <T extends LDAPDirectoryEntity> LdapName asLdapName(String dn, String entityName, Class<T> entityClass) throws UserNotFoundException, GroupNotFoundException
    {
        try
        {
            return new LdapName(dn);
        } catch (InvalidNameException e)
        {
            throw typedEntityNotFoundException(entityName, entityClass);
        }
    }

    /**
     * Convenience method to convert group DN to LdapName,
     * throwing a GNFE with the supplied group name if unable
     * to construct the LdapName.
     *
     * @param dn        DN of the Group.
     * @param groupName for GNFE exception.
     * @return LdapName for DN.
     * @throws GroupNotFoundException unable to construct LdapName.
     */
    protected LdapName asLdapGroupName(String dn, String groupName) throws GroupNotFoundException
    {
        try
        {
            return asLdapName(dn, groupName, LDAPGroupWithAttributes.class);
        } catch (UserNotFoundException e)
        {
            throw new AssertionError("Should not throw UserNotFoundException.");
        }
    }

    /**
     * Convenience method to convert user DN to LdapName,
     * throwing a GNFE with the supplied user name if unable
     * to construct the LdapName.
     *
     * @param dn       DN of the User.
     * @param userName for GNFE exception.
     * @return LdapName for DN.
     * @throws UserNotFoundException unable to construct LdapName.
     */
    protected LdapName asLdapUserName(String dn, String userName) throws UserNotFoundException
    {
        try
        {
            return asLdapName(dn, userName, LDAPUserWithAttributes.class);
        } catch (GroupNotFoundException e)
        {
            throw new AssertionError("Should not throw GroupNotFoundException.");
        }
    }

    /**
     * Storing active/inactive flag for users in LDAP is currently not supported.
     *
     * @return false
     */
    public boolean supportsInactiveAccounts()
    {
        return false;
    }

    @Override
    public RemoteDirectory getAuthoritativeDirectory()
    {
        return this;
    }

    /**
     * Returns <tt>true</tt> if the group exists.
     *
     * @param group Group to check
     * @return <tt>true</tt> if the group exists.
     * @throws OperationFailedException if the operation failed for any reason.
     */
    private boolean groupExists(final Group group) throws OperationFailedException
    {
        try
        {
            findGroupByName(group.getName());
            return true;
        } catch (GroupNotFoundException e)
        {
            return false;
        }
    }

    @Override
    public Iterable<Membership> getMemberships() throws OperationFailedException
    {
        return new DirectoryMembershipsIterable(this);
    }
}
