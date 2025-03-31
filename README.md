# coconlib

*coconlib* is a lightweight library for managing complex revision systems. 
The library provides all core functionality for creating, editing and querying revision graphs and multi-revision graphs.
*coconlib* is designed to be easily extensible, technology-agnostic and made to work on any underlying physical revision system.

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

### Creating a Multi-Revision Graph via API

### Creating a Multi-Revision Graph via Text

### Editing a Multi-Revision Graph

### Querying a Multi-Revision Graph