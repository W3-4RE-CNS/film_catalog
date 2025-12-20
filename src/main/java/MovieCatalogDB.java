import java.sql.*;
import java.util.Scanner;


class Movie {
    private String title;
    private String genre;
    private int year;

    public Movie(String title, String genre, int year) {
        this.title = title;
        this.genre = genre;
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return String.format("Название: %s | Жанр: %s | Год: %d", title, genre, year);
    }
}


public class MovieCatalogDB {
    private static final String DB_URL = "jdbc:sqlite:movies.db";
    private static Scanner scanner = new Scanner(System.in);

    // Статический блок для загрузки драйвера при загрузке класса
    static {
        loadDatabaseDriver();
    }

    private static void loadDatabaseDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Не удалось загрузить драйвер SQLite: " + e.getMessage());
            System.err.println("Убедитесь, что sqlite-jdbc.jar добавлен в classpath.");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        System.out.println("Запуск каталога фильмов...");
        initializeDatabase();
        populateDatabaseIfEmpty();

        while (true) {
            showMenu();
            int choice = getIntInput();
            switch (choice) {
                case 1:
                    addMovie();
                    break;
                case 2:
                    showAllMovies();
                    break;
                case 3:
                    findMovie();
                    break;
                case 4:
                    filterByGenre();
                    break;
                case 0:
                    System.out.println("Выход из программы.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
            System.out.println();
        }
    }

    private static void initializeDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS movies (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                genre TEXT NOT NULL,
                year INTEGER NOT NULL
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("База данных инициализирована.");
        } catch (SQLException e) {
            System.err.println("Ошибка при инициализации базы данных: " + e.getMessage());
        }
    }

    private static void populateDatabaseIfEmpty() {
        int count = 0;
        String countSQL = "SELECT COUNT(*) FROM movies;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {
            count = rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке записей: " + e.getMessage());
        }

        if (count == 0) {
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
                insertMovie(movie);
            }
            System.out.println("Тестовые фильмы добавлены.");
        }
    }

    private static void insertMovie(Movie movie) {
        String insertSQL = "INSERT INTO movies (title, genre, year) VALUES (?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setInt(3, movie.getYear());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении фильма: " + e.getMessage());
        }
    }

    private static void showMenu() {
        System.out.println("=== Каталог фильмов (БД) ===");
        System.out.println("1. Добавить фильм");
        System.out.println("2. Показать все фильмы");
        System.out.println("3. Найти фильм по названию");
        System.out.println("4. Фильтровать по жанру");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
    }

    private static void addMovie() {
        System.out.print("Введите название фильма: ");
        String title = scanner.nextLine().trim();

        System.out.print("Введите жанр фильма: ");
        String genre = scanner.nextLine().trim();

        System.out.print("Введите год выпуска: ");
        int year = getIntInput();

        Movie movie = new Movie(title, genre, year);
        insertMovie(movie);
        System.out.println("Фильм добавлен в базу!");
    }

    private static void showAllMovies() {
        String selectSQL = "SELECT * FROM movies ORDER BY id;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            if (!rs.isBeforeFirst()) {
                System.out.println("Каталог пуст.");
            } else {
                System.out.println("Список всех фильмов:");
                int index = 1;
                while (rs.next()) {
                    System.out.printf("%d. Название: %s | Жанр: %s | Год: %d%n",
                            index++,
                            rs.getString("title"),
                            rs.getString("genre"),
                            rs.getInt("year")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении фильмов: " + e.getMessage());
        }
    }

    private static void findMovie() {
        System.out.print("Введите название фильма для поиска: ");
        String query = scanner.nextLine().trim().toLowerCase();

        String selectSQL = "SELECT * FROM movies WHERE LOWER(title) LIKE ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                System.out.printf("Найдено: Название: %s | Жанр: %s | Год: %d%n",
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getInt("year")
                );
                found = true;
            }

            if (!found) {
                System.out.println("Фильмы по запросу не найдены.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске фильма: " + e.getMessage());
        }
    }

    private static void filterByGenre() {
        System.out.print("Введите жанр для фильтрации: ");
        String genreQuery = scanner.nextLine().trim().toLowerCase();

        String selectSQL = "SELECT * FROM movies WHERE LOWER(genre) LIKE ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, "%" + genreQuery + "%");
            ResultSet rs = pstmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                System.out.printf("Найдено: Название: %s | Жанр: %s | Год: %d%n",
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getInt("year")
                );
                found = true;
            }

            if (!found) {
                System.out.println("Фильмы с таким жанром не найдены.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при фильтрации: " + e.getMessage());
        }
    }


    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Некорректный ввод. Введите целое число: ");
            }
        }
    }
}