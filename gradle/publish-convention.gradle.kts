/**
 * Convention script for publishing Anchor-DI library modules to Sonatype / Maven Central.
 * Applied to: anchor-di-api, anchor-di-runtime, anchor-di-ksp, anchor-di-compose, anchor-di-navigation.
 *
 * Requires (in gradle.properties or env):
 *   - LIBRARY_GROUP, LIBRARY_VERSION (or VERSION)
 *   - SONATYPE_USERNAME, SONATYPE_PASSWORD (Maven Central user token from central.sonatype.com)
 *   - Signing: signing.keyId, signing.password, signing.secretKeyRingFile
 *     OR (CI): signingInMemoryKeyId, signingInMemoryKeyPassword, signingInMemoryKey
 *
 * Usage:
 *   ./gradlew publishAllPublicationsToSonatypeRepository   # stage to Sonatype
 *   ./gradlew publishToMavenLocal                           # local only
 */

val libraryGroup: String = project.findProperty("LIBRARY_GROUP") as? String ?: "io.github.12345debdut"
val libraryVersion: String = project.findProperty("LIBRARY_VERSION") as? String ?: project.findProperty("VERSION") as? String ?: "0.1.0-SNAPSHOT"

group = libraryGroup
version = libraryVersion

val pomName: String = project.findProperty("POM_NAME") as? String ?: "Anchor DI"
val pomDescription: String = project.findProperty("POM_DESCRIPTION") as? String ?: "Compile-time dependency injection for Kotlin Multiplatform."
val pomUrl: String = project.findProperty("POM_URL") as? String ?: "https://github.com/12345debdut/anchor-di"
val pomScmUrl: String = project.findProperty("POM_SCM_URL") as? String ?: pomUrl
val pomScmConnection: String = project.findProperty("POM_SCM_CONNECTION") as? String ?: "scm:git:git://github.com/12345debdut/anchor-di.git"
val pomScmDevConnection: String = project.findProperty("POM_SCM_DEV_CONNECTION") as? String ?: "scm:git:ssh://git@github.com/12345debdut/anchor-di.git"
val pomLicenseName: String = project.findProperty("POM_LICENSE_NAME") as? String ?: "The Apache License, Version 2.0"
val pomLicenseUrl: String = project.findProperty("POM_LICENSE_URL") as? String ?: "https://www.apache.org/licenses/LICENSE-2.0.txt"
val pomDeveloperId: String = project.findProperty("POM_DEVELOPER_ID") as? String ?: "12345debdut"
val pomDeveloperName: String = project.findProperty("POM_DEVELOPER_NAME") as? String ?: "Debdut Saha"
val pomDeveloperUrl: String = project.findProperty("POM_DEVELOPER_URL") as? String ?: "https://github.com/12345debdut"

plugins.apply("maven-publish")
plugins.apply("signing")

// Sonatype OSSRH staging (use Central Portal user token as username/password)
val sonatypeUsername: String? = project.findProperty("SONATYPE_USERNAME") as? String ?: System.getenv("ORG_GRADLE_PROJECT_SONATYPE_USERNAME")?.ifBlank { null }
val sonatypePassword: String? = project.findProperty("SONATYPE_PASSWORD") as? String ?: System.getenv("ORG_GRADLE_PROJECT_SONATYPE_PASSWORD")?.ifBlank { null }

project.extensions.configure<org.gradle.api.publish.PublishingExtension> {
    repositories {
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = sonatypeUsername ?: "unknown"
                password = sonatypePassword ?: ""
            }
        }
        mavenLocal()
    }
    publications.withType<org.gradle.api.publish.maven.MavenPublication>().configureEach {
        groupId = libraryGroup
        version = libraryVersion
        pom {
            name.set("$pomName - ${artifactId}")
            description.set(pomDescription)
            url.set(pomUrl)
            licenses {
                license {
                    name.set(pomLicenseName)
                    url.set(pomLicenseUrl)
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set(pomDeveloperId)
                    name.set(pomDeveloperName)
                    url.set(pomDeveloperUrl)
                }
            }
            scm {
                url.set(pomScmUrl)
                connection.set(pomScmConnection)
                developerConnection.set(pomScmDevConnection)
            }
        }
    }
}

// Sign all publications (required for Maven Central). Skip when no key is configured (e.g. publishToMavenLocal only).
project.extensions.configure<org.gradle.plugins.signing.SigningExtension> {
    val signingKeyId: String? = project.findProperty("signing.keyId") as? String ?: project.findProperty("signingInMemoryKeyId") as? String
    val signingPassword: String? = project.findProperty("signing.password") as? String ?: project.findProperty("signingInMemoryKeyPassword") as? String
    val signingKey: String? = project.findProperty("signingInMemoryKey") as? String
    val hasSecretKey = project.findProperty("signing.secretKeyRingFile") != null || (signingKey != null && signingKeyId != null && signingPassword != null)
    if (signingKey != null && signingKeyId != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    }
    if (hasSecretKey || signingKey != null) {
        sign(project.extensions.getByType<org.gradle.api.publish.PublishingExtension>().publications)
    }
}
