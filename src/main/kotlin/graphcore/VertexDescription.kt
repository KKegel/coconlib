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

package graphcore

data class VertexDescription(
    val graph: String,
    val shortId: String,
    val longId: String,
    val location: String,
    val payload: String = ""
){

    init {
        assert(graph.isNotEmpty()) { "Graph must not be empty" }
        assert(shortId.isNotEmpty()) { "Short ID must not be empty" }
        assert(longId.isNotEmpty()) { "Long ID must not be empty" }
        assert(location.isNotEmpty()) { "Location must not be empty" }
        assert(graph.contains(";").not()) { "Graph must not contain semicolon" }
        assert(shortId.contains(";").not()) { "Short ID must not contain semicolon" }
        assert(longId.contains(";").not()) { "Long ID must not contain semicolon" }
        assert(location.contains(";").not()) { "Location must not contain semicolon" }
        assert(payload.contains(";").not()) { "Payload must not contain semicolon" }
    }

    fun serialize(): String {
        return serialize(this)
    }

    companion object {

        fun serialize(vertexDescription: VertexDescription): String {
            return "V;${vertexDescription.graph};${vertexDescription.shortId};" +
                    "${vertexDescription.longId};${vertexDescription.location};${vertexDescription.payload}"
        }

        fun parse(serialized: String): VertexDescription {
            assert(serialized.startsWith("V;")) { "Serialized string must have VERTEX format" }
            assert(serialized.count { it == ';' } == 5) { "Serialized string must have 5 entries" }

            val parts = serialized.split(";")
            return VertexDescription(parts[1], parts[2], parts[3], parts[4], parts[5])
        }

    }
}