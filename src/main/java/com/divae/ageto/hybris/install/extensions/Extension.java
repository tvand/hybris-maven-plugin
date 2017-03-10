package com.divae.ageto.hybris.install.extensions;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.divae.ageto.hybris.install.extensions.binary.ExtensionBinary;
import com.divae.ageto.hybris.install.extensions.binary.None;
import com.divae.ageto.hybris.utils.HybrisConstants;
import com.google.common.base.Throwables;

/**
 * @author Marvin Haagen
 */
public class Extension {

    private final File            baseDirectory;
    private final String          name;
    private final ExtensionBinary binary;
    private final Set<Extension>  dependencies;
    private File                  originalLocation;

    public Extension(final File baseDirectory, final String name, final ExtensionBinary binary) {
        this(baseDirectory, name, binary, null);
    }

    public Extension(final File baseDirectory, final String name, final ExtensionBinary binary,
            final Set<Extension> dependencies) {
        this.baseDirectory = baseDirectory;
        this.name = name;
        this.binary = binary;
        this.dependencies = dependencies;
    }

    public File getOriginalLocation() {
        return originalLocation;
    }

    public void setOriginalLocation(File originalLocation) {
        this.originalLocation = originalLocation;
    }

    public File getExternalDependenciesXML(final File hybrisDirectory) {
        if (new File(hybrisDirectory, new File(baseDirectory, HybrisConstants.HYBRIS_EXTERNAL_DEPENDENCIES_XML).toString()).exists()) {
            return new File(hybrisDirectory, new File(baseDirectory, HybrisConstants.HYBRIS_EXTERNAL_DEPENDENCIES_XML).toString());
        }
        if (!(binary instanceof None)) {
            if (new File(binary.getExtensionBinaryPath(), HybrisConstants.HYBRIS_EXTERNAL_DEPENDENCIES_XML).exists()) {
                return new File(binary.getExtensionBinaryPath(), HybrisConstants.HYBRIS_EXTERNAL_DEPENDENCIES_XML);
            }
        }
        return findExternalDependenciesXML(hybrisDirectory);
    }

    private File findExternalDependenciesXML(final File hybrisDirectory) {
        final File[] externalDependenciesFile = { null };
        try {
            Files.walkFileTree(new File(hybrisDirectory, baseDirectory.toString()).toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.endsWith(HybrisConstants.HYBRIS_EXTERNAL_DEPENDENCIES_XML)) {
                        externalDependenciesFile[0] = file.toFile();
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        return externalDependenciesFile[0];
    }

    File getExtensionDirectory() {
        return new File(getName());
    }

    public File getSourcesDirectory() {
        return new File(getExtensionDirectory(), HybrisConstants.DEFAULT_SRC_MAIN_JAVA);
    }

    public File getTestSourcesDirectory() {
        return new File(getExtensionDirectory(), HybrisConstants.DEFAULT_SRC_TEST_JAVA);
    }

    public File getResourcesDirectory() {
        return new File(getExtensionDirectory(), HybrisConstants.DEFAULT_SRC_MAIN_RESOURCES);
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public String getName() {
        return name;
    }

    public ExtensionBinary getBinary() {
        return binary;
    }

    public Set<Extension> getDependencies() {
        return dependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Extension extension = (Extension) o;
        return Objects.equals(name, extension.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(name);
        if (dependencies != null) {
            builder.append(" ");
            builder.append(dependencies.stream().map(Extension::getName).collect(Collectors.toList()));
        }
        return builder.toString();
    }

}
