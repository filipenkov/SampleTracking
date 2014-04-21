package com.atlassian.security.random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

/**
 * A factory which returns properly initialised instances of {@link SecureRandom}.
 * <p/>
 * Clients should not access this class directly, but instead use {@link SecureRandomService}
 * or {@link SecureTokenGenerator} for their random data generation.
 */
public final class SecureRandomFactory
{
    private static final Logger log = LoggerFactory.getLogger(SecureRandomFactory.class);

    private SecureRandomFactory()
    {
        // prevent construction
    }

    /**
     * Creates and fully initialises a new {@link SecureRandom} instance.
     * <p/>
     * The instance is created via {@link SecureRandom#SecureRandom()}, which uses the default algorithm
     * provided by the JVM.
     * <p/>
     * The initialisation involves forcing the self-seeding of the instance by calling
     * {@link SecureRandom#nextBytes(byte[])}.
     *
     * @return self-seeded {@link SecureRandom} instance.
     */
    public static SecureRandom newInstance()
    {
        log.debug("Starting creation of new SecureRandom");
        long start = System.currentTimeMillis();

        SecureRandom random = new SecureRandom();

        // force self-seeding
        random.nextBytes(new byte[1]);

        long end = System.currentTimeMillis();
        log.debug("Finished creation new SecureRandom in {} ms", (end - start));

        return random;
    }
}
