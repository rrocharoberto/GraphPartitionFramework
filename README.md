# Graph Partition Framework

This framework helps the development of programs which use algorithms for graph partitioning, enabling information handling both in memory and in a graph database. 
It was designed to work with 2-way, k-way and multilevel algorithms, providing support to the algorithms to make use of the Neo4J database structures. This feature allows the implementations of the algorithms are performed independently of the resource used (memory or disc), providing the researcher a generic and flexible data structure.

# Framework de Particionamento de Grafos
Este framework auxilia o desenvolvimento de programas que usam algoritmos para o particionamento de grafos, permitindo a manipulação das informações tanto em memória quanto em um banco de dados orientado a grafos.
Ele foi projetado para trabalhar com algoritmos 2-way, k-way e multiníveis, dando suporte para que os algoritmos utilizem as estruturas do banco Neo4J. Esta característica permite que as implementações dos algoritmos sejam realizadas independentemente do recurso utilizado (memória ou disco), fornecendo ao pesquisador uma estrutura de dados genérica e flexível.


This project uses the Neo4J graph database (in an embedded way) through Maven dependencies.

For running the algoritms, please see the projects started with "Memory_..." and "Neo4j_..." where is located the main methods.

In order to compile the project, please use the following Maven command on the root directory of the project:

mvn clean install -Dmaven.test.skip=true

The projects can be imported in Eclipse, so that, it's necessary to have Maven and its repository configured properly in the machine.
Inside the Eclipse, you must create a classpath variable called M2_REPO, pointing to local Maven repository.

