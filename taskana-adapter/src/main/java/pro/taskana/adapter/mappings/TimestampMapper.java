package pro.taskana.adapter.mappings;

import java.time.Instant;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Mapper for the Timestamps of the last creation of Taskana tasks or completion of general tasks.
 *
 * @author kkl
 */
@Mapper
public interface TimestampMapper {

    @Select("<script>"
        + "SELECT MAX(QUERY_TIMESTAMP) "
        + "FROM LAST_QUERY_TIME "
        + "WHERE SYSTEM_URL = #{systemUrl}"
        + "</script>")
    Instant getLatestQueryTimestamp(@Param("systemUrl") String systemUrl);

    @Select("<script>"
        + "SELECT MAX(CREATED) "
        + "FROM TASKS "
        + "WHERE SYSTEM_URL = #{systemUrl}"
        + "</script>")
    Instant getLatestCreatedTaskCreationTimestamp(@Param("systemUrl") String systemUrl);

    @Select("<script>"
        + "SELECT ID "
        + "FROM TASKS "
        + "WHERE (SYSTEM_URL = #{systemUrl} "
        + "AND  ID IN (<foreach item='item' collection='taskIdsIn' separator=',' >#{item}</foreach>))"
        + "</script>")
    @Results(value = {@Result(property = "taskId", column = "ID")})
    List<String> findExistingTaskIds(@Param("systemUrl") String systemUrl, @Param("taskIdsIn") List<String> taskIdsIn);

    @Select("<script>"
        + "SELECT ID "
        + "FROM TASKS "
        + "WHERE ID IN (<foreach item='item' collection='taskIdsIn' separator=',' >#{item}</foreach>) "
        + "AND NOT (COMPLETED IS NULL) "
        + "</script>")
    @Results(value = {@Result(property = "taskId", column = "ID")})
    List<String> findAlreadyCompletedTaskIds(@Param("taskIdsIn") List<String> taskIdsIn);

    @Insert("INSERT INTO TASKS (ID, CREATED, SYSTEM_URL) VALUES (#{id}, #{created}, #{systemUrl})")
    void registerCreatedTask(@Param("id") String id,
        @Param("created") Instant created,
        @Param("systemUrl") String systemUrl);

    @Insert("INSERT INTO LAST_QUERY_TIME (ID, QUERY_TIMESTAMP, SYSTEM_URL) VALUES (#{id}, #{queryTimestamp}, #{systemUrl})")
    void rememberSystemQueryTime(@Param("id") String id,
        @Param("queryTimestamp") Instant queryTimestamp,
        @Param("systemUrl") String systemUrl);

    @Select("<script>SELECT MAX(COMPLETED) FROM TASKS </script>")
    Instant getLatestCompletedTimestamp();

    @Update("UPDATE TASKS SET COMPLETED = #{completed} where ID = #{id}")
    void registerTaskCompleted(@Param("id") String id,
        @Param("completed") Instant completed);

    @Delete(value = "DELETE FROM TASKS WHERE COMPLETED < #{completedBefore}")
    void cleanupTasksCompletedBefore(@Param("completedBefore") Instant completedBefore);

    @Delete(value = "DELETE FROM LAST_QUERY_TIME where QUERY_TIMESTAMP < #{queriedBefore}")
    void cleanupQueryTimestamps(@Param("queriedBefore") Instant queriedBefore);

}