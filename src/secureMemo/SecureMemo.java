package secureMemo;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class SecureMemo {

    private Config config;
    private XStream xstream;
    private File configFile;
    private Crypto crypto;

    public SecureMemo(String keyFile) {
        xstream = new XStream(new DomDriver());
        xstream.alias("config", Config.class);
        xstream.alias("entry", Entry.class);
        xstream.alias("message", Message.class);

        configFile = new File(keyFile);
        if(!configFile.exists())
            offerNewConfigFileGeneration();
        else
            config = (Config)xstream.fromXML(configFile);

        crypto = new Crypto(xstream, config);
    }

    public void Run() {
        String message = "Hello world";

        Message msg = crypto.Encrypt(message, config.entry.fingerprint);
        String xml = xstream.toXML(msg);
        PrintWriter out = null;
        try {
            out = new PrintWriter("messagedata.smx");
            out.print(xml);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null)
                out.close();
        }
    }

    private void offerNewConfigFileGeneration(){
        System.out.println("Keyfile doesn't exist. Would you like to generate new one? y/n");
        if(Utils.getString() == "n")
            Utils.exitWithError(3);
        config = new Config();
        config.entry = new Entry();
        config.entry.hint = "My hint";

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
            kpg.initialize(4096, sr);

            KeyPair keys = kpg.generateKeyPair();

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec publicKeySpec = keyFactory.getKeySpec(keys.getPublic(), RSAPublicKeySpec.class);
            RSAPrivateKeySpec privateKeySpec = keyFactory.getKeySpec(keys.getPrivate(), RSAPrivateKeySpec.class);

            config.entry.privateKey = privateKeySpec.getPrivateExponent() + "|" + privateKeySpec.getModulus();
            config.entry.publicKey = publicKeySpec.getPublicExponent() + "|" + publicKeySpec.getModulus();
            config.entry.fingerprint = Crypto.Signature(config.entry.publicKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        String xmlString = xstream.toXML(config);
        PrintWriter out = null;
        try {
            out = new PrintWriter(configFile.getName());
            out.print(xmlString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(out != null)
                out.close();
        }
    }

}
