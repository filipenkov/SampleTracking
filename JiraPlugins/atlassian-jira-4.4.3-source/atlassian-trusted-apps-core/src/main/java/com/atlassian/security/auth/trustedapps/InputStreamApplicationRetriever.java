package com.atlassian.security.auth.trustedapps;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 */
public class InputStreamApplicationRetriever implements ApplicationRetriever {

    private final InputStream in;
    private final EncryptionProvider encryptionProvider;

    public InputStreamApplicationRetriever(InputStream in, EncryptionProvider encryptionProvider) {
        this.in = in;
        this.encryptionProvider = encryptionProvider;
    }

    public Application getApplication() throws RetrievalException {
        final InputStreamReader reader = new InputStreamReader(in);
        final ReaderApplicationRetriever retriever = new ReaderApplicationRetriever(reader, encryptionProvider);
        return retriever.getApplication();
    }
}
