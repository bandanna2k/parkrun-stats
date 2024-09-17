package dnt.parkrun.pindex;

import dnt.parkrun.datastructures.AthleteCourseSummary;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dnt.parkrun.pindex.PIndex.pIndexAndNeeded;
import static org.assertj.core.api.Assertions.assertThat;

public class PIndexTest
{
        /*
                // 4, 4, 4          pIndex 3, need 4
                // 2, 2, 4, 4, 4    pIndex 3, need 1
                // 20               pIndex 1, need 2
                // 1, 1, 1, 1       pIndex 1, need 1
                // 2, 2, 2, 2       pIndex 2, need 3 to get to P3
                // 5, 5, 5, 5       pIndex 4, need 1 to get to P6
                // 100, 4, 4, 3     pIndex 3, need 1
                // 4, 4, 4, 4       pIndex 4, need 9
     */

    @Test
    public void testPIndex()
    {
        testPIndex(3,2,  2, 2, 4, 4, 4);
        testPIndex(1, 2, 20);
        testPIndex(0, 1);
        testPIndex(1,  2, 1, 1, 1, 1);
        testPIndex(2,  3, 2, 2, 2, 2);
        testPIndex(4,  5, 5, 5, 5, 5);
        testPIndex(3,  4, 100, 4, 4, 3);
        testPIndex(4,  9, 4, 4, 4, 4);

        testPIndex(8,  9, 94, 57, 19, 17, 13, 9, 8, 8, 2, 1, 1, 1);

        testPIndex(10,  6,
                65, 12, 6, 4, 3, 11, 15, 13, 1, 10, 2, 1, 19, 2, 2, 5, 68, 4, 22, 2, 19);
        testPIndex(14, 15,
                65, 51, 47, 36, 30, 22, 22, 16, 15, 15, 14, 14, 14, 14, // 14 (1 + 1 + 1 + 1 + 11)
                4, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1
                );
    }
    private void testPIndex(int expectedPIndex, int expectedNeededForNextPIndex, int ... countOfRuns)
    {
        List<AthleteCourseSummary> summaries = Arrays.stream(countOfRuns)
                .mapToObj(n -> new AthleteCourseSummary(null, null, n))
                .collect(Collectors.toList());
        assertThat(athleteCourseSummaryPIndex(summaries).pIndex).isEqualTo(expectedPIndex);
        assertThat(athleteCourseSummaryPIndex(summaries).neededForNextPIndex).isEqualTo(expectedNeededForNextPIndex);
        assertThat(PIndex.pIndex(summaries.stream().map(acs -> acs.countOfRuns).collect(Collectors.toList()))).isEqualTo(expectedPIndex);
    }

    private static PIndex.Result athleteCourseSummaryPIndex(List<AthleteCourseSummary> listOfRuns)
    {
        return pIndexAndNeeded(new ArrayList<>(
                listOfRuns.stream().map(acs -> acs.countOfRuns).toList()));
    }
}
