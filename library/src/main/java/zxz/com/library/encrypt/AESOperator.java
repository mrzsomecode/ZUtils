package zxz.com.library.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * AES CBC加密
 */
public class AESOperator {

    public static AESOperator getInstance() {
        return Nested.instance;
    }

    //于内部静态类只会被加载一次，故该实现方式时线程安全的！
    private static class Nested {
        private static AESOperator instance = new AESOperator();
    }

    /**
     * 加密
     *
     * @param content
     * @param key
     * @param vector
     * @return
     * @throws Exception
     */
    public String encrypt(String content, String key, String vector) throws Exception {
        if (key == null) {
            return null;
        }
        if (key.length() != 16) {
            return null;
        }
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        IvParameterSpec iv = new IvParameterSpec(vector.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(content.getBytes("UTF-8"));
        return Base64.encode(encrypted);// 此处使用BASE64做转码。
    }

    /**
     * 解密
     *
     * @param content
     * @param key
     * @param vector
     * @return
     * @throws Exception
     */
    public String decrypt(String content, String key, String vector) throws Exception {
        try {
            if (key == null) {
                return null;
            }
            if (key.length() != 16) {
                return null;
            }
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            IvParameterSpec iv = new IvParameterSpec(vector.getBytes());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = Base64.decode(content);// 先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original, "UTF-8");
            return originalString;
        } catch (Exception ex) {
            return null;
        }
    }

}
