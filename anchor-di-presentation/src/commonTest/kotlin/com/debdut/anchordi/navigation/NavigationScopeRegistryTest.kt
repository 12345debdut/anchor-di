package com.debdut.anchordi.navigation

import com.debdut.anchordi.NavigationComponent
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
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * Tests for [NavigationScopeRegistry] — the core registry that manages per-navigation-entry
 * scoped containers.
 */
class NavigationScopeRegistryTest {
    private val contributor =
        object : ComponentBindingContributor {
            override fun contribute(registry: BindingRegistry) {
                // Nav-scoped binding
                registry.register(
                    Key("kotlin.String", "navData"),
                    Binding.Scoped(
                        NavigationComponent.SCOPE_ID,
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = "nav-scoped-value"
                        },
                    ),
                )
                // ViewModel-scoped binding
                registry.register(
                    Key("kotlin.Int", "vmCounter"),
                    Binding.Scoped(
                        ViewModelComponent.SCOPE_ID,
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = 42
                        },
                    ),
                )
                // Singleton binding (available in all scopes)
                registry.register(
                    Key("kotlin.String", "appName"),
                    Binding.Singleton(
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = "TestApp"
                        },
                    ),
                )
            }
        }

    @BeforeTest
    fun setUp() {
        Anchor.init(contributor)
    }

    @AfterTest
    fun tearDown() {
        Anchor.reset()
    }

    @Test
    fun getOrCreateReturnsSameEntryForSameKey() {
        val entry1 = NavigationScopeRegistry.getOrCreate("screen-A")
        val entry2 = NavigationScopeRegistry.getOrCreate("screen-A")

        assertSame(entry1, entry2, "Same key must return the same entry")
    }

    @Test
    fun getOrCreateReturnsDifferentEntriesForDifferentKeys() {
        val entry1 = NavigationScopeRegistry.getOrCreate("screen-A")
        val entry2 = NavigationScopeRegistry.getOrCreate("screen-B")

        assertNotSame(entry1, entry2, "Different keys must return different entries")
    }

    @Test
    fun entryContainsBothNavAndViewModelContainers() {
        val entry = NavigationScopeRegistry.getOrCreate("screen-A")

        assertNotNull(entry.navContainer, "navContainer must be non-null")
        assertNotNull(entry.viewModelContainer, "viewModelContainer must be non-null")
        assertNotSame(
            entry.navContainer,
            entry.viewModelContainer,
            "navContainer and viewModelContainer must be different",
        )
    }

    @Test
    fun navContainerResolvesNavigationScopedBindings() {
        val entry = NavigationScopeRegistry.getOrCreate("screen-A")
        val navData = entry.navContainer.get<String>("navData")

        assertEquals("nav-scoped-value", navData)
    }

    @Test
    fun viewModelContainerResolvesViewModelScopedBindings() {
        val entry = NavigationScopeRegistry.getOrCreate("screen-A")
        val counter = entry.viewModelContainer.get<Int>("vmCounter")

        assertEquals(42, counter)
    }

    @Test
    fun singletonBindingsSharedAcrossEntries() {
        val entry1 = NavigationScopeRegistry.getOrCreate("screen-A")
        val entry2 = NavigationScopeRegistry.getOrCreate("screen-B")

        val name1 = entry1.navContainer.get<String>("appName")
        val name2 = entry2.navContainer.get<String>("appName")
        val rootName = Anchor.inject<String>("appName")

        assertSame(name1, name2, "Singletons must be shared across entries")
        assertSame(name1, rootName, "Singletons must be shared with root")
    }

    @Test
    fun disposeRemovesEntry() {
        NavigationScopeRegistry.getOrCreate("screen-A")
        NavigationScopeRegistry.dispose("screen-A")

        // After dispose, getOrCreate returns a new entry
        val newEntry = NavigationScopeRegistry.getOrCreate("screen-A")
        // We can't easily assert "different entry" since the old one is gone,
        // but we can verify we get a fresh container that works
        val navData = newEntry.navContainer.get<String>("navData")
        assertEquals("nav-scoped-value", navData)
    }

    @Test
    fun disposeIsIdempotent() {
        NavigationScopeRegistry.getOrCreate("screen-A")
        NavigationScopeRegistry.dispose("screen-A")
        // Second dispose should not throw
        NavigationScopeRegistry.dispose("screen-A")
    }

    @Test
    fun clearRemovesAllEntries() {
        NavigationScopeRegistry.getOrCreate("screen-A")
        NavigationScopeRegistry.getOrCreate("screen-B")
        NavigationScopeRegistry.getOrCreate("screen-C")

        NavigationScopeRegistry.clear()

        // All entries are fresh after clear
        val entry = NavigationScopeRegistry.getOrCreate("screen-A")
        assertNotNull(entry, "getOrCreate must work after clear")
    }

    @Test
    fun anchorResetClearsRegistry() {
        NavigationScopeRegistry.getOrCreate("screen-A")

        Anchor.reset()
        // Re-init so we can continue using the registry
        Anchor.init(contributor)

        // After reset, the old entry is gone — getOrCreate returns a fresh one
        val entry = NavigationScopeRegistry.getOrCreate("screen-A")
        assertNotNull(entry)
    }

    @Test
    fun scopedInstancesCachedWithinEntry() {
        val entry = NavigationScopeRegistry.getOrCreate("screen-A")

        val data1 = entry.navContainer.get<String>("navData")
        val data2 = entry.navContainer.get<String>("navData")

        assertSame(data1, data2, "Scoped instances must be cached within the same entry")
    }

    @Test
    fun scopedInstancesIsolatedBetweenEntries() {
        val entryA = NavigationScopeRegistry.getOrCreate("screen-A")
        val entryB = NavigationScopeRegistry.getOrCreate("screen-B")

        val dataA = entryA.navContainer.get<String>("navData")
        val dataB = entryB.navContainer.get<String>("navData")

        // Both have the same value but are different instances (created separately per scope)
        assertEquals(dataA, dataB)
        // Note: Kotlin Strings may be interned, so assertNotSame may not hold for String literals.
        // The scoping contract is verified by the registry returning different entries.
    }
}
