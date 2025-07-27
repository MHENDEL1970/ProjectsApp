package projects;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.exception.DbException;
import projects.service.ProjectService;
import projects.entity.Project;

public class ProjectsApp {

    private Scanner scanner = new Scanner(System.in);
    private ProjectService projectService = new ProjectService();

    private List<String> operations = List.of(
        "1) Add a project"
    );

    public static void main(String[] args) {
        new ProjectsApp().processUserSelections();
    }

    private void processUserSelections() {
        boolean done = false;
        while (!done) {
            try {
                int selection = getUserSelection();
                switch (selection) {
                    case -1:
                        done = exitMenu();
                        break;
                    case 1:
                        createProject();
                        break;
                    default:
                        System.out.println("\n" + selection + " is not a valid selection. Try again.");
                }
            } catch (Exception e) {
                System.out.println("\nError: " + e.toString());
            }
        }
    }

    private void createProject() {
        String projectName = getStringInput("Enter the project name");
        java.math.BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
        java.math.BigDecimal actualHours = getDecimalInput("Enter the actual hours");
        Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
        String notes = getStringInput("Enter the project notes");

        Project project = new Project();
        project.setProjectName(projectName);
        project.setEstimatedHours(estimatedHours);
        project.setActualHours(actualHours);
        project.setDifficulty(difficulty);
        project.setNotes(notes);

        Project dbProject = projectService.addProject(project);
        System.out.println("\nYou have successfully created project: " + dbProject);
    }

    private java.math.BigDecimal getDecimalInput(String prompt) {
        String input = getStringInput(prompt);
        if (Objects.isNull(input)) {
            return null;
        }
        try {
            return new java.math.BigDecimal(input).setScale(2);
        } catch (NumberFormatException e) {
            throw new DbException(input + " is not a valid decimal number. Try again.");
        }
    }

    private int getUserSelection() {
        printOperations();
        Integer input = getIntInput("Enter a menu selection");
        return Objects.isNull(input) ? -1 : input;
    }

    private void printOperations() {
        System.out.println("\nAvailable operations:");
        operations.forEach(op -> System.out.println("  " + op));
    }

    private Integer getIntInput(String prompt) {
        String input = getStringInput(prompt);
        if (Objects.isNull(input)) {
            return null;
        }
        try {
            return Integer.valueOf(input);
        } catch (NumberFormatException e) {
            throw new DbException(input + " is not a valid number. Try again.");
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt + ": ");
        String input = scanner.nextLine();
        return input.isBlank() ? null : input.trim();
    }

    private boolean exitMenu() {
        System.out.println("\nExiting the menu, TTFN.");
        return true;
    }
}
