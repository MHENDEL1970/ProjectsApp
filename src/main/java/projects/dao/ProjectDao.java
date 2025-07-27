package projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectDao {

    private static final String PROJECT_TABLE = "project";

    public Project insertProject(Project project) {
        String sql = "INSERT INTO " + PROJECT_TABLE +
                     " (project_name, estimated_hours, actual_hours, difficulty, notes) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, project.getProjectName());
            stmt.setBigDecimal(2, project.getEstimatedHours());
            stmt.setBigDecimal(3, project.getActualHours());
            stmt.setInt(4, project.getDifficulty());
            stmt.setString(5, project.getNotes());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    project.setProjectId(rs.getInt(1));
                }
            }

            return project;
        } catch (SQLException e) {
            throw new DbException("Error inserting project: " + e.getMessage(), e);
        }
    }
}
