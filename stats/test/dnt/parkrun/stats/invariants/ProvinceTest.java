package dnt.parkrun.stats.invariants;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.region.NewZealandRegionChecker;
import dnt.parkrun.region.RegionChecker;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static org.junit.Assert.assertTrue;

public class ProvinceTest
{
    @Test
    public void allNzCoursesShouldHaveAProvince() throws SQLException
    {
        RegionChecker regionChecker = new NewZealandRegionChecker();
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl("parkrun_stats"), "stats", "statsfractalstats");
        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);

        courseRepository.getCourses(Country.NZ).stream().filter(c -> c.country == Country.NZ).forEach(c -> {
            System.out.println(c);
            assertTrue("Bad course " + c,
                    regionChecker.isSameRegion(c, c));
        });

    }
}
