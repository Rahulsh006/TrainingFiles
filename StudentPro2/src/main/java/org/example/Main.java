package org.example;

import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        // Step 1: Read from Excel and Convert to JSON
        List<Student> students = readExcelFile("C:\\Users\\RahulSH\\IdeaProjects\\StudentPro2\\src\\main\\resources\\studentdata.xlsx");
        if (students.isEmpty()) {
            System.out.println("No students found in Excel file.");
            return;
        }

        String jsonData = convertStudentsToJSON(students);
        System.out.println("Complete Student Data in JSON Format: " + jsonData);

        // Step 2: Store each student in MySQL Database
        for (Student student : students) {
            saveStudentToDatabase(student);
        }

        // Step 3: Search for a student by admission number or name
        Scanner scanner = new Scanner(System.in);
        System.out.println("Search by (1) Admission Number or (2) Name?");
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        String searchKey;
        if (choice == 1) {
            System.out.print("Enter Admission Number: ");
            searchKey = scanner.nextLine();
        } else if (choice == 2) {
            System.out.print("Enter Name: ");
            searchKey = scanner.nextLine();
        } else {
            System.out.println("Invalid choice. Exiting.");
            return;
        }

        String studentData = findStudentByAdmissionNumberOrName(searchKey);
        System.out.println("Searched Student: " + studentData);
    }

    // Method to read the Excel file and return a list of Student objects
    public static List<Student> readExcelFile(String excelFileName) {
        List<Student> students = new ArrayList<>();

        try (InputStream excelFile = new FileInputStream(excelFileName);
             Workbook workbook = new XSSFWorkbook(excelFile)) {

            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet

            // Iterate over rows, skipping the header row
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                // Extract cell values
                String name = getCellValueAsString(row.getCell(0));
                String admissionNumber = getCellValueAsString(row.getCell(1));
                int physicsMark = getCellValueAsInt(row.getCell(2));
                int chemistryMark = getCellValueAsInt(row.getCell(3));
                int mathsMark = getCellValueAsInt(row.getCell(4));

                // Add to student list
                students.add(new Student(name, admissionNumber, physicsMark, chemistryMark, mathsMark));
            }
        } catch (Exception e) {
            System.err.println("Error reading Excel file: " + e.getMessage());
        }

        return students;
    }

    // Method to convert a list of students to JSON format using Gson
    public static String convertStudentsToJSON(List<Student> students) {
        Gson gson = new Gson();
        return gson.toJson(students);
    }

    // Method to save a student into the MySQL database
    public static void saveStudentToDatabase(Student student) {
        // Check if the student already exists
        if (studentExists(student.getAdmissionNumber())) {
//            System.out.println("Student with Admission Number " + student.getAdmissionNumber() + " already exists.");
            return;
        }

        String sql = "INSERT INTO students (name, admission_number, physics_mark, chemistry_mark, maths_mark) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, student.getName());
            preparedStatement.setString(2, student.getAdmissionNumber());
            preparedStatement.setInt(3, student.getPhysicsMark());
            preparedStatement.setInt(4, student.getChemistryMark());
            preparedStatement.setInt(5, student.getMathsMark());

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Rows inserted: " + rowsAffected);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    // Method to check if a student already exists in the database
    public static boolean studentExists(String admissionNumber) {
        String sql = "SELECT COUNT(*) FROM students WHERE admission_number = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, admissionNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }

        return false;
    }

    // Method to find a student by admission number or name
    public static String findStudentByAdmissionNumberOrName(String searchKey) {
        String sql = "SELECT * FROM students WHERE admission_number = ? OR name = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, searchKey);
            preparedStatement.setString(2, searchKey);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<Student> foundStudents = new ArrayList<>();
            while (resultSet.next()) {
                Student student = new Student(
                        resultSet.getString("name"),
                        resultSet.getString("admission_number"),
                        resultSet.getInt("physics_mark"),
                        resultSet.getInt("chemistry_mark"),
                        resultSet.getInt("maths_mark")
                );
                foundStudents.add(student);
            }

            Gson gson = new Gson();
            return foundStudents.isEmpty() ? "{ \"message\": \"Student not found!\" }" : gson.toJson(foundStudents);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }

        return "{ \"message\": \"Error retrieving student data\" }";
    }

    // Method to establish a connection to MySQL database
    public static Connection getConnection() {
        String url = "jdbc:mysql://localhost:3306/student_db";
        String user = "root";
        String password = "root";  // Set your MySQL password here

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
            return null;
        }
    }

    // Utility method to get cell value as String
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue()); // Convert to int if numeric
            default:
                return "";
        }
    }

    // Utility method to get cell value as int
    private static int getCellValueAsInt(Cell cell) {
        if (cell == null) {
            return 0;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }
}
