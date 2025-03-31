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

import coconlib.core.TinkerRevisionGraph
import coconlib.system.Relation
import coconlib.system.SystemDescription
import kotlin.test.Test
import kotlin.test.assertEquals

class SystemDescriptionUnitTests {

    val systemDescriptionString = """
            GRAPHS
            G;g1
            V;g1;1a;2aaa;C:/a;
            V;g1;1b;2bbb;C:/b;
            V;g1;1c;2ccc;C:/c;
            E;1a;1b;SUCCESSOR
            E;1a;1c;SUCCESSOR
            G;g2
            V;g2;2a;2aaa;C:/a;
            V;g2;2b;2bbb;C:/b;
            V;g2;2c;2ccc;C:/c;
            E;2a;2b;SUCCESSOR
            E;2b;2c;SUCCESSOR
            RELATIONS
            L;g1;g2;1a;2a;some constraint
            L;g1;g2;1b;2b;some other constraint
            PROJECTIONS
            P;MyView;g1a,g2b;x/y/z
        """.trimIndent()

    @Test
    fun testBasicParse() {
        val systemDescription = SystemDescription.parse(systemDescriptionString, TinkerRevisionGraph::build)
        assertEquals(2, systemDescription.parts.size)
        assertEquals(2, systemDescription.relations.size)
        assertEquals(1, systemDescription.projections.size)

        assertEquals("MyView", systemDescription.projections.first().projectionId)
        assertEquals(Relation("g1", "g2", "1a", "2a", "some constraint"),
            systemDescription.relations.find { it.fromRevision == "1a" && it.toRevision == "2a" }!!)

        val graph1 = systemDescription.parts.find { it.graphId == "g1" }!!
        val graph2 = systemDescription.parts.find { it.graphId == "g2" }!!

        assertEquals(3, graph1.getRevisions().size)
        assertEquals(3, graph2.getRevisions().size)
        assertEquals("1a", graph1.transform(graph1.getRootRevision()).revId)
        assertEquals(2, graph1.getEdges().size)
        assertEquals(2, graph2.getEdges().size)
    }

    @Test
    fun testBasicSerialize() {
        val systemDescription = SystemDescription.parse(systemDescriptionString, TinkerRevisionGraph::build)
        val serialized = systemDescription.serialize()
        assertEquals(systemDescriptionString, serialized)
    }

}