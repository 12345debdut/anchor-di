package com.debdut.anchordi.runtime

import com.debdut.anchordi.ViewModelComponent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Unit tests for [AnchorContainer]: get (Unscoped, Singleton, Scoped),
 * createScope, resolveContainer behavior, missing binding.
 */
class AnchorContainerTest {

    private val viewModelScopeId = "com.debdut.anchordi.ViewModelComponent"

    private fun containerWithUnscopedOnly(): AnchorContainer {
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(TestFoo::class.qualifiedName!!),
                    Binding.Unscoped(object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = TestFoo()
                    })
                )
            }
        }
        return AnchorContainer(listOf(contributor))
    }

    private fun containerWithSingleton(): AnchorContainer {
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(TestFoo::class.qualifiedName!!),
                    Binding.Singleton(object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = TestFoo()
                    })
                )
            }
        }
        return AnchorContainer(listOf(contributor))
    }

    private fun containerWithScopedBinding(): AnchorContainer {
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(TestScopedService::class.qualifiedName!!),
                    Binding.Scoped(
                        viewModelScopeId,
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = TestScopedService()
                        }
                    )
                )
            }
        }
        return AnchorContainer(listOf(contributor))
    }

    private fun containerWithUnscopedDependingOnScoped(): AnchorContainer {
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(TestScopedService::class.qualifiedName!!),
                    Binding.Scoped(
                        viewModelScopeId,
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = TestScopedService()
                        }
                    )
                )
                registry.register(
                    Key(TestConsumer::class.qualifiedName!!),
                    Binding.Unscoped(object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any {
                            val service = container.get<TestScopedService>()
                            return TestConsumer(service)
                        }
                    })
                )
            }
        }
        return AnchorContainer(listOf(contributor))
    }

    @Test
    fun unscoped_returnsNewInstanceEachTime() {
        val container = containerWithUnscopedOnly()
        val a = container.get<TestFoo>()
        val b = container.get<TestFoo>()
        assertTrue(a is TestFoo)
        assertTrue(b is TestFoo)
        assertNotSame(a, b)
    }

    @Test
    fun singleton_returnsSameInstance() {
        val container = containerWithSingleton()
        val a = container.get<TestFoo>()
        val b = container.get<TestFoo>()
        assertSame(a, b)
    }

    @Test
    fun missingBinding_throws() {
        val container = containerWithUnscopedOnly()
        val ex = assertFailsWith<IllegalStateException> {
            container.get<TestBar>()
        }
        assertTrue(ex.message!!.contains("No binding found"))
        assertTrue(ex.message!!.contains("TestBar"))
    }

    @Test
    fun scopedBinding_resolvedOutsideScope_throws() {
        val container = containerWithScopedBinding()
        val ex = assertFailsWith<IllegalStateException> {
            container.get<TestScopedService>()
        }
        assertTrue(ex.message!!.contains("requires a scope"))
        assertTrue(ex.message!!.contains("ViewModelComponent"), "Message should identify ViewModel scope: ${ex.message}")
    }

    @Test
    fun scopedBinding_resolvedInsideScope_succeeds() {
        val root = containerWithScopedBinding()
        var resolved: TestScopedService? = null
        root.createScope(ViewModelComponent::class) { scoped ->
            resolved = scoped.get<TestScopedService>()
        }
        assertTrue(resolved is TestScopedService)
    }

    @Test
    fun scopedBinding_sameScope_returnsSameInstance() {
        val root = containerWithScopedBinding()
        var first: TestScopedService? = null
        var second: TestScopedService? = null
        root.createScope(ViewModelComponent::class) { scoped ->
            first = scoped.get<TestScopedService>()
            second = scoped.get<TestScopedService>()
        }
        assertSame(first, second)
    }

    @Test
    fun scopedBinding_differentScope_returnsDifferentInstances() {
        val root = containerWithScopedBinding()
        var fromScope1: TestScopedService? = null
        var fromScope2: TestScopedService? = null
        root.createScope(ViewModelComponent::class) { fromScope1 = it.get<TestScopedService>() }
        root.createScope(ViewModelComponent::class) { fromScope2 = it.get<TestScopedService>() }
        assertNotSame(fromScope1, fromScope2)
    }

    @Test
    fun unscopedDependingOnScoped_insideScope_succeeds() {
        val root = containerWithUnscopedDependingOnScoped()
        var consumer: TestConsumer? = null
        root.createScope(ViewModelComponent::class) { scoped ->
            consumer = scoped.get<TestConsumer>()
        }
        assertTrue(consumer is TestConsumer)
        assertTrue(consumer!!.service is TestScopedService)
    }

    @Test
    fun unscopedDependingOnScoped_outsideScope_throws() {
        val root = containerWithUnscopedDependingOnScoped()
        assertFailsWith<IllegalStateException> {
            root.get<TestConsumer>()
        }
    }

    @Test
    fun createScope_inheritsBindings() {
        val root = containerWithUnscopedOnly()
        root.createScope(ViewModelComponent::class) { scoped ->
            val foo = scoped.get<TestFoo>()
            assertTrue(foo is TestFoo)
        }
    }

    @Test
    fun get_withQualifier_usesQualifiedKey() {
        val key = Key("some.Type", "qualifier")
        assertEquals("some.Type", key.typeName)
        assertEquals("qualifier", key.qualifier)
    }

    @Test
    fun get_withExplicitKey_returnsInstance() {
        val container = containerWithUnscopedOnly()
        val key = Key(TestFoo::class.qualifiedName!!, null)
        val a = container.get(key)
        val b = container.get<TestFoo>()
        assertTrue(a is TestFoo)
        assertTrue(b is TestFoo)
        assertNotSame(a, b)
    }

    @Test
    fun get_withQualifiedKey_returnsQualifiedBinding() {
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(TestFoo::class.qualifiedName!!, "main"),
                    Binding.Unscoped(object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = TestFoo()
                    })
                )
            }
        }
        val container = AnchorContainer(listOf(contributor))
        val key = Key(TestFoo::class.qualifiedName!!, "main")
        val foo = container.get(key)
        assertTrue(foo is TestFoo)
    }

    @Test
    fun singleton_resolvedFromChildScope_returnsSameInstanceAsRoot() {
        val container = containerWithSingleton()
        val fromRoot = container.get<TestFoo>()
        var fromScope: TestFoo? = null
        container.createScope(ViewModelComponent::class) { scoped ->
            fromScope = scoped.get<TestFoo>()
        }
        assertSame(fromRoot, fromScope)
    }

    @Test
    fun createScope_withScopeIdString_resolvesScopedBinding() {
        val container = containerWithScopedBinding()
        var resolved: TestScopedService? = null
        container.createScope(viewModelScopeId) { scoped ->
            resolved = scoped.get<TestScopedService>()
        }
        assertTrue(resolved is TestScopedService)
    }

    @Test
    fun nestedCreateScope_childResolvesParentScopedBinding() {
        val scopeAId = "com.example.ScopeA"
        val scopeBId = "com.example.ScopeB"
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(TestScopedA::class.qualifiedName!!),
                    Binding.Scoped(scopeAId, object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = TestScopedA()
                    })
                )
                registry.register(
                    Key(TestScopedB::class.qualifiedName!!),
                    Binding.Scoped(scopeBId, object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = TestScopedB()
                    })
                )
            }
        }
        val root = AnchorContainer(listOf(contributor))
        var fromA: TestScopedA? = null
        var fromB_A: TestScopedA? = null
        var fromB_B: TestScopedB? = null
        root.createScope(scopeAId) { scopeA ->
            fromA = scopeA.get<TestScopedA>()
            scopeA.createScope(scopeBId) { scopeB ->
                fromB_A = scopeB.get<TestScopedA>()
                fromB_B = scopeB.get<TestScopedB>()
            }
        }
        assertSame(fromA, fromB_A)
        assertTrue(fromB_B is TestScopedB)
    }

    @Test
    fun nestedCreateScope_sameScopeTypeDifferentInstances_returnsDifferentScopedInstances() {
        val scopeAId = "com.example.ScopeA"
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(TestScopedA::class.qualifiedName!!),
                    Binding.Scoped(scopeAId, object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = TestScopedA()
                    })
                )
            }
        }
        val root = AnchorContainer(listOf(contributor))
        var fromScope1: TestScopedA? = null
        var fromScope2: TestScopedA? = null
        root.createScope(scopeAId) { fromScope1 = it.get<TestScopedA>() }
        root.createScope(scopeAId) { fromScope2 = it.get<TestScopedA>() }
        assertNotSame(fromScope1, fromScope2)
    }

    @Test
    fun missingBinding_messageContainsTroubleshootingHints() {
        val container = containerWithUnscopedOnly()
        val ex = assertFailsWith<IllegalStateException> {
            container.get<TestBar>()
        }
        val msg = ex.message!!
        assertTrue(msg.contains("No binding found"))
        assertTrue(msg.contains("Add @Inject") || msg.contains("Add @Provides") || msg.contains("@Binds"))
        assertTrue(msg.contains("Rebuild") || msg.contains("KSP"))
    }

    @Test
    fun unscoped_factoryReceivesResolvingContainer() {
        val root = containerWithUnscopedDependingOnScoped()
        root.createScope(ViewModelComponent::class) { scoped ->
            val consumer = scoped.get<TestConsumer>()
            assertTrue(consumer is TestConsumer)
            assertTrue(consumer.service is TestScopedService)
        }
    }

    @Test
    fun provider_returnsSupplierThatResolvesFromContainer() {
        val container = containerWithUnscopedOnly()
        val provider = container.provider<TestFoo>()
        val a = provider.get()
        val b = provider.get()
        assertTrue(a is TestFoo)
        assertTrue(b is TestFoo)
        assertNotSame(a, b)
    }

    @Test
    fun provider_forSingleton_returnsSameInstance() {
        val container = containerWithSingleton()
        val provider = container.provider<TestFoo>()
        assertSame(provider.get(), provider.get())
    }

    @Test
    fun createScopeContainer_holdsScopeAndCachesScopedBindings() {
        val container = containerWithScopedBinding()
        val scopedContainer = container.createScopeContainer(viewModelScopeId)
        val a = scopedContainer.get<TestScopedService>()
        val b = scopedContainer.get<TestScopedService>()
        assertSame(a, b)
        val otherScoped = container.createScopeContainer(viewModelScopeId)
        val c = otherScoped.get<TestScopedService>()
        assertNotSame(a, c)
    }

    @Test
    fun multibindingSet_mergesContributions() {
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.registerSetContribution(
                    Key("kotlin.collections.Set<${TestTracker::class.qualifiedName!!}>", null),
                    object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = TestTracker("a")
                    }
                )
                registry.registerSetContribution(
                    Key("kotlin.collections.Set<${TestTracker::class.qualifiedName!!}>", null),
                    object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = TestTracker("b")
                    }
                )
            }
        }
        val container = AnchorContainer(listOf(contributor))
        val set = container.getSet<TestTracker>()
        assertEquals(2, set.size)
        assertTrue(set.any { it.id == "a" })
        assertTrue(set.any { it.id == "b" })
        assertSame(set, container.getSet<TestTracker>())
    }

    @Test
    fun multibindingMap_mergesContributions() {
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.registerMapContribution(
                    Key("kotlin.collections.Map<kotlin.String,${TestTracker::class.qualifiedName!!}>", null),
                    "firebase",
                    object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = TestTracker("firebase")
                    }
                )
                registry.registerMapContribution(
                    Key("kotlin.collections.Map<kotlin.String,${TestTracker::class.qualifiedName!!}>", null),
                    "analytics",
                    object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = TestTracker("analytics")
                    }
                )
            }
        }
        val container = AnchorContainer(listOf(contributor))
        val map = container.getMap<TestTracker>()
        assertEquals(2, map.size)
        assertEquals("firebase", map["firebase"]!!.id)
        assertEquals("analytics", map["analytics"]!!.id)
        assertSame(map, container.getMap<TestTracker>())
    }
}

// Test types (qualified names used in Key)
private class TestFoo
private class TestBar
private class TestScopedService
private class TestConsumer(val service: TestScopedService)
private class TestScopedA
private class TestScopedB
private class TestTracker(val id: String)
