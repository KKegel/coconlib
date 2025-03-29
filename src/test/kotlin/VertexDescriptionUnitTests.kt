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

import graph.RevisionDescription
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class VertexDescriptionUnitTests {

    @Test
    fun testInit() {
        val vertex = RevisionDescription("graph1", "short1", "long1", "location1", "payload1")
        assertEquals("graph1", vertex.graph)
        assertEquals("short1", vertex.shortId)
        assertEquals("long1", vertex.longId)
        assertEquals("location1", vertex.location)
        assertEquals("payload1", vertex.payload)
    }

    @Test
    fun testInitErrorGraphEmpty() {
        assertThrows<AssertionError> { RevisionDescription("", "short1", "long1", "location1", "payload1") }
    }

    @Test
    fun testInitErrorShortIdEmpty() {
        assertThrows<AssertionError> { RevisionDescription("graph1", "", "long1", "location1", "payload1") }
    }

    @Test
    fun testInitErrorLongIdEmpty() {
        assertThrows<AssertionError> { RevisionDescription("graph1", "short1", "", "location1", "payload1") }
    }

    @Test
    fun testInitErrorLocationEmpty() {
        assertThrows<AssertionError> { RevisionDescription("graph1", "short1", "long1", "", "payload1") }
    }

    @Test
    fun testInitErrorGraphContainsSemicolon() {
        assertThrows<AssertionError> { RevisionDescription("graph;1", "short1", "long1", "location1", "payload1") }
    }

    @Test
    fun testInitErrorShortIdContainsSemicolon() {
        assertThrows<AssertionError> { RevisionDescription("graph1", "short;1", "long1", "location1", "payload1") }
    }

    @Test
    fun testInitErrorLongIdContainsSemicolon() {
        assertThrows<AssertionError> { RevisionDescription("graph1", "short1", "long;1", "location1", "payload1") }
    }

    @Test
    fun testInitErrorLocationContainsSemicolon() {
        assertThrows<AssertionError> { RevisionDescription("graph1", "short1", "long1", "location;1", "payload1") }
    }

    @Test
    fun testInitErrorPayloadContainsSemicolon() {
        assertThrows<AssertionError> { RevisionDescription("graph1", "short1", "long1", "location1", "payload;1") }
    }

    @Test
    fun testSerialize() {
        val vertex = RevisionDescription("graph1", "short1", "long1", "location1", "payload1")
        val serialized = vertex.serialize()
        assertEquals("V;graph1;short1;long1;location1;payload1", serialized)
    }

    @Test
    fun testParse() {
        val serialized = "V;graph1;short1;long1;location1;payload1"
        val vertex = RevisionDescription.parse(serialized)
        assertEquals("graph1", vertex.graph)
        assertEquals("short1", vertex.shortId)
        assertEquals("long1", vertex.longId)
        assertEquals("location1", vertex.location)
        assertEquals("payload1", vertex.payload)
    }

    @Test
    fun testDefaultPayload() {
        val vertex = RevisionDescription("graph1", "short1", "long1", "location1")
        assertEquals("", vertex.payload)
    }

    @Test
    fun testParseErrorTooShort() {
        val serialized = "V;graph1;short1;long1;location1"
        assertThrows<AssertionError> { RevisionDescription.parse(serialized) }
    }

    @Test
    fun testParseErrorTooLong() {
        val serialized = "V;graph1;short1;long1;location1;payload1;extra"
        assertThrows<AssertionError> { RevisionDescription.parse(serialized) }
    }

    @Test
    fun testParseErrorWrongPrefix() {
        val serialized = "X;graph1;short1;long1;location1;payload1"
        assertThrows<AssertionError> { RevisionDescription.parse(serialized) }
    }

}