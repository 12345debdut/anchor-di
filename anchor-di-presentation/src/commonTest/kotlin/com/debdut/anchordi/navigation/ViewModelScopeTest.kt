package com.debdut.anchordi.navigation

import com.debdut.anchordi.ViewModelComponent
import com.debdut.anchordi.runtime.Anchor
import com.debdut.anchordi.runtime.AnchorContainer
import com.debdut.anchordi.runtime.Binding
import com.debdut.anchordi.runtime.BindingRegistry
import com.debdut.anchordi.runtime.ComponentBindingContributor
import com.debdut.anchordi.runtime.Factory
import com.debdut.anchordi.runtime.Key
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Tests for [ViewModelScope] and [ViewModelScopeRegistry] — the cross-platform ViewModel
 * scoping API that works on all platforms (Android, iOS, JVM, JS).
 */
class ViewModelScopeTest {
    private var createCount = 0

    private val contributor =
        object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                registry.register(
                    Key("kotlin.Int", "counter"),
                    Binding.Scoped(
                        ViewModelComponent.SCOPE_ID,
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = ++createCount
                        },
                    ),
                )
            }
        }

    @BeforeTest
    fun setUp() {
        createCount = 0
        Anchor.init(contributor)
    }

    @AfterTest
    fun tearDown() {
        Anchor.reset()
    }

    // --- ViewModelScopeRegistry tests ---

    @Test
    fun registryGetOrCreateReturnsSameContainerForSameKey() {
        val container1 = ViewModelScopeRegistry.getOrCreate("vm-1")
        val container2 = ViewModelScopeRegistry.getOrCreate("vm-1")

        assertSame(container1, container2)
    }

    @Test
    fun registryDisposeReleasesScope() {
        val container1 = ViewModelScopeRegistry.getOrCreate("vm-1")
        val val1 = container1.get<Int>("counter")

        ViewModelScopeRegistry.dispose("vm-1")

        val container2 = ViewModelScopeRegistry.getOrCreate("vm-1")
        val val2 = container2.get<Int>("counter")

        // After dispose + re-create, factory runs again = different value
        assertEquals(1, val1)
        assertEquals(2, val2)
    }

    // --- ViewModelScope.holder() tests ---

    @Test
    fun holderReturnsWorkingContainer() {
        val holder = ViewModelScope.holder("screen-1")
        val counter = holder.get<Int>("counter")

        assertEquals(1, counter)
        holder.close()
    }

    @Test
    fun holderCachesWithinScope() {
        val holder = ViewModelScope.holder("screen-1")
        val val1 = holder.get<Int>("counter")
        val val2 = holder.get<Int>("counter")

        assertSame(val1, val2, "Holder must cache scoped instances")
        holder.close()
    }

    @Test
    fun holderCloseDisposesScope() {
        val holder = ViewModelScope.holder("screen-1")
        holder.get<Int>("counter")
        assertEquals(1, createCount)

        holder.close()

        // After close, creating a new holder creates fresh instances
        val holder2 = ViewModelScope.holder("screen-1")
        holder2.get<Int>("counter")
        assertEquals(2, createCount, "Factory must be called again after holder is closed")
        holder2.close()
    }

    // --- ViewModelScope.use() tests ---

    @Test
    fun useBlockResolvesAndAutoDisposes() {
        val result =
            ViewModelScope.use("screen-1") { container ->
                container.get<Int>("counter")
            }

        assertEquals(1, result)

        // After use{} exits, scope is disposed — new use{} creates fresh instance
        val result2 =
            ViewModelScope.use("screen-1") { container ->
                container.get<Int>("counter")
            }

        assertEquals(2, result2, "use{} must auto-dispose on exit")
    }

    @Test
    fun useAutoDisposesEvenOnException() {
        try {
            ViewModelScope.use("screen-1") { container ->
                container.get<Int>("counter")
                error("Simulated exception")
            }
        } catch (_: IllegalStateException) {
            // Expected
        }

        // Scope should be disposed despite the exception
        val result =
            ViewModelScope.use("screen-1") { container ->
                container.get<Int>("counter")
            }

        assertEquals(2, result, "use{} must auto-dispose even on exception")
    }
}
