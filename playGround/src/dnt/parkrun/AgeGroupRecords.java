package dnt.parkrun;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.*;
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

            ageGroupRecord.maybeAdd(result);
        });

        ageGroupToCourseToAgeGradeRecord.forEach((ageGroup, courseToAgeGradeRecord) ->
                courseToAgeGradeRecord.forEach((key, record) ->
        {
            Course course = courseRepository.getCourse(key);
            System.out.println(ageGroup + " " + course);
            System.out.println(record.resultGold);
            System.out.println(record.resultSilver);
            System.out.println(record.resultBronze);
            System.out.println();
        }));
    }

    private static class AgeGroupRecord
    {
        Result resultGold = new Result(0, null, 0, null, null, null, AgeGrade.newInstance(0));
        Result resultSilver = new Result(0, null, 0, null, null, null, AgeGrade.newInstance(0));
        Result resultBronze = new Result(0, null, 0, null, null, null, AgeGrade.newInstance(0));

        public void maybeAdd(Result result)
        {
            if(result.ageGrade.ageGrade > resultGold.ageGrade.ageGrade)
            {
                resultBronze = resultSilver;
                resultSilver = resultGold;
                resultGold = result;
            }
            else if(result.ageGrade.ageGrade > resultSilver.ageGrade.ageGrade)
            {
                resultBronze = resultSilver;
                resultSilver = result;
            }
            else if(result.ageGrade.ageGrade > resultBronze.ageGrade.ageGrade)
            {
                resultBronze = result;
            }
        }

        @Override
        public String toString()
        {
            return "AgeGroupRecord{" +
                    "resultGold=" + resultGold +
                    ", resultSilver=" + resultSilver +
                    ", resultBronze=" + resultBronze +
                    '}';
        }
    }
}
