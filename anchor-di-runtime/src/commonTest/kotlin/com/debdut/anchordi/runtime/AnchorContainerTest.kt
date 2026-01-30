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
        assertTrue(ex.message!!.contains(viewModelScopeId))
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
}

// Test types (qualified names used in Key)
private class TestFoo
private class TestBar
private class TestScopedService
private class TestConsumer(val service: TestScopedService)
