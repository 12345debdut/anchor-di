# Contributing to Anchor DI

Thank you for your interest in contributing. This document covers how to run the project, tests, and what to expect from the codebase.

---

## Prerequisites

- **JDK 17+**
- **Android SDK** (for Android targets)
- **Xcode** (for iOS targets; macOS only)

---

## Building and running

- **Build all:** `./gradlew build`
- **Run sample app (desktop):** `./gradlew :composeApp:run`
- **Android:** Open in Android Studio and run `androidApp` or `composeApp` (Compose UI).
- **iOS:** Open `iosApp/` in Xcode; build and run. Ensure the Kotlin framework is built first (e.g. run the Compose framework Gradle task).

---

## Tests

- **Core (anchor-di-core):** `./gradlew :anchor-di-core:test` — unit tests for `Anchor`, `AnchorContainer` (init, reset, inject, withScope, singleton, scoped, provider).
- **KSP (anchor-di-ksp):** `./gradlew :anchor-di-ksp:test` — unit tests for validators and code generation.
- **Sample app:** `./gradlew :composeApp:test` — commonTest for composeApp.

Run all library tests: `./gradlew :anchor-di-core:test :anchor-di-ksp:test`

---

## Code style and structure

- **Kotlin:** Formatting is enforced by **Spotless** with **ktlint**. CI runs `./gradlew spotlessCheck`. Before pushing, run `./gradlew spotlessApply` to fix formatting, or `spotlessCheck` to verify.
- **Conventions:** 4-space indent, ktlint rules; trailing whitespace trimmed, files end with newline.
- **Modules:**
  - **anchor-di-api:** Annotations and public API only; no runtime logic.
  - **anchor-di-core:** Container, scopes, `Anchor`; no Compose or nav.
  - **anchor-di-ksp:** Symbol processing and codegen; JVM-only.
  - **anchor-di-compose:** Compose helpers (`anchorInject`, `viewModelAnchor`).
  - **anchor-di-presentation:** Presentation-scoped DI (`NavScopeContainer`, `NavigationScopedContent`, disposal when popped).

---

## Publishing

To publish the library to Sonatype / Maven Central, see **[docs/PUBLISHING.md](docs/PUBLISHING.md)** for credentials, signing, Gradle tasks, release flow, and CI.

---

## KSP expectations

- Generated code lives in `commonMain` (or the consuming module’s generated source set).
- Scope IDs for built-in components (Singleton, ViewModel, Navigation) use **literal** strings in KSP; custom components use the **referenced class’s qualified name** (see `docs/SCOPES_AND_CUSTOM_COMPONENTS.md`).
- Adding a new annotation or binding kind typically requires: API (annotation), KSP analysis (model builder), validation (validator), and codegen (code generator).

---

## Documentation

- **docs/README.md** — User-facing quick start and concepts.
- **docs/DESIGN.md** — Architecture and module structure.
- **docs/FEATURES.md** — Feature checklist.
- **docs/SCOPES_AND_CUSTOM_COMPONENTS.md** — Scope resolution and custom components.
- **docs/SAMPLE_APP_TESTBED.md** — What the sample app demonstrates and how to test.

When adding or changing behavior, update the relevant doc and KDoc.

---

## Pull requests

- Prefer small, focused PRs.
- Use the [PR template](.github/PULL_REQUEST_TEMPLATE.md) and complete the checklist before requesting review.
- Ensure CI passes: run `./gradlew build` (or at least `:anchor-di-core:jvmTest` and `:anchor-di-ksp:jvmTest`) locally before pushing.
- Update documentation if you change public API or behavior.

For required CI checks and branch protection, see **[docs/PR_CHECKS.md](docs/PR_CHECKS.md)**.

---

## Questions

Open an issue for bugs, feature ideas, or questions about the design.
