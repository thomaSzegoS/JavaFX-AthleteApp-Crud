package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;

import java.sql.*;
import java.util.Optional;

public class Controller {

    // FXML UI Components
    @FXML private TextField idField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private ComboBox<String> sportComboBox;

    @FXML private TextField idField1;
    @FXML private TextField firstNameField1;
    @FXML private TextField lastNameField1;
    @FXML private ComboBox<String> sportComboBox1;

    @FXML private TableView<Athlete> tableView;
    @FXML private TableColumn<Athlete, Integer> idColumn;
    @FXML private TableColumn<Athlete, String> firstNameColumn;
    @FXML private TableColumn<Athlete, String> lastNameColumn;
    @FXML private TableColumn<Athlete, String> sportColumn;

    private ObservableList<Athlete> athleteList;
    private Database db;

    @FXML
    public void initialize() {
        db = new Database();
        db.initialize();

        idField.setDisable(true);
        idField.setPromptText("" + db.getNextId());

        // Φόρτωση από τη βάση
        athleteList = FXCollections.observableArrayList(db.getAllAthletes());
        tableView.setItems(athleteList);

        // Σύνδεση των στηλών με τα πεδία της κλάσης Athlete
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        firstNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLastName()));
        sportColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSport()));

        // ComboBox options
        ObservableList<String> sportSelection = FXCollections.observableArrayList(
                "Ποδόσφαιρο", "Μπάσκετ", "Βόλεϊ", "Στίβος", "Κολύμβηση"
        );
        sportComboBox.getItems().addAll(sportSelection);


        // Διαγραφή η επεξεργασία αθλητή onAction δεξί κλικ
        ContextMenu contextMenu = new ContextMenu();

        MenuItem deleteItem = new MenuItem("Διαγραφή");
        deleteItem.setOnAction(e -> {
            Athlete selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDeleteRightClick(selected);
            }
        });

        MenuItem editItem = new MenuItem("Επεξεργασία");
        editItem.setOnAction(e -> {
            Athlete selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditDialog(selected);
            }
        });

        contextMenu.getItems().addAll(deleteItem, editItem);
        // Σύνδεση του menu με το TableView
        tableView.setContextMenu(contextMenu);


        // Διπλό κλικ για επεξεργασία
        tableView.setRowFactory(tv -> {
            TableRow<Athlete> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    Athlete selected = row.getItem();
                    showEditDialog(selected);
                }
            });
            return row;
        });
    }

    private void showEditDialog(Athlete athlete) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Επεξεργασία Αθλητή");

        TextField firstNameField = new TextField(athlete.getFirstName());
        TextField lastNameField = new TextField(athlete.getLastName());
        ChoiceBox<String> sportChoice = new ChoiceBox<>(FXCollections.observableArrayList(
                "Ποδόσφαιρο", "Μπάσκετ", "Βόλεϊ", "Στίβος", "Κολύμβηση"
        ));
        sportChoice.setValue(athlete.getSport());

        GridPane grid = new GridPane();
        grid.add(new Label("Όνομα:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Επώνυμο:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Άθλημα:"), 0, 2);
        grid.add(sportChoice, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            athlete.setFirstName(firstNameField.getText());
            athlete.setLastName(lastNameField.getText());
            athlete.setSport(sportChoice.getValue());

            db.updateAthlete(athlete); // Αποθήκευση στη βάση
            tableView.refresh();        // Ενημέρωση TableView
        }
    }

    @FXML
    private void handleAdd() {
        try {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String sport = sportComboBox.getValue();

            if (firstName.isEmpty() || lastName.isEmpty() || sport == null) {
                showAlert("Σφάλμα", "Συμπλήρωσε όλα τα πεδία!");
                return;
            }

            // Δημιουργία αθλητή χωρίς id
            Athlete athlete = new Athlete(0, firstName, lastName, sport);
            // Το id επιστρέφεται και ορίζεται αυτόματα από τη βάση
            int generatedId = db.addAthlete(athlete);
            athlete.setId(generatedId);
            // Προσθήκη αθλητή στη λίστα
            athleteList.add(athlete);
            // Προσθήκη αθλητή στη βάση δεδομένων
            db.addAthlete(athlete);

            // Καθάρισμα πεδίων μετά την εισαγωγή
            idField.clear();
            firstNameField.clear();
            lastNameField.clear();
            sportComboBox.setValue(null);
            sportComboBox.setPromptText("ΑΘΛΗΜΑ");

        } catch (NumberFormatException e) {
            showAlert("Σφάλμα", "Το ID πρέπει να είναι αριθμός!");
        }
    }

    @FXML
    private void handleDelete() {
        String idText = idField1.getText().trim();
        String firstName = firstNameField1.getText().trim();
        String lastName = lastNameField1.getText().trim();
        String sport = sportComboBox1.getValue();

        if (idText.isEmpty() && firstName.isEmpty() && lastName.isEmpty() && (sport == null || sport.isEmpty())) {
            showAlert("Σφάλμα", "Συμπλήρωσε τουλάχιστον ένα πεδίο για διαγραφή.");
            return;
        }

        try {
            Integer id = null;
            if (!idText.isEmpty()) {
                try {
                    id = Integer.parseInt(idText);
                } catch (NumberFormatException e) {
                    showAlert("Σφάλμα", "Το ID πρέπει να είναι αριθμός!");
                    return;
                }
            }

            // Διαγραφή από τη βάση
            int deleted = db.deleteAthleteByCriteria(id, firstName, lastName, sport);

            if (deleted == 0) {
                showAlert("Προσοχή", "Δεν βρέθηκε καμία εγγραφή για διαγραφή.");
            }

            // Διαγραφή με πολλαπλά πεδία ταυτόχρονα
            athleteList.removeIf(athlete ->
                    (idText.isEmpty() || athlete.getId() == Integer.parseInt(idText)) &&
                            (firstName.isEmpty() || athlete.getFirstName().equalsIgnoreCase(firstName)) &&
                            (lastName.isEmpty() || athlete.getLastName().equalsIgnoreCase(lastName)) &&
                            (sport == null || sport.isEmpty() || athlete.getSport().equalsIgnoreCase(sport))
            );

            // Καθάρισμα textFields μετά τη διαγραφή
            idField1.clear();
            firstNameField1.clear();
            lastNameField1.clear();
            sportComboBox1.setValue(null);

        } catch (Exception e) {
            showAlert("Σφάλμα", "Σφάλμα κατά τη διαγραφή!");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteRightClick(Athlete athlete) {
        if (athlete != null) {
            athleteList.remove(athlete);
            // Διαγραφή από τη βάση
            db.deleteAthleteById(athlete.getId());
        } else {
            showAlert("Σφάλμα", "Επέλεξε πρώτα έναν αθλητή για διαγραφή!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

