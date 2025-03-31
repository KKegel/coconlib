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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import coconlib.core.TinkerRevisionGraph
import kotlin.test.assertTrue

class TinkerRevisionGraphValidationUnitTests {

    private lateinit var validGraph: TinkerRevisionGraph


    @BeforeEach
            /**
             * a --> b --> e --> f --> g
             *       \---> c --/
             */
    fun setUp() {
        validGraph = TinkerRevisionGraph.Companion.build(
            GraphDescription("g1", listOf(
                RevisionDescription("g1", "a", "A", "./a"),
                RevisionDescription("g1", "b", "B", "./b"),
                RevisionDescription("g1", "c", "C", "./c"),
                RevisionDescription("g1", "e", "E", "./e"),
                RevisionDescription("g1", "f", "F", "./f"),
                RevisionDescription("g1", "g", "G", "./g"),

            ), listOf(
                EdgeDescription("a", "b", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "c", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "e", EdgeLabel.SUCCESSOR),
                EdgeDescription("c", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("e", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("f", "g", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "f", EdgeLabel.MERGE),
            )))
    }

    @Test
    fun testCyclicGraph() {
        val revisionGraphCyclic5 = TinkerRevisionGraph.Companion.build(
            GraphDescription("g1", listOf(
                RevisionDescription("g1", "a", "A", "./a"),
                RevisionDescription("g1", "b", "B", "./b"),
                RevisionDescription("g1", "c", "C", "./c"),
                RevisionDescription("g1", "d", "D", "./d"),
                RevisionDescription("g1", "e", "E", "./e"),
                RevisionDescription("g1", "f", "F", "./f"),
                RevisionDescription("g1", "g", "G", "./g"),

                ), listOf(
                EdgeDescription("a", "b", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "c", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "e", EdgeLabel.SUCCESSOR),
                EdgeDescription("c", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("e", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("f", "g", EdgeLabel.SUCCESSOR),
                EdgeDescription("g", "a", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "f", EdgeLabel.MERGE),
            )))
        assertTrue(revisionGraphCyclic5.hasCycles(5))
    }

    @Test
    fun testCyclicGraph2() {
        val revisionGraphCyclic5 = TinkerRevisionGraph.Companion.build(
            GraphDescription("g1", listOf(
                RevisionDescription("g1", "a", "A", "./a"),
                RevisionDescription("g1", "b", "B", "./b"),
                RevisionDescription("g1", "c", "C", "./c"),
                RevisionDescription("g1", "d", "D", "./d"),
                RevisionDescription("g1", "e", "E", "./e"),
                RevisionDescription("g1", "f", "F", "./f"),
                RevisionDescription("g1", "g", "G", "./g"),

                ), listOf(
                EdgeDescription("a", "b", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "c", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "e", EdgeLabel.SUCCESSOR),
                EdgeDescription("c", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("e", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("f", "g", EdgeLabel.SUCCESSOR),
                EdgeDescription("g", "a", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "f", EdgeLabel.MERGE),
            )))
        assertFalse(revisionGraphCyclic5.hasCycles(3))
    }

    @Test
    fun testCyclesValidGraph() {
        assertFalse(validGraph.hasCycles(10))
    }

    @Test
    fun testIsConnectedValidGraph() {
        val miniValidGraph = TinkerRevisionGraph.Companion.build(
            GraphDescription("g2", listOf(
                RevisionDescription("g2", "a", "A", "./a"),
                RevisionDescription("g2", "b", "B", "./b"),
                RevisionDescription("g2", "c", "C", "./c"),
                RevisionDescription("g2", "e", "E", "./e")
                ), listOf(
                EdgeDescription("a", "b", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "c", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "e", EdgeLabel.SUCCESSOR)
            )))
        assertTrue(miniValidGraph.hasOnlyOneRoot())
    }

    @Test
    fun testIsConnectedInvalidGraph() {
        validGraph.removeEdge(EdgeDescription("b", "c", EdgeLabel.SUCCESSOR))
        assertFalse(validGraph.hasOnlyOneRoot())
    }

    @Test
    fun testHasOnlyOneRoot() {
        assertTrue(validGraph.hasOnlyOneRoot())
    }

    @Test
    fun testHasOnlyOneRootInvalid() {
        val invalidGraph = TinkerRevisionGraph.Companion.build(
            GraphDescription("g1", listOf(
                RevisionDescription("g1", "a", "A", "./a"),
                RevisionDescription("g1", "b", "B", "./b"),
                RevisionDescription("g1", "c", "C", "./c"),
                RevisionDescription("g1", "e", "E", "./e"),
                RevisionDescription("g1", "f", "F", "./f")

                ), listOf(
                EdgeDescription("a", "b", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "c", EdgeLabel.SUCCESSOR),
                EdgeDescription("c", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("e", "f", EdgeLabel.SUCCESSOR),
                EdgeDescription("b", "f", EdgeLabel.MERGE),
            )))

        assertFalse(invalidGraph.hasOnlyOneRoot())
    }

    @Test
    fun testMaxTwoIncomingSuccessors() {
        assertTrue(validGraph.maxTwoIncomingSuccessors())
    }

    @Test
    fun testMaxTwoIncomingSuccessorsInvalid() {
        validGraph.addEdge(EdgeDescription("a", "c", EdgeLabel.SUCCESSOR))
        validGraph.addEdge(EdgeDescription("f", "c", EdgeLabel.SUCCESSOR))
        assertFalse(validGraph.maxTwoIncomingSuccessors())
    }

    @Test
    fun testMaxOneIncomingMerge() {
        assertTrue(validGraph.maxOneIncomingMerge())
    }

    @Test
    fun testMaxOneIncomingMergeInvalid() {
        validGraph.addEdge(EdgeDescription("a", "f", EdgeLabel.MERGE))
        assertFalse(validGraph.maxOneIncomingMerge())
    }

    @Test
    fun testTwoIncomingSuccessorsRequiresMerge() {
        assertTrue(validGraph.twoIncomingSuccessorsRequiresMerge())
    }

    @Test
    fun testTwoIncomingSuccessorsRequiresMergeInvalid() {
        validGraph.removeEdge(EdgeDescription("b", "f", EdgeLabel.MERGE))
        assertFalse(validGraph.twoIncomingSuccessorsRequiresMerge())
    }


}
