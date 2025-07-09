package coconlib.core

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

import coconlib.region.Region
import coconlib.region.RegionType
import coconlib.graph.EdgeDescription
import coconlib.graph.RevisionDescription
import org.apache.tinkerpop.gremlin.structure.Vertex

class GraphQueryInterface(private val revisionGraph: RevisionGraph) {

    fun findRegion(revisionShortId: String, regionType: RegionType, depth: Int): Region {
        var vertex: Vertex
        try {
            vertex = revisionGraph.getRevision(revisionShortId)
        } catch (e: Exception) {
            //e.printStackTrace()
            throw IllegalArgumentException("Revision with short ID $revisionShortId not found")
        }
        return findRegion(vertex, regionType, depth)
    }

    private fun findRegion(vertex: Vertex, regionType: RegionType, depth: Int): Region {
        if (depth < 0 && depth != Region.UNBOUNDED) {
            throw IllegalArgumentException("Depth must be greater or equal 0 or Region.UNBOUNDED (-1)")
        }
        return if (regionType == RegionType.TIME) {
            findTimeRegion(vertex, depth)
        } else if (regionType == RegionType.SPACE) {
            findSpaceRegion(vertex, depth)
        } else {
            throw IllegalArgumentException("Region type $regionType not support by this interface!")
        }
    }

    private fun findTimeRegion(vertex: Vertex, depth: Int): Region {
        val cardinality = if (depth == Region.UNBOUNDED) {
            Int.MAX_VALUE
        } else {
            depth
        }
        val revisions = revisionGraph.getPathToRoot(vertex, depth)
        return Region(
            RegionType.TIME,
            cardinality,
            revisions.map { revisionGraph.transform(it) }.toSet()
        )
    }

    private fun findSpaceRegion(vertex: Vertex, depth: Int): Region {
        val cardinality = if (depth == Region.UNBOUNDED) {
            Int.MAX_VALUE
        } else {
            depth
        }
        val neighbors = revisionGraph.getNeighbors(vertex, depth)
        return Region(
            RegionType.SPACE,
            cardinality,
            neighbors.map { revisionGraph.transform(it) }.toSet()
        )
    }

    fun getRevisions(): List<RevisionDescription> {
        return revisionGraph.getRevisions()
    }

    fun getEdges(): List<EdgeDescription> {
        return revisionGraph.getEdges()
    }

    fun getRoot(): RevisionDescription {
        return revisionGraph.transform(revisionGraph.getRootRevision())
    }

    fun addRevision(revision: RevisionDescription) {
        revisionGraph.addRevision(revision)
    }

    fun addEdge(edge: EdgeDescription) {
        revisionGraph.addEdge(edge)
    }

    fun removeRevision(revision: RevisionDescription) {
        revisionGraph.removeRevision(revision)
    }

    fun removeEdge(edge: EdgeDescription) {
        revisionGraph.removeEdge(edge)
    }

    fun validate(): Boolean{
        return revisionGraph.validate()
    }
}