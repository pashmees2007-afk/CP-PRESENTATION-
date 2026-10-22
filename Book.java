public class Book {

    private final int bookId;
    private final String title;
    private final String author;
    private final String publisher;
    private final int quantity;

    public Book(int bookId, String title, String author, String publisher, int quantity) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.quantity = quantity;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getBookId() {
        return bookId;
    }
}
