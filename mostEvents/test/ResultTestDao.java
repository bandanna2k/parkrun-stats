import dnt.parkrun.mostevents.dao.ResultDao;
import org.junit.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultTestDao
{
    private ResultDao resultDao = new ResultDao();

    public ResultTestDao() throws SQLException
    {
    }

    @Test
    public void shouldQueryDatabase()
    {
        assertThat(resultDao.getResults()).isNotEmpty();
    }
}
