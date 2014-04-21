package com.atlassian.jira.web.servlet;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.StreamCopyingConsumer;
import com.atlassian.jira.web.exception.WebExceptionChecker;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract servlet to serve avatar images
 *
 * @since v4.2
 */
public abstract class AbstractAvatarServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(AbstractAvatarServlet.class);

    static final int AVATAR_BUFFER_SIZE = 4096;

    private static final String PARAM_SIZE = "size";
    private static final String PARAM_OWNER_ID = "ownerId";
    private static final String VALUE_SMALL = "small";
    private static final int ABOUT_ONE_YEAR = 60 * 60 * 24 * 365;

    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        boolean bytesWritten = false;
        try
        {
            String ownerId = StringUtils.trim(request.getParameter(getOwnerIdParamName()));
            if (StringUtils.isBlank(ownerId))
            {
                //Don't trim here or this will break usernames with leading/trailing whitespace. Yes they *do* exist!
                ownerId = request.getParameter(PARAM_OWNER_ID);
            }
            final Long avatarId = NumberUtils.createLong(request.getParameter("avatarId"));

            final Long realAvatarId = validateInput(ownerId, avatarId, response);
            if (realAvatarId == null)
            {
                return;
            }

            // now we must have avatar id
            final Avatar avatar = getAvatarManager().getById(realAvatarId);
            if (avatar == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Avatar not found");
            }
            else if (!avatarOkForOwner(ownerId, avatar))
            {
                // wrong project for the avatar!
                log.warn("Attempted access to avatar " + avatar.getId() + " for owner "
                        + ownerId + " when it doesn't belong to that owner.");
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            else
            {
                sendAvatar(request, response, avatar);
                bytesWritten = true;
            }
        }
        catch (NumberFormatException e)
        {
            // avatarId was requested but it wasn't an avatarId
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        catch (IOException e)
        {
            handleOutputStreamingException(response, bytesWritten, e);
        }
        catch (RuntimeException e)
        {
            handleOutputStreamingException(response, bytesWritten, e);
        }
    }

    protected boolean avatarOkForOwner(final String ownerId, final Avatar avatar)
    {
        final String owner = avatar.getOwner();
        return owner == null || ownerId != null && ownerId.equals(owner);
    }

    protected static void handleOutputStreamingException(final HttpServletResponse response, final boolean bytesWritten, final Exception e)
            throws IOException, ServletException
    {
        if (WebExceptionChecker.canBeSafelyIgnored(e))
        {
            return;
        }
        // something went wrong streaming the file off disk, could have been a FNF
        if (!bytesWritten && ! response.isCommitted())
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        else
        {
            // This should be impossible since if bytes were written then the only thing that should happen
            // after is quietly shutting down the streams, but if it does, we can't set the response code
            // and we can't give a valid output, so we must die.
            throw new ServletException("Unable to write a coherent reponse for avatar request", e);
        }
    }

    private void sendAvatar(final HttpServletRequest request, final HttpServletResponse response, final Avatar avatar)
            throws IOException
    {
        response.setContentType(avatar.getContentType());
        // prevents old HTTP 1.0 proxies from caching (for security reasons) but is overridden by max-age below
        // firefox has a bug which means that it incorrectly makes this stop caching in the browser dispite a Cache-control header.
        // TODO comment this line back in when a fixed firefox is sufficiently widely deployed, probably early 2010
//        response.setHeader("Expires", "Fri, 01 Jan 1990 00:00:00 GMT");
        //JRADEV-671 As part of the work on icon caching fixed the max-age, the HTTP spec says
        // maximum max-age should be 1 year (in seconds)
        response.setHeader("Cache-control", "private, max-age=" + ABOUT_ONE_YEAR);
        final OutputStream out = response.getOutputStream();
        StreamCopyingConsumer streamCopier = new StreamCopyingConsumer(out, AVATAR_BUFFER_SIZE);

        // large avatar is the default. Small is only used if small size is requested.
        AvatarManager.ImageSize size = AvatarManager.ImageSize.fromString(request.getParameter(PARAM_SIZE));
        getAvatarManager().readAvatarData(avatar, size, streamCopier);
    }

    /**
     * Ensures the input is valid and if validation passes returns a valid avatarId
     * to serve.  If validation fails, this
     * method should send errors on the response and return a null avatar id.
     *
     * @param ownerId The owner of the avatar
     * @param avatarId The avatarId requested
     * @param response The response to send
     * @return The avatarId to serve to the user or null if validation fails
     * @throws IOException If there's an error updating the response object
     */
    protected abstract Long validateInput(final String ownerId, final Long avatarId, final HttpServletResponse response)
            throws IOException;

    /**
     * Slight hack to stay backwards compatible for project avatars via the 'pid' param.  The project avatar servlet
     * implementation can return pid here instead of ownerId.
     *
     * @return The parameter used to identify the owner in the request.
     */
    protected abstract String getOwnerIdParamName();

    AvatarManager getAvatarManager()
    {
        return ComponentAccessor.getAvatarManager();
    }
}
