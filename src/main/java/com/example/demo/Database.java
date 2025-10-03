package com.example.demo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private final String url;

    // Default db file athletes.db
    public Database() {
        this("jdbc:sqlite:athletes.db");
    }

    public Database(String jdbcUrl) {
        this.url = jdbcUrl;
    }

    // Δημιουργεί το πίνακα αν δεν υπάρχει
    public void initialize() {
        String sql = "CREATE TABLE IF NOT EXISTS athletes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "firstName TEXT NOT NULL," +
                "lastName TEXT NOT NULL," +
                "sport TEXT NOT NULL)";

        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite driver loaded.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(url);
             Statement st = conn.createStatement()) {
            st.execute(sql);
            System.out.println("Table 'athletes' initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Προσθήκη: αν athlete.getId() > 0 προσπαθεί να εισάγει με το id, αλλιώς παίρνει generated key
    public int addAthlete(Athlete a) {
        if (a == null) return -1;
        String withId = "INSERT INTO athletes(id, firstName, lastName, sport) VALUES (?, ?, ?, ?)";
        String noId = "INSERT INTO athletes(firstName, lastName, sport) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url)) {
            if (a.getId() > 0) {
                try (PreparedStatement ps = conn.prepareStatement(withId)) {
                    ps.setInt(1, a.getId());
                    ps.setString(2, a.getFirstName());
                    ps.setString(3, a.getLastName());
                    ps.setString(4, a.getSport());
                    ps.executeUpdate();
                    return a.getId();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(noId, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, a.getFirstName());
                    ps.setString(2, a.getLastName());
                    ps.setString(3, a.getSport());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            int id = rs.getInt(1);
                            a.setId(id);
                            return id;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Φόρτωμα όλων
    public List<Athlete> getAllAthletes() {
        List<Athlete> list = new ArrayList<>();
        String sql = "SELECT id, firstName, lastName, sport FROM athletes ORDER BY id";
        try (Connection conn = DriverManager.getConnection(url);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Athlete(
                        rs.getInt("id"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("sport")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Ενημέρωση (update) με βάση id
    public void updateAthlete(Athlete athlete) {
        String sql = "UPDATE athletes SET firstName = ?, lastName = ?, sport = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, athlete.getFirstName());
            ps.setString(2, athlete.getLastName());
            ps.setString(3, athlete.getSport());
            ps.setInt(4, athlete.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Διαγραφή κατά id
    public void deleteAthleteById(int id) {
        String sql = "DELETE FROM athletes WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Διαγραφή με δυναμικά κριτήρια (επιστρέφει πλήθος διαγραμμένων)
    public int deleteAthleteByCriteria(Integer id, String firstName, String lastName, String sport) {
        StringBuilder sb = new StringBuilder("DELETE FROM athletes WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (id != null) { sb.append(" AND id = ?"); params.add(id); }
        if (firstName != null && !firstName.isEmpty()) { sb.append(" AND LOWER(firstName) = LOWER(?)"); params.add(firstName); }
        if (lastName != null && !lastName.isEmpty()) { sb.append(" AND LOWER(lastName) = LOWER(?)"); params.add(lastName); }
        if (sport != null && !sport.isEmpty()) { sb.append(" AND LOWER(sport) = LOWER(?)"); params.add(sport); }

        if (params.isEmpty()) return 0; // τίποτα για διαγραφή

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
