import de.vandermeer.asciitable.AsciiTable;
import model.ColumnSchema;
import model.QueryResult;
import model.TableSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    private final Scanner scanner;

    public Client() {
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            List<String> databases = DatabaseExplorer.listDatabases();
            if (databases.isEmpty()) {
                System.out.println("No databases found.");
                break;
            }

            System.out.println("Available Databases:");
            for (int i = 0; i < databases.size(); i++) {
                System.out.println((i + 1) + ". " + databases.get(i));
            }

            System.out.print("Select a database (number) or type 'exit' to quit: ");

            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            try {
                int choice = Integer.parseInt(input);
                if (choice < 1 || choice > databases.size()) {
                    System.out.println("Invalid selection. Try again.");
                    continue;
                }
                String selectedDb = databases.get(choice - 1);

                databaseMenu(selectedDb);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        System.out.println("Exiting program.");
    }

    private void databaseMenu(String dbName) {
        while (true) {
            System.out.println("Database: " + dbName);
            System.out.println("1. List all tables");
            System.out.println("2. Query");
            System.out.println("3. Go back");
            System.out.print("Enter choice: ");

            String input = scanner.nextLine();
            switch (input) {
                case "1" -> allTablesMenu(dbName);
                case "2" -> queryMenu(dbName);
                case "3" -> { return; }
                default -> System.out.println("Invalid selection.");
            }
        }
    }

    private void queryMenu(String dbName) {
        String query;
        while (true) {
            System.out.print("Enter your query (or type 'back' to go back): ");
            query = scanner.nextLine().trim();

            if (query.equalsIgnoreCase("back")) {
                System.out.println("Returning to previous menu.");
                return;
            } else {
                try {
                    long startTime = System.nanoTime();
                    QueryResult res = QueryHandler.handle(dbName, query);
                    long endTime = System.nanoTime();
                    long durationInMillis = (endTime - startTime) / 1_000_000;

                    System.out.println("Query execution time: " + durationInMillis + " ms");
                    System.out.println(res.getNumRows() + " rows returned by query.");

//                    TableSchema tableSchema = DatabaseExplorer.getTableSchema(dbName, res.getTableName());
//                    AsciiTable at = new AsciiTable();
//
//                    String[] header = new String[tableSchema.getColumns().size()];
//                    for (int i = 0; i < tableSchema.getColumns().size(); i++) {
//                        header[i] = tableSchema.getColumns().get(i).getColumnName();
//                    }
//                    at.addRule();
//                    at.addRow((Object[]) header);
//                    at.addRule();
//
//                    for (int i = 0; i < res.getNumRows().size(); i++) {
//                        Object[] row = res.getNumRows().get(i);
//                        at.addRow(row);
//                        at.addRule();
//                    }
//
//                    System.out.println(at.render());

                    return;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private void allTablesMenu(String dbName) {
        List<String> tables = DatabaseExplorer.listTables(dbName);
        if (tables.isEmpty()) {
            System.out.println("No tables found in database '" + dbName + "'.");
            return;
        }

        while (true) {
            System.out.println("\nTables in " + dbName + ":");
            for (int i = 0; i < tables.size(); i++) {
                System.out.println((i + 1) + ". " + tables.get(i));
            }
            System.out.println((tables.size() + 1) + ". Go back");
            System.out.print("Select a table (number): ");
            String input = scanner.nextLine();

            try {
                int choice = Integer.parseInt(input);
                if (choice == tables.size() + 1) {
                    break;
                }
                if (choice < 1 || choice > tables.size()) {
                    System.out.println("Invalid selection. Try again.");
                    continue;
                }
                String tableName = tables.get(choice - 1);
                tableMenu(dbName, tableName);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void tableMenu(String dbName, String tableName) throws Exception {
        while (true) {
            System.out.println("\nTable: " + tableName);
            System.out.println("1. View schema");
            System.out.println("2. View data");
            System.out.println("3. Go back");
            System.out.print("Enter choice: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1" -> {
                    TableSchema tableSchema = DatabaseExplorer.getTableSchema(dbName, tableName);
                    System.out.println("\nSchema for " + tableName + ":");

                    AsciiTable at = new AsciiTable();
                    at.addRule();
                    at.addRow("Column Name", "Data Type");
                    at.addRule();

                    for (ColumnSchema columnSchema : tableSchema.getColumns()) {
                        at.addRow(columnSchema.getColumnName(), columnSchema.getColumnType());
                        at.addRule();
                    }

                    System.out.println(at.render());
                }
                case "2" -> {
                    List<String[]> data = new ArrayList<>();
                    int suffix = 0;
                    while (true) {
                        try {
                            data.addAll(DatabaseExplorer.getTableData(dbName, tableName + "_" + suffix));
                            suffix++;
                        } catch (Exception e) {
                            break;
                        }
                    }
                    System.out.println("\nData for " + tableName + ":");

                    System.out.println(tableName);
                    TableSchema tableSchema = DatabaseExplorer.getTableSchema(dbName, tableName);
                    AsciiTable at = new AsciiTable();

                    String[] header = new String[tableSchema.getColumns().size()];
                    for (int i = 0; i < tableSchema.getColumns().size(); i++) {
                        header[i] = tableSchema.getColumns().get(i).getColumnName();
                    }
                    at.addRule();
                    at.addRow((Object[]) header);
                    at.addRule();

                    // max 20 rows
                    int maxRowsToPrint = Math.min(data.size(), 20);
                    for (int i = 1; i < maxRowsToPrint; i++) {
                        String[] row = data.get(i);
                        at.addRow((Object[]) row);
                        at.addRule();
                    }

                    System.out.println(at.render());
                }
                case "3" -> {
                    return;
                }
                default -> System.out.println("Invalid selection.");
            }
        }
    }

}