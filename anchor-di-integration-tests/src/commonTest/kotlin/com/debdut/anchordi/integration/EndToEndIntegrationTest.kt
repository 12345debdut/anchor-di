package com.debdut.anchordi.integration

import com.debdut.anchordi.ViewModelComponent
import com.debdut.anchordi.runtime.Anchor
import com.debdut.anchordi.runtime.AnchorContainer
import com.debdut.anchordi.runtime.AnchorProvider
import com.debdut.anchordi.runtime.Binding
import com.debdut.anchordi.runtime.BindingRegistry
import com.debdut.anchordi.runtime.ComponentBindingContributor
import com.debdut.anchordi.runtime.Factory
import com.debdut.anchordi.runtime.Key
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * End-to-end integration tests that simulate the full KSP-generated pipeline:
 * Annotations -> KSP generates ComponentBindingContributor -> Anchor.init() -> resolve dependencies.
 *
 * These tests use hand-written contributors (same structure as KSP-generated code) to verify
 * the entire runtime path from initialization through dependency resolution.
 */
class EndToEndIntegrationTest {
    @AfterTest
    fun tearDown() {
        Anchor.reset()
    }

    // --- Domain types (simulating user's annotated classes) ---

    interface Repository

    class RepositoryImpl : Repository

    interface ApiService

    class ApiServiceImpl(val baseUrl: String) : ApiService

    class UserViewModel(val repository: Repository, val api: ApiService)

    class AnalyticsTracker(val name: String)

    // --- Test: Full Singleton pipeline ---

    @Test
    fun singletonBindingReturnsSameInstance() {
        val contributor =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    // Simulates: @Singleton @Inject constructor() -> RepositoryImpl
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.RepositoryImpl"),
                        Binding.Singleton(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = RepositoryImpl()
                            },
                        ),
                    )
                    // Simulates: @Binds @Singleton -> Repository = RepositoryImpl
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.Repository"),
                        Binding.Singleton(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any =
                                    container.get(
                                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.RepositoryImpl"),
                                    )
                            },
                        ),
                    )
                }
            }

        Anchor.init(contributor)

        val repo1 = Anchor.inject<Repository>()
        val repo2 = Anchor.inject<Repository>()

        assertSame(repo1, repo2, "Singleton must return the same instance")
        assertIs<RepositoryImpl>(repo1)
    }

    // --- Test: Unscoped returns new instance each time ---

    @Test
    fun unscopedBindingReturnsNewInstanceEachTime() {
        val contributor =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.RepositoryImpl"),
                        Binding.Unscoped(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = RepositoryImpl()
                            },
                        ),
                    )
                }
            }

        Anchor.init(contributor)

        val repo1 = Anchor.inject<RepositoryImpl>()
        val repo2 = Anchor.inject<RepositoryImpl>()

        assertNotSame(repo1, repo2, "Unscoped must return different instances")
    }

    // --- Test: ViewModel-scoped binding with transitive dependencies ---

    @Test
    fun viewModelScopedBindingWithTransitiveDependencies() {
        val contributor =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    // Singleton: Repository
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.Repository"),
                        Binding.Singleton(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = RepositoryImpl()
                            },
                        ),
                    )
                    // Singleton: ApiService with @Named("baseUrl") -> String
                    registry.register(
                        Key("kotlin.String", "baseUrl"),
                        Binding.Singleton(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = "https://api.example.com"
                            },
                        ),
                    )
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.ApiService"),
                        Binding.Singleton(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any =
                                    ApiServiceImpl(
                                        container.get(Key("kotlin.String", "baseUrl")) as String,
                                    )
                            },
                        ),
                    )
                    // ViewModelScoped: UserViewModel depends on Repository + ApiService
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.UserViewModel"),
                        Binding.Scoped(
                            ViewModelComponent.SCOPE_ID,
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any =
                                    UserViewModel(
                                        container.get(
                                            Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.Repository"),
                                        ) as Repository,
                                        container.get(
                                            Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.ApiService"),
                                        ) as ApiService,
                                    )
                            },
                        ),
                    )
                }
            }

        Anchor.init(contributor)

        // ViewModel-scoped resolution via withScope
        Anchor.withScope(ViewModelComponent.SCOPE_ID) { scopedContainer ->
            val vm1 = scopedContainer.get<UserViewModel>()
            val vm2 = scopedContainer.get<UserViewModel>()

            // Same instance within scope (scoped caching)
            assertSame(vm1, vm2, "Scoped binding must cache within the same scope")

            // Transitive dependencies resolved correctly
            assertIs<RepositoryImpl>(vm1.repository)
            assertIs<ApiServiceImpl>(vm1.api)
            val apiService = assertIs<ApiServiceImpl>(vm1.api)
            assertEquals("https://api.example.com", apiService.baseUrl)

            // Singleton is shared: repo from scope == repo from root
            val rootRepo = Anchor.inject<Repository>()
            assertSame(vm1.repository, rootRepo, "Singletons must be shared across scopes")
        }
    }

    // --- Test: Different scopes get different scoped instances ---

    @Test
    fun differentScopesGetDifferentScopedInstances() {
        val contributor =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.RepositoryImpl"),
                        Binding.Scoped(
                            ViewModelComponent.SCOPE_ID,
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = RepositoryImpl()
                            },
                        ),
                    )
                }
            }

        Anchor.init(contributor)

        var scope1Instance: RepositoryImpl? = null
        var scope2Instance: RepositoryImpl? = null

        Anchor.withScope(ViewModelComponent.SCOPE_ID) { container ->
            scope1Instance = container.get<RepositoryImpl>()
        }

        Anchor.withScope(ViewModelComponent.SCOPE_ID) { container ->
            scope2Instance = container.get<RepositoryImpl>()
        }

        assertNotSame(
            scope1Instance,
            scope2Instance,
            "Different scope invocations must create different scoped instances",
        )
    }

    // --- Test: Scoped binding fails without scope ---

    @Test
    fun scopedBindingFailsWithoutScope() {
        val contributor =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.UserViewModel"),
                        Binding.Scoped(
                            ViewModelComponent.SCOPE_ID,
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any =
                                    UserViewModel(
                                        RepositoryImpl(),
                                        ApiServiceImpl(""),
                                    )
                            },
                        ),
                    )
                }
            }

        Anchor.init(contributor)

        val ex =
            assertFailsWith<IllegalStateException> {
                Anchor.inject<UserViewModel>()
            }
        assertTrue(
            ex.message!!.contains("Scoped binding"),
            "Error must mention 'Scoped binding', got: ${ex.message}",
        )
        assertTrue(
            ex.message!!.contains("viewModelAnchor()"),
            "Error must suggest viewModelAnchor(), got: ${ex.message}",
        )
    }

    // --- Test: Qualified binding disambiguation ---

    @Test
    fun qualifiedBindingsDisambiguateCorrectly() {
        val contributor =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    registry.register(
                        Key("kotlin.String", "baseUrl"),
                        Binding.Singleton(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = "https://prod.api.com"
                            },
                        ),
                    )
                    registry.register(
                        Key("kotlin.String", "debugUrl"),
                        Binding.Singleton(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = "https://debug.api.com"
                            },
                        ),
                    )
                }
            }

        Anchor.init(contributor)

        val prodUrl = Anchor.inject<String>("baseUrl")
        val debugUrl = Anchor.inject<String>("debugUrl")

        assertEquals("https://prod.api.com", prodUrl)
        assertEquals("https://debug.api.com", debugUrl)
    }

    // --- Test: Provider (Lazy resolution) ---

    @Test
    fun providerDefersResolutionUntilGet() {
        var factoryCallCount = 0

        val contributor =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.RepositoryImpl"),
                        Binding.Unscoped(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any {
                                    factoryCallCount++
                                    return RepositoryImpl()
                                }
                            },
                        ),
                    )
                }
            }

        Anchor.init(contributor)

        val provider: AnchorProvider<RepositoryImpl> = Anchor.provider<RepositoryImpl>()
        assertEquals(0, factoryCallCount, "Factory must not be called before provider.get()")

        provider.get()
        assertEquals(1, factoryCallCount)

        provider.get()
        assertEquals(2, factoryCallCount, "Unscoped provider creates new instances per get()")
    }

    // --- Test: Multibinding Set ---

    @Test
    fun multibindingSetMergesContributions() {
        val contributor =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    val key = Key("kotlin.collections.Set<com.debdut.anchordi.integration.EndToEndIntegrationTest.AnalyticsTracker>")
                    registry.registerSetContribution(
                        key,
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = AnalyticsTracker("firebase")
                        },
                    )
                    registry.registerSetContribution(
                        key,
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = AnalyticsTracker("mixpanel")
                        },
                    )
                    registry.registerSetContribution(
                        key,
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = AnalyticsTracker("amplitude")
                        },
                    )
                }
            }

        Anchor.init(contributor)

        val trackers = Anchor.injectSet<AnalyticsTracker>()
        assertEquals(3, trackers.size)

        val names = trackers.map { it.name }.toSet()
        assertTrue("firebase" in names)
        assertTrue("mixpanel" in names)
        assertTrue("amplitude" in names)
    }

    // --- Test: Multibinding Map ---

    @Test
    fun multibindingMapMergesContributions() {
        val contributor =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    val mapType =
                        "kotlin.collections.Map<kotlin.String," +
                            "com.debdut.anchordi.integration.EndToEndIntegrationTest.AnalyticsTracker>"
                    val key = Key(mapType)
                    registry.registerMapContribution(
                        key,
                        "firebase",
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = AnalyticsTracker("firebase")
                        },
                    )
                    registry.registerMapContribution(
                        key,
                        "mixpanel",
                        object : Factory<Any> {
                            override fun create(container: AnchorContainer): Any = AnalyticsTracker("mixpanel")
                        },
                    )
                }
            }

        Anchor.init(contributor)

        val trackerMap = Anchor.injectMap<AnalyticsTracker>()
        assertEquals(2, trackerMap.size)
        assertEquals("firebase", trackerMap["firebase"]?.name)
        assertEquals("mixpanel", trackerMap["mixpanel"]?.name)
    }

    // --- Test: Multi-module contributors merge correctly ---

    @Test
    fun multipleContributorsMergeBindings() {
        // Simulates two Gradle modules each generating their own contributor
        val module1 =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.Repository"),
                        Binding.Singleton(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = RepositoryImpl()
                            },
                        ),
                    )
                }
            }

        val module2 =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.ApiService"),
                        Binding.Singleton(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = ApiServiceImpl("https://api.com")
                            },
                        ),
                    )
                }
            }

        // Init with multiple contributors (multi-module)
        Anchor.init(module1, module2)

        val repo = Anchor.inject<Repository>()
        val api = Anchor.inject<ApiService>()

        assertIs<RepositoryImpl>(repo)
        assertIs<ApiServiceImpl>(api)
    }

    // --- Test: Missing binding produces helpful error ---

    @Test
    fun missingBindingProducesHelpfulError() {
        val contributor =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    // Register nothing
                }
            }

        Anchor.init(contributor)

        val ex =
            assertFailsWith<IllegalStateException> {
                Anchor.inject<Repository>()
            }
        assertTrue(
            ex.message!!.contains("No binding found"),
            "Error must contain 'No binding found', got: ${ex.message}",
        )
        assertTrue(
            ex.message!!.contains("@Inject"),
            "Error must suggest @Inject, got: ${ex.message}",
        )
    }

    // --- Test: Reset enables re-initialization ---

    @Test
    fun resetEnablesReinitialization() {
        val contributor1 =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    registry.register(
                        Key("kotlin.String", "env"),
                        Binding.Singleton(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = "production"
                            },
                        ),
                    )
                }
            }

        val contributor2 =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    registry.register(
                        Key("kotlin.String", "env"),
                        Binding.Singleton(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = "test"
                            },
                        ),
                    )
                }
            }

        Anchor.init(contributor1)
        assertEquals("production", Anchor.inject<String>("env"))

        Anchor.reset()

        Anchor.init(contributor2)
        assertEquals("test", Anchor.inject<String>("env"))
    }

    // --- Test: Nested scopes (component hierarchy) ---

    @Test
    fun nestedScopeResolution() {
        val sessionScopeId = "com.example.SessionScope"
        val screenScopeId = "com.example.ScreenScope"

        val contributor =
            object : ComponentBindingContributor {
                override fun contribute(registry: BindingRegistry) {
                    // Singleton
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.Repository"),
                        Binding.Singleton(
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = RepositoryImpl()
                            },
                        ),
                    )
                    // Session-scoped
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.ApiService"),
                        Binding.Scoped(
                            sessionScopeId,
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any = ApiServiceImpl("session-url")
                            },
                        ),
                    )
                    // Screen-scoped depends on session-scoped + singleton
                    registry.register(
                        Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.UserViewModel"),
                        Binding.Scoped(
                            screenScopeId,
                            object : Factory<Any> {
                                override fun create(container: AnchorContainer): Any =
                                    UserViewModel(
                                        container.get(
                                            Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.Repository"),
                                        ) as Repository,
                                        container.get(
                                            Key("com.debdut.anchordi.integration.EndToEndIntegrationTest.ApiService"),
                                        ) as ApiService,
                                    )
                            },
                        ),
                    )
                }
            }

        Anchor.init(contributor)

        Anchor.withScope(sessionScopeId) { sessionContainer ->
            sessionContainer.createScope(screenScopeId) { screenContainer ->
                val vm = screenContainer.get<UserViewModel>()

                // Screen-scoped VM resolved successfully
                assertIs<UserViewModel>(vm)

                // Its dependencies come from the right scopes
                assertIs<RepositoryImpl>(vm.repository)
                val vmApi = assertIs<ApiServiceImpl>(vm.api)
                assertEquals("session-url", vmApi.baseUrl)

                // Singleton is shared with root
                assertSame(Anchor.inject<Repository>(), vm.repository)
            }
        }
    }
}
