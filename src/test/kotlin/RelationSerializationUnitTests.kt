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

import org.junit.jupiter.api.Test
import coconlib.system.Relation
import org.junit.jupiter.api.Assertions.assertEquals

class RelationSerializationUnitTests {

    @Test
    fun testParse() {
        val serialized = "L;g1;g2;a;b;payload"
        val relation = Relation.parse(serialized)
        assertEquals("g1", relation.fromGraph)
        assertEquals("g2", relation.toGraph)
        assertEquals("a", relation.fromRevision)
        assertEquals("b", relation.toRevision)
        assertEquals("payload", relation.payload)
    }

    @Test
    fun testParseNoPayload() {
        val serialized = "L;g1;g2;a;b;"
        val relation = Relation.parse(serialized)
        assertEquals("g1", relation.fromGraph)
        assertEquals("g2", relation.toGraph)
        assertEquals("a", relation.fromRevision)
        assertEquals("b", relation.toRevision)
        assertEquals("", relation.payload)
    }

    @Test
    fun testSerialize() {
        val relation = Relation("g1", "g2", "a", "b", "payload")
        val serialized = relation.serialize()
        assertEquals("L;g1;g2;a;b;payload", serialized)
    }

}