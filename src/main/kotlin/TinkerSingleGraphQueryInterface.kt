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

import cocontext.ConsistencyContext
import cocontext.ConsistencyContextType
import graphcore.EdgeDescription
import graphcore.VertexDescription

class TinkerSingleGraphQueryInterface(val revisionGraph: TinkerRevisionGraph) : SingleGraphQueryInterface {

    override fun findContextByShortId(
        revisionShortId: String,
        contextType: ConsistencyContextType,
        depth: Int
    ): ConsistencyContext {
        TODO("Not yet implemented")
    }

    override fun findContextByLongId(
        revisionLongId: String,
        contextType: ConsistencyContextType,
        depth: Int
    ): ConsistencyContext {
        TODO("Not yet implemented")
    }

    override fun getRevisions(): List<VertexDescription> {
        TODO("Not yet implemented")
    }

    override fun getEdges(): List<EdgeDescription> {
        TODO("Not yet implemented")
    }

}