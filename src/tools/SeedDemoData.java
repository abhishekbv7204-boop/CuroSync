import com.curasync.utils.DBConnection;
import java.sql.*;

/**
 * Seeds the CuraSync database with rich demo data across all modules:
 * Patients, Doctors, Hospitals, Appointments, Emergency Cases
 */
public class SeedDemoData {
    public static void main(String[] args) throws Exception {
        System.out.println("=== CuraSync Demo Data Seeder ===");
        try (Connection conn = DBConnection.getConnection()) {
            seedPatients(conn);
            seedDoctors(conn);
            seedAppointments(conn);
            seedEmergencies(conn);
        }
        System.out.println("\n✅ All demo data seeded successfully!");
    }

    static void seedPatients(Connection conn) throws SQLException {
        System.out.println("\n[1/4] Seeding Patients...");
        conn.createStatement().execute("DELETE FROM Patients");
        String sql = "INSERT INTO Patients (name, age, gender, contact, medical_history) VALUES (?,?,?,?,?)";
        String[][] data = {
            {"Ravi Kumar",      "34", "Male",   "+91 9845123456", "Hypertension, Diabetes Type 2"},
            {"Priya Sharma",    "28", "Female", "+91 9741234567", "Asthma, Allergic Rhinitis"},
            {"Mohammed Irfan",  "52", "Male",   "+91 9632145678", "Cardiac Arrhythmia, High Cholesterol"},
            {"Sunita Devi",     "45", "Female", "+91 9523456789", "Hypothyroidism, Osteoporosis"},
            {"Arun Patel",      "19", "Male",   "+91 9412345670", "Appendicitis (post-op)"},
            {"Lakshmi Bai",     "67", "Female", "+91 9301234561", "Knee Osteoarthritis, Anaemia"},
            {"Deepak Reddy",    "41", "Male",   "+91 9190123452", "Peptic Ulcer, Anxiety Disorder"},
            {"Kavitha Nair",    "33", "Female", "+91 9089012343", "Migraine, PCOS"},
            {"Suresh Gowda",    "58", "Male",   "+91 9978901234", "Chronic Kidney Disease Stage 3"},
            {"Ananya Singh",    "25", "Female", "+91 9867890125", "Iron Deficiency Anaemia"},
            {"Ramesh Yadav",    "72", "Male",   "+91 9756789016", "Parkinson's Disease, Hypertension"},
            {"Meena Kumari",    "39", "Female", "+91 9645678907", "Breast Cancer (Stage 1, on chemo)"},
            {"Vijay Krishnan",  "47", "Male",   "+91 9534567898", "Spinal Stenosis, Disc Herniation"},
            {"Farida Begum",    "31", "Female", "+91 9423456789", "Gestational Diabetes"},
            {"Sanjay Bhatt",    "55", "Male",   "+91 9312345670", "COPD, Smoking history 20 yrs"},
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] row : data) {
                ps.setString(1, row[0]); ps.setInt(2, Integer.parseInt(row[1]));
                ps.setString(3, row[2]); ps.setString(4, row[3]); ps.setString(5, row[4]);
                ps.executeUpdate();
            }
        }
        System.out.println("   ✓ 15 patients added");
    }

    static void seedDoctors(Connection conn) throws SQLException {
        System.out.println("[2/4] Seeding Doctors...");
        conn.createStatement().execute("DELETE FROM Doctors");
        String sql = "INSERT INTO Doctors (name, specialization, contact, availability_status, hospital_id) VALUES (?,?,?,?,?)";
        String[][] data = {
            {"Dr. Anand Mehta",      "Cardiology",       "+91 9811001001", "1", "1"},
            {"Dr. Sneha Prabhu",     "Neurology",        "+91 9822002002", "1", "1"},
            {"Dr. Ranjit Sharma",    "Orthopedics",      "+91 9833003003", "1", "2"},
            {"Dr. Faiza Khan",       "Pediatrics",       "+91 9844004004", "1", "2"},
            {"Dr. Vikram Naidu",     "Oncology",         "+91 9855005005", "0", "3"},
            {"Dr. Deepa Iyer",       "General Medicine", "+91 9866006006", "1", "3"},
            {"Dr. Sunil Joshi",      "Trauma Surgery",   "+91 9877007007", "1", "4"},
            {"Dr. Preethi Rao",      "Cardiology",       "+91 9888008008", "1", "4"},
            {"Dr. Abdul Kalam",      "Neurology",        "+91 9899009009", "0", "1"},
            {"Dr. Nirmala Devi",     "General Medicine", "+91 9810010010", "1", "2"},
            {"Dr. Harish Babu",      "Orthopedics",      "+91 9821011011", "1", "3"},
            {"Dr. Sushma Verma",     "Oncology",         "+91 9832012012", "1", "4"},
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] row : data) {
                ps.setString(1, row[0]); ps.setString(2, row[1]);
                ps.setString(3, row[2]); ps.setBoolean(4, row[3].equals("1")); ps.setInt(5, Integer.parseInt(row[4]));
                ps.executeUpdate();
            }
        }
        System.out.println("   ✓ 12 doctors added (10 available, 2 on leave)");
    }

    static void seedAppointments(Connection conn) throws SQLException {
        System.out.println("[3/4] Seeding Appointments...");
        conn.createStatement().execute("DELETE FROM Appointments");
        String sql = "INSERT INTO Appointments (patient_id, doctor_id, appointment_date, time_slot, status) VALUES (?,?,?,?,?)";
        String[][] data = {
            {"1",  "1",  "2026-06-12", "09:00 AM", "Scheduled"},
            {"2",  "2",  "2026-06-12", "10:30 AM", "Scheduled"},
            {"3",  "8",  "2026-06-12", "11:00 AM", "In Progress"},
            {"4",  "6",  "2026-06-12", "02:00 PM", "Scheduled"},
            {"5",  "7",  "2026-06-12", "03:30 PM", "Scheduled"},
            {"6",  "3",  "2026-06-13", "09:00 AM", "Scheduled"},
            {"7",  "6",  "2026-06-13", "10:00 AM", "Scheduled"},
            {"8",  "2",  "2026-06-13", "11:30 AM", "Scheduled"},
            {"9",  "10", "2026-06-14", "09:00 AM", "Scheduled"},
            {"10", "4",  "2026-06-14", "10:00 AM", "Scheduled"},
            {"11", "2",  "2026-06-10", "09:00 AM", "Completed"},
            {"12", "5",  "2026-06-10", "11:00 AM", "Completed"},
            {"13", "3",  "2026-06-11", "02:00 PM", "Completed"},
            {"14", "6",  "2026-06-11", "03:00 PM", "Completed"},
            {"15", "7",  "2026-06-11", "04:00 PM", "Cancelled"},
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] row : data) {
                ps.setInt(1, Integer.parseInt(row[0])); ps.setInt(2, Integer.parseInt(row[1]));
                ps.setDate(3, java.sql.Date.valueOf(row[2])); ps.setString(4, row[3]); ps.setString(5, row[4]);
                ps.executeUpdate();
            }
        }
        System.out.println("   ✓ 15 appointments added (10 upcoming, 4 completed, 1 cancelled)");
    }

    static void seedEmergencies(Connection conn) throws SQLException {
        System.out.println("[4/4] Seeding Emergency Cases...");
        conn.createStatement().execute("DELETE FROM EmergencyCases");
        String sql = "INSERT INTO EmergencyCases (patient_name, severity_level, status) VALUES (?,?,?)";
        String[][] data = {
            {"Rajesh Nayak",      "1", "Pending"},   // Critical
            {"Usha Kumari",       "2", "Pending"},   // Serious
            {"Pavan Kumar",       "1", "In Progress"},
            {"Shalini Desai",     "3", "Pending"},
            {"Mohammed Yusuf",    "2", "In Progress"},
            {"Geetha Devi",       "4", "Pending"},
            {"Ramu Swamy",        "1", "Pending"},   // Critical
            {"Chandrakala",       "3", "Resolved"},
            {"Subbaiah Gowda",    "5", "Resolved"},
            {"Hema Malini",       "2", "Pending"},
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] row : data) {
                ps.setString(1, row[0]); ps.setInt(2, Integer.parseInt(row[1])); ps.setString(3, row[2]);
                ps.executeUpdate();
            }
        }
        System.out.println("   ✓ 10 emergency cases added (3 critical, pending triage)");
    }
}
