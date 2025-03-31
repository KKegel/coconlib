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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import coconlib.system.Projection

class ProjectionUnitTests {

    @Test
    fun testParse() {
        val serialized = "P;g1;a,b,c,d;view"
        val projection = Projection.parse(serialized)
        assertEquals("g1", projection.projectionId)
        assertEquals(listOf("a", "b", "c", "d"), projection.sources)
        assertEquals("view", projection.target)
    }

    @Test
    fun testParseWithSpacing() {
        val serialized = "P;g1   ;a  ,b,c,  d;    view"
        val projection = Projection.parse(serialized)
        assertEquals("g1", projection.projectionId)
        assertEquals(listOf("a", "b", "c", "d"), projection.sources)
        assertEquals("view", projection.target)
    }

    @Test
    fun testParseErrorTooShort() {
        val serialized = "P;g1;a,b,c,d"
        assertThrows<AssertionError> { Projection.parse(serialized) }
    }

    @Test
    fun testParseErrorInvalidFormat() {
        val serialized = "P;g1;a,b,c,d;view;extra"
        assertThrows<AssertionError> { Projection.parse(serialized) }
    }

    @Test
    fun testSerialize() {
        val projection = Projection("g1", listOf("a", "b", "c", "d"), "view")
        val serialized = projection.serialize()
        assertEquals("P;g1;a,b,c,d;view", serialized)
    }

}