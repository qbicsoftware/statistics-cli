package io.queries.utils;

public enum OpenBisTerminology {
    NCBI_ORGANISM("Q_NCBI_ORGANISM"),
    BIO_ENTITY("Q_BIOLOGICAL_ENTITY");

    private final String term;

    OpenBisTerminology(String term){
        this.term = term;
    }

    public String get() {
        return term;
    }
}
