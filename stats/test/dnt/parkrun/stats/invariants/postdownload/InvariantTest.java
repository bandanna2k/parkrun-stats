package dnt.parkrun.stats.invariants.postdownload;

import dnt.parkrun.database.Database;
import dnt.parkrun.database.LiveDatabase;
import dnt.parkrun.stats.invariants.CourseEventSummaryChecker;
import org.junit.Test;

import java.util.List;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;
import static org.assertj.core.api.Assertions.assertThat;

public class InvariantTest
{
    @Test
    public void courseEventSummaryInvariantCheck()
    {
        Database database = new LiveDatabase(NZ, getDataSourceUrl(), "stats", "4b0e7ff1");
        long seed = System.currentTimeMillis();
        //seed = 1724541784239L;
        CourseEventSummaryChecker courseEventSummaryChecker = new CourseEventSummaryChecker(database, seed);
        List<String> validate = courseEventSummaryChecker.validate();
        assertThat(validate).isEmpty();
    }
}
