package ac.simons.neo4j.twitch;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Transaction;

public class GraphApplication {

	public static void main(String... a) {

		try (
			var driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "secret"));
			var session = driver.session()
		) {
			var movies = session.readTransaction(GraphApplication::findMovieAndTheirActors);
			movies.forEach(System.out::println);
		}
	}

	record Person(String name) {
	}

	record Movie(String title, List<Person>actedIn) {
	}

	static List<Movie> findMovieAndTheirActors(Transaction tx) {

		var query = """
			MATCH (m:Movie) <- [:ACTED_IN] - (p:Person)
			WHERE m.title =~ $movieTitle
			RETURN m.title AS title, collect(p.name) AS actors
			""";

		return tx.run(query, Map.of("movieTitle", ".*The Matrix.*")).list(r -> {

			var actors = r.get("actors").asList(v -> new Person(v.asString()));
			var movie = new Movie(r.get("title").asString(), actors);
			return movie;
		});
	}
}
