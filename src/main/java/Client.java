import model.QueryResult;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;

public class Client {
    private static String selectedDatabaseName = null;

    public static void start() {
        System.out.println(
                "   _____ _                 __        _____ ____    __       ______            _          \n" +
                "  / ___/(_)___ ___  ____  / /__     / ___// __ \\  / /      / ____/___  ____ _(_)___  ___ \n" +
                "  \\__ \\/ / __ `__ \\/ __ \\/ / _ \\    \\__ \\/ / / / / /      / __/ / __ \\/ __ `/ / __ \\/ _ \\\n" +
                " ___/ / / / / / / / /_/ / /  __/   ___/ / /_/ / / /___   / /___/ / / / /_/ / / / / /  __/\n" +
                "/____/_/_/ /_/ /_/ .___/_/\\___/   /____/\\___\\_\\/_____/  /_____/_/ /_/\\__, /_/_/ /_/\\___/ \n" +
                "                /_/                                                 /____/               ");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            } else if (input.equalsIgnoreCase("exit")) {
                return;
            }

            Matcher showDatabasesMatcher = Constants.SHOW_DATABASES_PATTERN.matcher(input);
            if (showDatabasesMatcher.matches()) {
                printDatabases();
                continue;
            }

            if (handleUseDatabaseCommand(input)) {
                continue;
            }

            if (selectedDatabaseName == null) {
                System.out.println("No database selected.");
                continue;
            }

            if (handleShowTablesCommand(input)) {
                continue;
            }

            if (handleQueryCommand(input)) {
                continue;
            }

            System.out.println("Command not supported.");
        }
    }

    private static boolean handleUseDatabaseCommand(String input) {
        Matcher useDatabaseMatcher = Constants.USE_DATABASE_COMMAND_PATTERN.matcher(input);
        if (!useDatabaseMatcher.matches()) {
            return false;
        }

        String inputDatabaseName = useDatabaseMatcher.group(1);
        if (!DatabaseExplorer.getDatabases().contains(inputDatabaseName)) {
            System.out.println("[" + inputDatabaseName + "] does not exist.");
            return false;
        }

        System.out.println("Using database [" + inputDatabaseName + "]");
        selectedDatabaseName = inputDatabaseName;
        return true;
    }

    private static boolean handleShowTablesCommand(String input) {
        Matcher showTablesMatcher = Constants.SHOW_TABLES_PATTERN.matcher(input);
        if (!showTablesMatcher.matches()) {
            return false;
        }

        printTables(selectedDatabaseName);
        return true;
    }

    private static boolean handleQueryCommand(String input) {
        Matcher countQueryMatcher = Constants.COUNT_QUERY_PATTERN.matcher(input);
        if (!countQueryMatcher.matches()) {
            return false;
        }

        String tableName = countQueryMatcher.group(1);
        String columnName = countQueryMatcher.group(2);
        String columnValue = countQueryMatcher.group(3);

        try {
            long startTime = System.nanoTime();
            QueryResult queryResult = QueryHandler.handle(selectedDatabaseName, tableName, columnName, columnValue);
            long endTime = System.nanoTime();

            long durationInMillis = (endTime - startTime) / 1_000_000;

            System.out.println("[" + queryResult.getNumRows() + "] rows found.");
            System.out.println("Query took [" + durationInMillis + "] ms.");

            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid query: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private static void printDatabases() {
        List<String> databases = DatabaseExplorer.getDatabases();
        if (databases.isEmpty()) {
            System.out.println("No databases found.");
            return;
        }

        for (int i = 0; i < databases.size(); i++) {
            System.out.println((i + 1) + ". " + databases.get(i));
        }
    }

    private static void printTables(String dbName) {
        List<String> tables = DatabaseExplorer.getTables(dbName);
        if (tables.isEmpty()) {
            System.out.println("No tables found in database [" + dbName + "].");
            return;
        }

        for (int i = 0; i < tables.size(); i++) {
            System.out.println((i + 1) + ". " + tables.get(i));
        }
    }

    private static void printHelp() {
        System.out.println("use database {database name}");
        System.out.println("show tables");
        System.out.println("select count(*) from {table name} where {column name} = {value}");
        System.out.println("exit");
    }
}