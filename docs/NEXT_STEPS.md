# Anchor DI — Next Steps and Improvements

This document outlines what’s achieved, what’s next, and concrete improvements to consider.

---

## 1. What We’ve Achieved

- **Phase 1 (MVP)** — Constructor injection, Singleton, Modules, @Provides, @Binds, qualifiers, KMP + KSP per target.
- **Phase 2 (Production)** — Custom `@Scoped`, Lazy/Provider, validation, cycles, multi-module.
- **Phase 3 (Polish)** — ViewModelComponent, viewModelAnchor(), ActivityScope, Compose integration, Anchor.reset(), docs.
- **Architecture** — Scope resolution documented; custom components reverted with lessons captured in [SCOPES_AND_CUSTOM_COMPONENTS.md](SCOPES_AND_CUSTOM_COMPONENTS.md).
- **Sample** — composeApp with DI, ViewModel, and SingletonComponent modules working.

---

## 2. Next Steps — Prioritized

### 2.1 Short-term (stability and confidence)

| Item | Description | Why |
|------|-------------|-----|
| **Unit tests for runtime** | Tests for `AnchorContainer` (get, Singleton, Scoped, Unscoped, createScope, resolveContainer). | Catch regressions; safe refactors. |
| **KSP processor tests** | Tests that generated code compiles and registers expected bindings (e.g. with a small test module). | Prevents scope ID / binding regressions. |
| **ViewModel scope integration test** | One test: init Anchor, enter ViewModel scope, resolve a type that depends on a ViewModel-scoped binding; assert success. | Validates the “requires a scope” path end-to-end. |
| **Docs polish** | docs/README.md: add ViewModelComponent, viewModelAnchor(), and “Scoped binding requires a scope” troubleshooting. DESIGN.md: link SCOPES doc. | Better onboarding and debugging. |

### 2.2 Medium-term (features and hardening)

| Item | Description | Why |
|------|-------------|-----|
| **Custom components (done right)** | Re-add `@InstallIn(MyScope::class)` using **symbol resolution** for scope ID (`KSClassDeclaration.qualifiedName?.asString()`), keep built-ins on **literals** only. Optional: `Anchor.withScope(scopeId: String)` and `ViewModelComponent.SCOPE_ID` for platforms where `KClass.qualifiedName` is unreliable. | User-defined scopes without reintroducing the previous bug. |
| **Optional: scopedContainer() again** | `Anchor.scopedContainer(scopeClass)` (and maybe `scopeId: String`) for “hold a scope and manage lifecycle” use cases. | Aligns with SCOPES_AND_CUSTOM_COMPONENTS.md and user expectations. |
| **Stronger error messages** | KSP: “Binding X is ViewModel-scoped; ensure the type is only requested inside viewModelAnchor() or Anchor.withScope(ViewModelComponent::class).” Runtime: suggest viewModelAnchor() when failing on ViewModel-scoped key. | Faster debugging. |
| **README / branding** | Root README: make Anchor DI the primary focus (quick start, link to docs/README.md), keep KMP/AGP template as “also included.” | Clear identity for the library. |

### 2.3 Phase 4 — Ecosystem

| Item | Description | Why |
|------|-------------|-----|
| **Publish** | Maven Central (or equivalent) for anchor-di-api, anchor-di-runtime, anchor-di-ksp, anchor-di-compose; version catalog / BOM. | Others can depend on Anchor DI. |
| **Sample app** | Already present (composeApp); add a short “Anchor DI in this app” section in docs and root README. | Demonstrates real usage. |
| **Benchmarks** | Optional: startup cost of Anchor.init + first inject, and scoped vs unscoped resolution. | Justify performance and guide optimizations. |
| **Contributing guide** | CONTRIBUTING.md: branch policy, how to run tests, KSP expectations, doc updates. | Easier external contributions. |

---

## 3. Improvements (by area)

### 3.1 Code and API

- **Key / qualifier** — Consider `Key.of<T>(qualifier)` or a small DSL for readability in generated code (optional).
- **Contributor order** — Document that later contributors can override earlier ones (e.g. tests); enforce or document order if needed.
- **ProGuard/R8** — Add keep rules for public API and generated types if not already present (androidApp / consumer apps).

### 3.2 KSP

- **Incremental** — Confirm KSP is incremental where possible to speed up builds.
- **Scope ID source** — For any new scope (e.g. custom), always use **declaration.qualifiedName?.asString()** from the resolved class symbol, never annotation value `toString()`.
- **Validation** — Optional: warn when a type is ViewModel-scoped but only ever requested from non-ViewModel code paths (hard to do fully; at least document the rule).

### 3.3 Compose / ViewModel

- **viewModelAnchor()** — Already correct. Optional: add `ViewModelComponent.SCOPE_ID` and `Anchor.withScope(scopeId: String)` so JS (or other targets where `qualifiedName` is null) can still use a constant scope ID.
- **ActivityScope** — Document that ActivityScope is “conceptual” until we add `Anchor.setActivityScope()` or similar; or add a small helper in androidMain that ties Activity lifecycle to a scope.

### 3.4 Documentation

- **docs/README.md** — Add: ViewModelComponent, viewModelAnchor(), ViewModelScoped; troubleshooting “Scoped binding requires a scope”; link to SCOPES_AND_CUSTOM_COMPONENTS.md for advanced scopes.
- **DESIGN.md** — Already links to SCOPES doc; keep Phase 4 table in sync with FEATURES.md.
- **FEATURES.md** — Add “Next steps” section that points to this doc and Phase 4 checkboxes.

### 3.5 Testing

- **anchor-di-runtime** — JVM or commonTest (if supported): tests for Anchor.init/reset, get (Unscoped/Singleton/Scoped), createScope, nested scope, missing binding.
- **anchor-di-ksp** — JVM test that runs KSP on a test module and asserts generated file contains expected strings (e.g. Binding.Scoped, correct scope ID).
- **composeApp** — Optional: one Compose test or Android instrumented test that launches a screen using viewModelAnchor() and a ViewModel-scoped dependency.

---

## 4. Suggested order of work

1. **Immediate** — Unit tests for AnchorContainer (and Anchor) + docs polish (ViewModel + troubleshooting).
2. **Next** — KSP test for generated bindings; ViewModel scope integration test.
3. **Then** — Custom components (with symbol-based scope ID) + optional SCOPE_ID/withScope(scopeId).
4. **After** — README focus, Phase 4 (publish, contributing guide, optional benchmarks).

---

## 5. Summary table

| Priority | Area | Action |
|----------|------|--------|
| P0 | Tests | Runtime unit tests; ViewModel scope integration test |
| P0 | Docs | ViewModel + “requires a scope” in docs/README; DESIGN/FEATURES links |
| P1 | Features | Custom components (symbol-based scope ID); optional scopedContainer/withScope(scopeId) |
| P1 | DX | Clearer KSP/runtime errors for scoped bindings |
| P2 | Ecosystem | Publish, CONTRIBUTING.md, sample app section in README |
| P2 | Polish | README branding; optional benchmarks |

This keeps the library stable and well-documented first, then adds custom components safely, then focuses on ecosystem and polish.
