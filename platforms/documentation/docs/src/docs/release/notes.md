The Gradle team is excited to announce Gradle @version@.

This release features [improvements for build authors](#build-authors), including the new [Problems API](#problems-api), a new method on the [NamedDomainObject API](#enhanced-filtering), improvements to the [ConfigurableFileCollection API](#provider-capabilities), and updates to the [Artifacts APIs](#update-api).

This release features a security enhancement to the [configuration cache](#configuration-cache) for [encryption keys](#encryption-key).

Additionally, this release comes with more helpful [error and warning messages](#error-improvements) and improvements to [build init](#build-init).
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
[​Andrzej Ressel](https://github.com/andrzejressel)

Be sure to check out the [Public Roadmap](https://blog.gradle.org/roadmap-announcement) for insight into what's planned for future releases.

## Upgrade instructions

Switch your build to use Gradle @version@ by updating your wrapper:

`./gradlew wrapper --gradle-version=@version@`

See the [Gradle 8.x upgrade guide](userguide/upgrading_version_8.html#changes_@baseVersion@) to learn about deprecations, breaking changes and other considerations when upgrading to Gradle @version@.

For Java, Groovy, Kotlin and Android compatibility, see the [full compatibility notes](userguide/compatibility.html).

## New features and usability improvements

<a name="build-authors"></a>
### Build authoring improvements

Gradle provides rich APIs for plugin authors and build engineers to develop custom build logic.

<a name="problems-api"></a>
#### Problems API

Gradle now has a new incubating API that allows build engineers and plugin authors to consume and report problems that occur during a build.

The [`Problems`](javadoc/org/gradle/api/problems/Problems.html) service can be used to describe problems with details (description, location information, link to documentation, etc.) and report them.
Reported problems are then exposed via the Tooling API, allowing Gradle IDE providers - IntelliJ IDEA, Visual Studio Code, Eclipse - to display details in the UI.
The reported problems carry location information; therefore, IDEs can easily integrate them into the developer experience, providing error markers, problem views, and more.

Gradle already emits problems from many components, including (but not limited to) deprecation warnings, dependency version catalog errors, task validation errors, and Java toolchain problems.

The current release focuses on reporting problems in the IDE.
Check out the [Problems API](userguide/implementing_gradle_plugins.html#reporting-problems) documentation to learn more.
Users can expect further enhancements to the Problems API aimed at console reporting in future releases.

<a name="enhanced-filtering"></a>
#### Enhanced name-based filtering on NamedDomainObject containers

A new [`named(Spec<String>)` method](javadoc/org/gradle/api/NamedDomainObjectCollection.html#named-org.gradle.api.specs.Spec-) has been added to all `NamedDomainObject` containers, simplifying name-based filtering and eliminating the need to touch any of the values, whether they be realized or unrealized.

Instead of:
```
    tasks.matching { it.name.contains("pack")
```

You can use:
```
    tasks.named { it.contains("pack") }
```

Using `named` will not cause registered tasks to be eagerly created as was the case with `matching`.

<a name="provider-capabilities"></a>
#### Allow Providers to be used with capabilities

[`Providers`](javadoc/org/gradle/api/provider/Provider.html) can now be passed to capability methods
[`ConfigurationPublications#capability(Object)`](javadoc/org/gradle/api/artifacts/ConfigurationPublications.html#capability-java.lang.Object-),
[`ModuleDependencyCapabilitiesHandler#requireCapability(Object)`](javadoc/org/gradle/api/artifacts/ModuleDependencyCapabilitiesHandler.html#requireCapability-java.lang.Object-),
and [`CapabilitiesResolution#withCapability(Object, Action)`](javadoc/org/gradle/api/artifacts/CapabilitiesResolution.html#withCapability-java.lang.Object-org.gradle.api.Action-).

```
dependencies {
    implementation("org.foo:bar:1.0") {
        capabilities {
            requireCapability(value)  // project.provider(() -> value
        }
    }
}
```

<a name="update-api"></a>
#### New `update()` API allows safe self-referencing lazy properties

Historically, Gradle did not support circular references when evaluating lazy properties:

```
var property = objects.property<String>()
property.set("some value")
property.set(property.map { "$it and more" })

// Circular evaluation detected (or StackOverflowError, before 8.6)
println(property.get()) // "some value and more"
```

[`Property`](javadoc/org/gradle/api/provider/Property.html#update-org.gradle.api.Transformer-) and [`ConfigurableFileCollection`](javadoc/org/gradle/api/file/ConfigurableFileCollection.html#update-org.gradle.api.Transformer-) now provide their respective `update(Transformer<...>)` methods which allow self-referencing updates safely:

```
var property = objects.property<String>()
property.set("some value")
property.update { it.map { "$it and more" } }

println(property.get()) // "some value and more"
```

Please refer to the javadoc for [`Property.update(Transformer<>)`](javadoc/org/gradle/api/provider/Property.html#update-org.gradle.api.Transformer-) and [`ConfigurableFileCollection.update(Transformer<>)`](javadoc/org/gradle/api/file/ConfigurableFileCollection.html#update-org.gradle.api.Transformer-) for more details, including limitations.

<a name="configuration-cache"></a>
### Configuration cache

The [configuration cache](userguide/configuration_cache.html) improves build time by caching the result of the configuration phase and reusing it for subsequent builds. This feature can significantly improve build performance.

<a name="encryption-key"></a>
#### Gradle encryption key via an environment variable

You may now provide Gradle with the key used to encrypt cached configuration data via the `GRADLE_ENCRYPTION_KEY` environment variable.
By default, Gradle creates and manages the key automatically, storing it in a keystore under the Gradle User Home.
This may be inappropriate in some environments.

More details can be found in the dedicated section of the [configuration cache](userguide/configuration_cache.html#config_cache:secrets:configuring_encryption_key) user manual chapter.

<a name="error-improvements"></a>
### Error and warning reporting improvements

Gradle provides a rich set of error and warning messages to help you understand and resolve problems in your build.

<a name="dependency-locking"></a>
#### Dependency locking now separates the error from the possible action to try

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
#### Assignment syntax in Kotlin DSL

Projects generated with Kotlin DSL scripts now use [simple property assignment](/8.4/release-notes.html#assign-stable) syntax with the `=` operator.

For instance, setting `mainClass` of an application looks like this:

```
application {
	mainClass = "org.example.AppKt"
}
```

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
