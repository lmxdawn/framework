package com.bizzan.bitrade.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.Key;

public class DESEncryptUtil {


    private static final String encoding = "UTF-8";

    private static final String defaultKey = "GwPODtRHm4UFX3J9";

    // public static void main(String[] args) {
    //     try {
    //         String text = "Buffer.BlockCopy";// 明文
    //         String key = "x9QYOTSCkE1CNFU6";// 长度控制为16，作为3DES加密用的key
    //         String encryptStr = EncryptData(text, key);// 3DES加密结果
    //
    //         System.out.println("明文：" + text);
    //         System.out.println("密钥：" + key);
    //         System.out.println("密文：" + encryptStr);
    //         System.out.println("解密：" + DecryptData(encryptStr, key));
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    /**
     * DESede加密，key长度为16
     *
     * @param plainText 明文
     * @return DESede加密结果
     */
    public static String EncryptData(String plainText) throws Exception {
        return EncryptData(plainText, defaultKey);
    }

    /**
     * DESede加密，key长度为16
     *
     * @param plainText 明文
     * @param key       密钥
     * @return DESede加密结果
     */
    public static String EncryptData(String plainText, String key) throws Exception {
        byte[] keyBytes = getKey(key);

        Key deskey = null;
        DESedeKeySpec spec = new DESedeKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        deskey = keyFactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");

        cipher.init(Cipher.ENCRYPT_MODE, deskey);
        byte[] bOut = cipher.doFinal(plainText.getBytes(encoding));

        return Base64.encodeBase64String(bOut);
    }

    /**
     * DESede解密，key长度为16
     *
     * @param input DESede加密的结果
     * @param key   密钥
     * @return DESede解密结果
     */
    public static String DecryptData(String input, String key) throws Exception {
        byte[] keyBytes = getKey(key);
        Key deskey = null;
        DESedeKeySpec spec = new DESedeKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        deskey = keyFactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");

        cipher.init(Cipher.DECRYPT_MODE, deskey);
        byte[] bOut = cipher.doFinal(Base64.decodeBase64(input));

        return new String(bOut, encoding);
    }

    /**
     * DESede解密，key长度为16
     *
     * @param input DESede加密的结果
     * @return DESede解密结果
     */
    public static String DecryptData(String input) throws Exception {
        return DecryptData(input, defaultKey);
    }

    public static byte[] getKey(String key) throws UnsupportedEncodingException {
        byte[] keyBytes = key.getBytes(encoding);
        if (keyBytes.length == 16) { // short key ? .. extend to 24 byte key
            byte[] tmpKey = new byte[24];
            System.arraycopy(keyBytes, 0, tmpKey, 0, 16);
            System.arraycopy(keyBytes, 0, tmpKey, 16, 8);
            keyBytes = tmpKey;
        }
        return keyBytes;
    }

}