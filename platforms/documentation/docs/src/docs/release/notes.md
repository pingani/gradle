The Gradle team is excited to announce Gradle @version@.

This release features support for [custom encryption keys](#encryption-key) for the [configuration cache](#configuration-cache) as well as more helpful [error and warning messages](#error-improvements).

Additionally, this release comes with several improvements to [build init](#build-init) and to [build authoring](#build-authors) for build engineers and plugin authors.
See the full release notes for details.

We would like to thank the following community members for their contributions to this release of Gradle:
[Baptiste Decroix](https://github.com/bdecroix-spiria),
[Björn Kautler](https://github.com/Vampire),
[Daniel Lacasse](https://github.com/lacasseio),
[Danny Thomas](https://github.com/DanielThomas),
[Hyeonmin Park](https://github.com/KENNYSOFT),
[jeffalder](https://github.com/jeffalder),
[Jendrik Johannes](https://github.com/jjohannes),
[John Jiang](https://github.com/johnshajiang),
[Kaiyao Ke](https://github.com/kaiyaok2),
[Kevin Mark](https://github.com/kmark),
[king-tyler](https://github.com/king-tyler),
[Marcin Dąbrowski](https://github.com/marcindabrowski),
[Marcin Laskowski](https://github.com/ILikeYourHat),
[Markus Gaisbauer](https://github.com/quijote),
[Mel Arthurs](https://github.com/arthursmel),
[Ryan Schmitt](https://github.com/rschmitt),
[Surya K N](https://github.com/Surya-KN),
[Vladislav Golubtsov](https://github.com/Shmuser),
[Yanshun Li](https://github.com/Chaoba),
[Andrzej Ressel](https://github.com/andrzejressel)

Be sure to check out the [Public Roadmap](https://blog.gradle.org/roadmap-announcement) for insight into what's planned for future releases.

## Upgrade instructions

Switch your build to use Gradle @version@ by updating your wrapper:

`./gradlew wrapper --gradle-version=@version@`

See the [Gradle 8.x upgrade guide](userguide/upgrading_version_8.html#changes_@baseVersion@) to learn about deprecations, breaking changes and other considerations when upgrading to Gradle @version@.

For Java, Groovy, Kotlin and Android compatibility, see the [full compatibility notes](userguide/compatibility.html).

## New features and usability improvements

<a name="configuration-cache"></a>
### Configuration cache improvements

The [configuration cache](userguide/configuration_cache.html) improves build time by caching the result of the configuration phase and reusing it for subsequent builds.
This feature can significantly improve build performance.

<a name="encryption-key"></a>
#### Custom encryption key

The configuration cache is encrypted to mitigate the risk of accidental exposure of sensitive data.
By default, Gradle automatically creates and manages the key, storing it in a keystore in the Gradle User Home directory.
While convenient, this may be inappropriate in some environments.

You may now provide Gradle with the key used to encrypt cached configuration data via the `GRADLE_ENCRYPTION_KEY` environment variable.

More details can be found in the [configuration cache](userguide/configuration_cache.html#config_cache:secrets:configuring_encryption_key) section of the Gradle User Manual.

<a name="error-improvements"></a>
### Error and warning reporting improvements

Gradle provides a rich set of error and warning messages to help you understand and resolve problems in your build.

<a name="dependency-locking"></a>
#### Clearer suggested actions in case of dependency locking errors

[Dependency locking](userguide/dependency_locking.html) is a mechanism that ensures reproducible builds when using dynamic dependency versions.

This release improves error messages by separating the error from the possible action to fix the issue in the console output.
Errors from invalid [lock file format](userguide/dependency_locking.html#lock_state_location_and_format) or [missing lock state when strict mode is enabled](userguide/dependency_locking.html#fine_tuning_dependency_locking_behaviour_with_lock_mode) are now displayed as illustrated below:

```
FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':dependencies'.
> Could not resolve all dependencies for configuration ':lockedConf'.
   > Invalid lock state for lock file specified in '<project>/lock.file'. Line: '<<<<<<< HEAD'

* Try:
> Verify the lockfile content. For more information on lock file format, please refer to https://docs.gradle.org/@version@/userguide/dependency_locking.html#lock_state_location_and_format in the Gradle documentation.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.
```

<a name="error-reporting"></a>
#### Better error reporting of circular references in providers

Before this release, evaluating a provider with a cycle in its value assignment would lead to a `StackOverflowError`.
With this release, circular references are properly detected and reported.

For instance, the following code:

```
def property = objects.property(String)
property.set("some value")

// wrong, self-references only supported via #update()
property.set(property.map { "$it and more" })

println(property.get()) // error when evaluating
```

Previously failed with a `StackOverflowError` and limited details:

```
FAILURE: Build failed with an exception.

* What went wrong:
A problem occurred evaluating root project 'test'.
> java.lang.StackOverflowError (no error message)

```

Starting with this release, you get a more helpful error message indicating the source of the cycle and a list of the providers in the chain that led to it:

```
FAILURE: Build failed with an exception.

* Where:
Build file '<project>/build.gradle' line: 7

* What went wrong:
A problem occurred evaluating root project 'test'.
> Circular evaluation detected: property(java.lang.String, map(java.lang.String map(<CIRCULAR REFERENCE>) check-type()))
   -> map(java.lang.String map(property(java.lang.String, <CIRCULAR REFERENCE>)) check-type())
   -> map(property(java.lang.String, map(java.lang.String <CIRCULAR REFERENCE> check-type())))
   -> property(java.lang.String, map(java.lang.String map(<CIRCULAR REFERENCE>) check-type()))
```

<a name="build-init"></a>
### Build init improvements

The [build init plugin](userguide/build_init_plugin.html) allows users to create a new Gradle build, supporting various types of projects.

<a name="simpler-packaging"></a>
#### Simpler source package handling

You no longer have to answer an interactive question about the source package.
Instead, a default value of `org.example` will be used.
You can override it using an existing option `--package` flag for the `init` task.
Additionally, you can set the default value by adding a new `org.gradle.buildinit.source.package` property in `gradle.properties` in the Gradle User Home.

```
// ~/.gradle/gradle.properties

org.gradle.buildinit.source.package=my.corp.domain
```

Names of the generated convention plugins now start with `buildlogic` instead of the package name, making them shorter and cleaner.

<a name="interactive"></a>
#### Generating without interactive questions

A new `--use-defaults` option applies default values for options that were not explicitly configured.
It also ensures the init command can be completed without interactive user input.
This is handy in shell scripts to ensure they do not accidentally hang.

For example, here is how you can generate a Kotlin library without answering any questions:

```
gradle init --use-defaults --type kotlin-library
```

<a name="kotlin-syntax"></a>
#### Simpler assignment syntax in Kotlin DSL

Projects generated with Kotlin DSL scripts now use [simple property assignment](/8.4/release-notes.html#assign-stable) syntax with the `=` operator.

For instance, setting `mainClass` of an application looks like this:

```
application {
	mainClass = "org.example.AppKt"
}
```

<a name="build-authors"></a>
### Build authoring improvements

Gradle provides rich APIs for plugin authors and build engineers to develop custom build logic.

<a name="enhanced-filtering"></a>
#### Lazy name-based filtering of tasks

Previously, filtering tasks by name required using the `matching` method using the following pattern:
```
    tasks.matching { it.name.contains("pack") }
```

The problem was that it triggered the [creation of the tasks](userguide/task_configuration_avoidance.html), even when the task was not part of the build execution.

Starting from this release, you can use:
```
    tasks.named { it.contains("pack") }
```

Using `named(Spec<String>)` will not cause the registered tasks to be eagerly created.

This new method is available on all Gradle containers that extend [`NamedDomainObjectSet`](userguide/custom_gradle_types.html#nameddomainobjectset).

<a name="provider-capabilities"></a>
#### Allow Providers to be used with dependency capabilities

Gradle supports [declaring capabilities](userguide/component_capabilities.html) for components to better manage dependencies by adding dependency rules.

Previously, capability methods only accepted inputs with the capability notation:

```
dependencies {
    implementation("org.foo:bar:1.0") {
        capabilities {
            requireCapability("org.foo:module-variant")
        }
    }
}
```

[`Providers`](javadoc/org/gradle/api/provider/Provider.html) can now be passed to capability methods
[`ConfigurationPublications#capability(Object)`](javadoc/org/gradle/api/artifacts/ConfigurationPublications.html#capability-java.lang.Object-),
[`ModuleDependencyCapabilitiesHandler#requireCapability(Object)`](javadoc/org/gradle/api/artifacts/ModuleDependencyCapabilitiesHandler.html#requireCapability-java.lang.Object-), and [`CapabilitiesResolution#withCapability(Object, Action)`](javadoc/org/gradle/api/artifacts/CapabilitiesResolution.html#withCapability-java.lang.Object-org.gradle.api.Action-). This allows computing the capability coordinates using values that change after calling these methods, such as in the following example:

```
dependencies {
    implementation("org.foo:bar:1.0") {
        capabilities {
            requireCapability(project.provider(() -> "${project.group}:${project.name}-platform:${project.version}"))
        }
    }
}
// Later, the version of the project is set. Without the provider, this value could not be used above.
project.version = "1.0.0"
```

<a name="update-api"></a>
#### New `update()` API allows safe self-referencing lazy properties

[Lazy configuration](userguide/lazy_configuration.html) delays calculating a property’s value until it is required for the build.
This can lead to accidental recursions when assigning property values of an object to itself:

```
var property = objects.property<String>()
property.set("some value")
property.set(property.map { "$it and more" })

// Circular evaluation detected (or StackOverflowError, before 8.6)
println(property.get()) // "some value and more"
```

Previously, Gradle did not support circular references when evaluating lazy properties.

[`Property`](javadoc/org/gradle/api/provider/Property.html#update-org.gradle.api.Transformer-) and [`ConfigurableFileCollection`](javadoc/org/gradle/api/file/ConfigurableFileCollection.html#update-org.gradle.api.Transformer-) now provide their respective `update(Transformer<...>)` methods which allow self-referencing updates safely:

```
var property = objects.property<String>()
property.set("some value")
property.update { it.map { "$it and more" } }

println(property.get()) // "some value and more"
```

Refer to the javadoc for [`Property.update(Transformer<>)`](javadoc/org/gradle/api/provider/Property.html#update-org.gradle.api.Transformer-) and [`ConfigurableFileCollection.update(Transformer<>)`](javadoc/org/gradle/api/file/ConfigurableFileCollection.html#update-org.gradle.api.Transformer-) for more details, including limitations.


## Promoted features
Promoted features are features that were incubating in previous versions of Gradle but are now supported and subject to backwards compatibility.
See the User Manual section on the “[Feature Lifecycle](userguide/feature_lifecycle.html)” for more information.

The following are the features that have been promoted in this Gradle release.

<!--
### Example promoted
-->

## Fixed issues

<!--
This section will be populated automatically
-->

## Known issues

Known issues are problems that were discovered post release that are directly related to changes made in this release.

<!--
This section will be populated automatically
-->

## External contributions

We love getting contributions from the Gradle community. For information on contributing, please see [gradle.org/contribute](https://gradle.org/contribute).

## Reporting problems

If you find a problem with this release, please file a bug on [GitHub Issues](https://github.com/gradle/gradle/issues) adhering to our issue guidelines.
If you're not sure you're encountering a bug, please use the [forum](https://discuss.gradle.org/c/help-discuss).

We hope you will build happiness with Gradle, and we look forward to your feedback via [Twitter](https://twitter.com/gradle) or on [GitHub](https://github.com/gradle).
