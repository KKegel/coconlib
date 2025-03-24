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
import graphcore.VertexDescription
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TinkerRevisionGraphUnitTests {

    private lateinit var revisionGraph: TinkerRevisionGraph

    @BeforeEach
    fun setUp() {
        revisionGraph = TinkerRevisionGraph("g1")
        val v1 = VertexDescription("g1", "v1", "Version 1", "./a", "payload")
        val v2 = VertexDescription("g1", "v2", "Version e", "./b", "payload")
        val v3 = VertexDescription("g1", "v3", "Version 3", "./c", "payload")
        revisionGraph.addRevision(v1)
        revisionGraph.addRevision(v2)
        revisionGraph.addRevision(v3)
        val edge1 = EdgeDescription("v1", "v2", EdgeLabel.SUCCESSOR)
        val edge2 = EdgeDescription("v1", "v3", EdgeLabel.SUCCESSOR)
        revisionGraph.addEdge(edge1)
        revisionGraph.addEdge(edge2)
    }

    @Test
    fun testToString() {
        println(revisionGraph.toString())
        assertEquals("tinkergraph[vertices:3 edges:4]", revisionGraph.toString())
    }

    @Test
    fun testPrettyPrint() {
        print(revisionGraph.prettyPrint())
        val result = revisionGraph.prettyPrint()
        val lines = result.split("\n")
        assertEquals(lines.size, 4)
        assertTrue(lines.contains("\"v1\" -SUCCESSOR-> \"v2\""))
        assertTrue(lines.contains("\"v1\" -SUCCESSOR-> \"v3\""))
    }

    @Test
    fun testGraphInitialization() {
        assertNotNull(revisionGraph.graph)
        assertEquals(3, revisionGraph.graph.traversal().V().count().next())
        assertEquals(4, revisionGraph.graph.traversal().E().count().next())
    }

    @Test
    fun testAddAndRemoveRevision() {
        val v4 = VertexDescription("g1", "v4", "Version 4", "./d", "payload")
        revisionGraph.addRevision(v4)
        assertEquals(4, revisionGraph.graph.traversal().V().count().next())
        assertEquals(4, revisionGraph.getRevisions().size)
        assertTrue(revisionGraph.hasRevision(v4))
        revisionGraph.removeRevision(v4)
        assertEquals(3, revisionGraph.graph.traversal().V().count().next())
        assertEquals(3, revisionGraph.getRevisions().size)
        assertFalse(revisionGraph.hasRevision(v4))
    }

    @Test
    fun testAddAndRemoveEdge() {
        assertEquals(4, revisionGraph.graph.traversal().E().count().next())
        assertEquals(2, revisionGraph.getEdges().size)
        val edge3 = EdgeDescription("v2", "v3", EdgeLabel.SUCCESSOR)
        revisionGraph.addEdge(edge3)
        assertEquals(6, revisionGraph.graph.traversal().E().count().next())
        assertEquals(3, revisionGraph.getEdges().size)
        assertTrue(revisionGraph.hasEdge(edge3))
        revisionGraph.removeEdge(edge3)
        assertEquals(4, revisionGraph.graph.traversal().E().count().next())
        assertEquals(2, revisionGraph.getEdges().size)
        assertFalse(revisionGraph.hasEdge(edge3))
    }

}