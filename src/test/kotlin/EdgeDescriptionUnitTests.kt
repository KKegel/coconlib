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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class EdgeDescriptionUnitTests {

    @Test
    fun testInit() {
        val edge = EdgeDescription("source1", "target1", EdgeLabel.SUCCESSOR)
        assertEquals("source1", edge.sourceShortId)
        assertEquals("target1", edge.targetShortId)
        assertEquals(EdgeLabel.SUCCESSOR, edge.label)
    }

    @Test
    fun testInitErrorSourceShortIdEmpty() {
        assertThrows<AssertionError> { EdgeDescription("", "target1", EdgeLabel.SUCCESSOR) }
    }

    @Test
    fun testInitErrorTargetShortIdEmpty() {
        assertThrows<AssertionError> { EdgeDescription("source1", "", EdgeLabel.SUCCESSOR) }
    }

    @Test
    fun testInitErrorSourceShortIdContainsSemicolon() {
        assertThrows<AssertionError> { EdgeDescription("source;1", "target1", EdgeLabel.SUCCESSOR) }
    }

    @Test
    fun testInitErrorTargetShortIdContainsSemicolon() {
        assertThrows<AssertionError> { EdgeDescription("source1", "target;1", EdgeLabel.SUCCESSOR) }
    }

    @Test
    fun testSerialize() {
        val edge = EdgeDescription("source1", "target1", EdgeLabel.SUCCESSOR)
        val serialized = edge.serialize()
        assertEquals("E;source1;target1;SUCCESSOR", serialized)
    }
    @Test
    fun testParse() {
        val serialized = "E;source1;target1;SUCCESSOR"
        val edge = EdgeDescription.parse(serialized)
        assertEquals("source1", edge.sourceShortId)
        assertEquals("target1", edge.targetShortId)
        assertEquals(EdgeLabel.SUCCESSOR, edge.label)
    }

    @Test
    fun testParseErrorTooShort() {
        val serialized = "E;source1;target1"
        assertThrows<AssertionError> { EdgeDescription.parse(serialized) }
    }

    @Test
    fun testParseErrorTooLong() {
        val serialized = "E;source1;target1;LABEL1;extra"
        assertThrows<AssertionError> { EdgeDescription.parse(serialized) }
    }

    @Test
    fun testParseErrorWrongPrefix() {
        val serialized = "X;source1;target1;LABEL1"
        assertThrows<AssertionError> { EdgeDescription.parse(serialized) }
    }

}