import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:movies.db";
    private static DatabaseManager instance;

    private DatabaseManager() {
        loadDriver();
        initializeDatabase();
        updateDatabaseSchema();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Не удалось загрузить драйвер SQLite: " + e.getMessage());
            System.exit(1);
        }
    }

    private void initializeDatabase() {
        String sql = """
            CREATE TABLE IF NOT EXISTS movies (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                genre TEXT NOT NULL,
                year INTEGER NOT NULL,
                rating REAL DEFAULT 0.0,
                votes_count INTEGER DEFAULT 0
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("База данных инициализирована.");
        } catch (SQLException e) {
            System.err.println("Ошибка при инициализации БД: " + e.getMessage());
        }
    }

    // ИЗМЕНИТЕ ЭТОТ МЕТОД НА PUBLIC!
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // ... остальные методы без изменений ...
    private void updateDatabaseSchema() {
        try (Connection conn = getConnection()) {
            if (!columnExists(conn, "movies", "rating")) {
                System.out.println("Добавляем колонку rating...");
                String sql = "ALTER TABLE movies ADD COLUMN rating REAL DEFAULT 0.0;";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }

            if (!columnExists(conn, "movies", "votes_count")) {
                System.out.println("Добавляем колонку votes_count...");
                String sql = "ALTER TABLE movies ADD COLUMN votes_count INTEGER DEFAULT 0;";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }

            System.out.println("Структура базы данных актуальна.");
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении структуры БД: " + e.getMessage());
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) {
        String query = "PRAGMA table_info(" + tableName + ");";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                if (columnName.equals(rs.getString("name"))) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке колонки: " + e.getMessage());
        }
        return false;
    }

    public void addMovie(Movie movie) {
        String sql = "INSERT INTO movies (title, genre, year, rating, votes_count) VALUES (?, ?, ?, ?, ?);";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setInt(3, movie.getYear());
            pstmt.setDouble(4, movie.getRating());
            pstmt.setInt(5, movie.getVotesCount());
            pstmt.executeUpdate();
            System.out.println("Фильм добавлен успешно!");
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении фильма: " + e.getMessage());
        }
    }

    public boolean rateMovie(int movieId, double rating) {
        if (rating < 1 || rating > 5) {
            System.err.println("Рейтинг должен быть от 1 до 5");
            return false;
        }

        Movie movie = getMovieById(movieId);
        if (movie == null) {
            System.err.println("Фильм с ID " + movieId + " не найден");
            return false;
        }

        movie.addRating(rating);

        String sql = "UPDATE movies SET rating = ?, votes_count = ? WHERE id = ?;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, movie.getRating());
            pstmt.setInt(2, movie.getVotesCount());
            pstmt.setInt(3, movieId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Рейтинг обновлен!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении рейтинга: " + e.getMessage());
        }
        return false;
    }

    public Movie getMovieById(int id) {
        String sql = "SELECT * FROM movies WHERE id = ?;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getInt("year"),
                        rs.getDouble("rating"),
                        rs.getInt("votes_count")
                );
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении фильма: " + e.getMessage());
        }
        return null;
    }

    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY rating DESC, title;";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Movie movie = new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getInt("year"),
                        rs.getDouble("rating"),
                        rs.getInt("votes_count")
                );
                movies.add(movie);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении фильмов: " + e.getMessage());
        }
        return movies;
    }

    public List<Movie> getTopRatedMovies(int limit) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE votes_count > 0 ORDER BY rating DESC LIMIT ?;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getInt("year"),
                        rs.getDouble("rating"),
                        rs.getInt("votes_count")
                );
                movies.add(movie);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении топ фильмов: " + e.getMessage());
        }
        return movies;
    }

    public List<Movie> searchMoviesByTitle(String title) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE LOWER(title) LIKE ? ORDER BY rating DESC;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + title.toLowerCase() + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getInt("year"),
                        rs.getDouble("rating"),
                        rs.getInt("votes_count")
                );
                movies.add(movie);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске фильмов: " + e.getMessage());
        }
        return movies;
    }

    public List<Movie> filterMoviesByGenre(String genre) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE LOWER(genre) LIKE ? ORDER BY rating DESC;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + genre.toLowerCase() + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getInt("year"),
                        rs.getDouble("rating"),
                        rs.getInt("votes_count")
                );
                movies.add(movie);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при фильтрации по жанру: " + e.getMessage());
        }
        return movies;
    }

    public void populateDefaultMovies() {
        if (getMoviesCount() == 0) {
            System.out.println("База пуста. Добавляем тестовые фильмы...");

            Movie[] defaultMovies = {
                    new Movie("Титаник", "Драма", 1997),
                    new Movie("Матрица", "Фантастика", 1999),
                    new Movie("Крёстный отец", "Криминал", 1972),
                    new Movie("Начало", "Фантастика", 2010),
                    new Movie("Форрест Гамп", "Драма", 1994),
                    new Movie("Звёздные войны", "Фантастика", 1977)
            };

            for (Movie movie : defaultMovies) {
                addMovie(movie);
            }
            System.out.println("Тестовые фильмы добавлены.");
        }
    }

    private int getMoviesCount() {
        String sql = "SELECT COUNT(*) FROM movies;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
    }
}