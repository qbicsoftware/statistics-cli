package io.queries;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import model.data.ChartConfig;

import java.util.Map;

/**
 * @author fhanssen
 * All query classes need to to implement this abstract class, in order to ensure they have all expected methods.
 * The abstracte class holds the needed openbis credentials.
 */
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

    abstract public Map<String, ChartConfig> query() throws Exception;

    //TODO 1: extend this class to your own query class (see OrganismCountPresenter for an example)

}
