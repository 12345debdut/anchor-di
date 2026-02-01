/**
 * Convention script for publishing Anchor-DI library modules to Sonatype / Maven Central.
 * Applied to: anchor-di-api, anchor-di-core, anchor-di-ksp, anchor-di-android, anchor-di-presentation, anchor-di-presentation-compose, anchor-di-compose.
 *
 * Requires (in gradle.properties or env):
 *   - LIBRARY_GROUP, LIBRARY_VERSION (or VERSION)
 *   - SONATYPE_USERNAME, SONATYPE_PASSWORD (Maven Central user token from central.sonatype.com)
 *   - Signing:
 *     Option A — Key from file (local, RECOMMENDED): signing.keyId, signing.password, signing.keyFile
 *       signing.keyFile = path to BINARY secret key file (export with: gpg --export-secret-keys KEY_ID > ~/.gradle/signing-key.gpg)
 *       Do NOT use --armor. Gradle reads the binary keyring file natively and avoids "Could not read PGP secret key" issues.
 *     Option B — Key in env (CI): signingInMemoryKeyId, signingInMemoryKeyPassword, signingInMemoryKey (ASCII-armored content)
 *
 * Usage:
 *   ./gradlew publishAllPublicationsToSonatypeRepository   # stage to Sonatype
 *   ./gradlew publishToMavenLocal                           # local only
 */

val libraryGroup: String = project.findProperty("LIBRARY_GROUP") as? String ?: "io.github.12345debdut"
val libraryVersion: String = project.findProperty("LIBRARY_VERSION") as? String ?: project.findProperty("VERSION") as? String ?: "0.1.0"

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

// Resolve signing from ROOT project so ~/.gradle/gradle.properties is visible (subprojects may not see it).
val root = rootProject
val signingKeyId: String? = root.findProperty("signing.keyId") as? String ?: root.findProperty("signingInMemoryKeyId") as? String ?: System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyId")?.ifBlank { null }
val signingPassword: String? = root.findProperty("signing.password") as? String ?: root.findProperty("signingInMemoryKeyPassword") as? String ?: System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword")?.ifBlank { null }
val keyFilePath: String? = root.findProperty("signing.keyFile") as? String ?: root.findProperty("signing.secretKeyRingFile") as? String
val keyFileAbsolutePath: String? = keyFilePath?.let { path ->
    val normalized = if (path.startsWith("~")) {
        System.getProperty("user.home", "").trimEnd('/') + path.drop(1)
    } else path
    val file = java.io.File(normalized)
    if (file.exists()) file.absolutePath else null
}
// Mode A: key file (binary keyring) — Gradle reads the file natively; avoids "Could not read PGP secret key".
val hasSigningKeyFromFile = keyFileAbsolutePath != null && signingKeyId != null && signingPassword != null
// Mode B: in-memory key content (CI) — ASCII-armored key in env/property.
val signingKeyContent: String? = (root.findProperty("signingInMemoryKey") as? String)?.takeIf { it.isNotBlank() } ?: System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey")?.takeIf { it.isNotBlank() }
val hasSigningKeyFromContent = signingKeyContent != null && signingPassword != null
val hasSigningKey = hasSigningKeyFromFile || hasSigningKeyFromContent

if (hasSigningKey) {
    if (hasSigningKeyFromFile) {
        // Traditional keyring file: set properties so Gradle signing plugin reads the BINARY file itself.
        project.extra["signing.keyId"] = signingKeyId!!
        project.extra["signing.password"] = signingPassword!!
        project.extra["signing.secretKeyRingFile"] = keyFileAbsolutePath!!
    }
    plugins.apply("signing")
}

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

// Configure signatory (in-memory only when using key content); register sign(publications) in afterEvaluate.
if (hasSigningKey) {
    if (hasSigningKeyFromContent) {
        project.extensions.configure<org.gradle.plugins.signing.SigningExtension> {
            val keyId = signingKeyId?.takeIf { it.isNotBlank() }
            if (keyId != null) {
                useInMemoryPgpKeys(keyId, signingKeyContent!!, signingPassword!!)
            } else {
                useInMemoryPgpKeys(signingKeyContent!!, signingPassword!!)
            }
        }
    }
    project.afterEvaluate {
        project.extensions.findByType<org.gradle.plugins.signing.SigningExtension>()?.let { signingExt ->
            signingExt.sign(project.extensions.getByType<org.gradle.api.publish.PublishingExtension>().publications)
        }
    }
}
