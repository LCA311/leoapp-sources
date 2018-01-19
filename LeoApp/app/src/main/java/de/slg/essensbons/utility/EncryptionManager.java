package de.slg.essensbons.utility;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import de.slg.leoapp.utility.Utils;

public class EncryptionManager {

    private final IvParameterSpec ivspec;
    private final SecretKeySpec   keyspec;
    private       Cipher          cipher;

    public EncryptionManager() {
        String iv = "15p60peADF4tT8u8";
        ivspec = new IvParameterSpec(iv.getBytes());
        String secretKey = "jHsj1C4XyXpEh7L9m0cVTLPgLU5QfXvh";
        keyspec = new SecretKeySpec(secretKey.getBytes(), "AES");
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Utils.logError(e);
        }
    }

    private static byte[] hexToBytes(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int    len    = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }

    public byte[] decrypt(String code) throws Exception {
        if (code == null || code.length() == 0)
            throw new Exception("Empty string");
        byte[] decrypted;
        try {
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            decrypted = cipher.doFinal(hexToBytes(code));
        } catch (Exception e) {
            throw new Exception("[decrypt] " + e.getMessage());
        }
        return decrypted;
    }
}