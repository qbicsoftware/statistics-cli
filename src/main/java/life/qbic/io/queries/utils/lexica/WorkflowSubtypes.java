package life.qbic.io.queries.utils.lexica;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author fhanssen
 *
 */
public enum WorkflowSubtypes {
    DNA,
    RNA,
    PX,
    MX,
    LX;

    private static final List<String> enumList = Arrays.asList(Stream.of(WorkflowSubtypes.values()).map(WorkflowSubtypes::name).toArray(String[]::new));

    public static List<String> getList(){
        return enumList;
    }
}
