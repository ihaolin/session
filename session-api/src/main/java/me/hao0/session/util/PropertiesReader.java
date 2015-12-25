package me.hao0.session.util;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Properties Reader
 */
public abstract class PropertiesReader {

    /**
     * read properties from classpath
     * @param classpath properties file classpath
     * @return Properties object
     * @throws java.io.IOException
     */
    public static final Properties read(String classpath) throws IOException{
        final URL url = Resources.getResource(classpath);
        final ByteSource byteSource = Resources.asByteSource(url);
        final Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = byteSource.openBufferedStream();
            properties.load(inputStream);
        } catch (final IOException ioException) {
            ioException.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException ioException) {
                    throw ioException;
                }
            }
        }
        return properties;
    }
}