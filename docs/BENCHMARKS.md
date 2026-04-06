# Anchor DI vs Koin — Performance Benchmarks

All benchmarks run on JVM using [kotlinx-benchmark](https://github.com/Kotlin/kotlinx-benchmark) (JMH under the hood). Higher is better (ops/ms = operations per millisecond).

## Container Initialization

How fast each framework initializes its container with N bindings (mix of leaf singletons and singletons with one transitive dependency).

| Bindings | Anchor DI (ops/ms) | Koin (ops/ms) | Anchor DI advantage |
|----------|---------------------|---------------|---------------------|
| 10       | **2,877**           | 514           | **5.6x faster**     |
| 100      | **300**             | 54            | **5.5x faster**     |
| 500      | **50**              | 11            | **4.7x faster**     |

Anchor DI's initialization is **~5x faster** across all scales because its compile-time generated code directly registers pre-built `Factory` instances. Koin's runtime DSL (`module { single { ... } }`) must interpret lambda definitions and build its internal registry dynamically.

## Dependency Resolution

How fast each framework resolves a dependency after initialization.

| Scenario | Anchor DI (ops/ms) | Koin (ops/ms) | Notes |
|----------|---------------------|---------------|-------|
| Warm singleton | 13,210 | **15,442** | Koin ~17% faster (both are HashMap lookups; Koin's key type is lighter) |
| Unscoped (factory) | **4,208** | 3,878 | Anchor DI ~9% faster |

For **warm singleton** resolution, both frameworks are extremely fast (nanosecond-range per call). Koin has a slight edge here because its internal key type has lower overhead than Anchor DI's `Key` data class. In practice, this difference is negligible — both resolve in under 100ns.

For **unscoped (factory)** resolution, Anchor DI is slightly faster because the generated `Factory.create()` calls constructors directly, while Koin invokes lambda closures stored in its registry.

## Key Takeaways

1. **Initialization is where Anchor DI dominates.** App startup is critical on mobile — a 5x faster init means the DI container is ready sooner. For a 500-binding app, that's the difference between ~20ms (Anchor DI) and ~90ms (Koin).

2. **Resolution speed is comparable.** Once initialized, both frameworks resolve dependencies in nanoseconds. The ~10% differences are within noise for real-world apps.

3. **Compile-time safety is the real win.** Beyond raw speed, Anchor DI catches missing bindings, circular dependencies, and scope violations at build time. Koin discovers these at runtime (`NoBeanDefFoundException`).

## Methodology

- **JVM:** OpenJDK 21+ (Gradle toolchain)
- **Benchmark framework:** kotlinx-benchmark 0.4.12 (JMH)
- **Configuration:** 5 warmup iterations, 5 measurement iterations, 1s each
- **Anchor DI bindings:** Hand-written `ComponentBindingContributor` implementations simulating KSP-generated code (direct `Factory` registration, `Key`-based lookup)
- **Koin bindings:** Standard `module { single { } }` DSL with `named()` qualifiers
- **Hardware:** Results will vary by machine. Run `./gradlew :benchmarks:jvmBenchmark` to reproduce on your hardware.

## Reproducing

```bash
./gradlew :benchmarks:jvmBenchmark
```

Results are written to `benchmarks/build/reports/benchmarks/`.
