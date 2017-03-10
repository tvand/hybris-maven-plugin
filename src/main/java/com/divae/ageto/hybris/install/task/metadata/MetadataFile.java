package com.divae.ageto.hybris.install.task.metadata;

import java.io.File;

import com.divae.ageto.hybris.utils.HybrisConstants;

/**
 * @author Marvin Haagen
 */
public enum MetadataFile {

    ;

    public static File getFileName(String extensionName) {
        return new File(String.format("%s-metadata.properties", extensionName));
    }

    public static File getFilePath(String extensionName) {
        return new File(String.format("%s/" + HybrisConstants.DEFAULT_SRC_MAIN_RESOURCES, extensionName));
    }

}
