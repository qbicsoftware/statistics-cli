package life.qbic.io.queries;

import submodule.data.ChartConfig;

import java.util.Map;

/**
 * @author fhanssen
 * All query classes need to to implement this abstract class, in order to ensure they have all expected methods.
 * The abstracte class holds the needed openbis credentials.
 */
public abstract class AQuery{

    abstract public  Map<String, ChartConfig> query();

    //This kills the efficiency a bit because downloaded open bis data as to be iterate twice. However,
    // it should serve as a reminder to not count results from blacklisted spaces, which IMO is more important.
    abstract void removeBlacklistedSpaces();

    //TODO 1: implement this class to your own query class (see OrganismCountPresenter for an example)


}
