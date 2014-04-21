package com.atlassian.security.random;

import java.security.SecureRandom;

/**
 * Implementation of the {@link SecureRandomService} which delegates to a single, shared instance
 * of {@link SecureRandom}.
 * <p/>
 * <p/>
 * Potential improvements include:
 * <ul>
 * <li>Periodic Re-seeding: currently we are not sure if this is a requirement, so it has not been implemented.
 * <li>Object Pooling: as {@link SecureRandom}s block on calls to produce random data, we may improve throughput
 * by pooling a collection of {@link SecureRandom}s. Contention is not yet been proved to be a problem,
 * so it has not been implemented.
 * </ul>
 * <p/>
 * The current implementation is guaranteed to be thread-safe as it delegates calls to the
 * underlying {@link java.security.SecureRandom} instance.
 */
public final class DefaultSecureRandomService implements SecureRandomService
{
    private static final SecureRandomService INSTANCE = new DefaultSecureRandomService(SecureRandomFactory.newInstance());

    private final SecureRandom random;

    DefaultSecureRandomService(SecureRandom random)
    {
        this.random = random;
    }

    /**
     * @return shared {@link DefaultSecureRandomService} instance which delegates to a single, shared instance
     * of {@link SecureRandom}.
     */
    public static SecureRandomService getInstance()
    {
        return INSTANCE;
    }

    /**
     * @inheritDoc
     */
    public void nextBytes(byte[] bytes)
    {
        random.nextBytes(bytes);
    }

    /**
     * @inheritDoc
     */
    public int nextInt()
    {
        return random.nextInt();
    }

    /**
     * @inheritDoc
     */
    public int nextInt(int n)
    {
        return random.nextInt(n);
    }

    /**
     * @inheritDoc
     */
    public long nextLong()
    {
        return random.nextLong();
    }

    /**
     * @inheritDoc
     */
    public boolean nextBoolean()
    {
        return random.nextBoolean();
    }

    /**
     * @inheritDoc
     */
    public float nextFloat()
    {
        return random.nextFloat();
    }

    /**
     * @inheritDoc
     */
    public double nextDouble()
    {
        return random.nextDouble();
    }
}
