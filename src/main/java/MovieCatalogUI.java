import java.util.List;
import java.util.Scanner;

public class MovieCatalogUI {
    private Scanner scanner;
    private DatabaseManager dbManager;

    public MovieCatalogUI() {
        scanner = new Scanner(System.in);
        dbManager = DatabaseManager.getInstance();
        dbManager.populateDefaultMovies();
    }

    public void start() {
        System.out.println("=== Каталог фильмов (БД) ===");
        System.out.println("Система рейтинга: 1-5 звезд ★☆☆☆☆ - ★★★★★");

        while (true) {
            showMenu();
            int choice = getIntInput();

            switch (choice) {
                case 1 -> addMovie();
                case 2 -> showAllMovies();
                case 3 -> searchMovie();
                case 4 -> filterByGenre();
                case 5 -> rateMovie(); // Новая опция
                case 6 -> showTopMovies(); // Новая опция
                case 0 -> {
                    System.out.println("Выход из программы.");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Неверный выбор. Попробуйте снова.");
            }
            System.out.println();
        }
    }

    private void showMenu() {
        System.out.println("\n=== МЕНЮ ===");
        System.out.println("1. Добавить фильм");
        System.out.println("2. Показать все фильмы");
        System.out.println("3. Найти фильм по названию");
        System.out.println("4. Фильтровать по жанру");
        System.out.println("5. Оценить фильм ★");
        System.out.println("6. Топ фильмов по рейтингу");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
    }

    private void addMovie() {
        System.out.println("\n=== ДОБАВЛЕНИЕ ФИЛЬМА ===");

        System.out.print("Введите название фильма: ");
        String title = scanner.nextLine().trim();

        System.out.print("Введите жанр фильма: ");
        String genre = scanner.nextLine().trim();

        System.out.print("Введите год выпуска: ");
        int year = getIntInput();

        Movie movie = new Movie(title, genre, year);
        dbManager.addMovie(movie);
    }

    private void showAllMovies() {
        System.out.println("\n=== ВСЕ ФИЛЬМЫ (отсортировано по рейтингу) ===");

        List<Movie> movies = dbManager.getAllMovies();

        if (movies.isEmpty()) {
            System.out.println("Каталог пуст.");
        } else {
            for (int i = 0; i < movies.size(); i++) {
                System.out.println((i + 1) + ". " + movies.get(i));
            }
            System.out.println("Всего фильмов: " + movies.size());
        }
    }

    private void searchMovie() {
        System.out.println("\n=== ПОИСК ФИЛЬМА ===");

        System.out.print("Введите название фильма для поиска: ");
        String query = scanner.nextLine().trim();

        List<Movie> movies = dbManager.searchMoviesByTitle(query);

        if (movies.isEmpty()) {
            System.out.println("Фильмы по запросу не найдены.");
        } else {
            System.out.println("Найдено фильмов: " + movies.size());
            for (int i = 0; i < movies.size(); i++) {
                System.out.println((i + 1) + ". " + movies.get(i));
            }
        }
    }

    private void filterByGenre() {
        System.out.println("\n=== ФИЛЬТРАЦИЯ ПО ЖАНРУ ===");

        System.out.print("Введите жанр для фильтрации: ");
        String genre = scanner.nextLine().trim();

        List<Movie> movies = dbManager.filterMoviesByGenre(genre);

        if (movies.isEmpty()) {
            System.out.println("Фильмы с таким жанром не найдены.");
        } else {
            System.out.println("Найдено фильмов: " + movies.size());
            for (int i = 0; i < movies.size(); i++) {
                System.out.println((i + 1) + ". " + movies.get(i));
            }
        }
    }

    private void rateMovie() {
        System.out.println("\n=== ОЦЕНКА ФИЛЬМА ===");

        // Сначала показываем список фильмов
        List<Movie> movies = dbManager.getAllMovies();
        if (movies.isEmpty()) {
            System.out.println("Каталог пуст. Невозможно оценить фильмы.");
            return;
        }

        System.out.println("Список фильмов:");
        for (Movie movie : movies) {
            System.out.println(movie.toStringWithoutRating());
        }

        System.out.print("\nВведите ID фильма для оценки: ");
        int movieId = getIntInput();

        System.out.print("Введите оценку (от 1 до 5): ");
        double rating = getDoubleInput(1, 5);

        boolean success = dbManager.rateMovie(movieId, rating);
        if (success) {
            Movie updatedMovie = dbManager.getMovieById(movieId);
            System.out.println("Фильм обновлен: " + updatedMovie);
        }
    }

    private void showTopMovies() {
        System.out.println("\n=== ТОП ФИЛЬМОВ ПО РЕЙТИНГУ ===");

        System.out.print("Сколько фильмов показать? (по умолчанию 10): ");
        int limit;
        try {
            limit = Integer.parseInt(scanner.nextLine().trim());
            if (limit <= 0) limit = 10;
        } catch (NumberFormatException e) {
            limit = 10;
        }

        List<Movie> topMovies = dbManager.getTopRatedMovies(limit);

        if (topMovies.isEmpty()) {
            System.out.println("Нет оцененных фильмов.");
        } else {
            System.out.println("Топ " + topMovies.size() + " фильмов:");
            for (int i = 0; i < topMovies.size(); i++) {
                System.out.println((i + 1) + ". " + topMovies.get(i));
            }
        }
    }

    private int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Некорректный ввод. Введите целое число: ");
            }
        }
    }

    private double getDoubleInput(double min, double max) {
        while (true) {
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.printf("Введите число от %.1f до %.1f: ", min, max);
                }
            } catch (NumberFormatException e) {
                System.out.print("Некорректный ввод. Введите число: ");
            }
        }
    }
}