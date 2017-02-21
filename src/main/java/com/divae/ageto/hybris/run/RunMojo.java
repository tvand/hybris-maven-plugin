package com.divae.ageto.hybris.run;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.divae.ageto.hybris.install.extensions.Extension;
import com.divae.ageto.hybris.utils.HybrisConstants;
import com.divae.ageto.hybris.utils.Utils;

/**
 * Create symbolic links between original Hybris installation and Maven reactor structure
 *  
 * Created by mhaagen on 05.09.2016.
 */
public class RunMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(property = "hybris.workDirectory", defaultValue = ".")
    private String workDirectory;

    protected void setWorkDirectory(final File workDirectory) {
        this.workDirectory = workDirectory.toString();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println(project.getModel().getBuild().getDirectory());
        
        final File hybrisReactorDir = new File(workDirectory);
        final File hybrisFakeDir = new File(hybrisReactorDir, HybrisConstants.HYBRIS_FAKE_DIRECTORY);
        final File platformDirectory = new File(hybrisFakeDir, "bin/platform");

        Utils.createSymLink(new File(platformDirectory, "pom.xml"), new File(hybrisReactorDir, "pom.xml"));

        for (Extension extension : Utils.readExtensionsFromReactorModules(hybrisReactorDir)) {
            final File baseDirectory = new File(hybrisFakeDir, extension.getBaseDirectory().toString());
            final File originalLocation = extension.getOriginalLocation();

            Utils.createSymLink(new File(baseDirectory, "pom.xml"), new File(originalLocation, "pom.xml"));

            extension.getName();
        }
    }

}
