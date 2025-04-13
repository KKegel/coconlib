# coconlib

*coconlib* is a lightweight library for managing complex revision systems. 
The library provides all core functionality for creating, editing and querying revision graphs and multi-revision graphs.
*coconlib* is designed to be easily extensible, technology-agnostic and made to work on any underlying physical revision system.

The official *coconlib* commandline application is available as **[cocon-cli](https://github.com/KKegel/cocon-cli)**.

## Contributing

This project is open source and we welcome contributions from the community.
The project is licensed under the Apache License 2.0.
The initial version of this project was developed as part of the research project *CONVIDE* at Dresden University of Technology, Germany by Karl Kegel.
Please see the [CONVIDE project](https://www.sfb1608.kit.edu/) for more information.

## About

Revision systems are used to manage the history of changes to a set of objects.
These objects can be anything from files to whole software projects.
An example for a simple revision system is a version control system (VCS) like Git.
In Git, each commit is a revision of the project and the history is the representation of the revision graph.
A Git repository comprising multiple Git-submodules is an example for a multi-revision system.
However, a revision system can exist without a system like Git. In the easiest case, a revision system is just a folder of
versioned files.

We define a revision system as a directed acylic graph of revisions - which we call revision graph.
Revisions are the nodes of the graph and the edges represent the relationship between revisions.
Possible relationships are *succession* and *unification* (merge).
A multi revision system is a forest of revision graphs, e.g., your Git repository with multiple submodules.
Revisions across different graphs can be *related* with a freely definable semantic relationship.
Revisions across different graphs can form *projections*, i.e., views.

*coconlib* is a library for managing revision graphs and multi-revision graphs.
It provides all core functionality for creating, editing and querying revision graphs and multi-revision graphs.
The central feature of *coconlib* is the ability to checkout revision contexts.
Revision contexts are groups of revisions that collaborate in a specific way.
Examples for contexts are the version history of a revision back to the initial version, or the group of revisions that are
related by a specific semantic relationship.

## Features

- A central data structure for revision graphs and multi-revision graphs based on the *Apache Tinkerpop* graph model framework.
- CRUD operations for revision graphs and multi-revision graphs.
- Serialization and deserialization of multi-revision graphs from and to text.
- A query interface for retrieving revisions and revision contexts.
- Different levels of validation for revision graphs.
- Entry points (hooks) for custom validation, extension and customization.

The *coconlib* library is designed to be easily extensible and technology-agnostic.
The implemented serialization and deserialization from text uses a simple human-readable format.
However, assuming the library should be used on top of a graph database, it is completely possible to just interact with the library using its API.

## Installation

As of right now, *coconlib* is not only available on GitHub. 
You can either include this repository as a submodule in your project; Use the JAR build artifact from our CI (releases); Or build the library yourself.

## Getting Started

The following guide is a short introduction to the library.
We provide extensive KotlinDoc documentation in the source code.

### Creating an empty Multi-Revision Graph
```kotlin
val revisionSystem: MultiRevisionSystem = MultiRevisionSystem.create(
  setOf<RevisionGraph>(),   //empty set of subsystems
  setOf<Relation>(),        //empty set of relations
  setOf<RevisionGraph>(),   //empty set of projections
)
```

### Creating a Multi-Revision Graph from String 
We provide the string syntax and examples at the end of this document.

```kotlin
val serializedSystem: String = "..." //String description
val rsd: SystemDescription = SystemDescription.parse(
  serializedSystem, 
  TinkerRevisionGraph::build  //use Tinkergraph Graph API as underlying library
)
val revisionSystem: MultiRevisionSystem = MultiRevisionSystem.create(
  rsd.parts,
  rsd.relations,
  rsd.projections
)
```

### Editing a Multi-Revision Graph

#### Subsystems
Adding & removing subsystems:
```kotlin
//mrs: MultiRevisionSystem
mrs.initNewGraph("new subsystem name")
//...
mrs.removeGraph("deleted sybsystem name)
```
Several methods for listing and traversing subsystems are provided. Please have a look at the implementation in ``MultiRevisionSystem.kt``.

#### Revisions
Adding & removing revisions:
```kotlin
//mrs: MultiRevisionSystem
//predecessors may be null
val revision = RevisionDescription(graphId, predecessor1Id, predecessor2Id, path)
//create zero, one, or both incoming edges:
val successorEdge1 = EdgeDescription(predecessor1, revisionId, EdgeLabel.SUCCESSOR) //optional
val successorEdge2 = EdgeDescription(predecessor2, revisionId, EdgeLabel.SUCCESSOR) //optional

//in case of just one predecessor:
mrs.addRevision(graphId, revision, safe = false)
mrs.addEdge(graphId, successorEdge1, safe = false)
mrs.validate() //optional

//in case of two predecessors:
mrs.addRevisionWithUnification(graphId, revision, successorEdge1, successorEdge2)

//OR DON'T ADD EDGES
//That's a root revision
mrs.addRevision(graphId, revision, safe = true)
```
Several methods for listing and traversing subsystems are provided. Please have a look at the implementation in ``MultiRevisionSystem.kt``.

#### Validation
We provide basic validation methods for RevisionGraphs and MultiRevisionGraphs via ``.validate()`` methods.
Note that these methods cannot find all possible errors. However, the detect common mistakes such as dangling edges, nodes, or cycles of small length.

### Querying a Multi-Revision Graph
We provide several methods for querying a MultiRevisionGraph. Save variants of these methods are implemented in ``MultiRevisionSystem.kt``. However, it is possible to access the underlying Tinkergraph strucutre directly via Gremlin queries.

Examples are:
```kotlin
//mrs: MultiRevisionSystem
val edges: List<EdgeDescription> = mrs.findEdges(revisionId)
val projections: List<Projection> = mrs.findProjections(revisionId)
val relations: List<Relation> = mrs.findRelations(revisionId)

val revisions = mrs.getParts().first { it.graphId == subsystemName }.getRevisions()
val relations = mrs.getRelations().filter { it.fromGraph == subsystemName || it.toGraph == subsystemName }

val revision: RevisionDescription = mrs.findRevision(revisionId)
```

#### Context Queries
We provide the following interface for querying contexts:
```kotlin
findLocalContext(graphId: String, revisionShortId: String, contextType: ContextType, depth: Int): Context
```
```kotlin
findGlobalContext(revisionShortId: String, contextType: ContextType): Context
```
where:
```kotlin
enum class ContextType {
    SPACE, TIME, RELATIONAL, PROJECTIVE
}

data class Context(
    val contextType: ContextType,
    val cardinality: Int,
    val participants: Set<RevisionDescription>
){
    companion object {
        const val UNBOUNDED: Int = -1
    }
}
```

#### coconlib String Format

A multi-revision system can be serialized from and two string.
The syntax is defined by the following template:
```
GRAPHS
G;<GRAPH ID>
   V;<GRAPH ID>;<REVISION ID>;<DESCRIPTION>;<URI>;<PAYLOAD (opt)>
   ...
   E;<START REVISION ID>;<END REVISION ID>;<EDGE TYPE>;<PAYLOAD (opt)>
   ...
G;<NEXT GRAPH ID>
   ...
RELATIONS
L;<FROM GRAPH>;<TO GRAPH>;<FROM REVISION ID>;<TO REVISION ID>;
...
PROJECTIONS
P;<PROJECTION ID>;<SOURCE REVISION 1 ID>,<SOURCE REVISION N ID>,...;<PROJECTION NAME>
...
```
where ``<EDGE TYPE> = SUCESSOR|MERGE``

An examplary revision graph could look like this:
```
Subsys X
                   /---> X.g --> X.p
X.a --> X.b --> X.e --> X.f
 \---> X.c --> X.h ---\
  \ ================== > X.j ---> X.k
   \--> X.d ----------/

Subsys Y
             /---> Y.x
 Y.a --> Y.b --> Y.c --> y
             \---> Y.z

LINKS:
Y.x -> X.k
X.p -> Y.z

PROJECTIONS:
A = X.a + Y.a
P = X.f
```

This system conforms to the following serialization:
```
GRAPHS
G;X
V;X;X.a;X.A;./a;
V;X;X.b;X.B;./b;
V;X;X.c;X.C;./c;
V;X;X.d;X.D;./d;
V;X;X.e;X.E;./e;
V;X;X.f;X.F;./f;
V;X;X.g;X.G;./g;
V;X;X.h;X.H;./h;
V;X;X.j;X.J;./j;
V;X;X.k;X.K;./k;
V;X;X.p;X.P;./p;
E;X.a;X.b;SUCCESSOR
E;X.a;X.c;SUCCESSOR
E;X.a;X.d;SUCCESSOR
E;X.a;X.j;MERGE
E;X.b;X.e;SUCCESSOR
E;X.c;X.h;SUCCESSOR
E;X.d;X.j;SUCCESSOR
E;X.e;X.f;SUCCESSOR
E;X.e;X.g;SUCCESSOR
E;X.g;X.p;SUCCESSOR
E;X.h;X.j;SUCCESSOR
E;X.j;X.k;SUCCESSOR
G;Y
V;Y;Y.a;y.A;./a;
V;Y;Y.b;y.B;./b;
V;Y;Y.c;y.C;./c;
V;Y;Y.x;y.X;./x;
V;Y;Y.y;y.Y;./y;
V;Y;Y.z;y.Z;./z;
E;Y.a;Y.b;SUCCESSOR
E;Y.b;Y.c;SUCCESSOR
E;Y.c;Y.x;SUCCESSOR
E;Y.c;Y.y;SUCCESSOR
E;Y.c;Y.z;SUCCESSOR
RELATIONS
L;X;Y;X.p;Y.z;
L;Y;X;Y.x;X.k;
PROJECTIONS
P;A;X.a,Y.a;A
```

