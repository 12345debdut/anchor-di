# Anchor DI — Production-Grade Roadmap

This document defines what “production grade” means for Anchor DI and lays out a phased plan to get there. It complements [NEXT_STEPS.md](NEXT_STEPS.md) (detailed improvements) and [FEATURES.md](FEATURES.md) (feature checklist).

---

## What “production grade” means

| Goal | Criteria |
|------|----------|
| **Stability** | No known regressions; critical paths covered by tests; KSP upgrades manageable. |
| **Quality** | Clear error messages; docs and troubleshooting for common failures; API surface stable or versioned. |
| **Confidence** | CI runs tests on every change; integration tests cover key flows (init → scope → resolve). |
| **Ecosystem** | Published to Maven Central; sample app and migration/quick start; ProGuard/R8 guidance for Android. |
| **1.0 readiness** | API and behavior documented; breaking changes (if any) planned and communicated; license and branding clear. |

---

## Phases overview

| Phase | Focus | Outcome |
|-------|--------|---------|
| **A** | Stability & tests | CI + integration test; KSP test strategy; no critical gaps. |
| **B** | Quality & DX | Error messages; docs polish; README/license; ProGuard. |
| **C** | Hardening & optional features | Custom components (symbol-based); scopedContainer/withScope(scopeId); optional benchmarks. |
| **D** | 1.0 readiness | API freeze policy; changelog; release checklist; optional BOM. |

---

## Phase A — Stability & tests

**Goal:** Every change is validated by CI; key flows are covered by integration tests; KSP tests are maintainable.

| # | Task | Description | Owner / Notes |
|---|------|-------------|----------------|
| A1 | **CI: test on every PR** | Add a GitHub Actions workflow that runs `./gradlew build` (or at least `:anchor-di-core:test` and `:anchor-di-ksp:test`) on push/PR to main. | Unblocks safe refactors. |
| A2 | **ViewModel scope integration test** | One test (runtime or composeApp): init Anchor with a contributor that has a ViewModel-scoped binding; enter ViewModel scope; resolve a type that depends on it; assert success. | Validates “requires a scope” path end-to-end. |
| A3 | **KSP test strategy** | Document or automate: (1) how KspFakes are kept in sync with KSP API, or (2) add a small test module that KSP processes and assert generated code compiles and registers expected bindings. | Reduces breakage on KSP upgrades. |
| A4 | **Stabilize validator test assertions** | Prefer asserting on stable substrings or error codes rather than full message text where possible; or centralize expected messages in constants. | Fewer brittle failures when copy changes. |

**Done when:** CI runs tests on PRs; at least one integration test covers ViewModel scope; KSP test approach is documented or automated.

---

## Phase B — Quality & DX

**Goal:** Users and contributors can resolve common issues quickly; legal and branding are clear.

| # | Task | Description | Owner / Notes |
|---|------|-------------|----------------|
| B1 | **README license** | Replace “TBD” in README with “Apache-2.0” and link to [LICENSE](../LICENSE). | Legal clarity. |
| B2 | **Docs: “Scoped binding requires a scope”** | In docs/README (or root README): add a short troubleshooting section for “Scoped binding for X requires a scope” — when it happens, how to fix (viewModelAnchor(), withScope, etc.). | Faster debugging. |
| B3 | **Docs: ViewModelComponent & viewModelAnchor()** | Ensure ViewModelComponent, viewModelAnchor(), and when to use them are clearly described in the main user-facing doc (README or docs/README). | Onboarding. |
| B4 | **Stronger KSP/runtime errors** | KSP: when a binding is ViewModel-scoped, suggest “ensure the type is only requested inside viewModelAnchor() or Anchor.withScope(ViewModelComponent::class)”. Runtime: when resolution fails for a scoped key, suggest viewModelAnchor() or withScope if applicable. | DX. |
| B5 | **ProGuard/R8 for consumers** | Add a short section in README or docs: “Android: if you use ProGuard/R8, add keep rules for…” (public API + generated types). Optionally ship a consumer ProGuard snippet. | Android production use. |

**Done when:** License is clear in README; troubleshooting and ViewModel docs are in place; error messages guide users; Android keep rules are documented or provided.

---

## Phase C — Hardening & optional features

**Goal:** Custom scopes and advanced usage are supported without regressions; optional performance evidence.

| # | Task | Description | Owner / Notes |
|---|------|-------------|----------------|
| C1 | **Custom components (symbol-based)** | Re-add `@InstallIn(MyScope::class)` using **symbol resolution** for scope ID (`KSClassDeclaration.qualifiedName?.asString()`); keep built-ins on literals only. See [SCOPES_AND_CUSTOM_COMPONENTS.md](SCOPES_AND_CUSTOM_COMPONENTS.md) and NEXT_STEPS. | User-defined scopes. |
| C2 | **scopedContainer / withScope(scopeId)** | Already present; document or add `ViewModelComponent.SCOPE_ID` (and similar) for targets where `KClass.qualifiedName` is unreliable. | JS / multi-target clarity. |
| C3 | **README / branding** | Make Anchor DI the primary focus of the root README; keep template/sample as “also included.” | Clear identity. |
| C4 | **Benchmarks (optional)** | Optional: measure Anchor.init + first inject, and scoped vs unscoped resolution; document or add to repo. | Performance narrative. |

**Done when:** Custom components work with symbol-based scope ID; scope IDs are documented for all targets; README centers on Anchor DI; optional benchmarks done or explicitly deferred.

---

## Phase D — 1.0 readiness

**Goal:** API and behavior are stable and documented; releases are repeatable and communicated.

| # | Task | Description | Owner / Notes |
|---|------|-------------|----------------|
| D1 | **API freeze / stability policy** | Document in CONTRIBUTING or a versioning doc: how we treat breaking changes (e.g. 0.x = minor breaks allowed with changelog; 1.0 = stable). | Set expectations. |
| D2 | **Changelog** | Maintain a CHANGELOG.md (or similar) for user-facing changes and breaking changes per release. | Transparency. |
| D3 | **Release checklist** | Short checklist before tagging 1.0: tests pass, docs updated, changelog updated, version bumped, publish workflow run. | Repeatable releases. |
| D4 | **Optional: BOM** | Consider a BOM (Bill of Materials) for anchor-di-* so consumers can depend on aligned versions with one coordinate. | Ecosystem polish. |

**Done when:** Stability policy and changelog exist; release process is documented; 1.0 can be cut with confidence.

---

## Suggested order of work

1. **Phase A** — CI (A1), integration test (A2), KSP strategy (A3). Then Phase B in parallel with C where possible.
2. **Phase B** — B1 (license), B2–B3 (docs), B4 (errors), B5 (ProGuard).
3. **Phase C** — C1 (custom components) if needed for 1.0; C2–C4 as capacity allows.
4. **Phase D** — Before announcing 1.0: D1–D3; D4 optional.

---

## Summary table

| Priority | Phase | Key deliverables |
|----------|--------|-------------------|
| P0 | A | CI on PRs, ViewModel integration test, KSP test strategy |
| P0 | B | README license, “requires a scope” + ViewModel docs, error message improvements |
| P1 | B | ProGuard/R8 guidance |
| P1 | C | Custom components (symbol-based), README branding |
| P2 | C | SCOPE_ID/withScope(scopeId) docs, optional benchmarks |
| P2 | D | API policy, changelog, release checklist |
| P3 | D | Optional BOM |

---

For detailed improvement ideas and area-by-area notes, see **[NEXT_STEPS.md](NEXT_STEPS.md)**.
