import context.Context
import context.ContextType
import graphextend.CrossLink
import graphextend.Projection

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
    private val links: MutableSet<CrossLink>,
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
        if (contextType in listOf(ContextType.VOLATILE, ContextType.LINK)) {
            throw IllegalArgumentException("Context type $contextType not supported by this method!")
        }
        val graph = getGraphById(graphId)
        return GraphQueryInterface(graph).findContextByShortId(revisionShortId, contextType, depth)
    }

    private fun getGlobalContext(revisionShortId: String, contextType: ContextType): Context {
        if (contextType in listOf(ContextType.TIME, ContextType.SPACE)) {
            throw IllegalArgumentException("Context type $contextType not supported by this method!")
        }
        TODO("Not yet implemented")
    }

    fun getRevisionGraphs(): Set<RevisionGraph> {
        return parts
    }

    fun getCrossLinks(): Set<CrossLink> {
        return links
    }

    fun getProjections(): Set<Projection> {
        return projections
    }

    companion object {

        fun create(): MultiRevisionSystem {
            return MultiRevisionSystem(mutableSetOf(), mutableSetOf(), mutableSetOf())
        }

        fun create(graphs: Set<RevisionGraph>, links: Set<CrossLink>, projections: Set<Projection>): MultiRevisionSystem {
            return MultiRevisionSystem(graphs.toMutableSet(), links.toMutableSet(), projections.toMutableSet())
        }





    }

}