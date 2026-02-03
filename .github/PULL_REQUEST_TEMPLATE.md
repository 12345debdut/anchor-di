## Description

<!-- Briefly describe what this PR does and why. Link any related issue with "Fixes #123" or "Relates to #123". -->

## Checklist

- [ ] **Format** — `./gradlew spotlessCheck` passes (run `spotlessApply` to fix).
- [ ] **CI passes** — `./gradlew build` (and `:anchor-di-core:jvmTest` / `:anchor-di-ksp:jvmTest`) pass locally or on this branch.
- [ ] **No unintended changes** — Only files relevant to this change are modified (no stray formatting or unrelated edits).
- [ ] **Public API** — If you added or changed public API (annotations, types, functions), the change is documented (KDoc or `docs/`).
- [ ] **Tests** — New or changed behavior is covered by tests where appropriate.
- [ ] **Documentation** — User-facing behavior or module structure changes are reflected in `CONTRIBUTING.md` or `docs/` if needed.
- [ ] **Changelog** — For user-visible fixes or features, a short entry is added to the changelog (or release notes) if the project maintains one.

## Notes for reviewers

<!-- Optional: call out anything reviewers should focus on, or known limitations. -->
