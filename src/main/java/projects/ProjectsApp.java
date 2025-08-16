package projects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
  private Scanner scanner = new Scanner(System.in);
  private ProjectService projectService = new ProjectService();
  private Project curProject;

  // Store last displayed list for mapping list number -> DB ID
  private List<Project> lastListedProjects;

  // Menu options
  private List<String> operations = List.of(
      "1) Add a project",
      "2) List projects",
      "3) Select a project",
      "4) Update project details",
      "5) Delete a project"
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
          case -1: done = exitMenu(); break;
          case 1:  createProject(); break;
          case 2:  listProjects(); break;
          case 3:  selectProject(); break;
          case 4:  updateProjectDetails(); break;
          case 5:  deleteProject(); break;
          default:
            System.out.println("\n" + selection + " is not a valid selection. Try again.");
        }
      } catch (Exception e) {
        System.out.println("\nError: " + e + " Try again.");
      }
    }
  }

  /** Option 5: Delete using list number, not DB ID */
  private void deleteProject() {
    listProjects(); // Refresh list

    Integer choice = getIntInput("Enter the number from the list to delete");
    if (choice == null || lastListedProjects == null ||
        choice < 1 || choice > lastListedProjects.size()) {
      System.out.println("Invalid selection. Nothing deleted.");
      return;
    }

    Project toDelete = lastListedProjects.get(choice - 1);
    Integer projectId = toDelete.getProjectId();
    String projectName = toDelete.getProjectName();

    try {
      projectService.deleteProject(projectId);
      System.out.println("Project '" + projectName + "' was deleted successfully.");
      if (curProject != null && Objects.equals(curProject.getProjectId(), projectId)) {
        curProject = null;
      }
    } catch (DbException ex) {
      System.out.println("No project with ID=" + projectId + " found. Nothing deleted.");
    }
  }

  private void updateProjectDetails() {
    if (curProject == null) {
      System.out.println("\nPlease select a project.");
      return;
    }

    String nameIn  = getStringInput("Enter the project name [" + curProject.getProjectName() + "]");
    BigDecimal estIn = getDecimalInput("Enter estimated hours [" + curProject.getEstimatedHours() + "]");
    BigDecimal actIn = getDecimalInput("Enter actual hours [" + curProject.getActualHours() + "]");
    Integer diffIn = getIntInput("Enter the project difficulty (1-5) [" + curProject.getDifficulty() + "]");
    String notesIn = getStringInput("Enter the project notes [" + curProject.getNotes() + "]");

    Project p = new Project();
    p.setProjectId(curProject.getProjectId());
    p.setProjectName(Objects.isNull(nameIn)  ? curProject.getProjectName()  : nameIn);
    p.setEstimatedHours(Objects.isNull(estIn) ? curProject.getEstimatedHours() : estIn);
    p.setActualHours(Objects.isNull(actIn)    ? curProject.getActualHours()    : actIn);
    p.setDifficulty(Objects.isNull(diffIn)    ? curProject.getDifficulty()     : diffIn);
    p.setNotes(Objects.isNull(notesIn)        ? curProject.getNotes()          : notesIn);

    projectService.modifyProjectDetails(p);
    curProject = projectService.fetchProjectById(curProject.getProjectId());

    System.out.println("Project updated.");
  }

  /** Option 3: Select project using list number */
  private void selectProject() {
    listProjects();
    Integer choice = getIntInput("Enter the number from the list to select");
    if (choice == null || lastListedProjects == null ||
        choice < 1 || choice > lastListedProjects.size()) {
      System.out.println("Invalid selection.");
      return;
    }
    Project selected = lastListedProjects.get(choice - 1);
    curProject = projectService.fetchProjectById(selected.getProjectId());
  }

  /** Option 2: Show numbered list for user, store mapping to DB IDs */
  private void listProjects() {
    lastListedProjects = projectService.fetchAllProjects();
    System.out.println("\nProjects:");
    int displayNum = 1;
    for (Project p : lastListedProjects) {
      System.out.println("   " + (displayNum++) + ": " + p.getProjectName());
    }
  }

  /** Option 1: Add new project */
  private void createProject() {
    String projectName = getStringInput("Enter the project name");
    BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
    BigDecimal actualHours = getDecimalInput("Enter the actual hours");
    Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
    String notes = getStringInput("Enter the project notes");

    Project project = new Project();
    project.setProjectName(projectName);
    project.setEstimatedHours(estimatedHours);
    project.setActualHours(actualHours);
    project.setDifficulty(difficulty);
    project.setNotes(notes);

    Project dbProject = projectService.addProject(project);
    System.out.println("Created project: " + dbProject.getProjectName());
  }

  private BigDecimal getDecimalInput(String prompt) {
    String input = getStringInput(prompt);
    if (Objects.isNull(input)) {
      return null;
    }
    try {
      return new BigDecimal(input).setScale(2, RoundingMode.HALF_UP);
    } catch (NumberFormatException e) {
      throw new DbException(input + " is not a valid decimal number.");
    }
  }

  private boolean exitMenu() {
    System.out.println("Exiting the menu.");
    scanner.close();
    return true;
  }

  private int getUserSelection() {
    printOperations();
    Integer input = getIntInput("Enter a menu selection");
    return Objects.isNull(input) ? -1 : input;
  }

  private Integer getIntInput(String prompt) {
    String input = getStringInput(prompt);
    if (Objects.isNull(input)) {
      return null;
    }
    try {
      return Integer.valueOf(input);
    } catch (NumberFormatException e) {
      throw new DbException(input + " is not a valid number.");
    }
  }

  private String getStringInput(String prompt) {
    System.out.print(prompt + ": ");
    String input = scanner.nextLine();
    return input.isBlank() ? null : input.trim();
  }

  private void printOperations() {
    System.out.println("\nThese are the available selections. Press the Enter key to quit:");
    operations.forEach(line -> System.out.println("  " + line));
    if (Objects.isNull(curProject)) {
      System.out.println("\nYou are not working with a project.");
    } else {
      System.out.println("\nYou are working with project: " + curProject.getProjectName());
    }
  }
} 
