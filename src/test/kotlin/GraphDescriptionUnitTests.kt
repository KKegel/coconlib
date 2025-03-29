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
import graphcore.RevisionDescription
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class GraphDescriptionUnitTests {

    @Test
    fun testSerializeParse() {
        val vertex1 = RevisionDescription("graph1", "v1", "vertex1", "location1")
        val vertex2 = RevisionDescription("graph1", "v2", "vertex2", "location2")
        val edge = EdgeDescription("v1", "v2", EdgeLabel.SUCCESSOR)

        val graph = GraphDescription("graph1", listOf(vertex1, vertex2), listOf(edge))
        val serialized = graph.serialize()
        val deserialized = GraphDescription.parse(serialized)

        assertEquals(graph, deserialized)
    }

    @Test
    fun testEmptyGraphId() {
        assertThrows<AssertionError> {
            GraphDescription("", listOf(), listOf())
        }
    }

    @Test
    fun testGraphIdSemicolonError() {
        assertThrows<AssertionError> {
            GraphDescription("graph;1", listOf(), listOf())
        }
    }

    @Test
    fun testEmptyGraph() {
        val graph = GraphDescription("graph1", listOf(), listOf())
        val serialized = graph.serialize()
        val deserialized = GraphDescription.parse(serialized)

        assertEquals(graph, deserialized)
    }

    @Test
    fun testNonEmptyGraph() {
        val vertex1 = RevisionDescription("graph1", "v1", "vertex1", "location1")
        val vertex2 = RevisionDescription("graph1", "v2", "vertex2", "location2")
        val vertex3 = RevisionDescription("graph1", "v3", "vertex3", "location3")
        val edge1 = EdgeDescription("v1", "v2", EdgeLabel.SUCCESSOR)
        val edge2 = EdgeDescription("v2", "v3", EdgeLabel.SUCCESSOR)

        val graph = GraphDescription("graph1", listOf(vertex1, vertex2, vertex3), listOf(edge1, edge2))
        val serialized = graph.serialize()
        val deserialized = GraphDescription.parse(serialized)

        assertEquals(graph, deserialized)
    }

}