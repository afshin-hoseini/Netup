package ir.afshin.netup;

import java.security.MessageDigest;
import java.util.Random;

/**
 * Created by Afshin on 9/6/2015.
 */
public class Coding {

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

// ____________________________________________________________________
    public static String makeMD5(String text)
    {
        String result = "Nothing";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            result = bytesToHex(messageDigest.digest(text.getBytes()));
        }catch (Exception e)
        {
            result = new Random().nextInt() + " TEMP.";
            e.printStackTrace();
        }

        return result;
    }

// ____________________________________________________________________

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
// ____________________________________________________________________
}
