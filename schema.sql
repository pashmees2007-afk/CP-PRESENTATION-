-- Library Management System - database schema
--
-- Usage:
--   mysql -u root -p < schema.sql
--
-- Creates the library_db database, its two tables, and a few sample rows
-- so the Swing application has something to display on first run.

CREATE DATABASE IF NOT EXISTS library_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE library_db;

-- ---------------------------------------------------------------------------
-- books
--
-- book_id is assigned by the user through the form rather than being
-- AUTO_INCREMENT, because the UI requires an explicit ID for update and
-- delete. quantity is the number of copies the library owns.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS books (
    book_id   INT          NOT NULL,
    title     VARCHAR(255) NOT NULL,
    author    VARCHAR(255) NOT NULL,
    publisher VARCHAR(255) NOT NULL,
    quantity  INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (book_id),
    -- Supports the title/author lookups the search feature performs.
    KEY idx_books_title  (title),
    KEY idx_books_author (author),
    CONSTRAINT chk_books_quantity CHECK (quantity >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- issued_books
--
-- One row per lending transaction. return_date stays NULL while the book is
-- still out, which is what the overdue and currently-issued reports key on.
--
-- The foreign key is RESTRICT on purpose: deleting a book that still has an
-- issue record raises MySQL error 1451, which the UI turns into a readable
-- message instead of losing the lending history.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS issued_books (
    issue_id      INT          NOT NULL AUTO_INCREMENT,
    book_id       INT          NOT NULL,
    borrower_name VARCHAR(255) NOT NULL,
    issue_date    DATE         NOT NULL,
    due_date      DATE         NOT NULL,
    return_date   DATE         NULL,
    PRIMARY KEY (issue_id),
    -- Finding the open loans for a book, and listing overdue loans.
    KEY idx_issued_books_book_id     (book_id),
    KEY idx_issued_books_return_date (return_date),
    KEY idx_issued_books_due_date    (due_date),
    CONSTRAINT fk_issued_books_book
        FOREIGN KEY (book_id) REFERENCES books (book_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_issued_books_due_date CHECK (due_date >= issue_date)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- Sample data
-- ---------------------------------------------------------------------------
INSERT INTO books (book_id, title, author, publisher, quantity) VALUES
    (1, 'The Pragmatic Programmer',      'Andrew Hunt',     'Addison-Wesley', 4),
    (2, 'Clean Code',                    'Robert C. Martin', 'Prentice Hall',  3),
    (3, 'Design Patterns',               'Erich Gamma',     'Addison-Wesley', 2),
    (4, 'Introduction to Algorithms',    'Thomas H. Cormen', 'MIT Press',      5),
    (5, 'Effective Java',                'Joshua Bloch',    'Addison-Wesley', 6)
ON DUPLICATE KEY UPDATE book_id = book_id;

-- issue_id is given explicitly here only so re-running this script does not
-- insert a second copy of the sample loans; the application lets MySQL
-- assign it.
INSERT INTO issued_books (issue_id, book_id, borrower_name, issue_date, due_date, return_date) VALUES
    (1, 1, 'Asha Menon',  '2026-09-01', '2026-09-15', '2026-09-12'),
    (2, 2, 'Rahul Verma', '2026-10-05', '2026-10-19', NULL),
    (3, 4, 'Priya Nair',  '2026-09-20', '2026-10-04', NULL)
ON DUPLICATE KEY UPDATE issue_id = issue_id;
