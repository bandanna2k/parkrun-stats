package dnt.parkrun.stats.invariants;

import dnt.parkrun.database.Database;
import dnt.parkrun.database.LiveDatabase;
import dnt.parkrun.datastructures.Country;
import org.assertj.core.api.Assertions;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public abstract class AbstractDatabaseInvariantTest
{
    protected final NamedParameterJdbcTemplate jdbc;
    protected Database database;
    protected Country country = NZ;

    public AbstractDatabaseInvariantTest()
    {
        Database database = new LiveDatabase(country, getDataSourceUrl(), "stats", "4b0e7ff1");
        jdbc = new NamedParameterJdbcTemplate(database.dataSource);
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
