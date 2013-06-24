package secureMemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {
    public static void exitWithError(int code)    {
        System.out.println("Error code: " + code);
        waitForKeyPress();
        System.exit(code);
    }

    public static void waitForKeyPress()    {
        System.out.println("Press any key to exit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getString() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String result = null;
        try {
            result = br.readLine();
        } catch(IOException e) {
            exitWithError(4);
        }
        return result;
    }
}
