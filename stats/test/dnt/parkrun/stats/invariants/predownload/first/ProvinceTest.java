package dnt.parkrun.stats.invariants.predownload.first;

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

import static dnt.parkrun.database.DataSourceUrlBuilder.Type.PARKRUN_STATS;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;
import static org.junit.Assert.assertTrue;

public class ProvinceTest
{
    final Country country = NZ;

    @Test
    public void allNzCoursesShouldHaveAProvince() throws SQLException
    {
        RegionChecker regionChecker = new NewZealandRegionChecker();
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl(PARKRUN_STATS, country), "stats", "4b0e7ff1");
        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);

        courseRepository.getCourses(country).stream().filter(c -> c.country == country).forEach(c -> {
            System.out.println(c);
            assertTrue("Bad course " + c,
                    regionChecker.isSameRegion(c, c));
        });

    }
}
