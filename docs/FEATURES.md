# Anchor DI — Feature Roadmap

Quick reference for implementing features. Check off as completed.

---

## Phase 1: MVP

- [x] **1.1** `@Inject` constructor injection
- [x] **1.2** `@Singleton` scope
- [x] **1.3** `@Module` + `@InstallIn(SingletonComponent::class)`
- [x] **1.4** `@Provides` in modules
- [x] **1.5** `@Binds` (interface → implementation)
- [x] **1.6** Qualifiers (`@Named`, custom `@Qualifier`)
- [x] **1.7** `Anchor.init(modules)` — container bootstrap
- [x] **1.8** `Anchor.inject<T>()` / `container.get<T>()`
- [x] **1.9** KMP: commonMain, androidMain, iosMain
- [x] **1.10** KSP: per-target processing (kspCommonMainMetadata, kspAndroid, kspIos*)

---

## Phase 2: Production Grade

- [x] **2.1** Custom scopes `@Scoped(MyScope::class)`
- [x] **2.2** `Lazy<T>` injection (kotlin.Lazy)
- [x] **2.3** `AnchorProvider<T>` injection
- [x] **2.4** Compile-time validation (missing bindings)
- [x] **2.5** Component dependencies (child/parent scopes) — nested createScope, cycle detection
- [x] **2.6** Multi-module dependency resolution — anchorDiModuleId KSP option

---

## Phase 3: Polish & Extensions

- [x] **3.1** Android ViewModel injection — @AnchorViewModel, viewModelAnchor()
- [x] **3.2** Android Activity scope — ActivityScope, docs
- [x] **3.3** Compose Multiplatform integration — anchorInject(), viewModelAnchor()
- [x] **3.4** Test containers / overrides — Anchor.reset(), later contributors override
- [x] **3.5** Clear error messages — improved runtime & KSP messages
- [x] **3.6** Full documentation — docs/README.md, DESIGN.md

---

## Phase 4: Ecosystem

- [x] **4.1** Maven Central / Gradle publish (Sonatype staging; see docs/PUBLISHING.md). First public release: **0.1.0** (beta).
- [x] **4.2** Sample app (composeApp present; document “Anchor DI in this app” in README)
- [ ] **4.3** Benchmarks (optional)
- [x] **4.4** Contributing guide (CONTRIBUTING.md)

---

---

## Multibinding (Phase 2.5)

- [x] **2.5.1** `@IntoSet` — contribute one element to a multibound `Set<T>`
- [x] **2.5.2** `@IntoMap` with `@StringKey` — contribute one entry to a multibound `Map<String, V>`
- [x] **2.5.3** Runtime: `Binding.MultibindingSet`, `Binding.MultibindingMap`, `BindingRegistry.registerSetContribution` / `registerMapContribution`
- [x] **2.5.4** `Anchor.injectSet<T>()` / `Anchor.injectMap<V>()`, `container.getSet<T>()` / `container.getMap<V>()`
- [x] **2.5.5** KSP: model, validation (duplicate map key), codegen for set/map contributions

---

## Next steps and improvements

- **Production-grade plan:** See **[PRODUCTION_ROADMAP.md](PRODUCTION_ROADMAP.md)** for phased work: Phase A (CI + integration tests), B (docs + errors + ProGuard), C (custom components + branding), D (1.0 readiness).
- **Detailed improvements:** See **[NEXT_STEPS.md](NEXT_STEPS.md)** for short/medium-term items, custom components, error messages, and area-by-area notes.