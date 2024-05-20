package dnt.parkrun.stats.invariants.postdownload;

import dnt.parkrun.database.Driver;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.stats.invariants.CourseEventSummaryChecker;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;
import static org.assertj.core.api.Assertions.assertThat;

public class InvariantTest
{
    @Test
    public void courseEventSummaryInvariantCheck() throws SQLException
    {
        System.setProperty("TEST", "false");

        Country country = NZ;
        DataSource dataSource = new SimpleDriverDataSource(Driver.getDriver(), getDataSourceUrl(), "stats", "4b0e7ff1");
        long seed = System.currentTimeMillis();
//        long seed = 1716210112848L;
        CourseEventSummaryChecker courseEventSummaryChecker = new CourseEventSummaryChecker(country, dataSource, seed);
        List<String> validate = courseEventSummaryChecker.validate();
        assertThat(validate).isEmpty();
    }
}
