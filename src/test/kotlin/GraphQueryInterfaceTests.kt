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
    fun testFindRegionUnboundedTime() {
        val region = graphQueryInterface.findRegion("f", RegionType.TIME, Region.UNBOUNDED)
        assertEquals(RegionType.TIME, region.regionType)
        assertEquals(Int.MAX_VALUE, region.cardinality)
        assertEquals(
            setOf("f", "e", "b", "a"),
            region.participants.map { it.revId }.toSet()
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
    fun testFindRegionBoundedTime() {
        val region = graphQueryInterface.findRegion("f", RegionType.TIME, 2)
        assertEquals(RegionType.TIME, region.regionType)
        assertEquals(2, region.cardinality)
        assertEquals(
            setOf("f", "e", "b"),
            region.participants.map { it.revId }.toSet()
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
    fun testFindRegionUnboundedTimeMergeTraversal() {
        val region = graphQueryInterface.findRegion("k", RegionType.TIME, Region.UNBOUNDED)
        assertEquals(RegionType.TIME, region.regionType)
        assertEquals(Int.MAX_VALUE, region.cardinality)
        assertEquals(
            setOf("a", "j", "k"),
            region.participants.map { it.revId }.toSet()
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
    fun testFindRegionUnboundedSpace() {
        val region = graphQueryInterface.findRegion("p", RegionType.SPACE, Region.UNBOUNDED)
        assertEquals(RegionType.SPACE, region.regionType)
        assertEquals(Int.MAX_VALUE, region.cardinality)
        assertEquals(
            setOf("f", "k", "p"),
            region.participants.map { it.revId }.toSet()
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
    fun testFindRegionBoundedSpace0() {
        val region = graphQueryInterface.findRegion("p", RegionType.SPACE, 0)
        assertEquals(RegionType.SPACE, region.regionType)
        assertEquals(0, region.cardinality)
        assertEquals(
            setOf("p"),
            region.participants.map { it.revId }.toSet()
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
    fun testFindRegionBoundedSpace1() {
        val region = graphQueryInterface.findRegion("p", RegionType.SPACE, 1)
        assertEquals(RegionType.SPACE, region.regionType)
        assertEquals(1, region.cardinality)
        assertEquals(
            setOf("p"),
            region.participants.map { it.revId }.toSet()
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
    fun testFindRegionBoundedSpace2() {
        val region = graphQueryInterface.findRegion("p", RegionType.SPACE, 2)
        assertEquals(RegionType.SPACE, region.regionType)
        assertEquals(2, region.cardinality)
        assertEquals(
            setOf("p", "f"),
            region.participants.map { it.revId }.toSet()
        )
    }


}