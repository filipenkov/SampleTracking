package com.atlassian.crowd.dao.token;

import com.atlassian.crowd.exception.ObjectNotFoundException;
import com.atlassian.crowd.model.token.Token;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Manages persistence of {@link Token}
 */
public interface TokenDAO
{
    /**
     * Finds token by random hash.
     *
     * @param randomHash Random hash.
     * @return Token.
     * @throws ObjectNotFoundException if the token identified by the random hash cannot be found.
     */
    Token findByRandomHash(String randomHash) throws ObjectNotFoundException;

    /**
     * Finds token by identifier hash.
     *
     * @param identifierHash Identifier hash.
     * @return Token.
     * @throws ObjectNotFoundException if the token identified by the identifier hash cannot be found.
     */
    Token findByIdentifierHash(String identifierHash) throws ObjectNotFoundException;

    /**
     * Persists a new token.
     *
     * @param token Token.
     * @return The persisted token.
     */
    Token add(Token token);

    /**
     * @param token token to update.
     * @return updates the last accessed date on the token (sets it to now).
     * @throws com.atlassian.crowd.exception.ObjectNotFoundException
     */
    Token update(Token token) throws ObjectNotFoundException;

    /**
     * Removes a token.
     *
     * @param token Token.
     */
    void remove(Token token);

    /**
     * Searches for token based on criteria.
     *
     * @param query Query.
     * @return List of tokens which qualify for the criteria.
     */
    List<Token> search(EntityQuery query);

    /**
     * Finds token by its id.
     *
     * @param ID id.
     * @return Token.
     * @throws ObjectNotFoundException if the Token cannot be found.
     */
    Token findByID(long ID) throws ObjectNotFoundException;

    /**
     * Remove token.
     *
     * @param directoryId Directory id.
     * @param name User or application name.
     */
    void remove(long directoryId, String name);

    /**
     * Remove all tokens associated with the given directory id.
     *
     * @param directoryId Directory id.
     */
    void removeAll(long directoryId);

    /**
     * Remove all tokens by expiryTime.
     *
     * @param expiryTime Expiry time.
     */
    void removeAccessedBefore(Date expiryTime);

    /**
     * Wipes all tokens from the store. Called after a successful loadAll() and persist into another store.
     * @throws org.springframework.dao.DataAccessException
     */
    void removeAll();

    /**
     * Used when switching implementations. Synchronisation is the caller's responsibility; don't allow calls to other
     * methods while this is in progress if you need to guarantee that the data are complete.
     * @return Collection<Token> of all active tokens.
     * @throws org.springframework.dao.DataAccessException If the tokens could not be retrieved.
     */
    Collection<Token> loadAll();

    /**
     * Used when switching implementations. Synchronization is the caller's reponsibility; don't allow calls to other
     * methods while this is in progress if you need to guarantee that the data are complete.
     * @param tokens all tokens to add.
     * @throws org.springframework.dao.DataAccessException
     */
    void saveAll(Collection<Token> tokens);
}
