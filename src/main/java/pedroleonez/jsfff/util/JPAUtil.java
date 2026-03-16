package pedroleonez.jsfff.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class JPAUtil {

    private static final EntityManagerFactory factory = buildFactory();

    private static EntityManagerFactory buildFactory() {
        Map<String, Object> overrides = new HashMap<>();

        String jdbcUrl = getenv("JDBC_URL");
        if (jdbcUrl == null) {
            jdbcUrl = toJdbcUrl(getenv("DATABASE_URL"));
        }
        String dbUser = getenv("DB_USER");
        String dbPassword = getenv("DB_PASSWORD");

        if (jdbcUrl != null) {
            overrides.put("jakarta.persistence.jdbc.url", jdbcUrl);
        }
        if (dbUser != null) {
            overrides.put("jakarta.persistence.jdbc.user", dbUser);
        }
        if (dbPassword != null) {
            overrides.put("jakarta.persistence.jdbc.password", dbPassword);
        }

        if (overrides.isEmpty()) {
            return Persistence.createEntityManagerFactory("tarefasPU");
        }
        return Persistence.createEntityManagerFactory("tarefasPU", overrides);
    }

    private static String getenv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private static String toJdbcUrl(String databaseUrl) {
        if (databaseUrl == null) {
            return null;
        }
        if (databaseUrl.startsWith("jdbc:")) {
            return databaseUrl;
        }
        try {
            URI uri = URI.create(databaseUrl);
            if (uri.getHost() == null) {
                return null;
            }
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath() != null ? uri.getPath() : "";
            String query = uri.getQuery();
            String jdbc = "jdbc:postgresql://" + uri.getHost() + ":" + port + path;
            if (query != null && !query.isBlank()) {
                jdbc = jdbc + "?" + query;
            }
            return jdbc;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Produces
    @RequestScoped
    public EntityManager getEntityManager() {
        return factory.createEntityManager();
    }
}
