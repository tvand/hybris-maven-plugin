package com.divae.ageto.hybris.codegenerator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.divae.ageto.hybris.AbstractHybrisDirectoryMojo;

/**
 * The code-generator goal generates the classes needed for a Hybris instance,
 * namely the Model classes out of the <code>*-items.xml</code> files.
 * 
 * @author Klaus Hauschild
 */
@Mojo(name = "code-generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
class CodeGeneratorMojo extends AbstractHybrisDirectoryMojo
{
    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The target directory for Hybris generated classes
     */
    @Parameter(defaultValue = "${basedir}/gensrc", readonly = true, required = true)
    private File targetGenSrcDir;
    
    /* Code generator dependencies not available in Maven Repository */
    private static final String[] JAR_FILES = {
            "/bootstrap/bin/ybootstrap.jar",
            "/ext/core/bin/coreserver.jar"
    };
    
    /* 
     * Path of the models.jar file. This will be deleted by the code generator unconditionally
     * so we make a copy and restore it on exit 
     */
    private static final String MODEL_FILE_NAME = "/bootstrap/bin/models.jar";
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        
        // Save models.jar if it exists
        final File models_jar = new File(getHybrisDirectory(), MODEL_FILE_NAME);
        final File models_jar_bak = new File(getHybrisDirectory(), MODEL_FILE_NAME + ".bak");
        
        if (models_jar.exists())
        {
            try
            {
                FileUtils.copyFile(models_jar, models_jar_bak);
            }
            catch (IOException e)
            {
                throw new MojoFailureException("Could not make backup copy of " + models_jar, e);
            }
        }
        
        try
        {
            ClassLoader hybrisClassLoader = getHybrisClassLoader(getHybrisDirectory());
            System.setProperty("platform.extensions", project.getArtifactId());
            CodeGenerator.generate(getBaseDirectory(), getHybrisDirectory(), hybrisClassLoader);
            project.addCompileSourceRoot(targetGenSrcDir.toString());
        }
        catch (final Exception exception)
        {
            throw new MojoFailureException("Code generation failed!", exception);
        }
        finally
        {
            if (models_jar.exists())
            {
                if (!models_jar_bak.delete())
                {
                    getLog().warn("Could not remove " + models_jar_bak);
                }
            }
            else
            {
                try
                {
                    FileUtils.moveFile(models_jar_bak, models_jar);
                }
                catch (IOException e)
                {
                    throw new MojoFailureException("Could not restore backup copy from " + models_jar_bak, e);
                }
            }
        }
    }

    private ClassLoader getHybrisClassLoader(File hybrisDirPlatform) throws MalformedURLException
    {
        URL[] jarUrl = new URL[JAR_FILES.length];
        for (int i = 0; i < JAR_FILES.length; i++)
        {
            jarUrl[i] = new File(hybrisDirPlatform, JAR_FILES[i]).toURI().toURL(); 
        }
        
        URLClassLoader classLoader = URLClassLoader.newInstance(jarUrl, getClass().getClassLoader());
        
        return classLoader;
    }
}
