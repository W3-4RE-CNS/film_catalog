import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {

    private DatabaseManager dbManager = DatabaseManager.getInstance();
    private TableView<Movie> moviesTable;
    private Label totalMoviesLabel;
    private Label ratedMoviesLabel;
    private Label averageRatingLabel;
    private TextField searchField;
    private TextField genreFilterField;
    private ComboBox<Integer> movieIdCombo;
    private ComboBox<Double> ratingCombo;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        root.setTop(createHeaderPane());
        root.setCenter(createMoviesTablePane());
        root.setLeft(createAddMoviePane());
        root.setRight(createSearchFilterPane());
        root.setBottom(createRatingPane());

        loadMovies();
        updateStatistics();
        setupRatingControls();

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("🎬 Каталог фильмов с рейтингом");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeaderPane() {
        HBox headerPane = new HBox(20);
        headerPane.setPadding(new Insets(10));
        headerPane.setAlignment(Pos.CENTER_LEFT);
        headerPane.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 10;");

        Label titleLabel = new Label("🎬 Каталог фильмов");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        totalMoviesLabel = new Label("Всего фильмов: 0");
        totalMoviesLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 14px;");

        ratedMoviesLabel = new Label("Оценено: 0");
        ratedMoviesLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 14px;");

        averageRatingLabel = new Label("Средний рейтинг: 0.0");
        averageRatingLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerPane.getChildren().addAll(titleLabel, spacer, totalMoviesLabel, ratedMoviesLabel, averageRatingLabel);
        return headerPane;
    }

    private VBox createAddMoviePane() {
        VBox addPane = new VBox(10);
        addPane.setPadding(new Insets(15));
        addPane.setPrefWidth(300);
        addPane.setStyle("-fx-background-color: #34495e; -fx-background-radius: 10;");

        Label titleLabel = new Label("Добавить новый фильм");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        TextField titleField = new TextField();
        titleField.setPromptText("Название фильма");
        titleField.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-prompt-text-fill: #95a5a6;");

        TextField genreField = new TextField();
        genreField.setPromptText("Жанр");
        genreField.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-prompt-text-fill: #95a5a6;");

        TextField yearField = new TextField();
        yearField.setPromptText("Год выпуска");
        yearField.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-prompt-text-fill: #95a5a6;");

        Button addButton = new Button("Добавить фильм");
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 200;");
        addButton.setOnAction(e -> handleAddMovie(titleField, genreField, yearField));

        Button refreshButton = new Button("Обновить список");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-width: 200;");
        refreshButton.setOnAction(e -> handleRefresh());

        Button topButton = new Button("Топ 10 фильмов");
        topButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-pref-width: 200;");
        topButton.setOnAction(e -> handleShowTopMovies());

        addPane.getChildren().addAll(
                titleLabel,
                new Label("Название:"),
                titleField,
                new Label("Жанр:"),
                genreField,
                new Label("Год:"),
                yearField,
                addButton,
                refreshButton,
                topButton
        );

        for (var node : addPane.getChildren()) {
            if (node instanceof Label && !((Label) node).getStyle().contains("bold")) {
                ((Label) node).setStyle("-fx-text-fill: #ecf0f1;");
            }
        }

        return addPane;
    }

    private VBox createSearchFilterPane() {
        VBox searchPane = new VBox(10);
        searchPane.setPadding(new Insets(15));
        searchPane.setPrefWidth(300);
        searchPane.setStyle("-fx-background-color: #34495e; -fx-background-radius: 10;");

        Label titleLabel = new Label("Поиск и фильтрация");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label searchLabel = new Label("Поиск по названию:");
        searchLabel.setStyle("-fx-text-fill: #ecf0f1;");

        searchField = new TextField();
        searchField.setPromptText("Введите название");
        searchField.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-prompt-text-fill: #95a5a6;");

        HBox searchButtons = new HBox(10);
        Button searchButton = new Button("Найти");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        searchButton.setOnAction(e -> handleSearch());

        Button clearSearchButton = new Button("Очистить");
        clearSearchButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        clearSearchButton.setOnAction(e -> {
            searchField.clear();
            handleRefresh();
        });

        searchButtons.getChildren().addAll(searchButton, clearSearchButton);

        Label filterLabel = new Label("Фильтр по жанру:");
        filterLabel.setStyle("-fx-text-fill: #ecf0f1;");

        genreFilterField = new TextField();
        genreFilterField.setPromptText("Введите жанр");
        genreFilterField.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-prompt-text-fill: #95a5a6;");

        HBox filterButtons = new HBox(10);
        Button filterButton = new Button("Фильтровать");
        filterButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
        filterButton.setOnAction(e -> handleFilterByGenre());

        Button clearFilterButton = new Button("Очистить");
        clearFilterButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        clearFilterButton.setOnAction(e -> {
            genreFilterField.clear();
            handleRefresh();
        });

        filterButtons.getChildren().addAll(filterButton, clearFilterButton);

        Button deleteButton = new Button("Удалить выбранный фильм");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 250;");
        deleteButton.setOnAction(e -> handleDeleteMovie());

        searchPane.getChildren().addAll(
                titleLabel,
                searchLabel,
                searchField,
                searchButtons,
                filterLabel,
                genreFilterField,
                filterButtons,
                deleteButton
        );

        return searchPane;
    }

    private VBox createMoviesTablePane() {
        VBox tablePane = new VBox(10);
        tablePane.setPadding(new Insets(10));

        moviesTable = new TableView<>();

        TableColumn<Movie, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(50);

        TableColumn<Movie, String> titleColumn = new TableColumn<>("Название");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setPrefWidth(250);

        TableColumn<Movie, String> genreColumn = new TableColumn<>("Жанр");
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        genreColumn.setPrefWidth(150);

        TableColumn<Movie, Integer> yearColumn = new TableColumn<>("Год");
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearColumn.setPrefWidth(80);

        TableColumn<Movie, String> ratingColumn = new TableColumn<>("Рейтинг");
        ratingColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(() ->
                        cellData.getValue().getRatingStars()
                )
        );
        ratingColumn.setPrefWidth(250);

        moviesTable.getColumns().addAll(idColumn, titleColumn, genreColumn, yearColumn, ratingColumn);
        moviesTable.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");

        VBox.setVgrow(moviesTable, Priority.ALWAYS);
        tablePane.getChildren().add(moviesTable);

        return tablePane;
    }

    private HBox createRatingPane() {
        HBox ratingPane = new HBox(20);
        ratingPane.setPadding(new Insets(15));
        ratingPane.setStyle("-fx-background-color: #34495e; -fx-background-radius: 10;");
        ratingPane.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Оценить фильм:");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        VBox movieSelectBox = new VBox(5);
        Label movieLabel = new Label("Выберите фильм:");
        movieLabel.setStyle("-fx-text-fill: #ecf0f1;");

        movieIdCombo = new ComboBox<>();
        movieIdCombo.setPrefWidth(200);
        movieIdCombo.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");

        movieSelectBox.getChildren().addAll(movieLabel, movieIdCombo);

        VBox ratingSelectBox = new VBox(5);
        Label ratingLabel = new Label("Выберите оценку:");
        ratingLabel.setStyle("-fx-text-fill: #ecf0f1;");

        ratingCombo = new ComboBox<>();
        ratingCombo.setPrefWidth(100);
        ratingCombo.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");

        ratingSelectBox.getChildren().addAll(ratingLabel, ratingCombo);

        Button rateButton = new Button("Поставить оценку ★");
        rateButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 40;");
        rateButton.setOnAction(e -> handleRateMovie());

        Label infoLabel = new Label("★ 1 - плохо, ★★★★★ 5 - отлично");
        infoLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-style: italic;");

        ratingPane.getChildren().addAll(
                titleLabel,
                movieSelectBox,
                ratingSelectBox,
                rateButton,
                infoLabel
        );

        return ratingPane;
    }

    private void loadMovies() {
        var movies = dbManager.getAllMovies();
        moviesTable.getItems().setAll(movies);
        updateMovieIdCombo();
    }

    private void updateMovieIdCombo() {
        var movies = dbManager.getAllMovies();
        movieIdCombo.getItems().clear();
        for (Movie movie : movies) {
            movieIdCombo.getItems().add(movie.getId());
        }
        if (!movieIdCombo.getItems().isEmpty()) {
            movieIdCombo.getSelectionModel().selectFirst();
        }
    }

    private void setupRatingControls() {
        for (double i = 1.0; i <= 5.0; i += 0.5) {
            ratingCombo.getItems().add(i);
        }
        ratingCombo.getSelectionModel().selectFirst();
    }

    private void updateStatistics() {
        var movies = dbManager.getAllMovies();
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

    private void handleAddMovie(TextField titleField, TextField genreField, TextField yearField) {
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

            titleField.clear();
            genreField.clear();
            yearField.clear();

            loadMovies();
            updateStatistics();

            showInfo("Успех", "Фильм '" + title + "' успешно добавлен!");

        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректный год!");
        }
    }

    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadMovies();
        } else {
            var movies = dbManager.searchMoviesByTitle(query);
            moviesTable.getItems().setAll(movies);
        }
    }

    private void handleFilterByGenre() {
        String genre = genreFilterField.getText().trim();
        if (genre.isEmpty()) {
            loadMovies();
        } else {
            var movies = dbManager.filterMoviesByGenre(genre);
            moviesTable.getItems().setAll(movies);
        }
    }

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

    private void handleShowTopMovies() {
        var topMovies = dbManager.getTopRatedMovies(10);
        moviesTable.getItems().setAll(topMovies);

        if (topMovies.isEmpty()) {
            showInfo("Топ фильмов", "Нет оцененных фильмов");
        }
    }

    private void handleRefresh() {
        loadMovies();
        updateStatistics();
        searchField.clear();
        genreFilterField.clear();
    }

    private void handleDeleteMovie() {
        Movie selectedMovie = moviesTable.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showAlert("Ошибка", "Выберите фильм для удаления!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление фильма");
        alert.setContentText("Вы уверены, что хотите удалить фильм:\n\"" + selectedMovie.getTitle() + "\"?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteMovieFromDatabase(selectedMovie.getId());
            }
        });
    }

    private void deleteMovieFromDatabase(int movieId) {
        String sql = "DELETE FROM movies WHERE id = ?;";
        try (var conn = dbManager.getConnection();
             var pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, movieId);
            pstmt.executeUpdate();

            loadMovies();
            updateStatistics();
            updateMovieIdCombo();

            showInfo("Успех", "Фильм удален!");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось удалить фильм: " + e.getMessage());
        }
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

    public static void main(String[] args) {
        launch(args);
    }
}