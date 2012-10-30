/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.publish;

import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.internal.artifacts.ArtifactPublicationServices;
import org.gradle.api.internal.artifacts.DependencyResolutionServices;
import org.gradle.api.publish.internal.DefaultPublicationContainer;
import org.gradle.api.publish.internal.DefaultPublishingExtension;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;

/**
 * Installs a {@link PublishingExtension} with name "publishing".
 */
@Incubating
public class PublishingPlugin implements Plugin<Project> {

    private final ArtifactPublicationServices artifactPublicationServices;
    private final Instantiator instantiator;

    @Inject
    public PublishingPlugin(DependencyResolutionServices dependencyResolutionServices, Instantiator instantiator) {
        this.artifactPublicationServices = dependencyResolutionServices.getPublishServicesFactory().create();
        this.instantiator = instantiator;
    }

    public void apply(Project project) {
        RepositoryHandler repositories = artifactPublicationServices.getRepositoryHandler();
        PublicationContainer publications = instantiator.newInstance(DefaultPublicationContainer.class, instantiator);
        project.getExtensions().create(PublishingExtension.NAME, DefaultPublishingExtension.class, repositories, publications);
    }
}
