package dnt.parkrun.stats.invariants;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Country;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.util.List;

import static dnt.parkrun.database.DataSourceUrlBuilder.Type.PARKRUN_STATS;
import static dnt.parkrun.database.DataSourceUrlBuilder.Type.WEEKLY_STATS;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public abstract class AbstractDatabaseInvariantTest
{
    protected SimpleDriverDataSource dataSource;
    protected SimpleDriverDataSource weeklyDataSource;
    protected NamedParameterJdbcTemplate jdbc;

    @Before
    public void setUp() throws Exception
    {
        Country country = NZ;
        dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl(PARKRUN_STATS, country), "stats", "4b0e7ff1");
        weeklyDataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl(WEEKLY_STATS, country), "stats", "4b0e7ff1");
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    protected void assertNoRowsMatch(String sql, String onErrorPrintField1, String... onErrorPrintFieldX)
    {
        List<String> errors = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%d: ", rowNum));
            sb.append(String.format("%s: %s", onErrorPrintField1, rs.getString(onErrorPrintField1)));
            for (String onErrorPrintField : onErrorPrintFieldX)
            {
                sb.append(String.format(", %s: %s", onErrorPrintField, rs.getString(onErrorPrintField)));
            }
            sb.append(";");
            return sb.toString();
        });
        Assertions.assertThat(errors.size())
                .describedAs(String.join("\n", errors))
                .isEqualTo(0);
    }
}
