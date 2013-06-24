package secureMemo;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.*;

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

        crypto = new Crypto(xstream, config);

        configFile = new File(keyFile);
        if(!configFile.exists())
            offerNewConfigFileGeneration();
        else
            config = (Config)xstream.fromXML(configFile);
    }

    public void Run() {

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
            kpg.initialize(2048, sr);
            KeyPair keys = kpg.generateKeyPair();
            PrivateKey priv = keys.getPrivate();
            PublicKey pub = keys.getPublic();
            config.entry.privateKey = Base64.encodeBase64String(priv.getEncoded());
            config.entry.publicKey = Base64.encodeBase64String(pub.getEncoded());
            config.entry.fingerprint = Crypto.Signature(config.entry.publicKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
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
