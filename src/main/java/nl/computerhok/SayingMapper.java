package nl.computerhok;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SayingMapper implements ResultSetMapper<Saying>
{
    public Saying map(int index, ResultSet r, StatementContext ctx) throws SQLException
    {
        return new Saying(r.getInt("id"), r.getString("content"));
    }
}
