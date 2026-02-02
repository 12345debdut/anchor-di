# PR checks and contribution standards

This document describes the standard checks and practices used to keep pull requests strict and future-proof.

---

## 1. CI checks (automated)

Every PR must pass the following in GitHub Actions:

| Check | What it does |
|-------|----------------|
| **Format (Spotless + ktlint)** | Runs `./gradlew spotlessCheck`. Ensures Kotlin (`.kt`) and Gradle Kotlin DSL (`.gradle.kts`) files match ktlint style. |
| **Test & Build** | Runs `:anchor-di-core:jvmTest`, `:anchor-di-ksp:jvmTest`, then full `./gradlew build`. Ensures tests pass and the whole project compiles. |

**Local pre-push:** Run `./gradlew spotlessCheck` (or `spotlessApply` to fix), then `./gradlew build` (or at least the two `jvmTest` tasks) before pushing so CI stays green.

If **Full build** fails in CI (e.g. missing Android SDK), add an Android SDK setup step to the workflow or restrict the build to library JVM targets (e.g. `./gradlew :anchor-di-api:build :anchor-di-core:build :anchor-di-ksp:build`).

---

## 2. Branch protection (repository settings)

Configure in **GitHub → Settings → Branches → Branch protection rules** for `main` (or your default branch):

| Rule | Purpose |
|------|--------|
| **Require a pull request before merging** | No direct pushes to `main`; all changes go through PRs. |
| **Require status checks to pass** | Select the CI job name (e.g. "Test & Build") so PRs cannot merge with failing CI. |
| **Require branches to be up to date** | Require latest `main` before merge to avoid hidden merge conflicts. |
| **Require review(s)** | At least one approval from a maintainer (adjust count as needed). |
| **Do not allow bypassing** | Apply rules to admins so no one merges without checks. |

Optional:

- **Require linear history** — Keeps history clean; use if you prefer rebase/linear merges.
- **Require signed commits** — For higher security; only if your team already uses signing.

---

## 3. PR template checklist

Every PR uses [.github/PULL_REQUEST_TEMPLATE.md](../.github/PULL_REQUEST_TEMPLATE.md). Contributors must confirm:

- CI passes.
- No unintended or unrelated changes.
- Public API changes are documented.
- New or changed behavior has tests where appropriate.
- Docs (e.g. `CONTRIBUTING.md`, `docs/`) updated when behavior or structure changes.
- Changelog/release notes updated for user-visible changes (if the project keeps one).

Reviewers can use the same list when approving.

---

## 4. Code quality (optional but recommended)

To make PRs even stricter and future-proof:

| Tool | Role |
|------|------|
| **Spotless + ktlint** | ✅ **In use.** Enforces Kotlin and Gradle Kotlin DSL formatting in CI via `./gradlew spotlessCheck`. Contributors run `./gradlew spotlessApply` to fix. |
| **Detekt** | Static analysis: complexity, naming, bugs. Run in CI and optionally fail on configurable rules. |
| **Dependency updates** | Use Dependabot or Renovate to open PRs for dependency updates; review and merge to stay current. |

Adding more later:

1. Add the plugin and config (e.g. `detekt`) to the root and/or subprojects.
2. Add a CI step that runs the check (e.g. `./gradlew detekt`).
3. Add the step to branch protection “Required status checks” so PRs cannot merge if the check fails.

---

## 5. Documentation and API stability

- **Public API:** Any new or changed public API (annotations, types, functions in published modules) should have KDoc and be mentioned in `docs/` if it affects usage.
- **Breaking changes:** Prefer deprecation + new API over removing or changing contracts; document in changelog/release notes.
- **Changelog:** For user-facing fixes or features, keep a short entry (e.g. in `docs/CHANGELOG.md` or release notes) so releases stay understandable.

---

## 6. Summary

| Area | Standard |
|------|----------|
| **CI** | Format (Spotless + ktlint), tests, and full build must pass on every PR. |
| **Branch protection** | Require PR, status checks, and review(s) on `main`. |
| **PR content** | Use the PR template checklist; reviewers verify it. |
| **Code quality** | Spotless + ktlint in CI; optional: add Detekt. |
| **Docs & API** | Document public API and user-visible changes; maintain a changelog where applicable. |

These checks keep contributions consistent, prevent broken builds on `main`, and make the project easier to maintain and evolve.
