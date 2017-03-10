package com.divae.ageto.hybris.clean;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.FileUtils;

import com.divae.ageto.hybris.AbstractHybrisDirectoryMojo;
import com.divae.ageto.hybris.utils.HybrisConstants;

/**
 * The clean goal deletes some Hybris-specific build files and directories
 *
 * @author tv@apache.org
 */
@Mojo(name = "clean", defaultPhase = LifecyclePhase.CLEAN)
public class CleanMojo extends AbstractHybrisDirectoryMojo
{
    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        final File touchDirectory = new File(getHybrisDirectory(), "../../temp/hybris/touch");
        final String touchWildCard = getExtensionName() + "_*touch";
        
        try
        {
            List<File> touchFiles = FileUtils.getFiles(touchDirectory, touchWildCard, "", true);
            for (File f : touchFiles)
            {
                getLog().info("Deleting " + f.getCanonicalPath());
                if (!f.delete())
                {
                    getLog().warn("Failed to delete " + f.getCanonicalPath());
                }
            }
        }
        catch (IOException e)
        {
            throw new MojoFailureException("Could not remove touch files from " + touchDirectory, e);
        }
        
        final File gensrcDirectory = new File(getBaseDirectory(), HybrisConstants.HYBRIS_GENSRC_DIRECTORY);
        if (gensrcDirectory.exists())
        {
            try
            {
                getLog().info("Deleting " + gensrcDirectory);
                FileUtils.deleteDirectory(gensrcDirectory);
            }
            catch (IOException e)
            {
                throw new MojoFailureException("Could not remove directory " + gensrcDirectory, e);
            }
        }
    }
}
