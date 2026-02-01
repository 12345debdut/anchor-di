package com.debdut.anchordi.ksp.validation

/**
 * Validates that the dependency graph has no cycles.
 * [graph] maps each node to the set of nodes it depends on.
 *
 * Reports all cycles found, not just the first one, so the developer can fix them all at once.
 */
object CycleValidator {

    fun validate(graph: Map<String, Set<String>>, reporter: ValidationReporter) {
        val globalVisited = mutableSetOf<String>()
        val reportedCycles = mutableSetOf<Set<String>>()

        fun dfs(node: String, path: MutableSet<String>, pathOrder: MutableList<String>) {
            if (path.contains(node)) {
                // Found a cycle
                val cycleStart = pathOrder.indexOf(node)
                val cycleNodes = pathOrder.drop(cycleStart).toSet()
                
                // Only report each unique cycle once (cycles can be discovered from different starting nodes)
                if (cycleNodes !in reportedCycles) {
                    reportedCycles.add(cycleNodes)
                    val cycle = pathOrder.drop(cycleStart) + node
                    val suggestType = cycle.firstOrNull() ?: node
                    val cycleNames = cycle.joinToString(" -> ") { it.substringAfterLast('.') }
                    reporter.error(
                        ValidationMessageFormat.formatError(
                            summary = "Circular dependency: cycle detected: $cycleNames.",
                            detail = "A binding cannot depend on itself (directly or indirectly). The container cannot create any of these types because each one waits on the next.",
                            fix = "Break the cycle by injecting Lazy<$suggestType> or AnchorProvider<$suggestType> for one of the dependencies, so that the dependency is resolved lazily when first used instead of at construction time."
                        ),
                        null
                    )
                }
                return
            }
            if (globalVisited.contains(node)) return
            
            path.add(node)
            pathOrder.add(node)
            
            graph[node]?.forEach { dep ->
                dfs(dep, path, pathOrder)
            }
            
            path.remove(node)
            pathOrder.removeAt(pathOrder.lastIndex)
            globalVisited.add(node)
        }

        // Start DFS from each node to find all cycles
        graph.keys.forEach { startNode ->
            if (startNode !in globalVisited) {
                dfs(startNode, mutableSetOf(), mutableListOf())
            }
        }
    }
}
