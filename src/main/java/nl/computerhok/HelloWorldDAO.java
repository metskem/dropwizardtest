package nl.computerhok;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface HelloWorldDAO {
    @SqlUpdate("create table saying (id int primary key, content varchar(100))")
    void createSomethingTable();

    @SqlUpdate("insert into saying (id, content) values (:id, :content)")
    void insert(@Bind("id") int id, @Bind("content") String content);

    @SqlQuery("select content from saying where id = :id")
    String findContentById(@Bind("id") long id);
}