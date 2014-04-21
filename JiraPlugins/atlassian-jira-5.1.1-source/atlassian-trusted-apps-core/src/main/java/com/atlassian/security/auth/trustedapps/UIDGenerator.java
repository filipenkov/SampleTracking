package com.atlassian.security.auth.trustedapps;

import java.net.InetAddress;
import java.security.SecureRandom;

/**
 * Utility class for UID generation.
 */
public class UIDGenerator
{
    /**
	 * Generates a a 32 character long unique ID. The generation is based on four components:
	 * - IP address
	 * - current time in milliseconds
	 * - secure random number
	 * - identity hash code of a newly created object
	 */
	public static String generateUID()
    {
        try
        {
        	String strRetVal = "";
        	String strTemp = "";
            // Get IPAddress Segment
            InetAddress addr = InetAddress.getLocalHost();

            byte[] ipaddr = addr.getAddress();
            for (int i=0; i<ipaddr.length; i++)
            {
                Byte b = new Byte(ipaddr[i]);

                strTemp = Integer.toHexString (b.intValue() & 0x000000ff);
                while (strTemp.length() < 2)
                {
                    strTemp = '0' + strTemp;
                }
                strRetVal += strTemp;
            }


            //Get CurrentTimeMillis() segment
            strTemp = Long.toHexString(System.currentTimeMillis());
            while (strTemp.length () < 12)
            {
                strTemp = '0' + strTemp;
            }
            strRetVal += strTemp;

            //Get Random Segment
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");

            strTemp = Integer.toHexString(prng.nextInt());
            while (strTemp.length () < 8)
            {
                strTemp = '0' + strTemp;
            }

            strRetVal += strTemp.substring(4);

            //Get IdentityHash() segment
            strTemp = Long.toHexString(System.identityHashCode(new Object()));
            while (strTemp.length() < 8)
            {
                strTemp = '0' + strTemp;
            }
            strRetVal += strTemp;
            return strRetVal.toUpperCase();
        }
        catch(java.net.UnknownHostException e)
        {
        	throw new RuntimeException(e);
        }
        catch(java.security.NoSuchAlgorithmException e)
        {
        	throw new RuntimeException(e);
        }
    }
}
