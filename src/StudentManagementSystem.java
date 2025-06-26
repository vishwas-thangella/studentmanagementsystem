import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentManagementSystem extends JFrame {
    private JTextField nameField, rollField, courseField;
    private DefaultTableModel tableModel;
    private JTable studentTable;
    private Connection conn;

    public StudentManagementSystem() {
        setTitle("Student Management System");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        connectToDatabase();
        createTableIfNotExists();

        JLabel nameLabel = new JLabel("Name:");
        JLabel rollLabel = new JLabel("Roll No:");
        JLabel courseLabel = new JLabel("Course:");

        nameField = new JTextField(15);
        rollField = new JTextField(10);
        courseField = new JTextField(10);

        JButton addButton = new JButton("Add Student");
        JButton deleteButton = new JButton("Delete Selected");

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Roll No", "Course"}, 0);
        studentTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(studentTable);

        JPanel formPanel = new JPanel();
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(rollLabel);
        formPanel.add(rollField);
        formPanel.add(courseLabel);
        formPanel.add(courseField);
        formPanel.add(addButton);
        formPanel.add(deleteButton);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadStudentData();

        addButton.addActionListener(e -> addStudent());
        deleteButton.addActionListener(e -> deleteStudent());

        setVisible(true);
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/student_db";
            String username = "root";
            String password = "your_password_here";
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void createTableIfNotExists() {
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS students (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "roll VARCHAR(50) NOT NULL, " +
                    "course VARCHAR(100) NOT NULL)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStudentData() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM students")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String roll = rs.getString("roll");
                String course = rs.getString("course");
                tableModel.addRow(new Object[]{id, name, roll, course});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addStudent() {
        String name = nameField.getText().trim();
        String roll = rollField.getText().trim();
        String course = courseField.getText().trim();

        if (name.isEmpty() || roll.isEmpty() || course.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO students (name, roll, course) VALUES (?, ?, ?)")) {
            pstmt.setString(1, name);
            pstmt.setString(2, roll);
            pstmt.setString(3, course);
            pstmt.executeUpdate();
            tableModel.setRowCount(0);
            loadStudentData();
            nameField.setText("");
            rollField.setText("");
            courseField.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int studentId = (int) tableModel.getValueAt(selectedRow, 0);

        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM students WHERE id = ?")) {
            pstmt.setInt(1, studentId);
            pstmt.executeUpdate();
            tableModel.setRowCount(0);
            loadStudentData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentManagementSystem::new);
    }
}
