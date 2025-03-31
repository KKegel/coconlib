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

package coconlib.core

import coconlib.graph.EdgeDescription
import coconlib.graph.GraphDescription
import coconlib.graph.RevisionDescription
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Vertex

abstract class RevisionGraph(val graphId: String) {

    abstract fun hasRevision(revId: String): Boolean
    abstract fun hasRevision(vertex: RevisionDescription): Boolean
    abstract fun hasEdge(edge: EdgeDescription): Boolean

    abstract fun getRevision(revId: String): Vertex
    abstract fun getEdge(sourceShortId: String, targetShortId: String): Edge

    abstract fun addRevision(vertex: RevisionDescription)
    abstract fun addEdge(edge: EdgeDescription)

    abstract fun removeRevision(vertex: RevisionDescription)
    abstract fun removeEdge(edge: EdgeDescription)

    abstract fun prettyPrint(): String

    abstract fun transform(revisionDescription: RevisionDescription): Vertex
    abstract fun transform(vertex: Vertex): RevisionDescription
    abstract fun transform(revId: String): Vertex
    abstract fun transform(edgeDescription: EdgeDescription): Edge
    abstract fun transform(sourceShortId: String, targetShortId: String): Edge
    abstract fun transform(edge: Edge): EdgeDescription

    abstract fun getRevisions(): List<RevisionDescription>
    abstract fun getEdges(): List<EdgeDescription>
    abstract fun getSuccessorEdges(): List<Edge>
    abstract fun getMergeEdges(): List<Edge>
    abstract fun getLeafRevisions(): List<Vertex>
    abstract fun getRootRevision(): Vertex
    abstract fun getPathToRoot(vertex: Vertex, pathLength: Int): List<Vertex>
    abstract fun getNeighbors(vertex: Vertex, recursionDepth: Int): Set<Vertex>

    abstract fun validate(depth: Int = 7): Boolean

    abstract fun toDescription(): GraphDescription

    companion object {
        const val REVISION = "revision"
    }

}