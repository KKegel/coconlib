import context.Context
import context.ContextType
import graphcore.RevisionDescription
import graphextend.Relation
import graphextend.Projection
import graphextend.SystemDescription

/**
 *  Copyright 2025 Karl Kegel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

class MultiRevisionSystem(
    private val parts: MutableSet<RevisionGraph>,
    private val links: MutableSet<Relation>,
    private val projections: MutableSet<Projection>) {

    private fun getGraphById(graphId: String): RevisionGraph {
        try {
            return parts.find { it.graphId == graphId }!!
        } catch (e: Exception) {
            //e.printStackTrace()
            throw IllegalArgumentException("Invalid graphId (subsystem not found)")
        }
    }

    fun findLocalContext(graphId: String, revisionShortId: String, contextType: ContextType, depth: Int): Context {
        if (contextType in listOf(ContextType.PROJECTIVE, ContextType.RELATIONAL)) {
            throw IllegalArgumentException("Context type $contextType not supported by this method!")
        }
        val graph = getGraphById(graphId)
        return GraphQueryInterface(graph).findContextByShortId(revisionShortId, contextType, depth)
    }

    fun getGlobalContext(revisionShortId: String, contextType: ContextType): Context {
        return if (contextType == ContextType.RELATIONAL) {
            getRelationalContext(revisionShortId)
        } else if (contextType == ContextType.PROJECTIVE) {
            getProjectiveContext(revisionShortId)
        } else {
            throw IllegalArgumentException("Context type $contextType not supported by this method!")
        }
    }

    private fun getRelationalContext(revisionShortId: String): Context {
        val revisionIds = mutableSetOf<String>(revisionShortId)
        for (link in links) {
            if (link.fromRevision == revisionShortId) {
                revisionIds.add(link.toRevision)
            }
        }
        val vertices: Set<RevisionDescription> = revisionIds.map { findRevision(it) }.toSet()
        return Context(ContextType.RELATIONAL, 0, vertices)
    }

    private fun getProjectiveContext(revisionShortId: String): Context {
        val targets = projections.filter { it.sources.contains(revisionShortId) }
        return Context(ContextType.PROJECTIVE, 0, targets.map { RevisionDescription("", it.projectionId, "", it.target) }.toSet())
    }

    private fun findRevision(revisionShortId: String): RevisionDescription {
        for (graph in parts) {
            if (graph.hasRevision(revisionShortId)) {
                return graph.transform(graph.getRevision(revisionShortId))
            }
        }
        throw IllegalArgumentException("Revision with short ID $revisionShortId not found in active system")
    }

    fun getRevisionGraphs(): Set<RevisionGraph> {
        return parts
    }

    fun getCrossLinks(): Set<Relation> {
        return links
    }

    fun getProjections(): Set<Projection> {
        return projections
    }

    fun getDescription(): SystemDescription {
        return SystemDescription(parts, links, projections)
    }

    companion object {

        fun create(): MultiRevisionSystem {
            return MultiRevisionSystem(mutableSetOf(), mutableSetOf(), mutableSetOf())
        }

        fun create(graphs: Set<RevisionGraph>, links: Set<Relation>, projections: Set<Projection>): MultiRevisionSystem {
            return MultiRevisionSystem(graphs.toMutableSet(), links.toMutableSet(), projections.toMutableSet())
        }

        fun create(systemDescription: SystemDescription): MultiRevisionSystem {
            return MultiRevisionSystem(
                systemDescription.parts.toMutableSet(),
                systemDescription.links.toMutableSet(),
                systemDescription.projections.toMutableSet())
        }

    }

}