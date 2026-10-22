public class Book {

    private final int bookId;
    private final String title;
    private final String publisher;
    private final int quantity;

    public Book(int bookId, String title, String publisher, int quantity) {
        this.bookId = bookId;
        this.title = title;
        this.publisher = publisher;
        this.quantity = quantity;
    }

    public String getTitle() {
        return title;
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
