package ac.simons.neo4j.twitch;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Transaction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class OneTimeQueryRunner implements CommandLineRunner {

	private final Driver driver;

	private final ConfigurableApplicationContext context;

	public OneTimeQueryRunner(Driver driver, ConfigurableApplicationContext context) {
		this.driver = driver;
		this.context = context;
	}

	@Override
	public void run(String... args) {

		try (
			var session = driver.session()
		) {
			var movies = session.readTransaction(OneTimeQueryRunner::findMovieAndTheirActors);
			movies.forEach(System.out::println);
		}

		// Close the spring context and thus exit the program again.
		context.close();
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
