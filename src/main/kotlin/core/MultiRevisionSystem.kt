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

package core

import context.Context
import context.ContextType
import graph.EdgeDescription
import graph.RevisionDescription
import system.Relation
import system.Projection
import system.SystemDescription

class MultiRevisionSystem(
    private val parts: MutableSet<RevisionGraph>,
    private val relations: MutableSet<Relation>,
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
        return GraphQueryInterface(graph).findContext(revisionShortId, contextType, depth)
    }

    fun findGlobalContext(revisionShortId: String, contextType: ContextType): Context {
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
        for (relation in relations) {
            if (relation.fromRevision == revisionShortId) {
                revisionIds.add(relation.toRevision)
            }
        }
        val vertices: Set<RevisionDescription> = revisionIds.map { findRevision(it) }.toSet()
        return Context(ContextType.RELATIONAL, 0, vertices)
    }

    private fun getProjectiveContext(revisionShortId: String): Context {
        val targets = projections.filter { it.sources.contains(revisionShortId) }
        return Context(ContextType.PROJECTIVE, 0, targets.map {
            RevisionDescription(Projection.PROJECTION, it.projectionId, Projection.PROJECTION, it.target) }.toSet())
    }

    private fun findRevision(revisionShortId: String): RevisionDescription {
        for (graph in parts) {
            if (graph.hasRevision(revisionShortId)) {
                return graph.transform(graph.getRevision(revisionShortId))
            }
        }
        throw IllegalArgumentException("Revision with short ID $revisionShortId not found in active core")
    }

    fun getRevisionGraphs(): Set<RevisionGraph> {
        return parts
    }

    fun getRelations(): Set<Relation> {
        return relations
    }

    fun getProjections(): Set<Projection> {
        return projections
    }

    fun initNewGraph(graphId: String): Boolean {
        val newGraph = TinkerRevisionGraph(graphId)
        parts.add(newGraph)
        return validate()
    }

    fun removeGraph(graphId: String): Boolean {
        val graph = getGraphById(graphId)
        //TODO remove all relations and projections targeting elements of the graph
        parts.remove(graph)
        return validate()
    }

    fun addRelation(relation: Relation): Boolean {
        relations.add(relation)
        return validate()
    }

    fun removeRelation(relation: Relation): Boolean {
        relations.remove(relation)
        return validate()
    }

    fun addProjection(projection: Projection): Boolean {
        projections.add(projection)
        return validate()
    }

    fun removeProjection(projection: Projection): Boolean {
        projections.remove(projection)
        return validate()
    }

    fun addRevision(graphId: String, revision: RevisionDescription): Boolean {
        val graph = getGraphById(graphId)
        graph.addRevision(revision)
        return graph.validate(Configuration.LOOKAHEAD)
    }

    fun removeRevision(graphId: String, revision: RevisionDescription): Boolean {
        val graph = getGraphById(graphId)
        graph.removeRevision(revision)
        return validate()
    }

    fun addEdge(graphId: String, edge: EdgeDescription): Boolean {
        val graph = getGraphById(graphId)
        graph.addEdge(edge)
        return graph.validate()
    }

    fun removeEdge(graphId: String, edge: EdgeDescription): Boolean {
        val graph = getGraphById(graphId)
        graph.removeEdge(edge)
        return graph.validate()
    }

    fun getDescription(): SystemDescription {
        return SystemDescription(parts, relations, projections)
    }

    fun serialize(): String {
        return getDescription().serialize()
    }

    /**
     * Validates the multi-revision system.
     * This method only provides a basic validation of the system to prevent common errors.
     * It cannot prove the correctness of the system.
     */
    fun validate(): Boolean {
        //check that no two graphs have the same ID
        if(parts.size != parts.map { it.graphId }.toSet().size){
            throw IllegalStateException("Duplicate graph IDs found!")
        }
        //check that all graphs are valid
        for (graph in parts) {
            if (!graph.validate(Configuration.LOOKAHEAD)) {
                throw IllegalStateException("Invalid graph ${graph.graphId}!")
            }
        }
        //check that all the targets of the relations exist
        for (relation in relations) {
            if (!parts.any { it.hasRevision(relation.fromRevision) } || !parts.any { it.hasRevision(relation.toRevision) }) {
                throw IllegalStateException("Invalid relation ${relation.fromRevision} -> ${relation.toRevision}!")
            }
        }
        //check that all the sources of the projections exist
        for (projection in projections) {
            for (source in projection.sources) {
                if (!parts.any { it.hasRevision(source) }) {
                    throw IllegalStateException("Invalid projection ${projection.projectionId} with source $source!")
                }
            }
        }
        return true
    }

    companion object {

        fun create(): MultiRevisionSystem {
            return MultiRevisionSystem(mutableSetOf(), mutableSetOf(), mutableSetOf())
        }

        fun create(graphs: Set<RevisionGraph>, relations: Set<Relation>, projections: Set<Projection>): MultiRevisionSystem {
            return MultiRevisionSystem(graphs.toMutableSet(), relations.toMutableSet(), projections.toMutableSet())
        }

    }

}