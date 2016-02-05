package ir.afshin.netup;

import android.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by afshin on 6/8/15.
 */
public class AuthManager {

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

// ____________________________________________________________________
    public AuthManager()
    {

    }

// ____________________________________________________________________

    public String getBase64_Hmac_SHA256(String value, String privateKey)
    {
        //TODO use get_Hmac_SHA256(String value, String privateKey) which returns a byte array.
        return new String(Base64.encode(get_Hmac_SHA256_HexString(value, privateKey).getBytes(), Base64.NO_WRAP));
    }

// ____________________________________________________________________
    public String get_Hmac_SHA256_HexString(String value, String privateKey)
    {
        return bytesToHex(get_Hmac_SHA256(value, privateKey));
    }
// ____________________________________________________________________
    public byte[] get_Hmac_SHA256(String value, String privateKey)
    {
        byte[] hashedData = null;

        try{

            String type = "HmacSHA256";
            SecretKeySpec secret = new SecretKeySpec(privateKey.getBytes(), type);
            Mac mac = Mac.getInstance(type);
            mac.init(secret);
            hashedData = mac.doFinal(value.getBytes());

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return hashedData;
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
