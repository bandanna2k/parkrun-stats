package dnt.parkrun.stats.speed;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.AgeGroup;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AgeGroupRecords
{
    public static void main(String[] args) throws SQLException
    {
        new AgeGroupRecords().go();
    }

    private void go() throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "stats", "statsfractalstats");

        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);

        Map<AgeGroup, Map<Integer, AgeGroupRecord>> ageGroupToCourseToAgeGradeRecord = new HashMap<>();

        ResultDao resultDao = new ResultDao(dataSource);
        resultDao.tableScan(result -> {
            Map<Integer, AgeGroupRecord> courseToAgeGradeRecord = ageGroupToCourseToAgeGradeRecord.computeIfAbsent(result.ageGroup, ageGroup -> new HashMap<>());
            AgeGroupRecord ageGroupRecord = courseToAgeGradeRecord.computeIfAbsent(result.courseId, courseId -> new AgeGroupRecord());

            ageGroupRecord.maybeAddByTime(new StatsRecord().result(result));
        });

        ageGroupToCourseToAgeGradeRecord.forEach((ageGroup, courseToAgeGradeRecord) ->
                courseToAgeGradeRecord.forEach((key, record) ->
        {
            Course course = courseRepository.getCourse(key);
            System.out.println(ageGroup + " " + course);
            System.out.println(record.recordGold);
            System.out.println(record.recordSilver);
            System.out.println(record.recordBronze);
            System.out.println();
        }));
    }

}
