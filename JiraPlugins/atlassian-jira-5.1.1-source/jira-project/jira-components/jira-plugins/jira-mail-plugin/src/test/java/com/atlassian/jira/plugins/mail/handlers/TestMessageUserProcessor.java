package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.model.user.UserTemplate;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestMessageUserProcessor extends AbstractTestMessageHandler
{

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
    }

    @Test
    public void testGetAuthorFromSenderNoUser() throws Exception
    {
        // test with no matching user to get
        Message pokemonMessage = createMessage("pokemon@atlassian.com", "Pocket Monster");
        assertNull("unexpected pokemon", messageUserProcessor.getAuthorFromSender(pokemonMessage));
    }

    @Test
    public void testGetAuthorFromSenderUserMatchByEmail() throws Exception
    {
        Message pokemonMessage = createMessage("pokemon@atlassian.com", "Pocket Monster");
        String pokemonEmail = "pokemon@atlassian.com";
        createMockUser("pokemon", "pokemon", pokemonEmail);
        User pokemonAuthor = messageUserProcessor.getAuthorFromSender(pokemonMessage);
        assertNotNull("should have found pokemon this time", pokemonAuthor);
        assertEquals(pokemonEmail, pokemonAuthor.getEmailAddress());
    }


    @Test
    public void testGetAuthorFromSenderMultipleUserMatchByEmail() throws Exception
    {
        String pokemonEmail = "pokemon@atlassian.com";
        Message pokemonMessage = createMessage(pokemonEmail, "Pocket Monster");
        createMockUser("digimon", "digimon", pokemonEmail);
        createMockUser("pokemon", "pokemon", pokemonEmail);
        createMockUser("yugiyo", "yugiyo", pokemonEmail);
        User pokemonAuthor = messageUserProcessor.getAuthorFromSender(pokemonMessage);
        assertNotNull("should have found one of the pokemons", pokemonAuthor);
        assertEquals(pokemonEmail, pokemonAuthor.getEmailAddress());
        String authorName = pokemonAuthor.getName();
        boolean foundOne = authorName.equals("digimon")
                || authorName.equals("pokemon")
                || authorName.equals("yugiyo");
        assertTrue("should have found either digimon or pokemon", foundOne);
    }

    /**
     * Tests getAuthorFromSender with no user with right email, but a user with the email as its username.
     *
     * @throws Exception whenever.
     */
    @Test
    public void testGetAuthorFromSenderUserMatchByUsername() throws Exception
    {
        String messageEmail = "bob@wailers.org";
        Message reggaeMsg = createMessage(messageEmail, "bobMarley");
        User useWithEmailAsUsername = createMockUser(messageEmail, messageEmail, "bob@marley.com");
        User author = messageUserProcessor.getAuthorFromSender(reggaeMsg);
        assertEquals(useWithEmailAsUsername, author);
    }

    /**
     * Test with user with email username (and wrong email) but checks that the user with right email is returned
     * instead.
     *
     * @throws Exception on error.
     */
    @Test
    public void testGetAuthorFromSenderEmailAndUsernameMatch() throws Exception
    {
        String messageEmail = "chris@atlassian.com";
        Message chrisEmail = createMessage(messageEmail, "chris");
        User userWithEmailAsUsername = createMockUser(messageEmail, messageEmail, "cmountford@atlassian.com");
        User userWithEmailAsEmail = createMockUser("christo", "christo", messageEmail);
        User authorFromSender = messageUserProcessor.getAuthorFromSender(chrisEmail);
        assertEquals(userWithEmailAsEmail, authorFromSender);
        assertFalse("returned the wrong user, should prefer email to email match", userWithEmailAsUsername.equals(authorFromSender));
    }

    @Test
    public void testFindUserByUsername()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        String god = "jimiHendrix";
        assertNull("god should not exist", messageUserProcessor.findUserByUsername(god));

        createMockUser(god);
        User foundUser = messageUserProcessor.findUserByUsername(god);
        assertNotNull("I should have found god", foundUser);
        assertEquals(god, foundUser.getName());
    }

    @Test
    public void testFindUserByEmail() throws Exception
    {
        User foundUser = messageUserProcessor.findUserByEmail(u1.getEmailAddress());
        assertNotNull("couldn't find user", foundUser);
        assertEquals("found the wrong user", foundUser, u1);

        assertNull("shouldn't have found a user!", messageUserProcessor.findUserByEmail("email@does.not.exist.com"));

        String email = "chris@atlassian.com";
        // create a user with email as username
        User chris = createMockUser(email);
        assertNull("shouldn't have found this user because their email is not set", messageUserProcessor.findUserByEmail(email));

        UserTemplate chrisWithEmail = new UserTemplate(email);
        chrisWithEmail.setEmailAddress(email);
        userManager.updateUser(chrisWithEmail);

        User userByEmail = messageUserProcessor.findUserByEmail(email);
        assertNotNull("should have found a user that time", userByEmail);
        assertEquals(email, userByEmail.getEmailAddress());

    }

    private Message createMessage(String address, String name)
            throws MessagingException, UnsupportedEncodingException
    {
        Message message = new MimeMessage(Session.getDefaultInstance(new Properties()));
        message.setText(MESSAGE_STRING);
        message.setSubject(MESSAGE_SUBJECT);
        message.setFrom(new InternetAddress(address, name));
        return message;
    }

}
