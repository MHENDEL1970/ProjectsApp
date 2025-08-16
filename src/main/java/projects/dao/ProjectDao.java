package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;
import projects.util.DbConnection;

/**
 * DAO = Data Access Object.
 * This class talks to the database.
 * Each method here runs SQL and returns data or a result.
 * We use transactions: start -> do work -> commit (or rollback on error).
 */
public class ProjectDao extends DaoBase {
  private static final String CATEGORY_TABLE = "category";
  private static final String MATERIAL_TABLE = "material";
  private static final String PROJECT_TABLE = "project";
  private static final String PROJECT_CATEGORY_TABLE = "project_category";
  private static final String STEP_TABLE = "step";

  /** 
   * Add a new project row.
   * Returns the same Project object, but now with its new ID set.
   */
  public Project insertProject(Project project) {
    String sql = "INSERT INTO " + PROJECT_TABLE
        + " (project_name, estimated_hours, actual_hours, difficulty, notes) "
        + "VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn); // start a transaction

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        // Set values for the INSERT
        setParameter(stmt, 1, project.getProjectName(), String.class);
        setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
        setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
        setParameter(stmt, 4, project.getDifficulty(), Integer.class);
        setParameter(stmt, 5, project.getNotes(), String.class);

        stmt.executeUpdate();

        // Get new ID and save it back on the object
        Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
        commitTransaction(conn); // success

        project.setProjectId(projectId);
        return project;
      } catch (Exception e) {
        rollbackTransaction(conn); // any error -> rollback
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * Get all projects. Sorted by ID, low to high.
   */
  public List<Project> fetchAllProjects() {
    String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_id ASC";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql);
           ResultSet rs = stmt.executeQuery()) {

        List<Project> projects = new LinkedList<>();
        while (rs.next()) {
          projects.add(extract(rs, Project.class)); // build Project from row
        }

        commitTransaction(conn);
        return projects;
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * Get one project by ID.
   * Also loads its materials, steps, and categories.
   * Returns Optional.empty() if not found.
   */
  public Optional<Project> fetchProjectById(Integer projectId) {
    String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try {
        Project project = null;

        // First, fetch the main project row
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
          setParameter(stmt, 1, projectId, Integer.class);
          try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
              project = extract(rs, Project.class);
            }
          }
        }

        // If found, load related lists
        if (Objects.nonNull(project)) {
          project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
          project.getSteps().addAll(fetchStepsForProject(conn, projectId));
          project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
        }

        commitTransaction(conn);
        return Optional.ofNullable(project);
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * Helper: get all categories linked to a project.
   */
  private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) {
    String sql = "SELECT c.* FROM " + CATEGORY_TABLE + " c "
               + "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
               + "WHERE project_id = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameter(stmt, 1, projectId, Integer.class);

      try (ResultSet rs = stmt.executeQuery()) {
        List<Category> categories = new LinkedList<>();
        while (rs.next()) {
          categories.add(extract(rs, Category.class));
        }
        return categories;
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * Helper: get all steps for a project.
   */
  private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
    String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameter(stmt, 1, projectId, Integer.class);

      try (ResultSet rs = stmt.executeQuery()) {
        List<Step> steps = new LinkedList<>();
        while (rs.next()) {
          steps.add(extract(rs, Step.class));
        }
        return steps;
      }
    }
  }

  /**
   * Helper: get all materials for a project.
   */
  private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId)
      throws SQLException {
    String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameter(stmt, 1, projectId, Integer.class);

      try (ResultSet rs = stmt.executeQuery()) {
        List<Material> materials = new LinkedList<>();
        while (rs.next()) {
          materials.add(extract(rs, Material.class));
        }
        return materials;
      }
    }
  }

  /**
   * Update one project's fields.
   * Returns true if exactly 1 row changed.
   * Returns false if the ID was not found.
   */
  public boolean modifyProjectDetails(Project project) {
    String sql = "UPDATE " + PROJECT_TABLE + " SET "
        + "project_name = ?, "
        + "estimated_hours = ?, "
        + "actual_hours = ?, "
        + "difficulty = ?, "
        + "notes = ? "
        + "WHERE project_id = ?";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        setParameter(stmt, 1, project.getProjectName(), String.class);
        setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
        setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
        setParameter(stmt, 4, project.getDifficulty(), Integer.class);
        setParameter(stmt, 5, project.getNotes(), String.class);
        setParameter(stmt, 6, project.getProjectId(), Integer.class);

        int rows = stmt.executeUpdate();
        commitTransaction(conn);
        return rows == 1; // true if updated, false if not found
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * Delete one project by ID.
   * Returns true if exactly 1 row was deleted.
   * Returns false if the ID was not found.
   * Note: If there are child rows (materials/steps/categories) and FKs block delete,
   *       MySQL will throw an error. Then we rollback and rethrow as DbException.
   */
  public boolean deleteProject(Integer projectId) {
    String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        setParameter(stmt, 1, projectId, Integer.class);

        int rows = stmt.executeUpdate();
        commitTransaction(conn);

        return rows == 1; // true if 1 row deleted, false if not found
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }
} 
