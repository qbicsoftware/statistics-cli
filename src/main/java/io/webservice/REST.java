package io.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author fhanssen
 */
public final class REST {

    public static InputStream call(String url) throws IOException{
        final HttpURLConnection connection = (HttpURLConnection) (new URL(url).openConnection());
        connection.setRequestMethod("GET");
        connection.connect();
        return connection.getInputStream();
    }

    public static InputStream call(String url, String headerKey, String headerValue) throws IOException{
        final HttpURLConnection connection = (HttpURLConnection) (new URL(url).openConnection());
        connection.setRequestMethod("GET");
        connection.setRequestProperty(headerKey, headerValue );
        connection.connect();

        return connection.getInputStream();
    }
}
