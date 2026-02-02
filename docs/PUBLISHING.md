# Publishing Anchor DI to Maven Central

This document describes how to publish the Anchor DI library modules to Maven Central via the **Central Publisher Portal** ([central.sonatype.com](https://central.sonatype.com/)). The legacy OSSRH service (`s01.oss.sonatype.org`) was retired June 30, 2025. This project uses the **Portal OSSRH Staging API** (`ossrh-staging-api.central.sonatype.com`), which works with the same Gradle `maven-publish` workflow and Central Portal tokens.

---

## 1. Prerequisites

- **Central Publisher Portal account**  
  Sign up or log in at [central.sonatype.com](https://central.sonatype.com/). Create or use a verified namespace (e.g. `com.debdut` or `io.github.<username>`). Migrated OSSRH publishers can use their existing username/password to log in.

- **PGP key for signing**  
  Maven Central requires all artifacts to be signed. Generate a key pair (e.g. with GnuPG) and upload the **public** key to a keyserver (e.g. `keyserver.ubuntu.com`). Keep the **private** key and passphrase secure. See **§1.1** below for where each GitHub secret comes from.

- **Maven Central user token**  
  In the Central Portal, go to [Setup Token-Based Authentication](https://central.sonatype.com/usertoken) and generate a **Portal** user token. Use the username and password from that token for publishing (not your account login). **Legacy OSSRH tokens return 401**; use a token from the Central Portal.

### 1.1 Where to get SIGNING_KEY_ID, SIGNING_PASSWORD, and GPG_PRIVATE_KEY

These three values come from **one PGP key pair** you create yourself. They are not found anywhere — you generate them once and reuse them for every publish.

| Secret | What it is | Where it comes from |
|--------|------------|----------------------|
| **SIGNING_KEY_ID** | Last **8 characters** of your PGP key ID | Shown after you create the key (see step 2 below). |
| **SIGNING_PASSWORD** | Passphrase that protects your private key | You **choose** this when creating the key (step 1). You must remember it; there is no “lookup”. |
| **GPG_PRIVATE_KEY** | Your PGP **private** key in text form | You **export** it from your keyring (step 3 below). |

**Step 1 — Install GnuPG (if needed)**

- **macOS:** `brew install gnupg`
- **Windows:** [Gpg4win](https://www.gpg4win.org/) or [GnuPG](https://gnupg.org/download/)
- **Linux:** `sudo apt install gnupg` (or your distro’s package manager)

**Step 2 — Create a key pair and get the key ID**

Run:

```bash
gpg --full-generate-key
```

- Choose default key type (e.g. RSA and RSA, or ECC).
- Key size: 4096 for RSA, or accept default for ECC.
- Expiration: 0 = no expiry (or set a date if you prefer).
- Enter your **name** and **email** (can be any; often the same as your Sonatype account).
- Enter a **passphrase** — this is your **SIGNING_PASSWORD**. Store it somewhere safe (e.g. password manager).

When it finishes, list your keys:

```bash
gpg --list-keys
```

Example output:

```
pub   rsa4096 2024-01-15 [SC]
      F175482952A225BFD4A07A713EE6B5F76620B385CE
uid           [ultimate] Your Name <you@example.com>
sub   rsa4096 2024-01-15 [E]
```

The long hex string (e.g. `F175482952A225BFD4A07A713EE6B5F76620B385CE`) is your **key ID**.  
**SIGNING_KEY_ID** = last **8** characters: `20B385CE` (use your own key’s last 8 chars).

**Step 3 — Export the private key (for GitHub secret GPG_PRIVATE_KEY)**

Replace `YOUR_KEY_ID` with the **full** key ID from step 2 (the long hex string):

```bash
gpg --armor --export-secret-keys YOUR_KEY_ID
```

Example:

```bash
gpg --armor --export-secret-keys F175482952A225BFD4A07A713EE6B5F76620B385CE
```

Copy the **entire** output, including the lines:

```
-----BEGIN PGP PRIVATE KEY BLOCK-----
...
-----END PGP PRIVATE KEY BLOCK-----
```

That block is your **GPG_PRIVATE_KEY**. Paste it into the GitHub secret `GPG_PRIVATE_KEY` (as one multi-line value).

**Step 4 — Upload the public key to a keyserver (required by Maven Central)**

Maven Central checks that your public key is published. Run (use your key ID):

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

**Summary for GitHub Secrets**

| GitHub secret | Value |
|---------------|--------|
| `SIGNING_KEY_ID` | Last 8 characters of your key ID (e.g. `20B385CE`). |
| `SIGNING_PASSWORD` | The passphrase you entered when creating the key. |
| `GPG_PRIVATE_KEY` | Full output of `gpg --armor --export-secret-keys YOUR_KEY_ID` (including `-----BEGIN...` and `-----END...`). |

**Local setup (key from file)**

Export your secret key as **binary** (no `--armor`): `gpg --export-secret-keys YOUR_KEY_ID > ~/.gradle/signing-key.gpg`. Then in `~/.gradle/gradle.properties` set `signing.keyFile=~/.gradle/signing-key.gpg` plus `signing.keyId` and `signing.password`. Gradle will read the keyring file directly and avoid “Could not read PGP secret key” issues.

---

## 2. Project configuration

### 2.1 Version and group

In **`gradle.properties`** (or via `-P` / env):

- **`LIBRARY_GROUP`** — Maven groupId (e.g. `io.github.12345debdut` or `com.debdut`). Must match a verified namespace.
- **`LIBRARY_VERSION`** — Version to publish (e.g. `x.x.x`). Do **not** use `-SNAPSHOT` for a release to Maven Central.

Optional POM metadata (also in `gradle.properties` or root `build.gradle.kts`):

- `POM_NAME`, `POM_DESCRIPTION`, `POM_URL`, `POM_SCM_*`, `POM_LICENSE_*`, `POM_DEVELOPER_*`

### 2.2 Where to store your token (do not commit)

**Recommended: user-level Gradle properties**

1. Open (or create) **`~/.gradle/gradle.properties`** in your home directory (not inside the project).
2. Add these two lines, using the **username** and **password** from your Maven Central token (the ones Central shows when you generate the token):

```properties
SONATYPE_USERNAME=<paste your token username here>
SONATYPE_PASSWORD=<paste your token password here>
```

3. Save the file. Gradle will read it automatically; this file is never committed to git.
4. **Do not** put these in the project’s `gradle.properties` — that file is usually committed.

**Alternative: environment variables**

- Set `ORG_GRADLE_PROJECT_SONATYPE_USERNAME` and `ORG_GRADLE_PROJECT_SONATYPE_PASSWORD` in your shell or CI. Gradle picks these up as project properties.

**Signing from key file (recommended for local)**

Use a **binary** secret key file so Gradle’s signing plugin reads it directly. That avoids “Could not read PGP secret key” from in-memory ASCII-armored parsing. In `~/.gradle/gradle.properties` add:

```properties
signing.keyId=<last-8-chars-of-your-key-id>
signing.password=<key-passphrase>
signing.keyFile=~/.gradle/signing-key.gpg
```

**Export the key as BINARY (no `--armor`):**

```bash
gpg --export-secret-keys YOUR_KEY_ID > ~/.gradle/signing-key.gpg
```

Do **not** use `gpg --armor --export-secret-keys` — Gradle’s file-based signatory expects a **binary** keyring file. You can use `signing.secretKeyRingFile` instead of `signing.keyFile` (same meaning). Paths starting with `~` are expanded to your home directory.

**Option B — Key content in env (CI)**

For GitHub Actions or CI, pass the key content as a secret (no file on the runner):

- `ORG_GRADLE_PROJECT_SONATYPE_USERNAME` / `ORG_GRADLE_PROJECT_SONATYPE_PASSWORD`
- `signingInMemoryKeyId` (last 8 chars of key ID)
- `signingInMemoryKeyPassword` (passphrase)
- `signingInMemoryKey` (full ASCII-armored private key, multi-line)

**Troubleshooting: "has no configured signatory"**

The convention reads signing properties from the **root project** (so `~/.gradle/gradle.properties` is visible). Ensure:

1. **File:** `~/.gradle/gradle.properties` (user home, not the project directory). Properties in the repo’s `gradle.properties` are fine too.
2. **Properties:** `signing.keyId`, `signing.password`, and `signing.keyFile` (or `signing.secretKeyRingFile`) are all set.
3. **Key file:** The path in `signing.keyFile` points to an existing file (e.g. `~/.gradle/signing-key.gpg`) that contains the full ASCII-armored private key (`-----BEGIN PGP PRIVATE KEY BLOCK-----` … `-----END PGP PRIVATE KEY BLOCK-----`).
4. **Path:** On some systems `~` may not expand; use an absolute path (e.g. `/Users/you/.gradle/signing-key.gpg`) if needed.

**Troubleshooting: "Could not read PGP secret key"**

When using a **key file**, Gradle expects a **binary** keyring, not ASCII-armored:

1. **Export as BINARY (no `--armor`):**
   ```bash
   gpg --export-secret-keys YOUR_KEY_ID > ~/.gradle/signing-key.gpg
   ```
   If you used `gpg --armor --export-secret-keys` before, re-export without `--armor` and overwrite the file.

2. **Passphrase:** `signing.password` must be the passphrase you set when creating the key.

3. **Key ID:** Use the **last 8 hex characters** from `gpg --list-keys --keyid-format short` (e.g. `20B385CE`).

---

## 3. Published modules

The root `build.gradle.kts` applies the publish convention to:

- **anchor-di-api**
- **anchor-di-core**
- **anchor-di-ksp**
- **anchor-di-compose**
- **anchor-di-presentation**

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

- **Publish to Central Publisher Portal (signing required):**
  ```bash
  ./gradlew publishAllPublicationsToSonatypeRepository
  ```
  This uploads to `https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/`.  
  If credentials are missing, the build will still run but the upload step may fail. Use a token from [central.sonatype.com/usertoken](https://central.sonatype.com/usertoken).

- **Per-module:**  
  `./gradlew :anchor-di-api:publishKotlinMultiplatformPublicationToSonatypeRepository` (and similar for other publications).

---

## 5. Release flow

1. Set **`LIBRARY_VERSION`** in `gradle.properties` to the release version (e.g. `x.x.x`), with **no** `-SNAPSHOT`.
2. Ensure **signing** and **Sonatype credentials** are configured (see §2.2).
3. Run:
   ```bash
   ./gradlew publishAllPublicationsToSonatypeRepository
   ```
4. In [Central Publisher Portal](https://central.sonatype.com/publishing), open the deployment created by the upload.
5. **Publish** the deployment to release to Maven Central (or **Drop** if needed). After validation, artifacts will be available on Maven Central (often within 15–30 minutes).
6. For the next development cycle, set **`LIBRARY_VERSION`** back to a snapshot (e.g. `0.1.1-SNAPSHOT`) if desired.

---

## 6. GitHub Actions (manual publish)

A **manual workflow** is provided so you can trigger a publish from GitHub with a custom version and module selection.

### 6.1 How to run

1. In your repo, go to **Actions** → **Publish to Maven Central**.
2. Click **Run workflow**.
3. Fill in:
   - **Version** — use the release version (e.g. `x.x.x`; no `-SNAPSHOT`). See README / version in one place.
   - **Namespace** — your Central Portal namespace (e.g. `io.github.USERNAME`). Must match [central.sonatype.com/publishing/namespaces](https://central.sonatype.com/publishing/namespaces). Default is `io.github.12345debdut`.
   - **Modules** — choose **all** or a single module (`anchor-di-api`, `anchor-di-core`, etc.). For first release use **all**.
   - **Custom modules** (optional) — comma-separated list to publish instead of the dropdown, e.g. `anchor-di-api,anchor-di-core,anchor-di-compose`. Leave empty to use the dropdown.
4. Click **Run workflow**. The job publishes to the OSSRH Staging API and then uploads the deployment to the Central Publisher Portal (same IP required).
5. In [central.sonatype.com/publishing](https://central.sonatype.com/publishing), open the new deployment → **Publish** to release to Maven Central (or **Drop** if needed).

### 6.2 Required repository secrets

Add these under **Settings** → **Secrets and variables** → **Actions**:

| Secret name | Description |
|-------------|-------------|
| `MAVEN_CENTRAL_USERNAME` | Central Portal user token username (from [central.sonatype.com/usertoken](https://central.sonatype.com/usertoken); legacy OSSRH tokens return 401) |
| `MAVEN_CENTRAL_PASSWORD` | Central Portal user token password |
| `SIGNING_KEY_ID` | Last **8 characters** of your PGP key ID (e.g. `20B385CE`) |
| `SIGNING_PASSWORD` | Passphrase for your PGP private key |
| `GPG_PRIVATE_KEY` | Full ASCII-armored private key (contents of `key.gpg` from `gpg --armor --export-secret-keys KEY_ID`) |

The workflow passes these to Gradle as `ORG_GRADLE_PROJECT_*` and `signingInMemoryKey*` so the publish convention can sign and upload.

---

## 7. Consuming published artifacts

After release, consumers can add:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.12345debdut:anchor-di-api:x.x.x")
    implementation("io.github.12345debdut:anchor-di-core:x.x.x")
    implementation("io.github.12345debdut:anchor-di-compose:x.x.x")
    implementation("io.github.12345debdut:anchor-di-presentation:x.x.x")
    add("kspCommonMainMetadata", "io.github.12345debdut:anchor-di-ksp:x.x.x")
    add("kspAndroid", "io.github.12345debdut:anchor-di-ksp:x.x.x")
    // … other KSP targets as needed
}
```

Replace `x.x.x` with the published version (see README for single source of version).

---

## 8. Convention plugin

The logic lives in **`buildSrc/src/main/kotlin/publish-convention.gradle.kts`** (precompiled script plugin, Gradle 10–compatible):

- Sets **group** and **version** from `LIBRARY_GROUP` / `LIBRARY_VERSION`.
- Applies **maven-publish** and **signing**.
- Adds the **Central Publisher Portal** (OSSRH Staging API) and **Maven Local** repositories.
- Configures **POM** (name, description, URL, license, developers, SCM) for all Maven publications.
- **Signs** all publications when a key is configured (file or in-memory); skips signing when no key is set so that `publishToMavenLocal` works without GPG.

To change default group, version, or POM metadata, edit **`gradle.properties`** or override via `-P` / environment.
