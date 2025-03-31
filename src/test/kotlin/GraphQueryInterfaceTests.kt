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

import coconlib.context.Context
import coconlib.context.ContextType
import coconlib.graph.EdgeDescription
import coconlib.graph.EdgeLabel
import coconlib.graph.GraphDescription
import coconlib.graph.RevisionDescription
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import coconlib.core.GraphQueryInterface
import coconlib.core.TinkerRevisionGraph

class GraphQueryInterfaceTests {

    lateinit var graphQueryInterface: GraphQueryInterface
    lateinit var revisions : List<RevisionDescription>
    lateinit var edges : List<EdgeDescription>
    lateinit var rootRevision : RevisionDescription

    /**
     *              /---> g --> p
     * a --> b --> e --> f
     * \---> c --> h ---\
     *  \ ============== > j ---> k
     *   \--> d --------/
     */
    @BeforeEach
    fun setUp() {
        revisions = listOf(
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
        )
        edges = listOf(
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
            EdgeDescription("a", "j", EdgeLabel.MERGE)
        )

        val revisionGraph = TinkerRevisionGraph.Companion.build(GraphDescription("g1", revisions, edges))
        rootRevision = revisions.first()
        graphQueryInterface = GraphQueryInterface(revisionGraph)
    }

    @Test
    fun testGetRoot() {
        assertEquals(rootRevision, graphQueryInterface.getRoot())
    }

    @Test
    fun testGetEdges() {
        assertEquals(edges.toSet(), graphQueryInterface.getEdges().toSet())
    }

    @Test
    fun testGetRevisions() {
        assertEquals(revisions.toSet(), graphQueryInterface.getRevisions().toSet())
    }

    /**
     *              /---> g --> p
     * a --> b --> e --> f
     * \---> c --> h ---\
     *  \ ============== > j ---> k
     *   \--> d --------/
     */
    @Test
    fun testFindContextUnboundedTime() {
        val context = graphQueryInterface.findContext("f", ContextType.TIME, Context.UNBOUNDED)
        assertEquals(ContextType.TIME, context.contextType)
        assertEquals(Int.MAX_VALUE, context.cardinality)
        assertEquals(
            setOf("f", "e", "b", "a"),
            context.participants.map { it.revId }.toSet()
        )
    }

    /**
     *              /---> g --> p
     * a --> b --> e --> f
     * \---> c --> h ---\
     *  \ ============== > j ---> k
     *   \--> d --------/
     */
    @Test
    fun testFindContextBoundedTime() {
        val context = graphQueryInterface.findContext("f", ContextType.TIME, 2)
        assertEquals(ContextType.TIME, context.contextType)
        assertEquals(2, context.cardinality)
        assertEquals(
            setOf("f", "e", "b"),
            context.participants.map { it.revId }.toSet()
        )
    }

    /**
     *              /---> g --> p
     * a --> b --> e --> f
     * \---> c --> h ---\
     *  \ ============== > j ---> k
     *   \--> d --------/
     */
    @Test
    fun testFindContextUnboundedTimeMergeTraversal() {
        val context = graphQueryInterface.findContext("k", ContextType.TIME, Context.UNBOUNDED)
        assertEquals(ContextType.TIME, context.contextType)
        assertEquals(Int.MAX_VALUE, context.cardinality)
        assertEquals(
            setOf("a", "j", "k"),
            context.participants.map { it.revId }.toSet()
        )
    }

    /**
     *              /---> g --> p
     * a --> b --> e --> f
     * \---> c --> h ---\
     *  \ ============== > j ---> k
     *   \--> d --------/
     */
    @Test
    fun testFindContextUnboundedSpace() {
        val context = graphQueryInterface.findContext("p", ContextType.SPACE, Context.UNBOUNDED)
        assertEquals(ContextType.SPACE, context.contextType)
        assertEquals(Int.MAX_VALUE, context.cardinality)
        assertEquals(
            setOf("f", "k", "p"),
            context.participants.map { it.revId }.toSet()
        )
    }

    /**
     *              /---> g --> p
     * a --> b --> e --> f
     * \---> c --> h ---\
     *  \ ============== > j ---> k
     *   \--> d --------/
     */
    @Test
    fun testFindContextBoundedSpace0() {
        val context = graphQueryInterface.findContext("p", ContextType.SPACE, 0)
        assertEquals(ContextType.SPACE, context.contextType)
        assertEquals(0, context.cardinality)
        assertEquals(
            setOf("p"),
            context.participants.map { it.revId }.toSet()
        )
    }

    /**
     *              /---> g --> p
     * a --> b --> e --> f
     * \---> c --> h ---\
     *  \ ============== > j ---> k
     *   \--> d --------/
     */
    @Test
    fun testFindContextBoundedSpace1() {
        val context = graphQueryInterface.findContext("p", ContextType.SPACE, 1)
        assertEquals(ContextType.SPACE, context.contextType)
        assertEquals(1, context.cardinality)
        assertEquals(
            setOf("p"),
            context.participants.map { it.revId }.toSet()
        )
    }

    /**
     *              /---> g --> p
     * a --> b --> e --> f
     * \---> c --> h ---\
     *  \ ============== > j ---> k
     *   \--> d --------/
     */
    @Test
    fun testFindContextBoundedSpace2() {
        val context = graphQueryInterface.findContext("p", ContextType.SPACE, 2)
        assertEquals(ContextType.SPACE, context.contextType)
        assertEquals(2, context.cardinality)
        assertEquals(
            setOf("p", "f"),
            context.participants.map { it.revId }.toSet()
        )
    }


}