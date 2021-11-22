package net.querz.mca;

/**
 * Represents an Entities data mca file (one that lives in the /entities folder). Entity mca files were added in 1.17
 * but this class can be used to read older /region/*.mca files as well - for an example of this see
 * EntitiesMCAFileTest testLoadingOldRegionMcaAsEntityMca
 */
public class EntitiesMCAFile extends MCAFileBase<EntitiesChunk> {
    public EntitiesMCAFile(int regionX, int regionZ) {
        super(regionX, regionZ);
    }

    public EntitiesMCAFile(int regionX, int regionZ, int defaultDataVersion) {
        super(regionX, regionZ, defaultDataVersion);
    }

    public EntitiesMCAFile(int regionX, int regionZ, DataVersion defaultDataVersion) {
        super(regionX, regionZ, defaultDataVersion);
    }

    @Override
    public Class<EntitiesChunk> chunkClass() {
        return EntitiesChunk.class;
    }

    @Override
    public EntitiesChunk createChunk() {
        EntitiesChunk chunk = new EntitiesChunk(getDefaultChunkDataVersion());
        return chunk;
    }
}
