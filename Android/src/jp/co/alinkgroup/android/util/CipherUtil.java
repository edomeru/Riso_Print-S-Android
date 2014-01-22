/*
 * Copyright (c) 2014 All rights reserved.
 *
 * CipherUtil.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.alinkgroup.android.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class CipherUtil {
    
    private CipherUtil() {
        // Avoid initialization
    }
    
    public static String encrypt(String input, String key) {
        String encryptedString = "";
        
        if (InputCheckerUtil.isValid(input)) {
            try {
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                
                encryptedString = Base64.encodeToString(cipher.doFinal(input.getBytes()), Base64.DEFAULT);
            } catch (IllegalBlockSizeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (BadPaddingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return encryptedString;
    }
    
    public static String decrypt(String input, String key) {
        Cipher cipher;
        String decryptedString = "";
        if (InputCheckerUtil.isValid(input)) {
            try {
                cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
                SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                decryptedString = new String(cipher.doFinal(Base64.decode(input.getBytes(), Base64.DEFAULT)));
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (BadPaddingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return decryptedString;
    }
    
}
