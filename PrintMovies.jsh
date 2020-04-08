// Run with (adapt the path to the Neo4j driver as needed)
// jshell --enable-preview --class-path neo4j-java-driver-4.0.1.jar:reactive-streams-1.0.3.jar PrintMovies.jsh


var aCypher = """
    MATCH (m:Movie) 
    RETURN m.title AS title
    """
    
System.out.println(aCypher)

import org.neo4j.driver.*

var driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "secret"))

try(var session = driver.session()) { 
    session.run(aCypher).forEachRemaining(r -> System.out.println(r.get("title")));
}
driver.close()

/exit