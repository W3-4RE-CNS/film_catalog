public class Movie {
    private int id;
    private String title;
    private String genre;
    private int year;
    private double rating;
    private int votesCount;

    public Movie(String title, String genre, int year) {
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.rating = 0.0;
        this.votesCount = 0;
    }

    public Movie(int id, String title, String genre, int year, double rating, int votesCount) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.rating = rating;
        this.votesCount = votesCount;
    }

    // Геттеры
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getYear() { return year; }
    public double getRating() { return rating; }
    public int getVotesCount() { return votesCount; }

    // Сеттеры для рейтинга
    public void setRating(double rating) { this.rating = rating; }
    public void setVotesCount(int votesCount) { this.votesCount = votesCount; }
    public void setId(int id) { this.id = id; }

    public void addRating(double userRating) {
        // Обновляем средний рейтинг
        double totalRating = (this.rating * this.votesCount) + userRating;
        this.votesCount++;
        this.rating = totalRating / this.votesCount;
    }

    public String getRatingStars() {
        if (votesCount == 0) return "Нет оценок";

        int stars = (int) Math.round(rating);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < stars) {
                sb.append("★");
            } else {
                sb.append("☆");
            }
        }
        return sb.toString() + String.format(" (%.1f/5.0, %d голосов)", rating, votesCount);
    }

    @Override
    public String toString() {
        return String.format("ID: %d | %s | %s | %d | Рейтинг: %s",
                id, title, genre, year, getRatingStars());
    }

    public String toStringWithoutRating() {
        return String.format("ID: %d | %s | %s | %d",
                id, title, genre, year);
    }
}