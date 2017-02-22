package com.divae.ageto.hybris;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author Klaus Hauschild
 */
public abstract class AbstractHybrisDirectoryMojo extends AbstractMojo
{

    /**
     * The platform directory of the Hybris commerce suite
     */
    @Parameter(property = "hybris.hybrisDirectory", defaultValue = ".")
    private File hybrisDirectory;

    /**
     * The project root directory
     */
    @Parameter(defaultValue = "${basedir}", readonly = true, required = true)
    private File projectRootDir;

    /**
     * Get the Hybris platform directory
     * 
     * @return a File object
     */
    protected File getHybrisDirectory()
    {
        return hybrisDirectory;
    }

    /**
     * Get the project root directory
     * 
     * @return a File object
     */
    protected File getBaseDirectory()
    {
        return projectRootDir;
    }
}
