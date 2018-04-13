package io.queries;

import submodule.data.ChartConfig;

import java.util.Map;

/**
 * @author fhanssen
 * All query classes need to to implement this abstract class, in order to ensure they have all expected methods.
 * The abstracte class holds the needed openbis credentials.
 */
public interface IQuery {


    Map<String, ChartConfig> query();

    //TODO 1: extend this class to your own query class (see OrganismCountPresenter for an example)

}
