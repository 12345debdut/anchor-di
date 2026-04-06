package com.debdut.anchordi.benchmark

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.TearDown
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

// ---------------------------------------------------------------------------
// Koin module builders — equivalent to Anchor DI's ScalableContributor
// ---------------------------------------------------------------------------

fun koinModuleWithBindings(count: Int) =
    module {
        for (i in 0 until count step 2) {
            single(named("bench.Service$i")) { Any() }
        }
        for (i in 1 until count step 2) {
            single(named("bench.Service$i")) {
                get<Any>(named("bench.Service${i - 1}"))
                Any()
            }
        }
    }

val koinResolveModule =
    module {
        singleOf(::SimpleService)
        singleOf(::DependentService)
        factory { ComplexService(get(), get()) }
    }

// ---------------------------------------------------------------------------
// Benchmarks
// ---------------------------------------------------------------------------

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class KoinInitBenchmark {
    @Benchmark
    fun init10Bindings() {
        val app = startKoin { modules(koinModuleWithBindings(10)) }
        stopKoin()
    }

    @Benchmark
    fun init100Bindings() {
        val app = startKoin { modules(koinModuleWithBindings(100)) }
        stopKoin()
    }

    @Benchmark
    fun init500Bindings() {
        val app = startKoin { modules(koinModuleWithBindings(500)) }
        stopKoin()
    }
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class KoinResolveBenchmark {
    private lateinit var koin: org.koin.core.Koin

    @Setup
    fun setup() {
        koin = startKoin { modules(koinResolveModule) }.koin
    }

    @TearDown
    fun teardown() {
        stopKoin()
    }

    /** Warm singleton — already cached after first access. */
    @Benchmark
    fun warmSingletonResolve(): Any = koin.get<SimpleService>()

    /** Unscoped — new instance every call (factory). */
    @Benchmark
    fun unscopedResolve(): Any = koin.get<ComplexService>()
}
