package com.opensymphony.user;

import com.opensymphony.user.provider.AccessProvider;
import com.opensymphony.user.provider.CredentialsProvider;
import com.opensymphony.user.provider.ProfileProvider;
import com.opensymphony.user.provider.UserProvider;

import java.util.ArrayList;
import static java.util.Collections.unmodifiableList;
import java.util.List;

@Deprecated
public interface Configuration
{
    List<CredentialsProvider> getCredentialsProviders();

    List<AccessProvider> getAccessProviders();

    List<ProfileProvider> getProfileProviders();

    @Deprecated
    public class Builder
    {
        private final List<AccessProvider> accessProviders = new ArrayList<AccessProvider>();
        private final List<CredentialsProvider> credentialsProviders = new ArrayList<CredentialsProvider>();
        private final List<ProfileProvider> profileProviders = new ArrayList<ProfileProvider>();

        public void addProvider(final UserProvider provider)
        {
            if (provider instanceof CredentialsProvider)
            {
                credentialsProviders.add((CredentialsProvider) provider);
            }

            if (provider instanceof ProfileProvider)
            {
                profileProviders.add((ProfileProvider) provider);
            }

            if (provider instanceof AccessProvider)
            {
                accessProviders.add((AccessProvider) provider);
            }
        }

        public Configuration toConfiguration()
        {
            return new Configuration()
            {
                public List<AccessProvider> getAccessProviders()
                {
                    return unmodifiableList(accessProviders);
                }

                public List<CredentialsProvider> getCredentialsProviders()
                {
                    return unmodifiableList(credentialsProviders);
                }

                public List<ProfileProvider> getProfileProviders()
                {
                    return unmodifiableList(profileProviders);
                }
            };
        }
    }
}
