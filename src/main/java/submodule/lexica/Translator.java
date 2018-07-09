package submodule.lexica;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author fhanssen
 * This enum class holds commonly used abbreviations suc DNA, RNA, etc.
 */
public enum Translator {
    CELL_LYSATE("CELL_LYSATE", "Cell Lysate"),
    SMALLMOLECULES("SMALLMOLECULES", "Small Molecules"),
    MA("MA", "Microarray"),
    MS("MS", "Mass Spectrometry");

    private final String original;
    private final String translation;

    private static final List<String> enumList = Arrays.asList(Stream.of(Translator.values()).map(Translator::name).toArray(String[]::new));


    Translator(String original, String translation){
        this.original = original;
        this.translation = translation;
    }

    @Override
    public String toString(){
        return this.original;
    }

    public String getTranslation(){
        return this.translation;
    }

    public static List<String> getList(){
        return enumList;
    }
}
