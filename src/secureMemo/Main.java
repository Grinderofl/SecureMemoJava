package secureMemo;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Main {

    private static String keyFile = "keys.xml";

    public static void main(String[] args) {
        SecureMemo secureMemo = new SecureMemo(keyFile);
        secureMemo.Run();

    }

}
