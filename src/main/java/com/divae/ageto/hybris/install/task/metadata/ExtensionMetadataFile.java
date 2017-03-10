package com.divae.ageto.hybris.install.task.metadata;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.divae.ageto.hybris.install.extensions.Extension;
import com.divae.ageto.hybris.install.extensions.binary.ClassFolder;
import com.divae.ageto.hybris.install.extensions.binary.ExtensionBinary;
import com.divae.ageto.hybris.install.extensions.binary.JARArchive;
import com.divae.ageto.hybris.install.extensions.binary.None;
import com.divae.ageto.hybris.utils.HybrisConstants;
import com.google.common.base.Throwables;

/**
 * @author Marvin Haagen
 */
public enum ExtensionMetadataFile {

    ;

    public static File createMetadataFile(final Extension extension, final File workDirectory) {
        try {
            final File metadataFolder = MetadataFile.getFilePath(extension.getName());
            final File metadataFile = new File(new File(workDirectory, metadataFolder.toString()),
                    MetadataFile.getFileName(extension.getName()).toString());
            metadataFile.getParentFile().mkdirs();
            metadataFile.createNewFile();
            final Properties properties = new Properties();

            properties.setProperty(HybrisConstants.HYBRIS_EXTENSION_NAME, extension.getName());
            properties.setProperty(HybrisConstants.HYBRIS_EXTENSION_DIRECTORY, extension.getBaseDirectory().toString());
            addExtensionBinaryProperties(properties, extension);
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(metadataFile))) {
                properties.store(outputStream, null);
            }
            return metadataFile;
        } catch (IOException exception) {
            throw Throwables.propagate(exception);
        }
    }

    public static Extension readMetadataFile(final File workDirectory, final String extensionName) {
        String metadataFolder = MetadataFile.getFilePath(extensionName).toString();
        final File metadataFile = new File(new File(workDirectory, metadataFolder),
                MetadataFile.getFileName(extensionName).toString());
        Properties properties = new Properties();
        try (final InputStream inputStream = new FileInputStream(metadataFile)) {
            properties.load(inputStream);
            final String name = properties.getProperty(HybrisConstants.HYBRIS_EXTENSION_NAME);
            final File baseFile = new File(properties.getProperty(HybrisConstants.HYBRIS_EXTENSION_DIRECTORY));
            final ExtensionBinary binary = getExtensionBinary(properties);
            return new Extension(baseFile, name, binary);
        } catch (final IOException exception) {
            throw Throwables.propagate(exception);
        }
    }

    private static ExtensionBinary getExtensionBinary(final Properties properties) {
        final String type = properties.getProperty(HybrisConstants.HYBRIS_EXTENSION_BINARY_TYPE);
        if (type.equals(new None().getType())) {
            return new None();
        }
        if (type.equals(new JARArchive(new File("")).getType())) {
            return new JARArchive(new File(properties.getProperty(HybrisConstants.HYBRIS_EXTENSION_BINARY_PATH)));
        }
        if (type.equals(new ClassFolder(new File("")).getType())) {
            return new ClassFolder(new File(properties.getProperty(HybrisConstants.HYBRIS_EXTENSION_BINARY_PATH)));
        }
        throw new RuntimeException("Invalid type: " + type);
    }

    private static void addExtensionBinaryProperties(final Properties config, final Extension extension) {
        config.setProperty(HybrisConstants.HYBRIS_EXTENSION_BINARY_TYPE, extension.getBinary().getType());
        if (extension.getBinary().getClass() != None.class) {
            config.setProperty(HybrisConstants.HYBRIS_EXTENSION_BINARY_PATH, extension.getBinary().getExtensionBinaryPath().toString());
        }
    }

}
