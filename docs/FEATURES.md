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

- [ ] **4.1** Maven Central / Gradle publish
- [ ] **4.2** Sample app (composeApp present; document “Anchor DI in this app” in README)
- [ ] **4.3** Benchmarks (optional)
- [ ] **4.4** Contributing guide (CONTRIBUTING.md)

---

## Next steps and improvements

See **[NEXT_STEPS.md](NEXT_STEPS.md)** for:

- **Short-term**: Runtime and KSP tests, ViewModel-scope integration test, docs polish (ViewModelComponent, “requires a scope” troubleshooting).
- **Medium-term**: Custom components (re-add with symbol-based scope ID), optional `scopedContainer` / `withScope(scopeId)`, stronger error messages, README focus on Anchor DI.
- **Phase 4**: Publish, contributing guide, optional benchmarks.
- **Improvements by area**: Code/API, KSP, Compose/ViewModel, docs, testing.
