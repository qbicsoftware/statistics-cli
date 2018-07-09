package submodule.lexica;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author fhanssen
 * This enum class holds commonly used abbreviations suc DNA, RNA, etc.
 */
public enum CommonAbbr {
    DNA("DNA"),
    RNA("RNA"),
    NGS("NGS");


    private final String abbreviation;

    private static final List<String> enumList = Arrays.asList(Stream.of(CommonAbbr.values()).map(CommonAbbr::name).toArray(String[]::new));


    CommonAbbr(String abbreviation){
        this.abbreviation = abbreviation;
    }

    @Override
    public String toString(){
        return this.abbreviation;
    }

    public static List<String> getList(){
        return enumList;
    }
}
