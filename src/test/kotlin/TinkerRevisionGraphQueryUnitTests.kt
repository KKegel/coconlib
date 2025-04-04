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

import coconlib.graph.EdgeDescription
import coconlib.graph.EdgeLabel
import coconlib.graph.GraphDescription
import coconlib.graph.RevisionDescription
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import coconlib.core.TinkerRevisionGraph

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
        revisionGraph = TinkerRevisionGraph.Companion.build(
            GraphDescription("g1", listOf(
                RevisionDescription("g1", "a", "A", "./a"),
                RevisionDescription("g1", "b", "B", "./b"),
                RevisionDescription("g1", "c", "C", "./c"),
                RevisionDescription("g1", "d", "D", "./d"),
                RevisionDescription("g1", "e", "E", "./e"),
                RevisionDescription("g1", "f", "F", "./f"),
                RevisionDescription("g1", "g", "G", "./g"),
                RevisionDescription("g1", "h", "H", "./h"),
                RevisionDescription("g1", "j", "J", "./j"),
                RevisionDescription("g1", "k", "K", "./k"),
                RevisionDescription("g1", "p", "P", "./p")
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
        assertEquals("a", revisionGraph.transform(root).revId)
    }

   @Test
   fun getLeafRevisionsTest() {
        val leafs = revisionGraph.getLeafRevisions()
        assertEquals(3, leafs.size)
        assertTrue(leafs.map { revisionGraph.transform(it).revId }.containsAll(listOf("f", "k", "p")))
   }

    /**
     * a --> b --> e --> f
     */
    @Test
    fun testGetPathToRootDepth0() {
        val vertex = revisionGraph.getRevision("f")
        val path = revisionGraph.getPathToRoot(vertex, 0)
        assertEquals(1, path.size)
        assertEquals("f", revisionGraph.transform(path[0]).revId)
    }

    @Test
    fun testGetPathToRootDepth1() {
        val vertex = revisionGraph.getRevision("f")
        val path = revisionGraph.getPathToRoot(vertex, 1)
        print(path.map { revisionGraph.transform(it).revId })
        assertEquals(2, path.size)
        assertEquals("f", revisionGraph.transform(path[0]).revId)
        assertEquals("e", revisionGraph.transform(path[1]).revId)
    }

    @Test
    fun testGetPathToRootDepth2() {
        val vertex = revisionGraph.getRevision("f")
        val path = revisionGraph.getPathToRoot(vertex, 2)
        print(path.map { revisionGraph.transform(it).revId })
        assertEquals(3, path.size)
        assertEquals("f", revisionGraph.transform(path[0]).revId)
        assertEquals("e", revisionGraph.transform(path[1]).revId)
        assertEquals("b", revisionGraph.transform(path[2]).revId)
    }

    @Test
    fun testGetPathToRootDepthUnbounded() {
        val vertex = revisionGraph.getRevision("f")
        val path = revisionGraph.getPathToRoot(vertex, -1)
        print(path.map { revisionGraph.transform(it).revId })
        assertEquals(4, path.size)
        assertEquals("f", revisionGraph.transform(path[0]).revId)
        assertEquals("e", revisionGraph.transform(path[1]).revId)
        assertEquals("b", revisionGraph.transform(path[2]).revId)
        assertEquals("a", revisionGraph.transform(path[3]).revId)
    }

    @Test
    fun testGetPathToRootDepthUnbounded2() {
        val vertex = revisionGraph.getRevision("p")
        val path = revisionGraph.getPathToRoot(vertex, -1)
        print(path.map { revisionGraph.transform(it).revId })
        assertEquals(5, path.size)
        assertEquals("p", revisionGraph.transform(path[0]).revId)
        assertEquals("g", revisionGraph.transform(path[1]).revId)
        assertEquals("e", revisionGraph.transform(path[2]).revId)
        assertEquals("b", revisionGraph.transform(path[3]).revId)
        assertEquals("a", revisionGraph.transform(path[4]).revId)
    }

    @Test
    fun testGetPathToRootDepthUnboundedMiddle() {
        val vertex = revisionGraph.getRevision("e")
        val path = revisionGraph.getPathToRoot(vertex, -1)
        print(path.map { revisionGraph.transform(it).revId })
        assertEquals(3, path.size)
        assertEquals("e", revisionGraph.transform(path[0]).revId)
        assertEquals("b", revisionGraph.transform(path[1]).revId)
        assertEquals("a", revisionGraph.transform(path[2]).revId)
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
        print(path.map { revisionGraph.transform(it).revId })
        assertEquals(3, path.size)
        assertEquals("k", revisionGraph.transform(path[0]).revId)
        assertEquals("j", revisionGraph.transform(path[1]).revId)
        assertEquals("a", revisionGraph.transform(path[2]).revId)
    }

    @Test
    fun testGetPathToRootUnboundedMergeTraversalBounded() {
        val vertex = revisionGraph.getRevision("j")
        val path = revisionGraph.getPathToRoot(vertex, 1)
        print(path.map { revisionGraph.transform(it).revId })
        assertEquals(2, path.size)
        assertEquals("j", revisionGraph.transform(path[0]).revId)
        assertEquals("a", revisionGraph.transform(path[1]).revId)
    }

    /**
     *              /---> g --> p
     * a --> b --> e --> f
     * \---> c --> h ---\
     *  \ ============== > j ---> k
     *   \--> d --------/
     */
    @Test
    fun getLeastCommonAncestorTest() {
        val lca1 = revisionGraph.getLeastCommonAncestor("f", "k")
        val lca2 = revisionGraph.getLeastCommonAncestor("k", "f")
        assertEquals("a", lca1.revId)
        assertEquals("a", lca2.revId)
    }

    @Test
    fun getLeastCommonAncestorTestShort() {
        val lca1 = revisionGraph.getLeastCommonAncestor("p", "f")
        val lca2 = revisionGraph.getLeastCommonAncestor("f", "p")
        assertEquals("e", lca1.revId)
        assertEquals("e", lca2.revId)
    }

    @Test
    fun getLeastCommonAncestorTestIdentity() {
        val lca1 = revisionGraph.getLeastCommonAncestor("f", "f")
        assertEquals("f", lca1.revId)
    }

    @Test
    fun testAddRevisionWithUnification(){
        val newRevision = RevisionDescription("g1", "x", "X", "./x")
        val edgeA = EdgeDescription("p", "x", EdgeLabel.SUCCESSOR)
        val edgeB = EdgeDescription("f", "x", EdgeLabel.SUCCESSOR)
        revisionGraph.addRevisionWithUnification(newRevision, edgeA, edgeB)
        assertTrue(revisionGraph.hasRevision(newRevision))
        val pathX = revisionGraph.getPathToRoot(revisionGraph.getRevision("x"), -1)
        assertEquals(4, pathX.size)
        assertEquals(setOf("a", "b", "e", "x"), pathX.map { revisionGraph.transform(it).revId }.toSet())
    }

    /**
     *              /---> g --> p
     * a --> b --> e --> f
     * \---> c --> h ---\
     *  \ ============== > j ---> k
     *   \--> d --------/
     */
    @Test
    fun getNeighbours0BoundedTest() {
        val vertex = revisionGraph.getRevision("p")
        val neighbours = revisionGraph.getNeighbors(vertex, 0)
        assertEquals(1, neighbours.size)
        assertTrue(neighbours.map { revisionGraph.transform(it).revId }.contains("p"))
    }

    @Test
    fun getNeighbours1BoundedTest() {
        val vertex = revisionGraph.getRevision("p")
        val neighbours = revisionGraph.getNeighbors(vertex, 1)
        println(neighbours.map { revisionGraph.transform(it).revId })
        assertEquals(1, neighbours.size)
        assertTrue(neighbours.map { revisionGraph.transform(it).revId }.contains("p"))
    }

    @Test
    fun getNeighbours2BoundedTest() {
        val vertex = revisionGraph.getRevision("p")
        val neighbours = revisionGraph.getNeighbors(vertex, 2)
        assertEquals(2, neighbours.size)
        assertTrue(neighbours.map { revisionGraph.transform(it).revId }.containsAll(listOf("f", "p")))
    }

    @Test
    fun getNeighboursUnboundedTest() {
        val vertex = revisionGraph.getRevision("p")
        val neighbours = revisionGraph.getNeighbors(vertex, -1)
        println(neighbours.map { revisionGraph.transform(it).revId })
        assertEquals(3, neighbours.size)
        assertTrue(neighbours.map { revisionGraph.transform(it).revId }.containsAll(listOf("f", "k", "p")))
    }

}