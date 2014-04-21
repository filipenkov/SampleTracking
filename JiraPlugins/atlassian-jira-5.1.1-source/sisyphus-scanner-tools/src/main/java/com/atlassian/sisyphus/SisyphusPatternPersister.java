/*
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

Copyright (c) 2009, Atlassian Software Systems
 */
package com.atlassian.sisyphus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

public class SisyphusPatternPersister
{
	private static final Logger log = Logger.getLogger(SisyphusPatternPersister.class);

	public static void writePatternsOut(Writer out, Map<String, SisyphusPattern> patterns) throws IOException
	{
		XStream xstream = new XStream();
		xstream.alias("RegexEntry", SisyphusPattern.class);
		ObjectOutputStream xmlout = xstream.createObjectOutputStream(new BufferedWriter(out));
		try
		{
			for(SisyphusPattern pattern : patterns.values())
				xmlout.writeObject(pattern);
		}
		finally
		{
			try
			{
				xmlout.close();
			}
			catch (Exception e)
			{
				log.debug(e);
			}
		}
	}

	public static Map<String, SisyphusPattern> readPatternsIn(Reader reader) throws IOException, ClassNotFoundException
	{
		Map<String, SisyphusPattern> patterns = new HashMap<String, SisyphusPattern>();
		XStream xstream = new XStream();
		xstream.alias("RegexEntry", SisyphusPattern.class);
		ObjectInputStream xmlIn = xstream.createObjectInputStream(new BufferedReader(reader));
		try
		{
			while(true)
			{
				try
				{
					SisyphusPattern pat = (SisyphusPattern) xmlIn.readObject();
					pat.getRegex(); // force compile
					if(pat.isBrokenPattern())
					{
	    				log.info("INVALID PATTERN: '"+pat.getRegex()+" at "+pat.getURL());
	    				continue;
					}

					patterns.put(pat.getId(), pat);
				}
				catch (PatternSyntaxException syntaxExc)
				{
					log.info(syntaxExc);
				}
				catch (EOFException eof)
				{
					break;
				}
			}
		}
		finally
		{
			try
			{
				xmlIn.close();
			}
			catch (Exception e)
			{
				log.debug(e);
			}
		}
		return patterns;
	}
}