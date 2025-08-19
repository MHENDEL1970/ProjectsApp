package projects.service;

import java.util.List;
import java.util.NoSuchElementException;

import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

/**
 * This class is between the menu (ProjectsApp) and the database (ProjectDao).
 * It asks the DAO to do things with the database, and checks the results.
 * If something is wrong, it throws an error for the menu to show.
 */
public class ProjectService {
  
  // This object talks to the database
  private ProjectDao projectDao = new ProjectDao();

  /**
   * Add a new project to the database.
   * @param project - the project details from the menu
   * @return the new project with its ID from the database
   */
  public Project addProject(Project project) {
    return projectDao.insertProject(project);
  }

  /**
   * Get all projects from the database (only basic info like ID and name).
   * @return a list of projects
   */
  public List<Project> fetchAllProjects() {
    return projectDao.fetchAllProjects();
  }

  /**
   * Get one project by ID, with all details.
   * If the project is not found, throw an error.
   * @param projectId - ID of the project
   * @return the full project details
   */
  public Project fetchProjectById(Integer projectId) {
    return projectDao.fetchProjectById(projectId)
        .orElseThrow(() -> new NoSuchElementException(
            "Project with ID=" + projectId + " does not exist."));
  }

  /**
   * Change (update) an existing project's details.
   * If no project is updated (wrong ID), throw an error.
   * @param project - project details to update
   */
  public void modifyProjectDetails(Project project) {
    boolean updated = projectDao.modifyProjectDetails(project); // true if 1 project updated
    if (!updated) {
      throw new DbException("Project with ID=" + project.getProjectId() + " does not exist.");
    }
  }

  /**
   * Delete a project from the database by ID.
   * If no project is deleted (wrong ID), throw an error.
   * @param projectId - ID of the project to delete
   */
  public void deleteProject(Integer projectId) {
    boolean deleted = projectDao.deleteProject(projectId); // true if 1 project deleted
    if (!deleted) {
      throw new DbException("Project with ID=" + projectId + " does not exist.");
    }
  }
} 
