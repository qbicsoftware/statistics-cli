package life.qbic.io.queries.utils.lexica;

public enum OmicsType {
    MULTI_OMICS("Multi-omics");


    private final String omics;

    OmicsType(String omics){
        this.omics = omics;
    }

    @Override
    public String toString() {
        return omics;
    }
}
