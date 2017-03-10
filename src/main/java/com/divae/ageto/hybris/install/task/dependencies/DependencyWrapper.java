package com.divae.ageto.hybris.install.task.dependencies;

import java.util.Objects;

import org.apache.maven.model.Dependency;

/**
 * Created by mhaagen on 24.08.2016.
 */
public class DependencyWrapper extends Dependency {

    /** Serial version */
    private static final long serialVersionUID = -1787586399767282279L;
    
    private final Dependency dependency;

    public DependencyWrapper(final Dependency dependency) {
        this.dependency = dependency;
    }

    public DependencyWrapper(final String groupId, final String artifactId) {
        dependency = new Dependency();
        dependency.setArtifactId(artifactId);
        dependency.setGroupId(groupId);
        dependency.setVersion("");
    }

    public DependencyWrapper(final String groupId, final String artifactId, final String version) {
        dependency = new Dependency();
        dependency.setArtifactId(artifactId);
        dependency.setGroupId(groupId);
        dependency.setVersion(version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DependencyWrapper that = (DependencyWrapper) o;
        return Objects.equals(getArtifactId(), that.getArtifactId()) 
                && Objects.equals(getGroupId(), that.getGroupId())
                && Objects.equals(getVersion(), that.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArtifactId(), getGroupId(), getVersion());
    }

    @Override
    public String toString() {
        return "DependencyWrapper{" 
                + "artifactId='" + getArtifactId() 
                + "', groupId='" + getGroupId() 
                + "', version='" + getVersion() + "'}";
    }
}
