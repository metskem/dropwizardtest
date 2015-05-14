package nl.computerhok;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface SayingDAO {
    @SqlUpdate("create table saying (id integer auto_increment primary key, content varchar(4096))")
    void createSomethingTable();

    @SqlUpdate("insert into saying (content) values (:content)")
    @GetGeneratedKeys
    long insert(@Bind("content") String content);

    @SqlQuery("select id,content from saying")
    @Mapper(SayingMapper.class)
    List<Saying> findAll();

    @SqlQuery("select content from saying where id = :id")
    String findContentById(@Bind("id") long id);
}