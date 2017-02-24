package com.divae.ageto.hybris.hyx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.apache.maven.shared.mapping.MappingUtils;
import org.apache.maven.shared.utils.StringUtils;
import org.apache.maven.shared.utils.io.FileUtils;
import org.apache.maven.shared.utils.io.FileUtils.FilterWrapper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.interpolation.InterpolationException;

import com.divae.ageto.hybris.AbstractHybrisDirectoryMojo;
import com.divae.ageto.hybris.utils.ClassesPackager;

/**
 * The hyx goal packages a Hybris Extension into a .hyx file
 *
 * @author tv@apache.org
 * 
 *         Expected directory tree:
 * 
 *         <pre>
 * /bin
 * /lib
 * /resources
 * /testsrc
 * /web/webroot
 *         </pre>
 * 
 *         https://help.hybris.com/6.3.0/hcd/8b49cab88669101489be9ac91a5f1ebb.html
 */
@Mojo(name = "hyx", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class HyxMojo extends AbstractHybrisDirectoryMojo
{
    /**
     * The source resource directory for the Hybris Extension.
     */
    @Parameter(defaultValue = "${basedir}/resources", required = true)
    private File hyxResourceDirectory;

    /**
     * The source web directory for the Hybris Extension.
     */
    @Parameter(defaultValue = "${basedir}/web", required = true)
    private File hyxWebDirectory;

    /**
     * The source hmc directory for the Hybris Extension.
     */
    @Parameter(defaultValue = "${basedir}/hmc", required = true)
    private File hyxHMCDirectory;

    /**
     * A comma separated list of inclusion rules.
     */
    @Parameter(required = false)
    private String packagingIncludes;

    /**
     * A comma separated list of exclusion rules.
     */
    @Parameter(required = false)
    private String packagingExcludes;

    /**
     * The final name of the generated artifact.
     */
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    private String finalName;

    /**
     * The build target directory.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    /**
     * An optional classifier for the artifact.
     */
    @Parameter
    private String classifier;

    /**
     * Specify that the hyx sources should be filtered.
     */
    @Parameter(defaultValue = "false")
    private boolean filtering;

    /**
     * Filters (property files) to include during the interpolation of the
     * pom.xml.
     */
    @Parameter
    private List<String> filters;

    /**
     * A list of file extensions that should not be filtered if filtering is
     * enabled.
     */
    @Parameter
    private List<String> nonFilteredFileExtensions;

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The Maven project helper.
     */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The archive configuration to use. See
     * <a href="http://maven.apache.org/shared/maven-archiver/index.html">Maven
     * Archiver Reference</a>.
     */
    @Parameter
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * The JAR archiver needed for archiving the classes directory into a JAR
     * file under bin.
     */
    @Component(role = Archiver.class, hint = "jar")
    private JarArchiver jarArchiver;

    /**
     * The archiver component that is used to package the Hybris Extension.
     */
    @Component(role = Archiver.class, hint = "jar")
    private JarArchiver hyxArchiver;

    /**
     * Used to copy the file with resource filtering.
     */
    @Component(role = MavenFileFilter.class, hint = "default")
    private MavenFileFilter mavenFileFilter;

    /**
     * Used to perform the file filtering.
     */
    @Component(role = MavenResourcesFiltering.class, hint = "default")
    private MavenResourcesFiltering mavenResourcesFiltering;

    /**
     * The current Maven session.
     */
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    /**
     * File filtering wrappers.
     */
    private List<FilterWrapper> filterWrappers;

    /**
     * The target bin directory for the Hybris Extension.
     */
    private static final String HYX_BIN_DIRECTORY = "bin";

    /**
     * The target lib directory for the Hybris Extension.
     */
    private static final String HYX_LIB_DIRECTORY = "lib";

    /**
     * The target resources directory for the Hybris Extension.
     */
    private static final String HYX_RESOURCES_DIRECTORY = "resources";

    /**
     * The target web directory for the Hybris Extension.
     */
    private static final String HYX_WEB_DIRECTORY = "web";

    /**
     * The target hmc directory for the Hybris Extension.
     */
    private static final String HYX_HMC_DIRECTORY = "hmc";
    
    /**
     * Called when the Maven plug-in is executing. It creates a ZIP file of the
     * Hybris Extension source files.
     *
     * @throws MojoExecutionException If there was an error that should stop the
     *         build.
     * @throws MojoFailureException If there was an error but the build might be
     *         allowed to continue.
     */
    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException
    {
        // Generate HYX file name
        final StringBuilder hyxFilename = new StringBuilder();
        hyxFilename.append(finalName);
        if (StringUtils.isNotEmpty(classifier))
        {
            hyxFilename.append('-');
            hyxFilename.append(classifier);
        }
        final File hyxTargetDirectory = new File(outputDirectory, hyxFilename.toString());
        final File hyxTargetBinDirectory = new File(hyxTargetDirectory, HYX_BIN_DIRECTORY);
        final File hyxTargetLibDirectory = new File(hyxTargetDirectory, HYX_LIB_DIRECTORY);
        final File hyxTargetResourcesDirectory = new File(hyxTargetDirectory, HYX_RESOURCES_DIRECTORY);
        final File hyxTargetWebDirectory = new File(hyxTargetDirectory, HYX_WEB_DIRECTORY);
        final File hyxTargetHMCDirectory = new File(hyxTargetDirectory, HYX_HMC_DIRECTORY);
        final File hyxBinFile = new File(hyxTargetBinDirectory, hyxFilename.toString() + ".jar");

        getLog().info("Copying resources");
        int fileCount = 0;
        fileCount += copyFiles(hyxResourceDirectory, hyxTargetResourcesDirectory, "**", null);
        fileCount += copyFiles(hyxWebDirectory, hyxTargetWebDirectory, null, "src/**,webroot/WEB-INF/classes/**");
        fileCount += copyFiles(hyxHMCDirectory, hyxTargetHMCDirectory, null, "bin/**,src/**,classes/**");
        getLog().info(fileCount + " files copied");

        // create the classes to be attached
        final File classesDirectory = new File(outputDirectory, "classes");
        if (classesDirectory.exists())
        {
            ClassesPackager packager = new ClassesPackager();
            getLog().info("Packaging classes");
            packager.packageClasses(classesDirectory, hyxBinFile, jarArchiver, session, project, archive);
            projectHelper.attachArtifact(project, "jar", classifier, hyxBinFile);
        }

        Set<Artifact> dependencies = project.getArtifacts();
        packageArtifacts(hyxTargetLibDirectory, dependencies);

        // Generate the HYX file
        final File hyxFile = new File(outputDirectory, hyxFilename.toString() + ".hyx");
        final String[] includes = splitParameter(packagingIncludes);
        final String[] excludes = splitParameter(packagingExcludes);

        hyxArchiver.addDirectory(hyxTargetDirectory, includes, excludes);
        hyxArchiver.setDestFile(hyxFile);

        try
        {
            hyxArchiver.createArchive();
        }
        catch (final IOException e)
        {
            throw new MojoExecutionException("Failed to package the Hybris Extension", e);
        }
        catch (final ArchiverException e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        // Attach the artifact to the build life-cycle

        if (StringUtils.isNotEmpty(classifier))
        {
            projectHelper.attachArtifact(project, "hyx", classifier, hyxFile);
        }
        else
        {
            project.getArtifact().setFile(hyxFile);
        }
    }

    /**
     * Recursively copy from a source directory to a destination directory
     * applying resource filtering if necessary.
     *
     * @param source The source directory.
     * @param destination The destination directory.
     * @param includes ant include patterns, comma separated
     * @param excludes ant exclude patterns, comma separated
     * @return number of files copied
     * @throws MojoExecutionException If there was an error during the recursive
     *         copying or filtering.
     */
    private int copyFiles(final File source, final File destination, String includes, String excludes) throws MojoExecutionException
    {
        int fileCount = 0;

        try
        {
            if (!destination.exists() && !destination.mkdirs())
            {
                throw new MojoExecutionException("Could not create directory: " + destination.getAbsolutePath());
            }
            for (final String sourceFileName : FileUtils.getFileAndDirectoryNames(source, includes, excludes, false, true, true, false))
            {
                final File destinationItem = new File(destination, sourceFileName);
                final File sourceItem = new File(source, sourceFileName);
                getLog().debug("Copy " + sourceItem + " to " + destinationItem);
                System.out.println("Copy " + sourceItem + " to " + destinationItem);
                fileCount++;

                if (filtering && !isNonFilteredExtension(sourceItem.getName()))
                {
                    mavenFileFilter.copyFile(sourceItem, destinationItem, true, getFilterWrappers(), null);
                }
                else
                {
                    FileUtils.copyFile(sourceItem, destinationItem);
                }
            }
        }
        catch (final MavenFilteringException e)
        {
            throw new MojoExecutionException("Failed to build filtering wrappers", e);
        }
        catch (final IOException e)
        {
            throw new MojoExecutionException("Error copying file: " + source.getAbsolutePath(), e);
        }

        return fileCount;
    }

    /**
     * Determine whether the file name should be filtered or not based on the
     * list of excluded file extensions.
     *
     * @param fileName The file name.
     * @return {@code true} if the file extension is not excluded and the file
     *         should be filtered. Otherwise {@code
     * false}.
     * @throws MojoExecutionException If there was an error determining whether
     *         the file name should be filtered.
     * @since 1.2.0
     */
    private boolean isNonFilteredExtension(final String fileName) throws MojoExecutionException
    {
        return !mavenResourcesFiltering.filteredFileExtension(fileName, nonFilteredFileExtensions);
    }

    /**
     * Build a list of filter wrappers.
     *
     * @return The list of filter wrappers.
     * @throws MojoExecutionException If there was a problem building the list
     *         of filter wrappers.
     */
    private List<FilterWrapper> getFilterWrappers() throws MojoExecutionException
    {
        if (filterWrappers == null)
        {
            try
            {
                final MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution();
                mavenResourcesExecution.setEscapeString("\\");
                filterWrappers = mavenFileFilter.getDefaultFilterWrappers(project, filters, true, session,
                        mavenResourcesExecution);
            }
            catch (final MavenFilteringException e)
            {
                throw new MojoExecutionException("Failed to build filtering wrappers: " + e.getMessage(), e);
            }
        }

        return filterWrappers;
    }

    /**
     * This helper method splits comma separated lists of directory inclusion
     * and exclusion rules returning the as a string array.
     *
     * @param parameter A comma separated list of directory inclusion and
     *        exclusion rules.
     * @return A string array of the individual inclusion/exclusion rules or
     *         {@code null} if the {@code parameter} was null or blank.
     */
    private String[] splitParameter(final String parameter)
    {
        if (StringUtils.isNotBlank(parameter))
        {
            return StringUtils.stripAll(StringUtils.split(parameter, ","));
        }
        else
        {
            return null;
        }
    }

    /**
     * Copy artifacts to target directory
     * 
     * @param targetPath target directory
     * @param artifacts set of artifacts
     * 
     * @throws MojoExecutionException
     */
    private void packageArtifacts(File targetPath, Set<Artifact> artifacts) throws MojoExecutionException
    {
        try
        {
            final ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
            final List<String> duplicates = findDuplicates(artifacts);

            for (Artifact artifact : artifacts)
            {
                String targetFileName = getArtifactFinalName(artifact);

                getLog().debug("Processing: " + targetFileName);

                if (duplicates.contains(targetFileName))
                {
                    getLog().debug("Duplicate found: " + targetFileName);
                    targetFileName = artifact.getGroupId() + "-" + targetFileName;
                    getLog().debug("Renamed to: " + targetFileName);
                }
                // context.getWebappStructure().registerTargetFileName(artifact, targetFileName);

                if (!artifact.isOptional() && filter.include(artifact))
                {
                    try
                    {
                        String type = artifact.getType();
                        if ("hyx".equals(type))
                        {
                            // TODO: handle special case, write to extensions.xml
                        }
                        else if ("jar".equals(type) || "test-jar".equals(type) || "bundle".equals(type))
                        {
                            FileUtils.copyFile(artifact.getFile(), new File(targetPath, targetFileName));
                        }
                        else
                        {
                            getLog().debug("Artifact of type [" + type + "] is not supported, ignoring [" + artifact + "]");
                        }
                    }
                    catch (IOException e)
                    {
                        throw new MojoExecutionException("Failed to copy file for artifact [" + artifact + "]", e);
                    }
                }
            }
        }
        catch (InterpolationException e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Searches a set of artifacts for duplicate filenames and returns a list of
     * duplicates.
     *
     * @param artifacts set of artifacts
     * @return List of duplicated artifacts as bundling file names
     */
    private List<String> findDuplicates(Set<Artifact> artifacts) throws InterpolationException
    {
        List<String> duplicates = new ArrayList<String>();
        List<String> identifiers = new ArrayList<String>();
        for (Artifact artifact : artifacts)
        {
            String candidate = getArtifactFinalName(artifact);
            if (identifiers.contains(candidate))
            {
                duplicates.add(candidate);
            }
            else
            {
                identifiers.add(candidate);
            }
        }
        return duplicates;
    }

    /**
     * Returns the final name of the specified artifact.
     * 
     * @param artifact the artifact
     * @return the converted filename of the artifact
     * @throws InterpolationException in case of interpolation problem.
     */
    private String getArtifactFinalName(Artifact artifact) throws InterpolationException
    {
        String classifier = artifact.getClassifier();
        if ((classifier != null) && !("".equals(classifier.trim())))
        {
            return MappingUtils.evaluateFileNameMapping(MappingUtils.DEFAULT_FILE_NAME_MAPPING_CLASSIFIER, artifact);
        }
        else
        {
            return MappingUtils.evaluateFileNameMapping(MappingUtils.DEFAULT_FILE_NAME_MAPPING, artifact);
        }

    }
}
