package dnt.parkrun.stats.speed;

import dnt.parkrun.datastructures.*;
import dnt.parkrun.htmlwriter.StatsRecord;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class AgeCategoryRecordTest
{
    /*
Set   gold: Result{courseId=40, date=2022-12-10, position=29, athlete=Athlete{name='Emily SMITH', athleteId=7365890}, time=23:44, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=62.36}}
Set silver: Result{courseId=40, date=2022-12-10, position=56, athlete=Athlete{name='Almaz Berenike BERGK', athleteId=2907533}, time=27:55, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=53.25}}
Set bronze: Result{courseId=40, date=2022-12-10, position=71, athlete=Athlete{name='Anna WHYTE', athleteId=7887738}, time=29:51, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=49.64}}
Set silver: Result{courseId=40, date=2022-12-17, position=22, athlete=Athlete{name='Olivia FOUNTAIN', athleteId=1587090}, time=24:26, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=60.85}}
Set gold,   same athlete: Result{courseId=40, date=2022-12-24, position=33, athlete=Athlete{name='Emily SMITH', athleteId=7365890}, time=23:32, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=62.89}}
Set bronze: Result{courseId=40, date=2022-12-24, position=64, athlete=Athlete{name='Larissa BLACK', athleteId=1581211}, time=26:53, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=55.42}}
Set silver, same athlete: Result{courseId=40, date=2023-01-28, position=21, athlete=Athlete{name='Olivia FOUNTAIN', athleteId=1587090}, time=23:45, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=62.6}}
Set   gold: Result{courseId=40, date=2023-02-04, position=6, athlete=Athlete{name='Lizzy BUNCKENBURG', athleteId=320068}, time=20:38, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=71.73}}
Set silver, same athlete: Result{courseId=40, date=2023-02-25, position=31, athlete=Athlete{name='Emily SMITH', athleteId=7365890}, time=23:26, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=63.16}}
Set bronze: Result{courseId=40, date=2023-03-18, position=27, athlete=Athlete{name='Susanna GARRATT', athleteId=6953504}, time=23:43, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=62.68}}
Set bronze, same athlete: Result{courseId=40, date=2023-03-25, position=21, athlete=Athlete{name='Susanna GARRATT', athleteId=6953504}, time=22:56, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=64.83}}
Set silver: Result{courseId=40, date=2023-08-26, position=10, athlete=Athlete{name='Caroline MELLSOP', athleteId=6851029}, time=20:47, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=71.29}}
Set bronze: Result{courseId=40, date=2023-11-04, position=8, athlete=Athlete{name='Rosie DAVIES', athleteId=540722}, time=22:32, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=65.68}}
Set silver: Result{courseId=40, date=2024-03-16, position=5, athlete=Athlete{name='Josie BROUGH', athleteId=8501974}, time=20:49, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=71.42}}
Set silver, same athlete: Result{courseId=40, date=2024-04-13, position=3, athlete=Athlete{name='Josie BROUGH', athleteId=8501974}, time=20:36, ageCategory=SW30_34, ageGrade=AgeGrade{assisted=false, ageGrade=72.17}}
     */
    @Test
    public void testAraHarakekeSW30_34()
    {
        AgeCategoryRecord record = new AgeCategoryRecord();
        addByAgeGrade(record, "Emily SMITH", 7365890, 62.36);
        addByAgeGrade(record, "Almaz BERGK", 2907533, 53.25);
        addByAgeGrade(record, "Anna WHYTE", 7887738, 49.64);
        addByAgeGrade(record, "Olivia FOUNTAIN", 1587090, 60.85);
        addByAgeGrade(record, "Emily SMITH", 7365890, 62.89);
        addByAgeGrade(record, "Larissa BLACK", 1581211, 55.42);
        addByAgeGrade(record, "Olivia FOUNTAIN", 1587090, 62.6);
        addByAgeGrade(record, "Lizzy BUNCKENBURG", 320068, 71.73);
        addByAgeGrade(record, "Emily SMITH", 7365890, 63.16);
        addByAgeGrade(record, "Susanna GARRATT", 6953504, 62.86);
        addByAgeGrade(record, "Susanna GARRATT", 6953504, 64.83);
        addByAgeGrade(record, "Caroline MELLSOP", 6851029, 71.29);
        addByAgeGrade(record, "Rosie DAVIES", 540722, 65.68);
        addByAgeGrade(record, "Josie BROUGH", 8501974, 71.42);
        addByAgeGrade(record, "Josie BROUGH", 8501974, 72.17);
        System.out.println();
        Arrays.stream(record.records).forEach(r -> System.out.println(r.result()));
        assertThat(record.records[0].result().athlete.athleteId).isEqualTo(8501974);
        assertThat(record.records[1].result().athlete.athleteId).isEqualTo(320068);
        assertThat(record.records[2].result().athlete.athleteId).isEqualTo(6851029);
    }

    private void addByAgeGrade(AgeCategoryRecord record, String name, int athleteId, double ageGrade)
    {
        record.maybeAddByAgeGrade(new StatsRecord().result(new Result(
                1,
                Date.from(Instant.EPOCH),
                1,
                1,
                Athlete.from(name, athleteId),
                Time.from(2000),
                AgeCategory.SW30_34,
                AgeGrade.newInstance(ageGrade))
        ));
    }
}