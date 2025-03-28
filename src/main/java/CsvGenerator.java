import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

public class CsvGenerator {

    public static void main(String[] args) {
        int rowsPerFile = 100_000_000;
        Random random = new Random();

        // 100 city-state pairs
        String[][] cities = {
                {"New York", "NY"}, {"Los Angeles", "CA"}, {"Chicago", "IL"}, {"Houston", "TX"},
                {"Phoenix", "AZ"}, {"Philadelphia", "PA"}, {"San Antonio", "TX"}, {"San Diego", "CA"},
                {"Dallas", "TX"}, {"San Jose", "CA"}, {"Austin", "TX"}, {"Jacksonville", "FL"},
                {"Fort Worth", "TX"}, {"Columbus", "OH"}, {"Charlotte", "NC"}, {"San Francisco", "CA"},
                {"Indianapolis", "IN"}, {"Seattle", "WA"}, {"Denver", "CO"}, {"Washington", "DC"},
                {"Boston", "MA"}, {"El Paso", "TX"}, {"Nashville", "TN"}, {"Detroit", "MI"},
                {"Oklahoma City", "OK"}, {"Portland", "OR"}, {"Las Vegas", "NV"}, {"Memphis", "TN"},
                {"Louisville", "KY"}, {"Baltimore", "MD"}, {"Milwaukee", "WI"}, {"Albuquerque", "NM"},
                {"Tucson", "AZ"}, {"Fresno", "CA"}, {"Mesa", "AZ"}, {"Sacramento", "CA"},
                {"Atlanta", "GA"}, {"Kansas City", "MO"}, {"Colorado Springs", "CO"}, {"Miami", "FL"},
                {"Raleigh", "NC"}, {"Omaha", "NE"}, {"Long Beach", "CA"}, {"Virginia Beach", "VA"},
                {"Oakland", "CA"}, {"Minneapolis", "MN"}, {"Tulsa", "OK"}, {"Arlington", "TX"},
                {"New Orleans", "LA"}, {"Wichita", "KS"}, {"Cleveland", "OH"}, {"Tampa", "FL"},
                {"Bakersfield", "CA"}, {"Aurora", "CO"}, {"Honolulu", "HI"}, {"Anaheim", "CA"},
                {"Lexington", "KY"}, {"Stockton", "CA"}, {"Corpus Christi", "TX"}, {"Henderson", "NV"},
                {"Riverside", "CA"}, {"Saint Paul", "MN"}, {"St. Louis", "MO"}, {"Cincinnati", "OH"},
                {"Pittsburgh", "PA"}, {"Greensboro", "NC"}, {"Anchorage", "AK"}, {"Plano", "TX"},
                {"Lincoln", "NE"}, {"Orlando", "FL"}, {"Irvine", "CA"}, {"Newark", "NJ"},
                {"Toledo", "OH"}, {"Durham", "NC"}, {"Chula Vista", "CA"}, {"Fort Wayne", "IN"},
                {"Jersey City", "NJ"}, {"St. Petersburg", "FL"}, {"Laredo", "TX"}, {"Madison", "WI"},
                {"Chandler", "AZ"}, {"Buffalo", "NY"}, {"Lubbock", "TX"}, {"Scottsdale", "AZ"},
                {"Reno", "NV"}, {"Glendale", "AZ"}, {"Gilbert", "AZ"}, {"Winston–Salem", "NC"},
                {"North Las Vegas", "NV"}, {"Norfolk", "VA"}, {"Chesapeake", "VA"}, {"Garland", "TX"},
                {"Irving", "TX"}, {"Hialeah", "FL"}, {"Fremont", "CA"}, {"Boise", "ID"},
                {"Richmond", "VA"}, {"Baton Rouge", "LA"}, {"Spokane", "WA"}, {"Des Moines", "IA"}
        };

        int idx = 0;
        for (int j = 0; j < 10; j++) {
            String fileName = "weatherdata_" + j + ".csv";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                for (int k = 0; k < rowsPerFile; k++) {
                    int index = random.nextInt(cities.length);
                    String city = cities[index][0];
                    String state = cities[index][1];

                    LocalDate date = LocalDate.now().minusDays(random.nextInt(365));
                    int temp = random.nextInt(56) - 10; // -10°C to 45°C

                    String row = String.format("%d,%s,%s,%s,%d", idx, city, state, date, temp);
                    writer.write(row);
                    writer.newLine();

                    if (k % 1_000_000 == 0) {
                        System.out.println(k + " rows written...");
                    }

                    idx++;
                }

                System.out.println("CSV generation complete: " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}