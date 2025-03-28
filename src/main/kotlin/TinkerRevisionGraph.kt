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

import graphcore.EdgeDescription
import graphcore.EdgeLabel
import graphcore.GraphDescription
import graphcore.VertexDescription
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal.Symbols.id
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Graph
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph

class TinkerRevisionGraph(graphId: String) : RevisionGraph(graphId) {

    val graph: Graph = TinkerGraph.open()

    override fun toString(): String {
        return graph.toString()
    }

    override fun hasRevision(vertex: VertexDescription): Boolean {
        return traversal().with(graph)
            .V()
            .has(id, vertex.shortId)
            .hasNext()
    }

    override fun hasEdge(edge: EdgeDescription): Boolean {
        return traversal().with(graph)
            .E()
            .where(`__`
                .outV()
                .has(id, edge.sourceShortId)
                .and()
                .inV()
                .has(id, edge.targetShortId)
            ).hasNext()
    }

    override fun addRevision(vertex: VertexDescription) {
        val g = traversal().with(graph)
        g.addV(REVISION)
            .property(id, vertex.shortId)
            .property("longId", vertex.longId)
            .property("path", vertex.location)
            .property("payload", vertex.payload)
            .next()
    }

    override fun addEdge(edge: EdgeDescription) {
        val source = traversal().with(graph)
            .V()
            .has(id, edge.sourceShortId)
            .next()
        val target = traversal().with(graph)
            .V()
            .has(id, edge.targetShortId)
            .next()

        traversal().with(graph).addE(edge.label.name)
            .from(source)
            .to(target)
            .next()

        traversal().with(graph).addE(inverseEdge(edge.label.name))
            .from(target)
            .to(source)
            .next()
    }

    override fun removeRevision(vertex: VertexDescription) {
        traversal().with(graph)
            .V()
            .has(id, vertex.shortId)
            .next()
            .remove()
    }

    override fun removeEdge(edge: EdgeDescription) {
        traversal().with(graph)
            .E()
            .hasLabel(edge.label.name)
            .where(`__`.outV()
                .has(id, edge.sourceShortId)
                .and()
                .inV()
                .has(id, edge.targetShortId)
            ).next().remove()

        traversal().with(graph)
            .E()
            .hasLabel(inverseEdge(edge.label.name))
            .where(`__`.outV()
                .has(id, edge.targetShortId)
                .and()
                .inV()
                .has(id, edge.sourceShortId)
            ).next().remove()
    }

    override fun prettyPrint(): String {
        val edges = traversal().with(graph)
            .E()
            .toList()
        return edges.joinToString("\n") { edge ->
            val outV = edge.outVertex().property<Any>(id).value()
            val inV = edge.inVertex().property<Any>(id).value()
            "\"$outV\" -${edge.label()}-> \"$inV\""
        }
    }

    override fun getRevisions(): List<VertexDescription> {
        return traversal().with(graph)
            .V()
            .toList()
            .map { vertex ->
            VertexDescription(
                graphId,
                vertex.property<String>(id).value().toString(),
                vertex.property<String>("longId").value().toString(),
                vertex.property<String>("path").value().toString(),
                vertex.property<String>("payload").value().toString()
            )
        }
    }

    override fun getEdges(): List<EdgeDescription> {
        return traversal().with(graph)
            .E()
            .toList()
            .filter { edge -> edge.label().startsWith("INVERSE_").not() }
            .map { edge ->
            EdgeDescription(
                edge.outVertex().property<String>(id).value().toString(),
                edge.inVertex().property<String>(id).value().toString(),
                EdgeLabel.valueOf(edge.label())
            )
        }
    }

    override fun transform(vertexDescription: VertexDescription): Vertex {
        return transform(vertexDescription.shortId)
    }

    override fun transform(edgeDescription: EdgeDescription): Edge {
        return transform(edgeDescription.sourceShortId, edgeDescription.targetShortId)
    }

    override fun transform(shortId: String): Vertex {
        return traversal().with(graph)
            .V()
            .has(id, shortId)
            .next()
    }

    override fun transform(sourceShortId: String, targetShortId: String): Edge {
        return traversal().with(graph)
            .E()
            .where(`__`.outV()
                .has(id, sourceShortId)
                .and()
                .inV()
                .has(id, targetShortId)
            ).next()
    }

    override fun transform(vertex: Vertex): VertexDescription {
        return VertexDescription(
            graphId,
            vertex.property<String>(id).value().toString(),
            vertex.property<String>("longId").value().toString(),
            vertex.property<String>("path").value().toString(),
            vertex.property<String>("payload").value().toString()
        )
    }

    override fun transform(edge: Edge): EdgeDescription {
        return EdgeDescription(
            edge.outVertex().property<String>(id).value().toString(),
            edge.inVertex().property<String>(id).value().toString(),
            EdgeLabel.valueOf(edge.label())
        )
    }

    override fun getLeafRevisions(): List<Vertex> {
        return traversal().with(graph)
            .V()
            .hasLabel(REVISION)
            .not(`__`.outE(EdgeLabel.SUCCESSOR.name))
            .toList()
    }

    fun getLeafsFromVertex(vertex: Vertex): List<Vertex> {
        return traversal().with(graph)
            .V(vertex)
            .repeat(`__`.outE(EdgeLabel.SUCCESSOR.name).inV())
            .emit()
            .hasLabel(REVISION)
            .not(`__`.outE(EdgeLabel.SUCCESSOR.name))
            .toList()
    }

    override fun getRootRevision(): Vertex {
        return traversal().with(graph)
            .V()
            .hasLabel(REVISION)
            .not(`__`.inE(EdgeLabel.SUCCESSOR.name))
            .next()
    }

    override fun getSuccessorEdges(): List<Edge> {
        return traversal().with(graph)
            .E()
            .hasLabel(EdgeLabel.SUCCESSOR.name)
            .toList()
    }

    override fun getMergeEdges(): List<Edge> {
        return traversal().with(graph)
            .E()
            .hasLabel(EdgeLabel.MERGE.name)
            .toList()
    }

    override fun getPathToRoot(vertex: Vertex, pathLength: Int): List<Vertex> {
        if (pathLength == 0) {
            return listOf(vertex)
        }
        val limit = if (pathLength == -1) {
            Int.MAX_VALUE
        } else {
            pathLength + 1
        }
        val path = mutableListOf<Vertex>(vertex)
        val root = getRootRevision()
        while (path.size < limit && path.contains(root).not()) {
            val last = path.last()
            var nextPredecessor = traversal().with(graph)
                .V(last)
                .outE(inverseEdge(EdgeLabel.MERGE.name))
                .inV()
                .toList()
                .firstOrNull()
            if (nextPredecessor == null) {
                nextPredecessor = traversal().with(graph)
                    .V(last)
                    .outE(inverseEdge(EdgeLabel.SUCCESSOR.name))
                    .inV()
                    .toList()
                    .firstOrNull()
            }
            assert(nextPredecessor != null) { "No next predecessor found" }
            path.add(nextPredecessor!!)
        }
        return path
    }

    override fun getRevision(shortId: String): Vertex {
        return traversal().with(graph)
            .V()
            .has(id, shortId)
            .next()
    }

    override fun getRevisionByLongId(longId: String): Vertex {
        return traversal().with(graph)
            .V()
            .has("longId", longId)
            .next()
    }

    override fun getEdge(sourceShortId: String, targetShortId: String): Edge {
        return traversal().with(graph)
            .E()
            .where(`__`
                .outV()
                .has(id, sourceShortId)
                .and()
                .inV()
                .has(id, targetShortId)
            ).next()
    }

    override fun getNeighbors(vertex: Vertex, recursionDepth: Int): Set<Vertex> {
        if (recursionDepth == 0) {
            return setOf(vertex)
        }
        val nthPredecessor = getPathToRoot(vertex, recursionDepth).last()
        return getLeafsFromVertex(nthPredecessor).toSet()
    }

    fun hasOnlyOneRoot(): Boolean {
        val rootVertices = traversal().with(graph)
            .V()
            .hasLabel(REVISION)
            .not(`__`.inE(EdgeLabel.SUCCESSOR.name))
            .toList()
        println(rootVertices.map { it.property<String>(id).value() })
        return rootVertices.size == 1
    }

    fun hasCycles(depth: Int): Boolean {
        val allVertices = traversal().with(graph)
            .V()
            .hasLabel(REVISION)
            .toList()

        for (vertex in allVertices) {
            val paths = traversal().with(graph)
                .V(vertex)
                .repeat(`__`.outE(EdgeLabel.SUCCESSOR.name, EdgeLabel.MERGE.name).inV())
                .times(depth)
                .path()
                .toList()
            for (path in paths) {
                val cycle = path.filter { it == vertex }
                if (cycle.size > 1) {
                    println(path)
                    return true
                }
            }

        }
        return false
    }

    fun getDirectSuccessors(vertex: Vertex): List<Vertex> {
        return traversal().with(graph)
            .V(vertex)
            .out(EdgeLabel.SUCCESSOR.name)
            .toList()
    }

    fun maxTwoIncomingSuccessors(): Boolean {
        val allVertices = traversal().with(graph)
            .V()
            .hasLabel(REVISION)
            .toList()

        for (vertex in allVertices) {
            val incomingEdges = traversal().with(graph)
                .V(vertex)
                .inE(EdgeLabel.SUCCESSOR.name)
                .count()
                .next()
            if (incomingEdges > 2) {
                return false
            }
        }
        return true
    }

    fun maxOneIncomingMerge(): Boolean {
        val allVertices = traversal().with(graph)
            .V()
            .hasLabel(REVISION)
            .toList()

        for (vertex in allVertices) {
            val incomingEdges = traversal().with(graph)
                .V(vertex)
                .inE(EdgeLabel.MERGE.name)
                .count()
                .next()
            if (incomingEdges > 1) {
                return false
            }
        }
        return true
    }

    fun twoIncomingSuccessorsRequiresMerge(): Boolean {
        val allVertices = traversal().with(graph)
            .V()
            .hasLabel(REVISION)
            .toList()

        for (vertex in allVertices) {
            val incomingSuccessorEdges = traversal().with(graph)
                .V(vertex)
                .inE(EdgeLabel.SUCCESSOR.name)
                .count()
                .next()
            if (incomingSuccessorEdges == 2L) {
                val incomingMergeEdges = traversal().with(graph)
                    .V(vertex)
                    .inE(EdgeLabel.MERGE.name)
                    .count()
                    .next()
                return incomingMergeEdges == 1L
            }
        }
        return true
    }

    /**
     * Validates the graph for the following conditions:
     * - Graph has only one root
     * - Graph has no cycles
     * - A vertex with two incoming successors must have a merge edge
     *
     * @param depth the maximum path length to check for cycles
     */
    override fun validate(depth: Int): Boolean {

        if (!hasOnlyOneRoot()) throw IllegalStateException("Graph has more than one root")

        if (!maxTwoIncomingSuccessors()) throw IllegalStateException("Vertex has more than two incoming successors")
        if (!maxOneIncomingMerge()) throw IllegalStateException("Vertex has more than one incoming merge edge")
        if (!twoIncomingSuccessorsRequiresMerge()) throw IllegalStateException("Two incoming successors require a merge edge")

        if (hasCycles(depth)) throw IllegalStateException("Graph has cycles within depth $depth")

        /*
        TODO: more validity checks possible
         */

        return true
    }

    companion object {

        fun inverseEdge(label: String): String {
            if(label.startsWith("INVERSE_")) {
                return label.substring(8)
            }
            return "INVERSE_$label"
        }

        fun build(graphDescription: GraphDescription): TinkerRevisionGraph {
            return build(graphDescription.id, graphDescription.vertices, graphDescription.edges)
        }

        fun build(graphId: String, vertices: List<VertexDescription>, edges: List<EdgeDescription>): TinkerRevisionGraph {

            assert(graphId.isNotEmpty()) { "Graph ID must not be empty" }
            val revisionGraph = TinkerRevisionGraph(graphId)

            vertices.forEach { vertex ->
                revisionGraph.addRevision(vertex)
            }
            edges.forEach { edge ->
                revisionGraph.addEdge(edge)
            }
            return revisionGraph
        }

        fun toDescription(graph: TinkerRevisionGraph): String {
            val vertices = graph.getRevisions()
            val edges = graph.getEdges()
            return GraphDescription(
                graph.graphId,
                vertices,
                edges
            ).serialize()
        }

    }

}