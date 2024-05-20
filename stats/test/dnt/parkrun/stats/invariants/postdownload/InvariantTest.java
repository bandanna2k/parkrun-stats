package dnt.parkrun.stats.invariants.postdownload;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.stats.invariants.CourseEventSummaryChecker;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static dnt.parkrun.database.DataSourceUrlBuilder.Type.PARKRUN_STATS;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;
import static org.assertj.core.api.Assertions.assertThat;

public class InvariantTest
{
    @Test
    public void courseEventSummaryInvariantCheck() throws SQLException
    {
        Country country = NZ;
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl(PARKRUN_STATS, country), "stats", "4b0e7ff1");
        CourseEventSummaryChecker courseEventSummaryChecker = new CourseEventSummaryChecker(country, dataSource, System.currentTimeMillis());
        List<String> validate = courseEventSummaryChecker.validate();
        assertThat(validate).isEmpty();
    }
}
