package dnt.parkrun.stats.invariants;

import com.mysql.jdbc.Driver;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CourseEventSummaryInvariantTest
{
    @Test
    public void checkCourseEventSummary() throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        CourseEventSummaryChecker courseEventSummaryChecker = new CourseEventSummaryChecker(dataSource, 1);
        List<String> validate = courseEventSummaryChecker.validate();
        assertThat(validate).isEmpty();
    }

}