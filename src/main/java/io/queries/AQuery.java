package io.queries;

import submodule.data.ChartConfig;

import java.util.Map;

/**
 * @author fhanssen
 * All query classes need to to implement this abstract class, in order to ensure they have all expected methods.
 * The abstracte class holds the needed openbis credentials.
 */
public abstract class AQuery{

    //TODO maybe add opebis credentials ere again since everyone is using them
    abstract public  Map<String, ChartConfig> query();

    //TODO 1: implement this class to your own query class (see OrganismCountPresenter for an example)


}
