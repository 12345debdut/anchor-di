package com.debdut.anchordi.runtime

import com.debdut.anchordi.ViewModelComponent
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Unit tests for [Anchor]: init, reset, inject, withScope.
 */
class AnchorTest {

    @AfterTest
    fun tearDown() {
        Anchor.reset()
    }

    @Test
    fun init_beforeAnyInject_succeeds() {
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(AnchorTestFoo::class.qualifiedName!!),
                    Binding.Unscoped(object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = AnchorTestFoo()
                    })
                )
            }
        }
        Anchor.init(contributor)
        val foo = Anchor.inject<AnchorTestFoo>()
        assertTrue(foo is AnchorTestFoo)
    }

    @Test
    fun init_twice_throws() {
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {}
        }
        Anchor.init(contributor)
        val ex = assertFailsWith<IllegalArgumentException> {
            Anchor.init(contributor)
        }
        assertTrue(ex.message!!.contains("already initialized"))
    }

    @Test
    fun inject_beforeInit_throws() {
        val ex = assertFailsWith<IllegalStateException> {
            Anchor.inject<AnchorTestFoo>()
        }
        assertTrue(ex.message!!.contains("not initialized"))
    }

    @Test
    fun reset_allowsReinit() {
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(AnchorTestFoo::class.qualifiedName!!),
                    Binding.Unscoped(object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = AnchorTestFoo()
                    })
                )
            }
        }
        Anchor.init(contributor)
        Anchor.inject<AnchorTestFoo>()
        Anchor.reset()
        Anchor.init(contributor)
        val foo = Anchor.inject<AnchorTestFoo>()
        assertTrue(foo is AnchorTestFoo)
    }

    @Test
    fun withScope_resolvesScopedBinding() {
        val viewModelScopeId = "com.debdut.anchordi.ViewModelComponent"
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(AnchorTestScopedService::class.qualifiedName!!),
                    Binding.Scoped(
                        viewModelScopeId,
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = AnchorTestScopedService()
                        }
                    )
                )
            }
        }
        Anchor.init(contributor)
        var resolved: AnchorTestScopedService? = null
        Anchor.withScope(ViewModelComponent::class) { scoped ->
            resolved = scoped.get<AnchorTestScopedService>()
        }
        assertTrue(resolved is AnchorTestScopedService)
    }

    @Test
    fun withScope_sameScope_returnsSameScopedInstance() {
        val viewModelScopeId = "com.debdut.anchordi.ViewModelComponent"
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(AnchorTestScopedService::class.qualifiedName!!),
                    Binding.Scoped(
                        viewModelScopeId,
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = AnchorTestScopedService()
                        }
                    )
                )
            }
        }
        Anchor.init(contributor)
        var first: AnchorTestScopedService? = null
        var second: AnchorTestScopedService? = null
        Anchor.withScope(ViewModelComponent::class) { scoped ->
            first = scoped.get<AnchorTestScopedService>()
            second = scoped.get<AnchorTestScopedService>()
        }
        assertSame(first, second)
    }

    @Test
    fun inject_withQualifier_usesQualifiedKey() {
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(AnchorTestFoo::class.qualifiedName!!, "named"),
                    Binding.Unscoped(object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = AnchorTestFoo()
                    })
                )
            }
        }
        Anchor.init(contributor)
        val foo = Anchor.inject<AnchorTestFoo>("named")
        assertTrue(foo is AnchorTestFoo)
    }

    @Test
    fun provider_returnsNewInstanceEachCall_forUnscoped() {
        val contributor = object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key(AnchorTestFoo::class.qualifiedName!!),
                    Binding.Unscoped(object : Factory<Any> {
                        override fun create(container: AnchorContainer): Any = AnchorTestFoo()
                    })
                )
            }
        }
        Anchor.init(contributor)
        val provider = Anchor.provider<AnchorTestFoo>()
        val a = provider.get()
        val b = provider.get()
        assertTrue(a is AnchorTestFoo)
        assertTrue(b is AnchorTestFoo)
        assertNotSame(a, b)
    }
}

// Test types for Anchor (must be top-level or in same file for reified get)
private class AnchorTestFoo
private class AnchorTestScopedService
