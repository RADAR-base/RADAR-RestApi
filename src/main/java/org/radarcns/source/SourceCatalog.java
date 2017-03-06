package org.radarcns.source;

import org.radarcns.avro.restapi.source.SourceType;

/**
 * All supported sources specifications.
 */
public class SourceCatalog {

    private static class EmpaticaInstanceHolder {
        private static final Empatica instance = new Empatica();
    }

    /**
     * Returns source's SourceDefinition.
     * @param source sourceType involved in the interaction
     * @return the SourceDefinition related to the input
     * @see {@link SourceDefinition}
     */
    public static SourceDefinition getInstance(SourceType source) {
        switch (source) {
            case ANDROID: break;
            case BIOVOTION: break;
            case EMPATICA: return EmpaticaInstanceHolder.instance;
            case PEBBLE: break;
            default: break;
        }

        throw new UnsupportedOperationException(source.name() + " is not"
            + " currently supported.");
    }

}
