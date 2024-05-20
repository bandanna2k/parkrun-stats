package dnt.parkrun.stats.invariants;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Country;
import org.assertj.core.api.Assertions;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.util.List;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public abstract class AbstractDatabaseInvariantTest
{
    private static final Driver DRIVER = dnt.parkrun.database.Driver.getDriver();

    protected SimpleDriverDataSource dataSource;
    protected NamedParameterJdbcTemplate jdbc;
    protected Country country = NZ;

    public AbstractDatabaseInvariantTest()
    {
        dataSource = new SimpleDriverDataSource(DRIVER, getDataSourceUrl(), "stats", "4b0e7ff1");
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
