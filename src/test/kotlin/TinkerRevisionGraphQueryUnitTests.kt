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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TinkerRevisionGraphQueryUnitTests {

    private lateinit var revisionGraph: TinkerRevisionGraph

    /**
     *              /---> g --> p
     * a --> b --> e --> f
     * \---> c --> h ---\
     *  \ ============== > j ---> k
     *   \--> d --------/
     */
    @BeforeEach
    fun setUp() {
        revisionGraph = TinkerRevisionGraph.build(
            GraphDescription("g1", listOf(
                VertexDescription("g1", "a", "A", "./a"),
                VertexDescription("g1", "b", "B", "./b"),
                VertexDescription("g1", "c", "C", "./c"),
                VertexDescription("g1", "d", "D", "./d"),
                VertexDescription("g1", "e", "E", "./e"),
                VertexDescription("g1", "f", "F", "./f"),
                VertexDescription("g1", "g", "G", "./g"),
                VertexDescription("g1", "h", "H", "./h"),
                VertexDescription("g1", "j", "J", "./j"),
                VertexDescription("g1", "k", "K", "./k"),
                VertexDescription("g1", "p", "P", "./p")
            ), listOf(
                EdgeDescription("a", "b", EdgeLabel.SUCCESSOR),
                EdgeDescription("a", "c", EdgeLabel.SUCCESSOR),
                EdgeDescription("a", "d", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "e", EdgeLabel.SUCCESSOR),
                EdgeDescription("c", "h", EdgeLabel.SUCCESSOR),
                EdgeDescription("h", "j", EdgeLabel.SUCCESSOR),
                EdgeDescription("d", "j", EdgeLabel.SUCCESSOR),
                EdgeDescription("j", "k", EdgeLabel.SUCCESSOR),
                EdgeDescription("e", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("e", "g", EdgeLabel.SUCCESSOR),
                EdgeDescription("g", "p", EdgeLabel.SUCCESSOR),
                EdgeDescription("a", "j", EdgeLabel.MERGE),
            )))
    }

    @Test
    fun setupTest() {
        assertEquals(11, revisionGraph.graph.traversal().V().count().next())
        assertEquals(24, revisionGraph.graph.traversal().E().count().next())
        assertEquals(12, revisionGraph.getEdges().size)
    }

    @Test
    fun getRootRevisionTest() {
        val root = revisionGraph.getRootRevision()
        assertEquals("a", revisionGraph.transform(root).shortId)
    }

   @Test
   fun getLeafRevisionsTest() {
        val leafs = revisionGraph.getLeafRevisions()
        assertEquals(3, leafs.size)
        assertTrue(leafs.map { revisionGraph.transform(it).shortId }.containsAll(listOf("f", "k", "p")))
   }

    /**
     * a --> b --> e --> f
     */
    @Test
    fun testGetPathToRootDepth0() {
        val vertex = revisionGraph.getRevision("f")
        val path = revisionGraph.getPathToRoot(vertex, 0)
        assertEquals(1, path.size)
        assertEquals("f", revisionGraph.transform(path[0]).shortId)
    }

    @Test
    fun testGetPathToRootDepth1() {
        val vertex = revisionGraph.getRevision("f")
        val path = revisionGraph.getPathToRoot(vertex, 1)
        print(path.map { revisionGraph.transform(it).shortId })
        assertEquals(2, path.size)
        assertEquals("f", revisionGraph.transform(path[0]).shortId)
        assertEquals("e", revisionGraph.transform(path[1]).shortId)
    }

    @Test
    fun testGetPathToRootDepth2() {
        val vertex = revisionGraph.getRevision("f")
        val path = revisionGraph.getPathToRoot(vertex, 2)
        print(path.map { revisionGraph.transform(it).shortId })
        assertEquals(3, path.size)
        assertEquals("f", revisionGraph.transform(path[0]).shortId)
        assertEquals("e", revisionGraph.transform(path[1]).shortId)
        assertEquals("b", revisionGraph.transform(path[2]).shortId)
    }

    @Test
    fun testGetPathToRootDepthUnbounded() {
        val vertex = revisionGraph.getRevision("f")
        val path = revisionGraph.getPathToRoot(vertex, -1)
        print(path.map { revisionGraph.transform(it).shortId })
        assertEquals(4, path.size)
        assertEquals("f", revisionGraph.transform(path[0]).shortId)
        assertEquals("e", revisionGraph.transform(path[1]).shortId)
        assertEquals("b", revisionGraph.transform(path[2]).shortId)
        assertEquals("a", revisionGraph.transform(path[3]).shortId)
    }

    @Test
    fun testGetPathToRootDepthUnbounded2() {
        val vertex = revisionGraph.getRevision("p")
        val path = revisionGraph.getPathToRoot(vertex, -1)
        print(path.map { revisionGraph.transform(it).shortId })
        assertEquals(5, path.size)
        assertEquals("p", revisionGraph.transform(path[0]).shortId)
        assertEquals("g", revisionGraph.transform(path[1]).shortId)
        assertEquals("e", revisionGraph.transform(path[2]).shortId)
        assertEquals("b", revisionGraph.transform(path[3]).shortId)
        assertEquals("a", revisionGraph.transform(path[4]).shortId)
    }

    @Test
    fun testGetPathToRootDepthUnboundedMiddle() {
        val vertex = revisionGraph.getRevision("e")
        val path = revisionGraph.getPathToRoot(vertex, -1)
        print(path.map { revisionGraph.transform(it).shortId })
        assertEquals(3, path.size)
        assertEquals("e", revisionGraph.transform(path[0]).shortId)
        assertEquals("b", revisionGraph.transform(path[1]).shortId)
        assertEquals("a", revisionGraph.transform(path[2]).shortId)
    }

    /**
     *              /---> g --> p
     * a --> b --> e --> f
     * \---> c --> h ---\
     *  \ ============== > j ---> k
     *   \--> d --------/
     */
    @Test
    fun testGetPathToRootUnboundedMergeTraversal() {
        val vertex = revisionGraph.getRevision("k")
        val path = revisionGraph.getPathToRoot(vertex, -1)
        print(path.map { revisionGraph.transform(it).shortId })
        assertEquals(3, path.size)
        assertEquals("k", revisionGraph.transform(path[0]).shortId)
        assertEquals("j", revisionGraph.transform(path[1]).shortId)
        assertEquals("a", revisionGraph.transform(path[2]).shortId)
    }

    @Test
    fun testGetPathToRootUnboundedMergeTraversalBounded() {
        val vertex = revisionGraph.getRevision("j")
        val path = revisionGraph.getPathToRoot(vertex, 1)
        print(path.map { revisionGraph.transform(it).shortId })
        assertEquals(2, path.size)
        assertEquals("j", revisionGraph.transform(path[0]).shortId)
        assertEquals("a", revisionGraph.transform(path[1]).shortId)
    }

}