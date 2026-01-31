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

- **Runtime (anchor-di-runtime):** `./gradlew :anchor-di-runtime:test` — unit tests for `Anchor`, `AnchorContainer` (init, reset, inject, withScope, singleton, scoped, provider).
- **KSP (anchor-di-ksp):** `./gradlew :anchor-di-ksp:test` — unit tests for validators and code generation.
- **Sample app:** `./gradlew :composeApp:test` — commonTest for composeApp.

Run all library tests: `./gradlew :anchor-di-runtime:test :anchor-di-ksp:test`

---

## Code style and structure

- **Kotlin:** Follow standard Kotlin style (e.g. 4-space indent, naming conventions). The project does not enforce a formatter in CI; keep style consistent with existing files.
- **Modules:**
  - **anchor-di-api:** Annotations and public API only; no runtime logic.
  - **anchor-di-runtime:** Container, scopes, `Anchor`; no Compose or nav.
  - **anchor-di-ksp:** Symbol processing and codegen; JVM-only.
  - **anchor-di-compose:** Compose helpers (`anchorInject`, `viewModelAnchor`).
  - **anchor-di-navigation:** Nav-scoped DI (`NavScopeContainer`, `NavigationScopedContent`, disposal when popped).

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
- Ensure `./gradlew build` and the test commands above pass.
- Update documentation if you change public API or behavior.

---

## Questions

Open an issue for bugs, feature ideas, or questions about the design.
