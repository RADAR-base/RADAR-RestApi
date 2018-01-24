package org.radarcns.service;

import org.radarcns.managementportal.Source;
import org.radarcns.managementportal.SourceTypeIdentifier;

public class SourceTypeService {

    public static SourceTypeIdentifier getSourceTypeIdFromSource(Source source) {
        return new SourceTypeIdentifier(source.getSourceTypeProducer() , source
                .getSourceTypeModel() ,source.getSourceTypeCatalogVersion());
    }

}
