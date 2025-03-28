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

import context.Context
import context.ContextType
import graphcore.EdgeDescription
import graphcore.VertexDescription
import org.apache.tinkerpop.gremlin.structure.Vertex

class TinkerSingleInterface(val revisionGraph: TinkerRevisionGraph) : SingleInterface {

    override fun findContextByShortId(revisionShortId: String, contextType: ContextType, depth: Int): Context {
        var vertex: Vertex
        try {
            vertex = revisionGraph.getRevision(revisionShortId)
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("Revision with short ID $revisionShortId not found")
        }
        return findContext(vertex, contextType, depth)
    }

    override fun findContextByLongId(revisionLongId: String, contextType: ContextType, depth: Int): Context {
        var vertex: Vertex
        try {
            vertex = revisionGraph.getRevisionByLongId(revisionLongId)
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("Revision with short ID $revisionLongId not found")
        }
        return findContext(vertex, contextType, depth)
    }

    private fun findContext(vertex: Vertex, contextType: ContextType, depth: Int): Context {
        if (depth <= 0 && depth != Context.UNBOUNDED) {
            throw IllegalArgumentException("Depth must be greater or equal 0 or Context.UNBOUNDED (-1)")
        }
        return if (contextType == ContextType.TIME) {
            findTimeContext(vertex, depth)
        } else if (contextType == ContextType.SPACE) {
            findSpaceContext(vertex, depth)
        } else {
            throw IllegalArgumentException("Context type $contextType not support by this interface!")
        }
    }

    private fun findTimeContext(vertex: Vertex, depth: Int): Context {
        val cardinality = if (depth == Context.UNBOUNDED) {
            Int.MAX_VALUE
        } else {
            depth
        }
        val revisions = revisionGraph.getPathToRoot(vertex, depth)
        return Context(
            ContextType.TIME,
            cardinality,
            revisions.map { revisionGraph.transform(it) }.toSet()
        )
    }

    private fun findSpaceContext(vertex: Vertex, depth: Int): Context {
        val cardinality = if (depth == Context.UNBOUNDED) {
            Int.MAX_VALUE
        } else {
            depth
        }
        val neighbors = revisionGraph.getNeighbors(vertex, depth)
        return Context(
            ContextType.SPACE,
            cardinality,
            neighbors.map { revisionGraph.transform(it) }.toSet()
        )
    }

    override fun getRevisions(): List<VertexDescription> {
        return revisionGraph.getRevisions()
    }

    override fun getEdges(): List<EdgeDescription> {
        return revisionGraph.getEdges()
    }

    override fun getRoot(): VertexDescription {
        return revisionGraph.transform(revisionGraph.getRootRevision())
    }
}