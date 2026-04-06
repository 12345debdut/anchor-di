package com.debdut.anchordi.benchmark

import com.debdut.anchordi.runtime.Anchor
import com.debdut.anchordi.runtime.AnchorContainer
import com.debdut.anchordi.runtime.Binding
import com.debdut.anchordi.runtime.BindingRegistry
import com.debdut.anchordi.runtime.ComponentBindingContributor
import com.debdut.anchordi.runtime.Factory
import com.debdut.anchordi.runtime.Key
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.TearDown

// ---------------------------------------------------------------------------
// Simulated generated code — mirrors what KSP would produce
// ---------------------------------------------------------------------------

/** A simple service class with no dependencies. */
class SimpleService

/** A service with one dependency, simulating typical constructor injection. */
class DependentService(val dep: SimpleService)

/** A service with two dependencies. */
class ComplexService(val a: SimpleService, val b: DependentService)

/**
 * Contributor that registers [count] singleton bindings.
 * Even-indexed bindings depend on the previous odd-indexed one, simulating
 * a realistic mix of leaf and intermediate services.
 */
class ScalableContributor(private val count: Int) : ComponentBindingContributor {
    override fun contribute(registry: BindingRegistry) {
        // Register leaf bindings (odd indices)
        for (i in 0 until count step 2) {
            val key = Key("bench.Service$i")
            registry.register(
                key,
                Binding.Singleton(
                    object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = Any()
                    },
                ),
            )
        }
        // Register dependent bindings (even indices → depend on i-1)
        for (i in 1 until count step 2) {
            val depKey = Key("bench.Service${i - 1}")
            val key = Key("bench.Service$i")
            registry.register(
                key,
                Binding.Singleton(
                    object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any {
                            container.get(depKey)
                            return Any()
                        }
                    },
                ),
            )
        }
    }
}

/** Small contributor for resolve benchmarks. */
class ResolveContributor : ComponentBindingContributor {
    override fun contribute(registry: BindingRegistry) {
        registry.register(
            Key(SimpleService::class.qualifiedName!!),
            Binding.Singleton(
                object : Factory<Any> {
                    override fun create(container: AnchorContainer): Any = SimpleService()
                },
            ),
        )
        registry.register(
            Key(DependentService::class.qualifiedName!!),
            Binding.Singleton(
                object : Factory<Any> {
                    override fun create(container: AnchorContainer): Any =
                        DependentService(container.get(Key(SimpleService::class.qualifiedName!!)) as SimpleService)
                },
            ),
        )
        registry.register(
            Key(ComplexService::class.qualifiedName!!),
            Binding.Unscoped(
                object : Factory<Any> {
                    override fun create(container: AnchorContainer): Any =
                        ComplexService(
                            container.get(Key(SimpleService::class.qualifiedName!!)) as SimpleService,
                            container.get(Key(DependentService::class.qualifiedName!!)) as DependentService,
                        )
                },
            ),
        )
    }
}

// ---------------------------------------------------------------------------
// Benchmarks
// ---------------------------------------------------------------------------

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class AnchorInitBenchmark {
    @Benchmark
    fun init10Bindings() {
        Anchor.reset()
        Anchor.init(ScalableContributor(10))
    }

    @Benchmark
    fun init100Bindings() {
        Anchor.reset()
        Anchor.init(ScalableContributor(100))
    }

    @Benchmark
    fun init500Bindings() {
        Anchor.reset()
        Anchor.init(ScalableContributor(500))
    }
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class AnchorResolveBenchmark {
    @Setup
    fun setup() {
        Anchor.reset()
        Anchor.init(ResolveContributor())
    }

    @TearDown
    fun teardown() {
        Anchor.reset()
    }

    /** Warm singleton — already cached after first access in setup. */
    @Benchmark
    fun warmSingletonResolve(): Any = Anchor.inject<SimpleService>()

    /** Unscoped — new instance every call (factory). */
    @Benchmark
    fun unscopedResolve(): Any = Anchor.inject<ComplexService>()
}
