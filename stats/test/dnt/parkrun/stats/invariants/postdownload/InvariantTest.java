package dnt.parkrun.stats.invariants.postdownload;

import com.mysql.jdbc.Driver;
import dnt.parkrun.stats.invariants.CourseEventSummaryChecker;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static org.assertj.core.api.Assertions.assertThat;

public class InvariantTest
{
    @Test
    public void courseEventSummaryInvariantCheck() throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl("parkrun_stats"), "dao", "daoFractaldao");
        CourseEventSummaryChecker courseEventSummaryChecker = new CourseEventSummaryChecker(dataSource, System.currentTimeMillis());
        List<String> validate = courseEventSummaryChecker.validate();
        assertThat(validate).isEmpty();
    }
}
