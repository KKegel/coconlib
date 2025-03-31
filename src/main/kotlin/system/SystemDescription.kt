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

package system

import graph.GraphDescription
import core.RevisionGraph

data class SystemDescription(val parts: Set<RevisionGraph>,
                             val relations: Set<Relation>,
                             val projections: Set<Projection>) {

    fun serialize(): String {
        return serialize(this)
    }

    companion object {

        fun serialize(systemDescription: SystemDescription): String {
            return "GRAPHS\n" +
                    systemDescription.parts.joinToString("\n") { it.toDescription().serialize() } + "\n" +
                    "RELATIONS\n" +
                    systemDescription.relations.map { Relation.serialize(it) }.toList().sorted().joinToString("\n") + "\n" +
                    "PROJECTIONS\n" +
                    systemDescription.projections.map { Projection.serialize(it) }.toList().sorted().joinToString("\n")
        }

        fun parse(serialized: String, graphBuilder: (GraphDescription) -> RevisionGraph): SystemDescription {
            assert(serialized.startsWith("GRAPHS")) { "Serialized string must have SYSTEM format" }

            val parts = serialized.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            assert(parts[0].startsWith("GRAPHS"))

            val graphLines = parts.subList(
                1,
                parts.indexOfFirst { it.startsWith("RELATIONS") }
            )

            val graphGroups: MutableList<MutableList<String>> = mutableListOf()
            for (line in graphLines) {
                if (line.isEmpty()) {
                    continue
                }
                if (line.startsWith("G;")) {
                    graphGroups.add(mutableListOf())
                }
                graphGroups.last().add(line)
            }

            val graphs = graphGroups.map { GraphDescription.parse(it.toList()) }.map { graphBuilder(it) }

            val relations = parts.subList(
                parts.indexOfFirst { it.startsWith("RELATIONS") } + 1,
                parts.indexOfFirst { it.startsWith("PROJECTIONS") }
            ).map { Relation.parse(it) }.toSet()

            val projections = parts.subList(
                parts.indexOfFirst { it.startsWith("PROJECTIONS") } + 1,
                parts.size
            ).map { Projection.parse(it) }.toSet()

            return SystemDescription(graphs.toSet(), relations, projections)
        }

    }

}