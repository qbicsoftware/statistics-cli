package io.queries.utils.lexica;

/**
 * @author fhanssen
 * This enum class holds the OpenBis terminology used at QBiC. As it may not always be obvious how it is named, this enum was created.
 */
public enum OpenBisTerminology {
    NCBI_ORGANISM("Q_NCBI_ORGANISM"),
    BIO_ENTITY("Q_BIOLOGICAL_ENTITY"),
    TEST_SAMPLE("Q_TEST_SAMPLE"),
    SAMPLE_TYPE("Q_SAMPLE_TYPE");

    private final String term;

    OpenBisTerminology(String term){
        this.term = term;
    }

    @Override
    public String toString() {
        return term;
    }
}
