# Publishing Anchor DI to Sonatype / Maven Central

This document describes how to publish the Anchor DI library modules to Sonatype (staging) and Maven Central.

---

## 1. Prerequisites

- **Sonatype / Maven Central account**  
  Sign up at [central.sonatype.com](https://central.sonatype.com/). Create or use a verified namespace (e.g. `com.debdut` or `io.github.<username>`).

- **PGP key for signing**  
  Maven Central requires all artifacts to be signed. Generate a key pair (e.g. with GnuPG) and upload the **public** key to a keyserver (e.g. `keyserver.ubuntu.com`). Keep the **private** key and passphrase secure.

- **Maven Central user token**  
  In the Central Portal, go to [Setup Token-Based Authentication](https://central.sonatype.com/usertoken) and generate a user token. Use the username and password from that token for publishing (not your account login).

---

## 2. Project configuration

### 2.1 Version and group

In **`gradle.properties`** (or via `-P` / env):

- **`LIBRARY_GROUP`** — Maven groupId (e.g. `io.github.12345debdut` or `com.debdut`). Must match a verified namespace.
- **`LIBRARY_VERSION`** — Version to publish (e.g. `0.1.0`). Do **not** use `-SNAPSHOT` for a release to Maven Central.

Optional POM metadata (also in `gradle.properties` or root `build.gradle.kts`):

- `POM_NAME`, `POM_DESCRIPTION`, `POM_URL`, `POM_SCM_*`, `POM_LICENSE_*`, `POM_DEVELOPER_*`

### 2.2 Credentials (do not commit)

**Option A — `~/.gradle/gradle.properties`**

```properties
SONATYPE_USERNAME=<your-central-portal-token-username>
SONATYPE_PASSWORD=<your-central-portal-token-password>

# Signing (keyring file)
signing.keyId=<last-8-chars-of-your-key-id>
signing.password=<key-passphrase>
signing.secretKeyRingFile=/path/to/secring.gpg
```

**Option B — In-memory (CI)**

Use environment variables or Gradle properties:

- `ORG_GRADLE_PROJECT_SONATYPE_USERNAME` / `ORG_GRADLE_PROJECT_SONATYPE_PASSWORD` (or `SONATYPE_USERNAME` / `SONATYPE_PASSWORD`)
- `signingInMemoryKeyId` (last 8 chars of key ID)
- `signingInMemoryKeyPassword` (passphrase)
- `signingInMemoryKey` (full ASCII-armored private key)

---

## 3. Published modules

The root `build.gradle.kts` applies the publish convention to:

- **anchor-di-api**
- **anchor-di-runtime**
- **anchor-di-ksp**
- **anchor-di-compose**
- **anchor-di-navigation**

Each is published with coordinates:

- **Group:** `LIBRARY_GROUP` (e.g. `io.github.12345debdut`)
- **Artifact:** module name (e.g. `anchor-di-api`, `anchor-di-api-jvm`, `anchor-di-api-android`, …)
- **Version:** `LIBRARY_VERSION`

---

## 4. Gradle tasks

- **Publish to local Maven repo (no signing required):**
  ```bash
  ./gradlew publishAllPublicationsToMavenLocal
  ```
  Or per module: `./gradlew :anchor-di-api:publishToMavenLocal`

- **Publish to Sonatype staging (signing required):**
  ```bash
  ./gradlew publishAllPublicationsToSonatypeRepository
  ```
  This uploads to `https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/`.  
  If credentials are missing, the build will still run but the upload step may fail.

- **Per-module:**  
  `./gradlew :anchor-di-api:publishKotlinMultiplatformPublicationToSonatypeRepository` (and similar for other publications).

---

## 5. Release flow

1. Set **`LIBRARY_VERSION`** in `gradle.properties` to the release version (e.g. `0.1.0`), with **no** `-SNAPSHOT`.
2. Ensure **signing** and **Sonatype credentials** are configured (see §2.2).
3. Run:
   ```bash
   ./gradlew publishAllPublicationsToSonatypeRepository
   ```
4. In [Sonatype Central](https://central.sonatype.com/), open the staging repository created by the upload.
5. **Close** the staging repo, then **Release** it. After validation, artifacts will be available on Maven Central (often within 15–30 minutes).
6. For the next development cycle, set **`LIBRARY_VERSION`** back to a snapshot (e.g. `0.1.1-SNAPSHOT`) if desired.

---

## 6. CI (e.g. GitHub Actions)

- Put **Sonatype username/password** and **signing key (in-memory)** in repository secrets.
- Run the publish task in a job that has those secrets as env vars (e.g. `ORG_GRADLE_PROJECT_SONATYPE_USERNAME`, `ORG_GRADLE_PROJECT_SONATYPE_PASSWORD`, `signingInMemoryKeyId`, `signingInMemoryKeyPassword`, `signingInMemoryKey`).
- Publish only on tags or a dedicated “release” workflow; do not publish on every push.
- **Apple targets** (iOS) require a macOS runner if you publish those variants from CI.

Example (conceptual):

```yaml
- name: Publish to Sonatype
  run: ./gradlew publishAllPublicationsToSonatypeRepository --no-configuration-cache
  env:
    ORG_GRADLE_PROJECT_SONATYPE_USERNAME: ${{ secrets.MAVEN_CENTRAL_USERNAME }}
    ORG_GRADLE_PROJECT_SONATYPE_PASSWORD: ${{ secrets.MAVEN_CENTRAL_PASSWORD }}
    SIGNING_KEY_ID: ${{ secrets.SIGNING_KEY_ID }}
    SIGNING_PASSWORD: ${{ secrets.SIGNING_PASSWORD }}
    SIGNING_KEY: ${{ secrets.GPG_KEY_CONTENTS }}
```

Use the same property names as in §2.2 (e.g. `signingInMemoryKeyId` → `SIGNING_KEY_ID` in your secrets and map to `ORG_GRADLE_PROJECT_signingInMemoryKeyId` if needed).

---

## 7. Consuming published artifacts

After release, consumers can add:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.12345debdut:anchor-di-api:0.1.0")
    implementation("io.github.12345debdut:anchor-di-runtime:0.1.0")
    implementation("io.github.12345debdut:anchor-di-compose:0.1.0")
    implementation("io.github.12345debdut:anchor-di-navigation:0.1.0")
    add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:0.1.0")
    // … other KSP targets as needed
}
```

Replace `0.1.0` with the published `LIBRARY_VERSION`.

---

## 8. Convention script

The logic lives in **`gradle/publish-convention.gradle.kts`**:

- Sets **group** and **version** from `LIBRARY_GROUP` / `LIBRARY_VERSION`.
- Applies **maven-publish** and **signing**.
- Adds the **Sonatype** and **Maven Local** repositories.
- Configures **POM** (name, description, URL, license, developers, SCM) for all Maven publications.
- **Signs** all publications when a key is configured (file or in-memory); skips signing when no key is set so that `publishToMavenLocal` works without GPG.

To change default group, version, or POM metadata, edit **`gradle.properties`** or override via `-P` / environment.
