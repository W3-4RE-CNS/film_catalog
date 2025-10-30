import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Класс для представления фильма
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

// Основной класс приложения
public class MovieCatalog {
    private static List<Movie> movies = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
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
                    return;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
            System.out.println(); // пустая строка для читаемости
        }
    }

    private static void showMenu() {
        System.out.println("=== Каталог фильмов ===");
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

        movies.add(new Movie(title, genre, year));
        System.out.println("Фильм добавлен!");
    }

    private static void showAllMovies() {
        if (movies.isEmpty()) {
            System.out.println("Каталог пуст.");
        } else {
            System.out.println("Список всех фильмов:");
            for (int i = 0; i < movies.size(); i++) {
                System.out.println((i + 1) + ". " + movies.get(i));
            }
        }
    }

    private static void findMovie() {
        System.out.print("Введите название фильма для поиска: ");
        String query = scanner.nextLine().trim().toLowerCase();

        boolean found = false;
        for (Movie movie : movies) {
            if (movie.getTitle().toLowerCase().contains(query)) {
                System.out.println("Найдено: " + movie);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Фильмы по запросу не найдены.");
        }
    }

    private static void filterByGenre() {
        System.out.print("Введите жанр для фильтрации: ");
        String genreQuery = scanner.nextLine().trim().toLowerCase();

        boolean found = false;
        for (Movie movie : movies) {
            if (movie.getGenre().toLowerCase().contains(genreQuery)) {
                System.out.println(movie);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Фильмы с таким жанром не найдены.");
        }
    }// Вспомогательный метод для безопасного ввода целого числа
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