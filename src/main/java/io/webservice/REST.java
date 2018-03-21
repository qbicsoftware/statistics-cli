package io.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class REST {

    public static InputStream call(String url) throws IOException{
        final HttpURLConnection connection = (HttpURLConnection) (new URL(url).openConnection());
        connection.setRequestMethod("GET");
        connection.connect();

        return connection.getInputStream();
    }

}
