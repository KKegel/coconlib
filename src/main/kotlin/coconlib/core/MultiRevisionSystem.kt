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

package coconlib.core

import coconlib.region.Region
import coconlib.region.RegionType
import coconlib.graph.EdgeDescription
import coconlib.graph.RevisionDescription
import coconlib.system.Relation
import coconlib.system.Projection
import coconlib.system.SystemDescription

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

    fun findLocalRegion(graphId: String, revisionShortId: String, regionType: RegionType, depth: Int): Region {
        if (regionType in listOf(RegionType.PROJECTIVE, RegionType.RELATIONAL)) {
            throw IllegalArgumentException("Region type $regionType not supported by this method!")
        }
        val graph = getGraphById(graphId)
        return GraphQueryInterface(graph).findRegion(revisionShortId, regionType, depth)
    }

    fun findGlobalRegion(revisionShortId: String, regionType: RegionType): Region {
        return if (regionType == RegionType.RELATIONAL) {
            getRelationalRegion(revisionShortId)
        } else if (regionType == RegionType.PROJECTIVE) {
            getProjectiveRegion(revisionShortId)
        } else {
            throw IllegalArgumentException("Region type $regionType not supported by this method!")
        }
    }

    private fun getRelationalRegion(revisionShortId: String): Region {
        val revisionIds = mutableSetOf<String>(revisionShortId)
        for (relation in relations) {
            if (relation.fromRevision == revisionShortId) {
                revisionIds.add(relation.toRevision)
            }
        }
        val vertices: Set<RevisionDescription> = revisionIds.map { findRevision(it) }.toSet()
        return Region(RegionType.RELATIONAL, 0, vertices)
    }

    private fun getProjectiveRegion(revisionShortId: String): Region {
        val targets = projections.filter { it.sources.contains(revisionShortId) }
        return Region(RegionType.PROJECTIVE, 0, targets.map {
            RevisionDescription(Projection.PROJECTION, it.projectionId, Projection.PROJECTION, it.target) }.toSet())
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

    fun getParts(): Set<RevisionGraph> {
        return parts
    }

    fun initNewGraph(graphId: String): Boolean {
        val newGraph = TinkerRevisionGraph(graphId)
        parts.add(newGraph)
        return validate()
    }

    fun removeGraph(graphId: String): Boolean {
        val graph = getGraphById(graphId)
        val revisionIds = graph.getRevisions().map { it.revId }
        //remove all relations having one of the revisions as source
        relations.removeIf { it.fromRevision in revisionIds }
        //remove all relations having one of the revisions as target
        relations.removeIf { it.toRevision in revisionIds }
        //remove all projections having one of the revisions as source
        projections.removeIf { it.sources.any { source -> source in revisionIds } }
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

    fun addRevision(graphId: String, revision: RevisionDescription, safe: Boolean = true): Boolean {
        val graph = getGraphById(graphId)
        graph.addRevision(revision)
        return if (safe) {
            validate()
        } else {
            true
        }
    }

    fun addRevisionWithUnification(graphId: String,
                                   revision: RevisionDescription,
                                   edgeA: EdgeDescription,
                                   edgeB: EdgeDescription): Boolean {
        val graph = getGraphById(graphId)
        graph.addRevisionWithUnification(revision, edgeA, edgeB)
        return validate()
    }

    fun removeRevision(revisionId: String): Boolean {
        val revision = findRevision(revisionId)
        return removeRevision(revision.graph, revision)
    }

    fun findEdges(revisionId: String): List<EdgeDescription> {
        val revision = findRevision(revisionId)
        val graph = getGraphById(revision.graph)
        return graph.getEdges().filter { it.sourceShortId == revision.revId || it.targetShortId == revision.revId }
    }

    fun findProjections(revisionId: String): List<Projection> {
        val revision = findRevision(revisionId)
        return projections.filter { it.sources.contains(revision.revId) }
    }

    fun findRelations(revisionId: String): List<Relation> {
        val revision = findRevision(revisionId)
        return relations.filter { it.fromRevision == revision.revId || it.toRevision == revision.revId }
    }

    fun findRevision(revisionId: String): RevisionDescription {
        for (graph in parts) {
            if (graph.hasRevision(revisionId)) {
                return graph.transform(graph.getRevision(revisionId))
            }
        }
        throw IllegalArgumentException("Revision with ID $revisionId not found in active core")
    }

    fun removeRevision(graphId: String, revision: RevisionDescription): Boolean {
        val graph = getGraphById(graphId)
        //remove all projections having the revision as source
        projections.removeIf { it.sources.contains(revision.revId) }
        //remove all relations having the revision as target
        relations.removeIf { it.toRevision == revision.revId }
        //remove all relations having the revision as source
        relations.removeIf { it.fromRevision == revision.revId }
        //edges in and out are removed automatically within the graph
        graph.removeRevision(revision)
        return validate()
    }

    fun addEdge(graphId: String, edge: EdgeDescription, safe: Boolean = true): Boolean {
        val graph = getGraphById(graphId)
        graph.addEdge(edge)
        return if (safe) {
            validate()
        } else {
            true
        }
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
        //check that all revisions have unique IDs
        val allRevisions = parts.toList().flatMap { it.getRevisions() }
        assert(allRevisions.size == allRevisions.map { it.revId }.toSet().size)
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