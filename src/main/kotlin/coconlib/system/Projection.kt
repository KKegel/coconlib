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

package coconlib.system

data class Projection(val projectionId: String, val sources: List<String>, val target: String) {

    fun serialize(): String {
        return serialize(this)
    }

    companion object {

        const val PROJECTION = "PROJECTION"

        fun serialize(projection: Projection): String {
            return "P;${projection.projectionId};${projection.sources.joinToString(",")};${projection.target}"
        }

        fun parse(serialized: String): Projection {
            assert(serialized.startsWith("P;")) { "Serialized string must have PROJECTION format" }
            assert(serialized.count { it == ';' } == 3) { "Serialized string must have 4 entries" }

            val parts = serialized.split(";")
            val sources = parts[2].split(",").map { it.trim() }
            val target = parts[3].trim()
            return Projection(parts[1].trim(), sources, target)
        }
    }

}