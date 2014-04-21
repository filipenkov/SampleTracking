package com.atlassian.core.spool;

import java.io.IOException;
import java.io.InputStream;
/**
 * Class that implement this interface will spool data from the provided InputStream, and return a new InputStream
 * to the spooled data. This is useful in situations when data must be staged between sources due to differences
 * in IO speeds and resource expenses (for example streaming JDBC blobs from databases to web clients)
 */
public interface Spool
{
	/**
	 * Return a new InputStream to the data provided. Implementations <strong>should</strong> guarantee that the
     * returned input stream is independent of the stream passed in. For example, the returned InputStream should still
     * be useable once the original InputStream is closed and its associated resources are released.
	 * 
	 * @param streamToSpool - Source stream to be spooled
	 * @return Spooled stream
	 * @throws IOException
	 */
	InputStream spool(InputStream streamToSpool) throws IOException;
	
}
