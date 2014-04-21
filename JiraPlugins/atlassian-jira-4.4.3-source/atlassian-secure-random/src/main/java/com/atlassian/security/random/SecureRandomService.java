package com.atlassian.security.random;

/**
 * A generator for random data which is cryptographically secure, based
 * on the same interface as {@link java.security.SecureRandom}.
 * <p/>
 * Using this interface instead of using {@link java.security.SecureRandom} directly will
 * allow implementations to provide security or performance improvements (such as dynamic
 * re-seeding) without any changes to client code.
 * <p/>
 * Implementations are required to be thread-safe.
 */
public interface SecureRandomService
{
    /**
     * Generates a user-specified number of random bytes.
     *
     * @param bytes the array to be filled in with random bytes.
     * @see java.security.SecureRandom#nextBytes(byte[])
     */
    void nextBytes(byte[] bytes);

    /**
     * Returns the next pseudorandom {@code int}.
     *
     * @return the next pseudorandom {@code int}.
     * @see java.security.SecureRandom#nextInt()
     */
    int nextInt();

    /**
     * Returns a pseudorandom, {@code int} value between 0 (inclusive) and
     * the specified value (exclusive).
     *
     * @param n the bound on the random number to be returned.  Must be
     *          positive.
     * @return the next pseudorandom, {@code int}
     *         value between {@code 0} (inclusive) and {@code n} (exclusive).
     * @throws IllegalArgumentException if n is not positive
     * @see java.security.SecureRandom#nextInt(int)
     */
    int nextInt(int n);

    /**
     * Returns the next pseudorandom {@code long}.
     *
     * @return the next pseudorandom {@code long}.
     * @see java.security.SecureRandom#nextLong()
     */
    long nextLong();

    /**
     * Returns the next pseudorandom {@code boolean}.
     *
     * @return the next pseudorandom {@code boolean}.
     * @see java.security.SecureRandom#nextBoolean()
     */
    boolean nextBoolean();

    /**
     * Returns the next pseudorandom {@code float}.
     *
     * @return the next pseudorandom {@code float}.
     * @see java.security.SecureRandom#nextFloat()
     */
    float nextFloat();

    /**
     * Returns the next pseudorandom {@code double}.
     *
     * @return the next pseudorandom {@code double}.
     * @see java.security.SecureRandom#nextDouble()
     */
    double nextDouble();
}
