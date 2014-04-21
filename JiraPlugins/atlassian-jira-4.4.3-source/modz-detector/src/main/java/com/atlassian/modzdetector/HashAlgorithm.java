package com.atlassian.modzdetector;

import java.io.InputStream;

/**
 * Represents the mechanism for hashing used by the registry.
 * TODO: try to reduce this to just use an InputStream
 */
public interface HashAlgorithm
{
    public String getHash(InputStream stream);

    public String getHash(byte[] bytes);
}
