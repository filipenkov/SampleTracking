package com.atlassian.jira.dev.backdoor;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableDirectory;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.user.util.UserManager;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 4.4
 */
public class PlaintextEncoderLoader implements PlaintextEncoderLoaderInterface, Startable
{
    private final Logger log = Logger.getLogger(PlaintextEncoderLoader.class);

    private final EventPublisher eventPublisher;
    private final CrowdService crowdService;
    private final CrowdDirectoryService crowdDirectoryService;
    private final UserManager userManager;

    private final String PLAINTEXT_ENCODER = "plaintext";

    public PlaintextEncoderLoader(final EventPublisher eventPublisher, final CrowdService crowdService, final CrowdDirectoryService crowdDirectoryService, final UserManager userManager)
    {
        this.eventPublisher = eventPublisher;
        this.crowdService = crowdService;
        this.crowdDirectoryService = crowdDirectoryService;
        this.userManager = userManager;
    }

    public void usePlainTextPasswords ()
    {
        List<Directory> directories = crowdDirectoryService.findAllDirectories();

        for (Directory directory : directories)
        {
            ImmutableDirectory.Builder builder = ImmutableDirectory.newBuilder(directory);
            Map<String, String> attributes = new HashMap<String, String>(directory.getAttributes());

            builder.setCreatedDate(new Date());
            builder.setUpdatedDate(new Date());

            attributes.put(DirectoryImpl.ATTRIBUTE_KEY_USER_ENCRYPTION_METHOD, PLAINTEXT_ENCODER);
            builder.setAttributes(attributes);
            crowdDirectoryService.updateDirectory(builder.toDirectory());
        }

        try
        {
            Iterable<User> users = userManager.getUsers();
            //Iterable<User> users = crowdService.search( QueryBuilder.queryFor(User.class, user()).returningAtMost(ALL_RESULTS) );

            for (User user: users)
            {
                crowdService.updateUserCredential(user, user.getName());
            }
        }
        catch (Exception e)
        {
            log.error("Unknown error", e);
        }
    }

    public void start() throws Exception
    {
        // eventPublisher.register(this);

       // usePlainTextPasswords();
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
       // usePlainTextPasswords();
    }
}