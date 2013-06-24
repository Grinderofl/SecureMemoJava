package secureMemo;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

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
        Message message = new Message();

        try {
            String publicKey = null;
            for(Entry e : _config.keys){
                if(e.fingerprint.equalsIgnoreCase(fingerprint)) {
                    publicKey = e.publicKey;
                    break;
                }
            }

            kf = KeyFactory.getInstance("RSA");
            String[] em = publicKey.split("\\|");

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(em[1]), new BigInteger(em[0]));
            PublicKey pub = kf.generatePublic(publicKeySpec);

            SecureRandom sr = new SecureRandom();
            byte[] key = new byte[16];
            sr.nextBytes(key);

            message.key = Base64.encodeBase64String(rsaEncrypt(key, pub));

            try {
                Cipher cipher = Cipher.getInstance("AES");
                SecretKey encryptionKey = new SecretKeySpec(key, "AES");
                cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
                message.data = Base64.encodeBase64String(cipher.doFinal(data.getBytes("UTF-8")));
                message.fingerprint = _config.entry.fingerprint;
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        return message;
    }

    public byte[] rsaEncrypt(byte[] data, PublicKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }
}
