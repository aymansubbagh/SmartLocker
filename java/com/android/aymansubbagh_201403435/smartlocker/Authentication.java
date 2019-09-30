
import android.content.Context;
import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Authentication {

    private static byte[] SecretKey = {0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f,
            0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f};

    SharedPreferences pref;
    String AesKey;

    Context con;

    public Authentication(Context con) {
        this.con = con;

        pref = con.getSharedPreferences("SmartLocker", Context.MODE_PRIVATE);
        AesKey = pref.getString("AesKey", "");

    }


    public byte[] BYTE_AESencrypt(byte[] mesg)
            throws UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        byte[] keybytes = AesKey.getBytes("UTF-8");
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(SecretKey);
        SecretKeySpec newKey = new SecretKeySpec(keybytes, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(mesg);
    }


    public byte[] BYTE_AESdecrypt(byte[] mesg, String Key, int i)
            throws UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        byte[] keybytes = null;
        if (i == 0) {
            keybytes = AesKey.getBytes("UTF-8");
        } else {
            keybytes = Key.getBytes("UTF-8");
        }
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(SecretKey);
        SecretKeySpec newKey = new SecretKeySpec(keybytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(mesg);
    }


}
