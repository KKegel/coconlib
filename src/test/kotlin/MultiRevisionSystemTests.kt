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
import core.MultiRevisionSystem
import core.RevisionGraph
import core.TinkerRevisionGraph
import graph.EdgeDescription
import graph.EdgeLabel
import graph.GraphDescription
import graph.RevisionDescription
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import system.Projection
import system.Relation
import kotlin.test.assertEquals

class MultiRevisionSystemTests {


    lateinit var revisionGraphX: RevisionGraph
    lateinit var revisionGraphY: RevisionGraph
    lateinit var projections: List<Projection>
    lateinit var relations: List<Relation>
    lateinit var system: MultiRevisionSystem


    @BeforeEach
    fun setUp() {
        /*
         * X
         *              /---> g --> p
         * a --> b --> e --> f
         * \---> c --> h ---\
         *  \ ============== > j ---> k
         *   \--> d --------/
         *
         * Y
         *             /---> x
         * a --> b --> c --> y
         *             \---> z
         *
         * LINKS: x -> k , p -> z
         * PROJECTIONS: A = X.a + Y.a
         *              P = X.f
        */
        val revisionsX = listOf(
            RevisionDescription("X", "X.a", "X.A", "./a"),
            RevisionDescription("X", "X.b", "X.B", "./b"),
            RevisionDescription("X", "X.c", "X.C", "./c"),
            RevisionDescription("X", "X.d", "X.D", "./d"),
            RevisionDescription("X", "X.e", "X.E", "./e"),
            RevisionDescription("X", "X.f", "X.F", "./f"),
            RevisionDescription("X", "X.g", "X.G", "./g"),
            RevisionDescription("X", "X.h", "X.H", "./h"),
            RevisionDescription("X", "X.j", "X.J", "./j"),
            RevisionDescription("X", "X.k", "X.K", "./k"),
            RevisionDescription("X", "X.p", "X.P", "./p")
        )
        val edgesX = listOf(
            EdgeDescription("X.a", "X.b", EdgeLabel.SUCCESSOR),
            EdgeDescription("X.a", "X.c", EdgeLabel.SUCCESSOR),
            EdgeDescription("X.a", "X.d", EdgeLabel.SUCCESSOR),
            EdgeDescription("X.b", "X.e", EdgeLabel.SUCCESSOR),
            EdgeDescription("X.c", "X.h", EdgeLabel.SUCCESSOR),
            EdgeDescription("X.h", "X.j", EdgeLabel.SUCCESSOR),
            EdgeDescription("X.d", "X.j", EdgeLabel.SUCCESSOR),
            EdgeDescription("X.j", "X.k", EdgeLabel.SUCCESSOR),
            EdgeDescription("X.e", "X.f", EdgeLabel.SUCCESSOR),
            EdgeDescription("X.e", "X.g", EdgeLabel.SUCCESSOR),
            EdgeDescription("X.g", "X.p", EdgeLabel.SUCCESSOR),
            EdgeDescription("X.a", "X.j", EdgeLabel.MERGE)
        )
        val revisionsY = listOf(
            RevisionDescription("Y", "Y.a", "y.A", "./a"),
            RevisionDescription("Y", "Y.b", "y.B", "./b"),
            RevisionDescription("Y", "Y.c", "y.C", "./c"),
            RevisionDescription("Y", "Y.x", "y.X", "./x"),
            RevisionDescription("Y", "Y.y", "y.Y", "./y"),
            RevisionDescription("Y", "Y.z", "y.Z", "./z")
        )
        val edgesY = listOf(
            EdgeDescription("Y.a", "Y.b", EdgeLabel.SUCCESSOR),
            EdgeDescription("Y.b", "Y.c", EdgeLabel.SUCCESSOR),
            EdgeDescription("Y.c", "Y.x", EdgeLabel.SUCCESSOR),
            EdgeDescription("Y.c", "Y.y", EdgeLabel.SUCCESSOR),
            EdgeDescription("Y.c", "Y.z", EdgeLabel.SUCCESSOR)
        )

        revisionGraphX = TinkerRevisionGraph.build(GraphDescription("X", revisionsX, edgesX))
        revisionGraphY = TinkerRevisionGraph.build(GraphDescription("Y", revisionsY, edgesY))

        projections = listOf(
            Projection("A", listOf("X.a", "Y.a"), "A"),
        )

        relations = listOf(
            Relation("Y", "X", "Y.x", "X.k"),
            Relation("X", "Y", "X.p", "Y.z")
        )

        system = MultiRevisionSystem(
            setOf(revisionGraphX, revisionGraphY).toMutableSet(),
            relations.toMutableSet(),
            projections.toMutableSet()
        )
    }

    @Test
    fun testGetRevisionGraphs() {
        val graphs: Set<RevisionGraph> = setOf(revisionGraphX, revisionGraphY)
        assertEquals(graphs, system.getRevisionGraphs())
    }

    @Test
    fun testGetRelations() {
        val expectedRelations = relations.toSet()
        assertEquals(expectedRelations, system.getRelations())
    }

    @Test
    fun testGetProjections() {
        val expectedProjections = projections.toSet()
        assertEquals(expectedProjections, system.getProjections())
    }


    /*
    * X
    *              /---> g --> p
    * a --> b --> e --> f
    * \---> c --> h ---\
    *  \ ============== > j ---> k
    *   \--> d --------/
    *
    * Y
    *             /---> x
    * a --> b --> c --> y
    *             \---> z
    *
    * LINKS: x -> k , p -> z
    * PROJECTIONS: A = X.a + Y.a
    *              P = X.f
    */
    @Test
    fun testGetSpaceContext(){
        val context = system.findLocalContext(revisionGraphX.graphId, "X.e", ContextType.SPACE, 1)
        assertEquals(ContextType.SPACE, context.contextType)
        assertEquals(1, context.cardinality)
        //println(context.participants.map { it.revId })
        assertEquals(3, context.participants.size)
        assertEquals(setOf("X.p", "X.f", "X.e"), context.participants.map { it.revId }.toSet())
    }

    /*
    * X
    *              /---> g --> p
    * a --> b --> e --> f
    * \---> c --> h ---\
    *  \ ============== > j ---> k
    *   \--> d --------/
    *
    * Y
    *             /---> x
    * a --> b --> c --> y
    *             \---> z
    *
    * LINKS: x -> k , p -> z
    * PROJECTIONS: A = X.a + Y.a
    *              P = X.f
    */
    @Test
    fun testGetTimeContext(){
        val context = system.findLocalContext(revisionGraphY.graphId, "Y.z", ContextType.TIME, Context.UNBOUNDED)
        assertEquals(ContextType.TIME, context.contextType)
        assertEquals(Int.MAX_VALUE, context.cardinality)
        assertEquals(4, context.participants.size)
        assertEquals(setOf("Y.a", "Y.b", "Y.c", "Y.z"), context.participants.map { it.revId }.toSet())
    }

    /*
    * X
    *              /---> g --> p
    * a --> b --> e --> f
    * \---> c --> h ---\
    *  \ ============== > j ---> k
    *   \--> d --------/
    *
    * Y
    *             /---> x
    * a --> b --> c --> y
    *             \---> z
    *
    * LINKS: x -> k , p -> z
    * PROJECTIONS: A = X.a + Y.a
    *              P = X.f
    */
    @Test
    fun testGetRelationalContext(){
        val context = system.findGlobalContext("X.p", ContextType.RELATIONAL)
        assertEquals(ContextType.RELATIONAL, context.contextType)
        assertEquals(0, context.cardinality)
        assertEquals(2, context.participants.size)
        assertEquals(setOf("X.p", "Y.z"), context.participants.map { it.revId }.toSet())
    }

    @Test
    fun testGetRelationalContextUnidirectional(){
        val context = system.findGlobalContext("Y.z", ContextType.RELATIONAL)
        assertEquals(ContextType.RELATIONAL, context.contextType)
        assertEquals(0, context.cardinality)
        assertEquals(1, context.participants.size)
        assertEquals(setOf("Y.z"), context.participants.map { it.revId }.toSet())
    }

    /*
    * X
    *              /---> g --> p
    * a --> b --> e --> f
    * \---> c --> h ---\
    *  \ ============== > j ---> k
    *   \--> d --------/
    *
    * Y
    *             /---> x
    * a --> b --> c --> y
    *             \---> z
    *
    * LINKS: x -> k , p -> z
    * PROJECTIONS: A = X.a + Y.a
    *              P = X.f
    */
    @Test
    fun testGetProjectiveContext(){
        val context = system.findGlobalContext("X.a", ContextType.PROJECTIVE)
        assertEquals(ContextType.PROJECTIVE, context.contextType)
        assertEquals(0, context.cardinality)
        assertEquals(1, context.participants.size)
        assertEquals(setOf("A"), context.participants.map { it.revId }.toSet())
    }

    @Test
    fun testGetProjectiveContextEmpty(){
        val context = system.findGlobalContext("X.b", ContextType.PROJECTIVE)
        assertEquals(ContextType.PROJECTIVE, context.contextType)
        assertEquals(0, context.cardinality)
        assertEquals(0, context.participants.size)
        assertEquals(setOf(), context.participants.map { it.revId }.toSet())
    }

}