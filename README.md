# Library Management System

A desktop Library Management System built with **Java Swing** and **MySQL**.
It provides a single-window CRUD interface over a library's book catalogue,
backed by a MySQL schema that also tracks lending records.

## Features

- Add, update and delete books
- Browse the full catalogue in a sortable table
- Click a row to load it into the form for editing
- Input validation on IDs, titles, authors, publishers and quantities
- Readable error messages for database failures, including an explicit
  message when a book cannot be deleted because it still has a lending record

## Requirements

| Component | Version |
| --- | --- |
| JDK | 17 or newer (developed against 21) |
| MySQL | 8.0 or newer |
| MySQL Connector/J | 8.x or newer |

## Setup

### 1. Create the database

```bash
mysql -u root -p < schema.sql
```

This creates the `library_db` database, the `books` and `issued_books`
tables, their indexes, and a handful of sample rows.

### 2. Download the JDBC driver

Download MySQL Connector/J from
<https://dev.mysql.com/downloads/connector/j/> and place the jar in a `lib/`
directory at the project root:

```
lib/mysql-connector-j-9.1.0.jar
```

`lib/` is git-ignored, so the jar is never committed.

### 3. Set the database password

The application reads the MySQL password from an environment variable rather
than from source, so no credential is ever committed:

```bash
export LIBRARY_DB_PASSWORD='your-mysql-password'
```

On Windows (PowerShell):

```powershell
$env:LIBRARY_DB_PASSWORD = 'your-mysql-password'
```

## Build and run

Linux / macOS:

```bash
javac -cp "lib/mysql-connector-j-9.1.0.jar" -d out *.java
java -cp "out:lib/mysql-connector-j-9.1.0.jar" LibraryManagementSystem
```

Windows (note the `;` classpath separator):

```powershell
javac -cp "lib\mysql-connector-j-9.1.0.jar" -d out *.java
java -cp "out;lib\mysql-connector-j-9.1.0.jar" LibraryManagementSystem
```

### Verify the connection on its own

`DBConnection` has a `main` method that checks connectivity without starting
the UI, which is the quickest way to tell a configuration problem apart from
an application bug:

```bash
java -cp "out:lib/mysql-connector-j-9.1.0.jar" DBConnection
```

## Project layout

| File | Purpose |
| --- | --- |
| `LibraryManagementSystem.java` | Swing UI, form handling and SQL for each operation |
| `Book.java` | Immutable model for a catalogue entry |
| `DBConnection.java` | JDBC connection factory, plus a connectivity check |
| `schema.sql` | Database, tables, indexes and sample data |

## Database schema

**books** — the catalogue. `book_id` is entered by the user rather than
auto-generated, because update and delete are driven by an explicit ID.

| Column | Type | Notes |
| --- | --- | --- |
| `book_id` | `INT` | Primary key |
| `title` | `VARCHAR(255)` | Not null |
| `author` | `VARCHAR(255)` | Not null |
| `publisher` | `VARCHAR(255)` | Not null |
| `quantity` | `INT` | Copies owned; must be >= 0 |

**issued_books** — one row per lending transaction. `return_date` is `NULL`
while a book is still out.

| Column | Type | Notes |
| --- | --- | --- |
| `issue_id` | `INT` | Primary key, auto-increment |
| `book_id` | `INT` | Foreign key to `books`, `ON DELETE RESTRICT` |
| `borrower_name` | `VARCHAR(255)` | Not null |
| `issue_date` | `DATE` | Not null |
| `due_date` | `DATE` | Not null; must be on or after `issue_date` |
| `return_date` | `DATE` | `NULL` until the book comes back |

The foreign key is deliberately `RESTRICT`: deleting a book that still has a
lending record fails rather than silently discarding history, and the UI
reports that as a readable message.

## Troubleshooting

| Symptom | Likely cause |
| --- | --- |
| `No suitable driver found` | Connector/J jar missing from the classpath |
| `Access denied for user 'root'` | `LIBRARY_DB_PASSWORD` unset or wrong |
| `Unknown database 'library_db'` | `schema.sql` has not been run |
| Delete reports a linked issue record | The book has a row in `issued_books` |
