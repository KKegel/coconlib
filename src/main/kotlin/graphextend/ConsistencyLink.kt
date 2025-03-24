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

package graphextend

data class ConsistencyLink(
    val fromGraph: String,
    val toGraph: String,
    val fromRevision: String,
    val toRevision: String,
    val payload: String
){

    fun serialize(): String {
        return serialize(this)
    }

    companion object {

        fun serialize(consistencyLink: ConsistencyLink): String {
            return "L;${consistencyLink.fromGraph};${consistencyLink.toGraph};" +
                    "${consistencyLink.fromRevision};${consistencyLink.toRevision};${consistencyLink.payload}"
        }

        fun parse(serialized: String): ConsistencyLink {
            assert(serialized.startsWith("L;")) { "Serialized string must have LINK format" }
            assert(serialized.count { it == ';' } == 5) { "Serialized string must have 5 entries" }

            val parts = serialized.split(";")
            return ConsistencyLink(parts[1], parts[2], parts[3], parts[4], parts[5])
        }

    }
}
