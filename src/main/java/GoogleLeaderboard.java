import com.google.api.client.util.Value;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import org.apache.commons.codec.binary.Hex;

public class GoogleLeaderboard {
    private static Sheets sheetsService;
    private static final String SPREADSHEET_ID = "1mVa3cwyS7bQhMgbcGDoCk8IWiADYLXNpzWaowZ42Q6M";

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials_DELUXE.json";

    public GoogleLeaderboard(Stage stage) throws IOException, GeneralSecurityException {
        stage.setTitle("GoogleLeaderboard");

        newPlayer("looks", 40);

        stage.setScene(createBoard());
        stage.show();
    }

    private Scene createBoard() throws IOException, GeneralSecurityException {
        // create a table view in a borderPane
        BorderPane bpane = new BorderPane();
        bpane.getStylesheets().add("StyleClass.css");

        TableView tableView = new TableView();

        // Table columns: x3 (Place, Name, Time Taken)
        TableColumn<Player, String> place = new TableColumn<>("Place");
        place.setMinWidth(75);
        place.setCellValueFactory(new PropertyValueFactory<>("place"));
        place.setId("notName");

        TableColumn<Player, String> name = new TableColumn<>("Name");
        name.setMinWidth(250);
        name.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Player, Double> time = new TableColumn<>("Time Taken");
        time.setMinWidth(150);
        time.setCellValueFactory(new PropertyValueFactory<>("time"));
        time.setId("notName");

        tableView.getColumns().addAll(place, name, time);

        // load all the entries already present
        tableView.setItems(loadBoard());

        bpane.setCenter(tableView);

        return new Scene(bpane);
    }

    private static Credential getCredentials() throws IOException, GeneralSecurityException {
        // Code necessary to get google credits needed for the OAuth process
        InputStream in = GoogleLeaderboard.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver())
                .authorize("user");

        return credential;
    }

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = getCredentials();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static ObservableList<Player> loadBoard() throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();

        ObservableList<Player> playerList = FXCollections.observableArrayList();

        int counter = 1;

        // Reads data already written in google sheets
        String range = "Sheet1!B2:C12";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = response.getValues();
        System.out.println(values.size());

        if(values == null || values.isEmpty()) {
            System.out.println("No data found");
        } else {
            System.out.println("Place, Name, Time:");

            for (List row: values) {
                System.out.printf("%s came in %s with a time of %s\n", row.get(0), counter, row.get(1));

                String name = (String) row.get(0);
                double time = Double.parseDouble((String) row.get(1));

                playerList.add(new Player(counter + ".", name, time));

                counter++;
            }
        }

        return playerList;
    }

    private static void write(ValueRange appendBody) throws IOException, GeneralSecurityException {
        // To write new data to sheets:
        sheetsService = getSheetsService();

        AppendValuesResponse appendResults = sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, "Sheet1", appendBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();
    }

    private static void edit(ValueRange body, String range) throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();

        // To edit data already present in google sheets:
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(SPREADSHEET_ID, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    private static void move(int startRow, int endRow, int dest) throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();

        // To move rows:
        MoveDimensionRequest moved = new MoveDimensionRequest()
                .setSource(
                        new DimensionRange()
                                .setSheetId(0)
                                .setDimension("ROWS")
                                .setStartIndex(startRow)
                                .setEndIndex(11)
                )
                .setDestinationIndex(12);

        List<Request> requests = new ArrayList<>();
        requests.add(new Request().setMoveDimension(moved));

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();
    }

    private static void delete(int startRow) throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();

        // To delete a row or column in google sheets
        DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                .setRange(
                        new DimensionRange()
                                .setSheetId(0)
                                .setDimension("ROWS")
                                .setStartIndex(startRow)
                );

        List<Request> requests = new ArrayList<>();
        requests.add(new Request().setDeleteDimension(deleteRequest));

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();
    }


    public static void newPlayer(String name, double time) throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();

        // Reads data from sheets
        String range = "Sheet1!B2:C11";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = response.getValues();

        // Checks state of the leaderboard
        if (values == null || values.isEmpty()) {
            // This is the first participant
            System.out.println("First entry");
            edit(new ValueRange().setValues(
                    Arrays.asList(Arrays.asList(name, time))
            ), "B2:C2");

        } else if (values.size() == 10) {
            // There already are 10 players on the leaderboard --> check if player beat someone on the board
            System.out.println("leaderboard full");

            // First check if player made the leaderboards
            boolean newHighscore = false;
            int counter = 0;    // To keep track of what position new player has beaten (if any)

            for (int i = 0; i < values.size(); i++) {
                double leaderTime = Double.parseDouble((String) values.get(i).get(1));
                if (time < leaderTime) {
                    System.out.println(time);
                    System.out.println(leaderTime);
                    newHighscore = true;
                } else {
                    counter++;
                }
            }
            int newPos = counter+1; // Since array index starts at 0

            // Process of adding the new player to the leaderboard
            if (newHighscore) {
                System.out.println("new board finisher");

                // add new players time at the bottom of the google sheets
                write(new ValueRange().setValues(
                        Arrays.asList(Arrays.asList(newPos, name, time))
                ));
                // move all slower times below the new time
                move(newPos, 10, 11);

                // delete 11th time to keep the leaderboard to the top 10
                delete(11);

                System.out.println("finished subbing in new time");
            }

        } else {   // There still is some space on the leaderboard (less than 10 players have played)
            System.out.println("leadboard still got some space");

            // Check if the current player was faster than those already on the leaderboard
            boolean newHighscore = false;
            int counter = 0;    // To keep track of what position new player has beaten (if any)

            for (int i = 0; i < values.size(); i++) {
                double leaderTime = Double.parseDouble((String) values.get(i).get(1));
                if (time < leaderTime) {
                    newHighscore = true;
                } else {
                    counter++;
                }
            }
            int newPos = counter+1;

            if (newHighscore) {
                // move all the slower players down 1
                move(newPos, values.size(), 12);

                // add new players time in his place
                String newPlayerRange = "A" + String.valueOf(newPos+1) + ":C" + String.valueOf(newPos+1);
                edit(new ValueRange().setValues(
                        Arrays.asList(Arrays.asList(newPos, name, time))
                ), newPlayerRange);

                System.out.println("finished switching in new player before top 10 full");
            } else {

                // If the new time is slower, add the new time to the bottom of the leaderboard
                String stringIndex = String.valueOf(values.size() + 2);   // to account for frozen row
                String editRange = "B" + stringIndex + ":C" + stringIndex;
                System.out.println(editRange);
                edit(new ValueRange().setValues(
                        Arrays.asList(Arrays.asList(name, time))
                        ),
                        editRange);
            }
        }


    }

    public static void sheetCommands(String command, int index) throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();

        List<Request> requests = new ArrayList<>();
        switch (command) {
            case "read":
                // To read data
                String range = "Sheet1!B2:C12";
                ValueRange response = sheetsService.spreadsheets().values()
                        .get(SPREADSHEET_ID, range)
                        .execute();

                List<List<Object>> values = response.getValues();
                int counter = 1;

                if(values == null || values.isEmpty()) {
                    System.out.println("No data found");
                } else {
                    System.out.println("Place, Name, Time:");
                    for (List row: values) {
                        System.out.printf("%s came in %s. with a time of %s\n", row.get(0), counter, row.get(1));
                        counter ++;
                    }
                }
//              double x = (double) Integer.parseInt((String) values.get(5).get(0));
//              System.out.println(x);
                System.out.println("");
                double newNum = 14.5;
                for (int i = 0; i<values.size(); i++) {

                    if (newNum < Double.parseDouble((String) values.get(i).get(2))) {
                        System.out.println("\ngonna move");
//                      x = 6;
//                      move();
                        break;
                    } else {
                        System.out.println(i);
                    }
                }
                break;
            case "write":
                // To write data:
                ValueRange appendBody = new ValueRange()
                        .setValues(Arrays.asList(
                                Arrays.asList("Bob", "20", "Unidentified", "did this work?")
                        ));

                AppendValuesResponse appendResults = sheetsService.spreadsheets().values()
                        .append(SPREADSHEET_ID, "Sheet1", appendBody)
                        .setValueInputOption("USER_ENTERED")
                        .setInsertDataOption("INSERT_ROWS")
                        .setIncludeValuesInResponse(true)
                        .execute();
                break;
            case "edit":
                // To edit data:
                ValueRange body = new ValueRange()
                        .setValues(Arrays.asList(
                                Arrays.asList("addedByUpdate", "asdf")
                        ));

                UpdateValuesResponse result = sheetsService.spreadsheets().values()
                        .update(SPREADSHEET_ID, "B3:C3", body)
                        .setValueInputOption("RAW")
                        .execute();
                break;
            case "delete":
                // To delete data:  (Rows or Columns ONLY)
                DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                        .setRange(
                                new DimensionRange()
                                    .setSheetId(0)
                                .setDimension("CELL")
                                .setStartIndex(2)
                                .setEndIndex(3)
                        );

                requests.add(new Request().setDeleteDimension(deleteRequest));
                break;
            case "move":
                 //To move rows:
                MoveDimensionRequest moved = new MoveDimensionRequest()
                        .setSource(
                                new DimensionRange()
                                    .setSheetId(0)
                                .setDimension("ROWS")
                                .setStartIndex(7)
                                .setEndIndex(11)
                        )
                        .setDestinationIndex(12);

                requests.add(new Request().setMoveDimension(moved));
                break;

        }

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();

        // To delete specific cells range:
//        DeleteRangeRequest deleteRequest = new DeleteRangeRequest()
//                .setRange(
//                        new GridRange()
//                            .setSheetId(0)
//                            .setStartColumnIndex(0)
//                            .setStartRowIndex(6)
//                            .setEndColumnIndex(3)
//                            .setEndRowIndex(7)
//                )
//                .setShiftDimension("ROWS");
//
//        List<Request> requests = new ArrayList<>();
//        requests.add(new Request().setDeleteRange(deleteRequest));
//
//        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
//        sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();
    }


}
