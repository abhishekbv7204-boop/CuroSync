package com.curasync.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class DBConnection {
    private static final String URL = "jdbc:sqlite:curasync_database.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(URL);
            // WAL mode allows concurrent reads with one writer; busy_timeout retries on lock
            try (Statement s = conn.createStatement()) {
                s.execute("PRAGMA journal_mode=WAL");
                s.execute("PRAGMA busy_timeout=5000");
            }
            initializeDatabase(conn);
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found!");
            throw new SQLException("Driver not found");
        }
    }

    private static void initializeDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Drop Hospitals table if it is of the old schema (checking if district column exists)
            try {
                stmt.execute("SELECT district FROM Hospitals LIMIT 1");
            } catch (SQLException e) {
                stmt.execute("DROP TABLE IF EXISTS Hospitals");
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS Patients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, age INTEGER, gender TEXT, contact TEXT, medical_history TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Doctors (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, specialization TEXT NOT NULL, contact TEXT, availability_status BOOLEAN DEFAULT 1, hospital_id INTEGER)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Hospitals (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, location_node INTEGER NOT NULL, contact TEXT, lat REAL, lng REAL, specializations TEXT, address TEXT, district TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Areas (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, lat REAL NOT NULL, lng REAL NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Appointments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INTEGER NOT NULL, doctor_id INTEGER NOT NULL, " +
                    "appointment_date DATE NOT NULL, time_slot TEXT NOT NULL, status TEXT DEFAULT 'Scheduled')");

            stmt.execute("CREATE TABLE IF NOT EXISTS EmergencyCases (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_name TEXT NOT NULL, severity_level INTEGER NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, status TEXT DEFAULT 'Pending')");

            // Seed default hospitals if empty
            var rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Hospitals");
            if (rs.next() && rs.getInt("count") == 0) {
                stmt.execute("INSERT INTO Hospitals (name, location_node, contact, lat, lng, specializations, address, district) VALUES " +
                        "('Ballari District Hospital', 0, '08192-111111', 15.1394, 76.9214, 'Cardiology,General Medicine', 'Ballari, Karnataka', 'Karnataka')," +
                        "('Hospet General Hospital', 1, '08932-222222', 15.2694, 76.3570, 'Neurology,Orthopedics,General Medicine', 'Hospet, Karnataka', 'Karnataka')," +
                        "('Mysore City Hospital', 2, '0821-333333', 12.2958, 76.6394, 'Pediatrics,Oncology,General Medicine', 'Mysuru, Karnataka', 'Karnataka')," +
                        "('Bengaluru Metro Hospital', 3, '080-4444444', 12.9716, 77.5946, 'Cardiology,Neurology,Trauma,General Medicine', 'Bengaluru, Karnataka', 'Karnataka')");
            }

            var rsDoc = stmt.executeQuery("SELECT COUNT(*) AS count FROM Doctors");
            if (rsDoc.next() && rsDoc.getInt("count") == 0) {
                stmt.execute("INSERT INTO Doctors (name, specialization, contact, hospital_id) VALUES " +
                        "('Dr. Alice Smith', 'Cardiology', '123-123-1234', 1)," +
                        "('Dr. Bob Jones', 'Neurologist', '234-234-2345', 2)," +
                        "('Dr. Charlie Brown', 'Pediatrician', '345-345-3456', 3)," +
                        "('Dr. Deepa Rao', 'General Medicine', '456-456-4567', 4)");
            }

            var rsAreas = stmt.executeQuery("SELECT COUNT(*) AS count FROM Areas");
            if (rsAreas.next() && rsAreas.getInt("count") == 0) {
                stmt.execute("INSERT INTO Areas (name, lat, lng) VALUES " +
                        "('Ballari Institute of Technology and Management', 15.1394, 76.9214)," +
                        "('Hospet', 15.2694, 76.3570)," +
                        "('Mysore', 12.2958, 76.6394)," +
                        "('Bengaluru', 12.9716, 77.5946)");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate doctors for a hospital using an EXISTING connection (no new connection opened).
     * This avoids SQLITE_BUSY when called inside an already-open transaction.
     */
    public static void generateDoctorsForHospital(Connection conn, int hospitalId, String hospitalName, String specializations) {
        String[] specArr = specializations.split(",");
        String[] firstNames = {"Amit", "Rajesh", "Sunita", "Priya", "Vikram", "Anjali", "Sanjay", "Neha", "Rohan", "Karan"};
        String[] lastNames  = {"Sharma", "Verma", "Patel", "Mehta", "Singh", "Joshi", "Rao", "Nair", "Das", "Reddy"};

        java.util.Random rand = new java.util.Random(hospitalId);
        String sql = "INSERT INTO Doctors (name, specialization, contact, availability_status, hospital_id) VALUES (?, ?, ?, 1, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String spec : specArr) {
                spec = spec.trim();
                if (spec.isEmpty()) continue;
                String name    = "Dr. " + firstNames[rand.nextInt(firstNames.length)] + " " + lastNames[rand.nextInt(lastNames.length)];
                String contact = "+91 " + (7000000000L + rand.nextInt(299999999));
                stmt.setString(1, name);
                stmt.setString(2, spec);
                stmt.setString(3, contact);
                stmt.setInt(4, hospitalId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DBConnection] generateDoctorsForHospital error: " + e.getMessage());
        }
    }

    /** Overload kept for backward compatibility — opens its own connection. */
    public static void generateDoctorsForHospital(int hospitalId, String hospitalName, String specializations) {
        try (Connection conn = getConnection()) {
            generateDoctorsForHospital(conn, hospitalId, hospitalName, specializations);
        } catch (SQLException e) {
            System.err.println("[DBConnection] generateDoctorsForHospital (standalone) error: " + e.getMessage());
        }
    }
}
