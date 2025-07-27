package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import projects.entity.Project;
import projects.exception.DbException;
import provided.util.DaoBase;

/**
 * This class uses JDBC to perform CRUD operations on the project tables.
 *
 * @author Promineo
 */
@SuppressWarnings("unused")
public class ProjectDao extends DaoBase {
    private static final String CATEGORY_TABLE = "category";
    private static final String MATERIAL_TABLE = "material";
    private static final String PROJECT_TABLE = "project";
    private static final String PROJECT_CATEGORY_TABLE = "project_category";
    private static final String STEP_TABLE = "step";

    /**
     * Insert a project row into the project table.
     *
     * @param project The project object to insert.
     * @return The project object with the primary key.
     * @throws DbException Thrown if an error occurs inserting the row.
     */
    public Project insertProject(Project project) {
        // @formatter:off
        String sql = ""
            + "INSERT INTO " + PROJECT_TABLE + " "
            + "(project_name, estimated_hours, actual_hours, difficulty, notes) "
            + "VALUES "
            + "(?, ?, ?, ?, ?)";
        // @formatter:on

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameter(stmt, 1, project.getProjectName(), String.class);
                setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
                setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
                setParameter(stmt, 4, project.getDifficulty(), Integer.class);
                setParameter(stmt, 5, project.getNotes(), String.class);

                stmt.executeUpdate();

                Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
                commitTransaction(conn);

                project.setProjectId(projectId);
                return project;
            } catch (Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

	private void rollbackTransaction(Connection conn) {
		// TODO Auto-generated method stub
		
	}

	private void commitTransaction(Connection conn) {
		// TODO Auto-generated method stub
		
	}

	private Integer getLastInsertId(Connection conn, String projectTable) {
		// TODO Auto-generated method stub
		return null;
	}

	private void setParameter(PreparedStatement stmt, int i, Integer difficulty, Class<Integer> class1) {
		// TODO Auto-generated method stub
		
	}

	private void setParameter(PreparedStatement stmt, int i, BigDecimal estimatedHours, Class<BigDecimal> class1) {
		// TODO Auto-generated method stub
		
	}

	private void setParameter(PreparedStatement stmt, int i, String projectName, Class<String> class1) {
		// TODO Auto-generated method stub
		
	}

	private void startTransaction(Connection conn) {
		// TODO Auto-generated method stub
		
	}
}