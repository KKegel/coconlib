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

data class GraphDescription(
    val id: String,
    val vertices: List<VertexDescription>,
    val edges: List<EdgeDescription>
) {

    init {
        assert(id.isNotEmpty()) { "ID must not be empty" }
        assert(id.contains(";").not()) { "ID must not contain semicolon" }
    }

    fun serialize(): String {
        return serialize(this)
    }

    companion object {

        fun serialize(graphDescription: GraphDescription): String {
            return "G;${graphDescription.id};\n" +
                    graphDescription.vertices.joinToString("\n") { VertexDescription.serialize(it) } + "\n" +
                    graphDescription.edges.joinToString("\n") { EdgeDescription.serialize(it) } + "\n"
        }

        fun parse(serialized: String): GraphDescription {
            assert(serialized.startsWith("G;")) { "Serialized string must have GRAPH format" }

            val parts = serialized.split("\n")
            assert(parts[0].startsWith("G;"))
            val id = parts[0].split(";")[1]

            var vertices = emptyList<VertexDescription>()
            var edges = emptyList<EdgeDescription>()

            if (parts.find { it.startsWith("V;") } != null) {
                vertices = parts.subList(
                    parts.indexOfFirst { it.startsWith("V;") },
                    parts.indexOfLast { it.startsWith("V;")} + 1 ).map {
                    VertexDescription.parse(it)
                }
            }
            if (parts.find { it.startsWith("E;") } != null) {
                edges = parts.subList(
                    parts.indexOfFirst { it.startsWith("E;") },
                    parts.indexOfLast { it.startsWith("E;")} + 1 ).map {
                    EdgeDescription.parse(it)
                }
            }

            return GraphDescription(id, vertices, edges)
        }

    }

}
