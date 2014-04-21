package com.atlassian.security.auth.trustedapps.filter;

import com.atlassian.security.auth.trustedapps.Application;
import com.atlassian.security.auth.trustedapps.ApplicationRetriever;
import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.EncryptedCertificate;
import com.atlassian.security.auth.trustedapps.TrustedApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.security.auth.trustedapps.filter.TrustedApplicationsFilter.CertificateServer;
import com.atlassian.security.auth.trustedapps.filter.TrustedApplicationsFilter.CertificateServerImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.PublicKey;
import java.util.Set;

import junit.framework.TestCase;

public class TestTrustedAppCertificateServerImpl extends TestCase
{

    public void testCertificateServer() throws Exception
    {
        TrustedApplicationsManager appManager = new TrustedApplicationsManager()
        {
            public CurrentApplication getCurrentApplication()
            {
                return new CurrentApplication()
                {
                    public String getID()
                    {
                        return "hehe-12345";
                    }

                    public PublicKey getPublicKey()
                    {
                        return new PublicKey()
                        {
                            public String getAlgorithm()
                            {
                                return "ALGY-RHYTHM";
                            }

                            public byte[] getEncoded()
                            {
                                return new byte[] { 1, 2, 3, 4 };
                            }

                            public String getFormat()
                            {
                                return "FROMAT";
                            }
                        };
                    }

                    public EncryptedCertificate encode(String userName)
                    {
                        throw new UnsupportedOperationException();
                    }
                    
                    public EncryptedCertificate encode(String userName, String urlToSign)
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            public TrustedApplication getTrustedApplication(String id)
            {
                throw new UnsupportedOperationException();
            }
        };
        CertificateServer server = new CertificateServerImpl(appManager);

        StringWriter writer = new StringWriter();
        server.writeCertificate(writer);

        BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));

        assertEquals("hehe-12345", reader.readLine());
        assertEquals("AQIDBA==", reader.readLine());
        assertEquals(TrustedApplicationUtils.Constant.VERSION.toString(), reader.readLine());
        assertEquals(TrustedApplicationUtils.Constant.MAGIC, reader.readLine());
        assertNull(reader.readLine());
    }

    public void testCertificateServerWriterExceptionThrowsWrappedRuntimeEx() throws Exception
    {
        TrustedApplicationsManager appManager = new TrustedApplicationsManager()
        {
            public CurrentApplication getCurrentApplication()
            {
                return new CurrentApplication()
                {
                    public String getID()
                    {
                        return "hehe-12345";
                    }

                    public PublicKey getPublicKey()
                    {
                        return new PublicKey()
                        {
                            public String getAlgorithm()
                            {
                                return "ALGY-RHYTHM";
                            }

                            public byte[] getEncoded()
                            {
                                return new byte[] { 1, 2, 3, 4 };
                            }

                            public String getFormat()
                            {
                                return "FROMAT";
                            }
                        };
                    }

                    public EncryptedCertificate encode(String userName)
                    {
                        throw new UnsupportedOperationException();
                    }
                    
                    public EncryptedCertificate encode(String userName, String urlToSign)
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            public TrustedApplication getTrustedApplication(String id)
            {
                throw new UnsupportedOperationException();
            }

            public TrustedApplication addTrustedApplication(Application app, long certificateTimeout, Set<String> urlPatterns, Set<String> ipPatterns)
            {
                throw new UnsupportedOperationException();
            }

            public Application getApplicationCertificate(String url) throws ApplicationRetriever.RetrievalException
            {
                throw new UnsupportedOperationException();
            }

            public void deleteApplication(String id)
            {
                throw new UnsupportedOperationException();
            }
        };
        CertificateServer server = new CertificateServerImpl(appManager);

        final IOException ioEx = new IOException("poo");
        Writer writer = new Writer()
        {
            public void write(char[] cbuf, int off, int len) throws IOException
            {
                throw ioEx;
            }

            public void close() throws IOException
            {
            }

            public void flush() throws IOException
            {
            }
        };
        try
        {
            server.writeCertificate(writer);
            fail("RuntimeException expected");
        }
        catch (RuntimeException ex)
        {
            assertSame(ioEx, ex.getCause());
        }
    }
}
