package graphextend

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

data class SystemDescription(val parts: Set<RevisionGraph>,
                        val links: Set<CrossLink>,
                        val projections: Set<Projection>) {

    fun serialize(): String {
        return serialize(this)
    }

    companion object {

        fun serialize(systemDescription: SystemDescription): String {
            return "GRAPHS\n" +
                    systemDescription.parts.joinToString("\n") { it.toDescription().serialize() } + "\n" +
                    "LINKS\n" +
                    systemDescription.links.joinToString("\n") { CrossLink.serialize(it) } + "\n" +
                    "PROJECTIONS\n" +
                    systemDescription.projections.joinToString("\n") { Projection.serialize(it) } + "\n"
        }

    }

}