package com.debdut.anchordi.ksp.validation

/**
 * Validates that the dependency graph has no cycles.
 * [graph] maps each node to the set of nodes it depends on.
 */
object CycleValidator {

    fun validate(graph: Map<String, Set<String>>, reporter: ValidationReporter) {
        val visited = mutableSetOf<String>()
        val path = mutableSetOf<String>()
        val pathOrder = mutableListOf<String>()

        fun dfs(node: String): Boolean {
            if (path.contains(node)) {
                val cycleStart = pathOrder.indexOf(node)
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
                return true
            }
            if (visited.contains(node)) return false
            visited.add(node)
            path.add(node)
            pathOrder.add(node)
            var hasCycle = false
            graph[node]?.forEach { dep ->
                if (dfs(dep)) hasCycle = true
            }
            path.remove(node)
            pathOrder.removeAt(pathOrder.lastIndex)
            return hasCycle
        }

        graph.keys.forEach { if (dfs(it)) return }
    }
}
