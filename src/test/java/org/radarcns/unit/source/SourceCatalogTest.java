package org.radarcns.unit.source;

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;
import static org.radarcns.avro.restapi.source.SourceType.PEBBLE;

import org.junit.Test;
import org.radarcns.source.SourceCatalog;

/**
 * Created by francesco on 05/03/2017.
 */
public class SourceCatalogTest {

    @Test(expected = UnsupportedOperationException.class)
    public void sourceCatalogTest() {
        assertEquals(EMPATICA, SourceCatalog.getInstance(EMPATICA).getType());

        SourceCatalog.getInstance(PEBBLE).getType();
    }

}
