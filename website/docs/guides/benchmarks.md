# Performance Benchmarks

Anchor DI is a compile-time DI framework. This page shows how it compares to Koin (a popular runtime DI framework) in real benchmarks.

All benchmarks use [kotlinx-benchmark](https://github.com/Kotlin/kotlinx-benchmark) (JMH under the hood) on JVM. Higher is better (ops/ms = operations per millisecond).

---

## Container Initialization

How fast each framework initializes its container with N bindings (mix of leaf singletons and singletons with one transitive dependency).

| Bindings | Anchor DI (ops/ms) | Koin (ops/ms) | Anchor DI Advantage |
|----------|---------------------|---------------|---------------------|
| 10       | **2,877**           | 514           | **5.6x faster**     |
| 100      | **300**             | 54            | **5.5x faster**     |
| 500      | **50**              | 11            | **4.7x faster**     |

Anchor DI's initialization is **~5x faster** because its compile-time generated code directly registers pre-built `Factory` instances. Koin's runtime DSL (`module { single { ... } }`) must interpret lambda definitions and build its internal registry dynamically.

**What this means for your app:** For a 500-binding app, that's the difference between ~20ms (Anchor DI) and ~90ms (Koin) at startup. On mobile, every millisecond of cold start matters.

---

## Dependency Resolution

How fast each framework resolves a dependency after initialization.

| Scenario | Anchor DI (ops/ms) | Koin (ops/ms) | Notes |
|----------|---------------------|---------------|-------|
| Warm singleton | 13,210 | 15,442 | Both sub-100ns per call |
| Unscoped (factory) | **4,208** | 3,878 | Anchor DI ~9% faster |

For **warm singleton** resolution, both frameworks are extremely fast. In practice, this difference is negligible -- both resolve cached singletons in under 100ns.

For **unscoped (factory)** resolution, Anchor DI is slightly faster because generated `Factory.create()` calls constructors directly, while Koin invokes lambda closures.

---

## The Real Win: Compile-Time Safety

Beyond raw speed, the biggest advantage of Anchor DI is **compile-time validation**:

| Error Type | Koin | Anchor DI |
|------------|------|-----------|
| Missing binding | Runtime crash (`NoBeanDefFoundException`) | Build fails |
| Circular dependency | Runtime crash or hang | Build fails |
| Scope violation | Runtime crash | Build fails |
| Duplicate binding | Silently overwritten or runtime crash | Build fails |

**If it compiles, it works.**

---

## Reproducing the Benchmarks

Run the benchmarks on your own hardware:

```bash
./gradlew :benchmarks:jvmBenchmark
```

Results are written to `benchmarks/build/reports/benchmarks/`.

**Configuration:** 5 warmup iterations, 5 measurement iterations, 1 second each. Results will vary by machine.
