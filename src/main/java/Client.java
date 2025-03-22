import java.util.List;
import java.util.Scanner;

public class Client {
    private final DatabaseExplorer explorer;
    private final Scanner scanner;

    // The storageDirLoc is where your "dbs" folder is located.
    public Client(String storageDirLoc) {
        this.explorer = new DatabaseExplorer(storageDirLoc);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            List<String> databases = explorer.listDatabases();
            if (databases.isEmpty()) {
                System.out.println("No databases found.");
                break;
            }

            System.out.println("\nAvailable Databases:");
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
                allDatabasesMenu(selectedDb);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        System.out.println("Exiting program.");
    }

    private void allDatabasesMenu(String dbName) {
        while (true) {
            System.out.println("\nDatabase: " + dbName);
            System.out.println("1. List all tables");
            System.out.println("2. Query the database");
            System.out.println("3. Go back");
            System.out.print("Enter choice: ");
            String input = scanner.nextLine();

            if (input.equals("1")) {
                allTablesMenu(dbName);
            } else if (input.equals("2")) {
                System.out.print("Enter your query: ");
                String query = scanner.nextLine();
                explorer.queryDatabase(dbName, query);
            } else if (input.equals("3")) {
                break;
            } else {
                System.out.println("Invalid selection.");
            }
        }
    }

    private void allTablesMenu(String dbName) {
        List<String> tables = explorer.listTables(dbName);
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
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void tableMenu(String dbName, String tableName) {
        while (true) {
            System.out.println("\nTable: " + tableName);
            System.out.println("1. View schema");
            System.out.println("2. View data");
            System.out.println("3. Go back");
            System.out.print("Enter choice: ");
            String input = scanner.nextLine();

            if (input.equals("1")) {
                String schema = explorer.viewSchema(dbName, tableName);
                System.out.println("\nSchema for " + tableName + ":");
                System.out.println(schema);
            } else if (input.equals("2")) {
                String data = explorer.viewData(dbName, tableName);
                System.out.println("\nData for " + tableName + ":");
                System.out.println(data);
            } else if (input.equals("3")) {
                break;
            } else {
                System.out.println("Invalid selection.");
            }
        }
    }

    public static void main(String[] args) {
        // Modify storageDirLoc as needed. Here we assume the current directory.
        String storageDirLoc = System.getProperty("user.home");
        System.out.println(storageDirLoc);
        Client client = new Client(storageDirLoc);
        client.start();
    }
}