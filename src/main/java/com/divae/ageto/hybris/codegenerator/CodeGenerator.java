package com.divae.ageto.hybris.codegenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;

import com.divae.ageto.hybris.utils.HybrisConstants;
import com.google.common.base.Throwables;

/**
 * @author Klaus Hauschild
 * @author tv@apache.org
 */
class CodeGenerator
{
    static void generate(final File hybrisReactorDir, final File hybrisDirPlatform, final ClassLoader hybrisClassLoader)
    {
        // final File hybrisFakeDirectory = HybrisFakeStructure.generate(hybrisReactorDir);
        invokeBootstrapCodeGenerator(hybrisReactorDir, hybrisDirPlatform, hybrisClassLoader);
        // createModelsArtifacts(hybrisDirPlatform, hybrisReactorDir);
    }

    private static void createModelsArtifacts(final File hybrisDirPlatform, final File hybrisReactorDir)
    {
        try
        {
            FileUtils.copyDirectory(new File(hybrisDirPlatform, HybrisConstants.HYBRIS_BOOTSTRAP_DIRECTORY + File.pathSeparator + HybrisConstants.HYBRIS_GENSRC_DIRECTORY),
                    new File(hybrisReactorDir, "models/" + HybrisConstants.DEFAULT_SRC_MAIN_JAVA));
        }
        catch (IOException e)
        {
            throw new RuntimeException(
                    String.format("Unable to copy files from [%s] to [%s]", hybrisDirPlatform, hybrisReactorDir), e);
        }
    }

    public static void invokeBootstrapCodeGenerator(final File hybrisReactorDir, final File hybrisDirPlatform, final ClassLoader hybrisClassLoader)
    {
        try
        {
            System.setProperty("platform.extensions.scan.dirs", hybrisReactorDir.getAbsolutePath());
            System.setProperty("platform.extensions.scan.maxdepth", "1");
            System.setProperty("platform.extensions.autoload", "false");
            
            final Class<?> bootstrapCodeGeneratorClass = 
                    Class.forName("de.hybris.bootstrap.codegenerator.CodeGenerator", true, hybrisClassLoader);
            final Method mainMethod = bootstrapCodeGeneratorClass.getMethod("main", String[].class);
            
            mainMethod.invoke(null, new Object[] { new String[] { hybrisDirPlatform.toString() } });
        }
        catch (final Exception exception)
        {
            throw Throwables.propagate(exception);
        }
    }
}
