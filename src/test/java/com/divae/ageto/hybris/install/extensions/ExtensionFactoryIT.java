package com.divae.ageto.hybris.install.extensions;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.divae.ageto.hybris.utils.EnvironmentUtils;

/**
 * @author Marvin Haagen
 */
public class ExtensionFactoryIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionFactoryIT.class);

    @Test
    public void getExtensionsFromPlatformTest() {
        final File hybrisInstallationDirectory = EnvironmentUtils.getHybrisInstallationDirectory();
        final Set<Extension> extensions = ExtensionFactory.getExtensions(hybrisInstallationDirectory);
        assertTrue(extensions.size() > 0);
        LOGGER.trace(String.format("Extensions found: %d", extensions.size()));

        if (LOGGER.isTraceEnabled()) {
            for (final Extension extension : extensions) {
                printExtension(extension, "  ");
            }
        }

        Set<Extension> transitiveExtensions = ExtensionFactory.getTransitiveExtensions(extensions);
        assertTrue(transitiveExtensions.size() > 0);
        LOGGER.trace(String.format("Transitive extensions found: %d", transitiveExtensions.size()));
    }

    private void printExtension(Extension extension, String indent) {
        LOGGER.trace(String.format("%s%s", indent, extension.getName()));
        for (Extension dependency : extension.getDependencies()) {
            printExtension(dependency, indent + "  ");
        }
    }

}
