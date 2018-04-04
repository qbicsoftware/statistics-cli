package io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import io.webservice.REST;
import model.data.ChartConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class AvailablePipelinesQuery extends AQuery{


    //TODO think this through
    public AvailablePipelinesQuery(IApplicationServerApi v3, String sessionToken) {
        super(v3, sessionToken);

    }


    @Override
    public Map<String, ChartConfig> query(){


        try (BufferedReader rd = new BufferedReader(new InputStreamReader(REST.call("https://api.github.com/orgs/qbicsoftware/repos?per_page=200")))){
            String line;
            while ((line = rd.readLine()) != null) {
                System.out.println(line);

            }

        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
