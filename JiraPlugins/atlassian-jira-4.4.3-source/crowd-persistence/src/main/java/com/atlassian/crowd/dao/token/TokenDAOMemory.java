package com.atlassian.crowd.dao.token;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.exception.ObjectNotFoundException;
import com.atlassian.crowd.manager.cache.CacheManager;
import com.atlassian.crowd.manager.cache.CacheManagerException;
import com.atlassian.crowd.manager.cache.NotInCacheException;
import com.atlassian.crowd.model.token.Token;
import com.atlassian.crowd.search.Entity;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.*;
import com.atlassian.crowd.search.query.entity.restriction.constants.TokenTermKeys;
import com.atlassian.crowd.search.util.SearchResultsUtil;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.*;

/**
 * An in-memory implementation of the TokenDAO. This will use the caching manager.
 *
 * Note: this is not thead-safe!
 */
public class TokenDAOMemory implements TokenDAO
{
    public static final String RANDOM_HASH_CACHE = Token.class.getName() + ".random-hash-cache";
    public static final String IDENTIFIER_HASH_CAHE = Token.class.getName() + ".identifier-hash-cache";

    private static final Logger logger = Logger.getLogger(TokenDAOMemory.class);

    private CacheManager cacheManager;

    public Token findByRandomHash(String randomHash) throws ObjectNotFoundException
    {
        Token token = null;
        try
        {
            token = (Token) cacheManager.get(RANDOM_HASH_CACHE, randomHash);
        }
        catch (CacheManagerException e)
        {
            logger.error(e.getMessage(), e);
        }
        catch (NotInCacheException e)
        {
            // miss
            throw new ObjectNotFoundException(getPersistentClass(), randomHash);
        }
        return token;
    }

    public Token findByIdentifierHash(final String identifierHash) throws ObjectNotFoundException
    {
        Token token = null;
        try
        {
            token = (Token) cacheManager.get(IDENTIFIER_HASH_CAHE, identifierHash);
        }
        catch (CacheManagerException e)
        {
            logger.error(e.getMessage(), e);
        }
        catch (NotInCacheException e)
        {
            // miss
            throw new ObjectNotFoundException(getPersistentClass(), identifierHash);
        }
        return token;
    }

    public Token add(Token token)
    {
        try
        {
            cacheManager.put(RANDOM_HASH_CACHE, token.getRandomHash(), token);
            cacheManager.put(IDENTIFIER_HASH_CAHE, token.getIdentifierHash(), token);
        }
        catch (CacheManagerException e)
        {
            logger.error(e.getMessage(), e);
        }
        return token;
    }

    public Token update(Token token)
    {

        // remove the existing token.
//        remove(token);        // Not needed. ehcache uses put() to update a value as well as add one. Extra Bonus
                                //  Special Feature: we regain thread-safety :-)

        // add the new/updated one.
        token.setLastAccessedDate(new Date());
        
        return add(token);

    }

    public void remove(Token token)
    {
        // remove the existing token.
        try
        {
            cacheManager.remove(RANDOM_HASH_CACHE, token.getRandomHash());
            cacheManager.remove(IDENTIFIER_HASH_CAHE, token.getIdentifierHash());

        }
        catch (CacheManagerException e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    public List<Token> search(final EntityQuery query)
    {
        if (query.getEntityDescriptor().getEntityType() != Entity.TOKEN)
        {
            throw new IllegalArgumentException("TokenDAO can only evaluate EntityQueries for Entity.TOKEN");
        }

        List<Token> tokens = new ArrayList<Token>();
        List<String> keys = findRandomHashKeys();

        for (String key : keys)
        {
            try
            {
                Token token = findByRandomHash(key);

                if (tokenMatchesSearchRestriction(token, query.getSearchRestriction()))
                {
                    tokens.add(token);
                }
            }
            catch (ObjectNotFoundException e)
            {
                logger.error(e.getMessage(), e);
            }
        }

        tokens = SearchResultsUtil.constrainResults(tokens, query.getStartIndex(), query.getMaxResults());

        return tokens;
    }

    private boolean tokenMatchesSearchRestriction(final Token token, final SearchRestriction searchRestriction)
    {
        if (searchRestriction instanceof NullRestriction)
        {
            return true;
        }
        else if (searchRestriction instanceof PropertyRestriction)
        {
            return tokenMatchesTermRestriction(token, (PropertyRestriction) searchRestriction);
        }
        else if (searchRestriction instanceof BooleanRestriction)
        {
            return tokenMatchesMultiTermRestriction(token, (BooleanRestriction) searchRestriction);
        }
        else
        {
            throw new IllegalArgumentException("SearchRestriction unsupported: " + searchRestriction.getClass());
        }
    }

    private boolean tokenMatchesMultiTermRestriction(final Token token, final BooleanRestriction multiRestriction)
    {
        if (multiRestriction.getBooleanLogic() == BooleanRestriction.BooleanLogic.AND)
        {
            boolean match = true;

            for (SearchRestriction restriction : multiRestriction.getRestrictions())
            {
                if (!tokenMatchesSearchRestriction(token, restriction))
                {
                    match = false;
                    break;
                }
            }

            return match;
        }
        else if (multiRestriction.getBooleanLogic() == BooleanRestriction.BooleanLogic.OR)
        {
            boolean match = false;

            for (SearchRestriction restriction : multiRestriction.getRestrictions())
            {
                if (tokenMatchesSearchRestriction(token, restriction))
                {
                    match = true;
                    break;
                }
            }

            return match;
        }
        else
        {
            throw new IllegalArgumentException("BooleanLogic unsupported: " + multiRestriction.getBooleanLogic().getClass());
        }
    }

    private boolean tokenMatchesTermRestriction(final Token token, final PropertyRestriction restriction)
    {
        if (restriction.getProperty().equals(TokenTermKeys.NAME))
        {
            String value = (String) restriction.getValue();

            switch (restriction.getMatchMode())
            {
                case STARTS_WITH:       return token.getName().startsWith(value);
                case CONTAINS:          return token.getName().contains(value);
                default:                return token.getName().equals(value);
            }
        }
        else if (restriction.getProperty().equals(TokenTermKeys.LAST_ACCESSED_DATE))
        {
            Date value = (Date) restriction.getValue();

            switch (restriction.getMatchMode())
            {
                case GREATER_THAN:      return token.getLastAccessedDate().after(value);
                case LESS_THAN:         return token.getLastAccessedDate().before(value);
                default:                return token.getLastAccessedDate().equals(value);
            }
        }
        else if (restriction.getProperty().equals(TokenTermKeys.DIRECTORY_ID))
        {
            Long value = (Long) restriction.getValue();

            switch (restriction.getMatchMode())
            {
                case GREATER_THAN:      return token.getDirectoryId() > value;
                case LESS_THAN:         return token.getDirectoryId() < value;
                default:                return token.getDirectoryId() == value;
            }
        }
        else if (restriction.getProperty().equals(TokenTermKeys.RANDOM_NUMBER))
        {
            Long value = (Long) restriction.getValue();

            switch (restriction.getMatchMode())
            {
                case GREATER_THAN:      return token.getRandomNumber() > value;
                case LESS_THAN:         return token.getRandomNumber() < value;
                default:                return token.getRandomNumber() == value;
            }
        }
        else
        {
            throw new IllegalArgumentException("ProperyRestriction unsupported: " + restriction.getClass());
        }
    }

    private List<String> findRandomHashKeys()
    {
        return cacheManager.getAllKeys(RANDOM_HASH_CACHE);
    }

    public Token findByID(long ID) throws ObjectNotFoundException
    {
        throw new UnsupportedOperationException("Currently we cannot load Token's by ID");
    }

    public void remove(long directoryId, String name)
    {
        List<String> keys = findRandomHashKeys();

        for (String key : keys)
        {
            try
            {
                Token token = findByRandomHash(key);
                if (token.getDirectoryId() == directoryId && token.getName().equals(name))
                {
                    remove(token);
                }
            }
            catch (ObjectNotFoundException e)
            {
                // key already removed
            }
        }
    }

    public void removeAll(final long directoryId)
    {
        List<String> keys = findRandomHashKeys();

        for (String key : keys)
        {
            try
            {
                Token token = findByRandomHash(key);
                if (token.getDirectoryId() == directoryId)
                {
                    remove(token);
                }
            }
            catch (ObjectNotFoundException e)
            {
                // key already removed
            }
        }
    }

    public void removeAccessedBefore(final Date expiryTime)
    {
        List<String> keys = findRandomHashKeys();

        for (String key : keys)
        {
            try
            {
                Token token = findByRandomHash(key);
                if (token.getLastAccessedDate().before(expiryTime))
                {
                    remove(token);
                }
            }
            catch (ObjectNotFoundException e)
            {
                // key already removed
            }
        }
    }

    public Class getPersistentClass()
    {
        return Token.class.getClass();
    }

    public void save(Object persistentObject)
    {
        add((Token) persistentObject);
    }

    public void update(Object persistentObject)
    {
        update((Token) persistentObject);
    }

    public void remove(Object persistentObject)
    {
        remove((Token) persistentObject);
    }

    public Object load(long ID)
    {
        throw new UnsupportedOperationException("Currently we cannot load Tokens by ID");
    }

    /**
     * {@see TokenDAOPersistence.loadAll()}
     */
    public Collection<Token> loadAll()
    {
        List<String> keys = findRandomHashKeys();
        List<Token> tokens = new ArrayList<Token>(keys.size());

        for (String key : keys)
        {
            try
            {
                Token token = findByRandomHash(key);
                tokens.add(token);
            }
            catch (ObjectNotFoundException e)
            {
                // key already removed by some other thread
            }
        }

        return tokens;
    }

    /**
     * {@see TokenDAOPersistence.saveAll()}
     */
    public void saveAll(Collection<Token> tokens)
    {
        if (tokens == null)
        {
            throw new DataRetrievalFailureException("Unable to save an empty collection of tokens");
        }
        for (Iterator tokenIt = tokens.iterator(); tokenIt.hasNext();)
        {
            save(tokenIt.next());
        }
    }

    /**
     * {@see TokenDAOPersistence.removeAll()}
     */
    public void removeAll()
    {
        cacheManager.removeAll(RANDOM_HASH_CACHE);
        cacheManager.removeAll(IDENTIFIER_HASH_CAHE);
    }

    public void setCacheManager(CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }
}
