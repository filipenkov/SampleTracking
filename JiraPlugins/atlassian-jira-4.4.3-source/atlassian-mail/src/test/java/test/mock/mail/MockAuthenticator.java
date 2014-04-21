/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Dec 6, 2002
 * Time: 1:41:02 PM
 * CVS Revision: $Revision: 1.2 $
 * Last CVS Commit: $Date: 2003/09/30 07:27:55 $
 * Author of last CVS Commit: $Author: mcannon $
 * To change this template use Options | File Templates.
 */
package test.mock.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class MockAuthenticator extends Authenticator
{
    private String username;
    private String password;

    public MockAuthenticator(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    public PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication(getUsername(), getPassword());
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }
}
