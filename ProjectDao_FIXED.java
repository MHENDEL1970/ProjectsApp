package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import projects.entity.Project;
import projects.exception.DbException;
import provided.util.DaoBase;

/**
 * This class uses JDBC to perform CRUD operations on the project table.
 * 
 * @author 
 */
public class ProjectDao extends DaoBase {

    private static final String PROJECT_TABLE = "project";

    /**
     * Inserts a new project record into the database.
     * 
     * @param project The project to insert.
     * @return The project with the generated projectId set.
     */
    public Project insertProject(Project project) {
        String sql = ""
            + "INSERT INTO " + PROJECT_TABLE + " "
            + "(project_name, estimated_hours, actual_hours, difficulty, notes) "
            + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            startTransaction(conn);

            setParameter(stmt, 1, project.getProjectName(), String.class);
            setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
            setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
            setParameter(stmt, 4, project.getDifficulty(), Integer.class);
            setParameter(stmt, 5, project.getNotes(), String.class);

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    project.setProjectId(keys.getInt(1));
                } else {
                    throw new DbException("Failed to retrieve project ID.");
                }
            }

            commitTransaction(conn);
            return project;

        } catch (SQLException e) {
            throw new DbException("Database error inserting project: " + e.getMessage(), e);
        }
    }

    private void rollbackTransaction(Connection conn) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException e) {
            throw new DbException("Failed to rollback transaction.", e);
        }
    }

    private void commitTransaction(Connection conn) {
        try {
            if (conn != null) conn.commit();
        } catch (SQLException e) {
            throw new DbException("Failed to commit transaction.", e);
        }
    }
}
