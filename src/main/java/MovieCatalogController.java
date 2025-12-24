import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class MovieCatalogController {

    private DatabaseManager dbManager = DatabaseManager.getInstance();

    // Таблица фильмов
    @FXML
    private TableView<Movie> moviesTable;
    @FXML
    private TableColumn<Movie, Integer> idColumn;
    @FXML
    private TableColumn<Movie, String> titleColumn;
    @FXML
    private TableColumn<Movie, String> genreColumn;
    @FXML
    private TableColumn<Movie, Integer> yearColumn;
    @FXML
    private TableColumn<Movie, String> ratingColumn;

    // Форма добавления фильма
    @FXML
    private TextField titleField;
    @FXML
    private TextField genreField;
    @FXML
    private TextField yearField;

    // Поиск и фильтрация
    @FXML
    private TextField searchField;
    @FXML
    private TextField genreFilterField;

    // Оценка фильма
    @FXML
    private ComboBox<Integer> movieIdCombo;
    @FXML
    private ComboBox<Double> ratingCombo;

    // Статистика
    @FXML
    private Label totalMoviesLabel;
    @FXML
    private Label ratedMoviesLabel;
    @FXML
    private Label averageRatingLabel;

    @FXML
    public void initialize() {
        setupTable();
        loadMovies();
        setupRatingControls();
        updateStatistics();
    }

    private void setupTable() {
        // Настраиваем колонки таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        ratingColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(() ->
                        cellData.getValue().getRatingStars()
                )
        );

        // Сортируем по рейтингу
        moviesTable.getSortOrder().add(ratingColumn);
    }

    private void loadMovies() {
        List<Movie> movies = dbManager.getAllMovies();
        ObservableList<Movie> observableList = FXCollections.observableArrayList(movies);
        moviesTable.setItems(observableList);

        // Обновляем комбобокс для оценки
        updateMovieIdCombo();
    }

    private void setupRatingControls() {
        // Заполняем рейтинг от 1 до 5 с шагом 0.5
        ObservableList<Double> ratings = FXCollections.observableArrayList();
        for (double i = 1.0; i <= 5.0; i += 0.5) {
            ratings.add(i);
        }
        ratingCombo.setItems(ratings);
        ratingCombo.getSelectionModel().selectFirst();
    }

    private void updateMovieIdCombo() {
        List<Movie> movies = dbManager.getAllMovies();
        ObservableList<Integer> ids = FXCollections.observableArrayList();
        for (Movie movie : movies) {
            ids.add(movie.getId());
        }
        movieIdCombo.setItems(ids);
        if (!ids.isEmpty()) {
            movieIdCombo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleAddMovie() {
        try {
            String title = titleField.getText().trim();
            String genre = genreField.getText().trim();
            String yearText = yearField.getText().trim();

            if (title.isEmpty() || genre.isEmpty() || yearText.isEmpty()) {
                showAlert("Ошибка", "Заполните все поля!");
                return;
            }

            int year = Integer.parseInt(yearText);
            if (year < 1888 || year > 2100) {
                showAlert("Ошибка", "Введите корректный год (1888-2100)");
                return;
            }

            Movie movie = new Movie(title, genre, year);
            dbManager.addMovie(movie);

            // Очищаем поля
            titleField.clear();
            genreField.clear();
            yearField.clear();

            // Обновляем таблицу
            loadMovies();
            updateStatistics();

            showInfo("Успех", "Фильм успешно добавлен!");

        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректный год!");
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadMovies();
        } else {
            List<Movie> movies = dbManager.searchMoviesByTitle(query);
            ObservableList<Movie> observableList = FXCollections.observableArrayList(movies);
            moviesTable.setItems(observableList);
        }
    }

    @FXML
    private void handleFilterByGenre() {
        String genre = genreFilterField.getText().trim();
        if (genre.isEmpty()) {
            loadMovies();
        } else {
            List<Movie> movies = dbManager.filterMoviesByGenre(genre);
            ObservableList<Movie> observableList = FXCollections.observableArrayList(movies);
            moviesTable.setItems(observableList);
        }
    }

    @FXML
    private void handleRateMovie() {
        Integer movieId = movieIdCombo.getValue();
        Double rating = ratingCombo.getValue();

        if (movieId == null || rating == null) {
            showAlert("Ошибка", "Выберите фильм и рейтинг!");
            return;
        }

        boolean success = dbManager.rateMovie(movieId, rating);
        if (success) {
            loadMovies();
            updateStatistics();
            showInfo("Успех", "Рейтинг обновлен!");
        } else {
            showAlert("Ошибка", "Не удалось обновить рейтинг");
        }
    }

    @FXML
    private void handleShowTopMovies() {
        List<Movie> topMovies = dbManager.getTopRatedMovies(10);
        ObservableList<Movie> observableList = FXCollections.observableArrayList(topMovies);
        moviesTable.setItems(observableList);

        if (topMovies.isEmpty()) {
            showInfo("Топ фильмов", "Нет оцененных фильмов");
        }
    }

    @FXML
    private void handleRefresh() {
        loadMovies();
        updateStatistics();
        searchField.clear();
        genreFilterField.clear();
    }

    @FXML
    private void handleDeleteMovie() {
        Movie selectedMovie = moviesTable.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showAlert("Ошибка", "Выберите фильм для удаления!");
            return;
        }

        // Подтверждение удаления
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление фильма");
        alert.setContentText("Вы уверены, что хотите удалить фильм: " + selectedMovie.getTitle() + "?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            deleteMovieFromDatabase(selectedMovie.getId());
            loadMovies();
            updateStatistics();
            updateMovieIdCombo();
        }
    }

    private void deleteMovieFromDatabase(int movieId) {
        String sql = "DELETE FROM movies WHERE id = ?;";
        try (var conn = dbManager.getConnection();
             var pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, movieId);
            pstmt.executeUpdate();
            showInfo("Успех", "Фильм удален!");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось удалить фильм: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        List<Movie> movies = dbManager.getAllMovies();
        int total = movies.size();

        long ratedCount = movies.stream()
                .filter(m -> m.getVotesCount() > 0)
                .count();

        double avgRating = movies.stream()
                .filter(m -> m.getVotesCount() > 0)
                .mapToDouble(Movie::getRating)
                .average()
                .orElse(0.0);

        totalMoviesLabel.setText("Всего фильмов: " + total);
        ratedMoviesLabel.setText("Оценено: " + ratedCount);
        averageRatingLabel.setText(String.format("Средний рейтинг: %.2f", avgRating));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Временный метод для доступа к соединению (добавьте в DatabaseManager геттер для Connection)
    // В DatabaseManager добавьте: public Connection getConnection() throws SQLException { ... }
}