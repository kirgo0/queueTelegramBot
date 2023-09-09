package main.java.com.ffc.bot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

    private PropertiesReader(){}

    public static String getProperty(String key) {
        Properties properties = new Properties();

        try {
            InputStream inputStream = new FileInputStream("application.properties");
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key);
    }
}
