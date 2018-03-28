package io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import model.data.ChartConfig;

import java.util.Map;

public abstract class AQuery {

    private final IApplicationServerApi v3;
    private final String sessionToken;

    AQuery(IApplicationServerApi v3, String sessionToken){
        this.v3 = v3;
        this.sessionToken = sessionToken;
    }

    IApplicationServerApi getV3() {
        return v3;
    }

    String getSessionToken() {
        return sessionToken;
    }

    abstract public Map<String, ChartConfig> query();

}
