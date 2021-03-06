package com.divae.ageto.hybris.version;

import java.text.SimpleDateFormat;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.divae.ageto.hybris.AbstractHybrisDirectoryMojo;

/**
 * Output the version information of the Hybris commerce suite
 * 
 * @author Klaus Hauschild
 */
@Mojo(name = "version", requiresProject = false)
class VersionMojo extends AbstractHybrisDirectoryMojo {

    private final SimpleDateFormat prettyDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final HybrisVersion hybrisVersion = HybrisVersion.of(getHybrisDirectory());
        getLog().info(String.format("hybris version:..........%s", hybrisVersion.getVersion()));
        getLog().info(String.format("hybris api version:......%s", hybrisVersion.getApiVersion()));
        getLog().info(String.format("build date:..............%s", prettyDateFormat.format(hybrisVersion.getBuildDate())));
        getLog().info(String.format("release date:............%s", prettyDateFormat.format(hybrisVersion.getReleaseDate())));
    }

}
