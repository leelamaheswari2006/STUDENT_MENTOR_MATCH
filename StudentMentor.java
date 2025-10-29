import java.sql.*;
import java.util.Scanner;

public class MentorMatchApp {

    static final String URL =
        "jdbc:mysql://localhost:3306/mentor_match?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static final String USER = "root";          // change this
    static final String PASS = "your_password"; // change this

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=== Mentor Match Console ===");
        System.out.println("1. Sign Up");
        System.out.println("2. Login");
        int choice = sc.nextInt(); sc.nextLine();

        switch (choice) {
            case 1 -> signup(sc);
            case 2 -> login(sc);
            default -> System.out.println("Invalid choice");
        }
    }

    static Connection getConnection() throws SQLException {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new SQLException("Driver not found"); }
        return DriverManager.getConnection(URL, USER, PASS);
    }

    static void signup(Scanner sc) {
        try (Connection con = getConnection()) {
            System.out.print("Are you Student or Mentor? ");
            String role = sc.nextLine().trim();

            System.out.print("Username: ");
            String username = sc.nextLine();
            System.out.print("Password: ");
            String password = sc.nextLine();
            System.out.print("Email: ");
            String email = sc.nextLine();

            String sql = role.equalsIgnoreCase("mentor")
                    ? "INSERT INTO mentors(username,password,email) VALUES (?,?,?)"
                    : "INSERT INTO students(username,password,email) VALUES (?,?,?)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, email);
                ps.executeUpdate();
                System.out.println("Signup successful!");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    static void login(Scanner sc) {
        try (Connection con = getConnection()) {
            System.out.print("Are you Student or Mentor? ");
            String role = sc.nextLine().trim();

            System.out.print("Username: ");
            String username = sc.nextLine();
            System.out.print("Password: ");
            String password = sc.nextLine();

            String table = role.equalsIgnoreCase("mentor") ? "mentors" : "students";
            String idCol = role.equalsIgnoreCase("mentor") ? "mentor_id" : "student_id";
            String sql = "SELECT " + idCol + " FROM " + table + " WHERE username=? AND password=?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("Login successful!");
                    if (role.equalsIgnoreCase("student"))
                        studentMenu(con, rs.getInt(idCol), sc);
                    else
                        mentorMenu(con, rs.getInt(idCol), sc);
                } else {
                    System.out.println("Invalid login");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    static void studentMenu(Connection con, int studentId, Scanner sc) throws SQLException {
        System.out.println("=== Student Menu ===");
        System.out.print("Team name: ");
        String team = sc.nextLine();
        System.out.print("Project title: ");
        String title = sc.nextLine();
        System.out.print("Description: ");
        String desc = sc.nextLine();

        System.out.println("Available mentors:");
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT mentor_id, username FROM mentors")) {
            while (rs.next()) {
                System.out.println(rs.getInt(1) + " - " + rs.getString(2));
            }
        }
        System.out.print("Choose mentor id: ");
        int mid = sc.nextInt(); sc.nextLine();

        String sql = """
            INSERT INTO student_projects(student_id,team_name,project_title,project_description,chosen_mentor_id)
            VALUES (?,?,?,?,?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, team);
            ps.setString(3, title);
            ps.setString(4, desc);
            ps.setInt(5, mid);
            ps.executeUpdate();
            System.out.println("Project submitted to mentor!");
        }
    }

    static void mentorMenu(Connection con, int mentorId, Scanner sc) throws SQLException {
        System.out.println("=== Mentor Menu ===");
        System.out.println("1. View Pending Requests");
        System.out.println("2. Accept/Reject Request");
        int ch = sc.nextInt(); sc.nextLine();

        switch (ch) {
            case 1 -> {
                String q = "SELECT project_id, team_name, project_title, status FROM student_projects WHERE chosen_mentor_id=?";
                try (PreparedStatement ps = con.prepareStatement(q)) {
                    ps.setInt(1, mentorId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            System.out.printf("%d | %s | %s | %s%n",
                                    rs.getInt("project_id"),
                                    rs.getString("team_name"),
                                    rs.getString("project_title"),
                                    rs.getString("status"));
                        }
                    }
                }
            }
            case 2 -> {
                System.out.print("Enter project id: ");
                int pid = sc.nextInt(); sc.nextLine();
                System.out.print("Accept or Reject: ");
                String act = sc.nextLine();
                String newStatus = act.equalsIgnoreCase("accept") ? "Accepted" : "Rejected";
                String u = "UPDATE student_projects SET status=? WHERE project_id=?";
                try (PreparedStatement ps = con.prepareStatement(u)) {
                    ps.setString(1, newStatus);
                    ps.setInt(2, pid);
                    ps.executeUpdate();
                }
                if (newStatus.equals("Accepted")) {
                    String ins = "INSERT INTO mentor_team_map(mentor_id,project_id) VALUES(?,?)";
                    try (PreparedStatement ps2 = con.prepareStatement(ins)) {
                        ps2.setInt(1, mentorId);
                        ps2.setInt(2, pid);
                        ps2.executeUpdate();
                    }
                }
                System.out.println("Updated!");
            }
        }
    }
}
