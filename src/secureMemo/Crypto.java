package secureMemo;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created with IntelliJ IDEA.
 * User: nero
 * Date: 24/06/13
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */
public class Crypto {

    private XStream _xstream;
    private Config _config;


    public Crypto(XStream xs, Config config) {
        _xstream = xs;
        _config = config;
    }

    public static String Signature(String data) {
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = data.getBytes("UTF-8");
            final byte[] r = md.digest(bytes);
            result = new String(Hex.encodeHex(r));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Message Encrypt(String data, String fingerprint) {
        KeyFactory kf = null;
        try {
            kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec priv = new PKCS8EncodedKeySpec(Base64.decodeBase64(_config.entry.privateKey));
            PrivateKey pk = kf.generatePrivate(priv);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }


        for(Entry e : _config.keys){
            if(e.fingerprint == fingerprint)
                 return null;
        }


        SecureRandom sr = new SecureRandom();
        byte[] key = new byte[32];
        byte[] result;
        sr.nextBytes(key);

        Message message = new Message();
        message.key = Base64.encodeBase64String(key);

        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKey encryptionKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
            message.data = Base64.encodeBase64String(cipher.doFinal(data.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return message;
    }
}
