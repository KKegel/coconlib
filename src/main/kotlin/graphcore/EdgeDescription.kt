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

data class EdgeDescription(
    val sourceShortId: String,
    val targetShortId: String,
    val label: EdgeLabel
){

    init {
        assert(sourceShortId.isNotEmpty()) { "Source short ID must not be empty" }
        assert(targetShortId.isNotEmpty()) { "Target short ID must not be empty" }
        assert(sourceShortId.contains(";").not()) { "Source short ID must not contain semicolon" }
        assert(targetShortId.contains(";").not()) { "Target short ID must not contain semicolon" }
    }

    fun serialize(): String {
        return serialize(this)
    }

    companion object{

        fun serialize(edgeDescription: EdgeDescription): String {
            assert(edgeDescription.sourceShortId.isNotEmpty()) { "Source short ID must not be empty" }
            assert(edgeDescription.targetShortId.isNotEmpty()) { "Target short ID must not be empty" }

            return "E;${edgeDescription.sourceShortId};${edgeDescription.targetShortId};${edgeDescription.label}"
        }

        fun parse(serialized: String): EdgeDescription {
            assert(serialized.startsWith("E;")) { "Serialized string must have EDGE format" }
            assert(serialized.count { it == ';' } == 3) { "Serialized string must have 3 entries" }

            val parts = serialized.split(";")
            return EdgeDescription(parts[1], parts[2], EdgeLabel.valueOf(parts[3]))
        }

    }
}