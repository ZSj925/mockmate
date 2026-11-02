// mapper/InterviewMapper.java
package zsj.mockmate.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zsj.mockmate.pojo.entity.InterviewMessageEntity;
import zsj.mockmate.pojo.entity.InterviewSessionEntity;
import zsj.mockmate.pojo.vo.InterviewSessionVO;

import java.util.List;


@Mapper
public interface InterviewMapper {

    /**
     * 新建一场会话。
     *
     * <p>单对象参数，不加 {@code @Param}，XML 里直接写 #{userId}、#{jobType}。
     *
     * <p>XML 上配了 {@code useGeneratedKeys="true" keyProperty="id"}：
     * 插入成功后自增 id 会被<b>回填到入参对象的 id 字段</b>，调用方可以立刻
     * {@code session.getId()} 拿到新号 —— 少了这个属性会拿到 null，
     * 记忆就会全落到 "null" 这一个坑里。
     *
     * @return 受影响行数（正常为 1）
     */
    int insertSession(InterviewSessionEntity session);

    /**
     * 按主键查一场会话。
     *
     * <p>单个简单类型参数，{@code @Param("id")} 让 XML 里的 #{id} 语义明确。
     *
     * @return 查不到返回 null
     */
    InterviewSessionEntity getSession(@Param("id") Long id);

    /**
     * 更新会话的当前轮次（聊到第几轮了）。
     *
     * <p>两个参数，{@code @Param} 必须加，否则 XML 只能写 #{param1}、#{param2}。
     *
     * @return 受影响行数
     */
    int touchRound(@Param("id") Long id, @Param("round") Integer round);

    /**
     * 插入一条消息（一问或一答）。
     *
     * <p>同 insertSession：单对象参数，不加 {@code @Param}，XML 写 #{sessionId}、#{role}。
     *
     * @return 受影响行数
     */
    int insertMessage(InterviewMessageEntity message);

    /**
     * 取某场会话最近 N 条消息 —— 只给模型看的"记忆窗口"用。
     *
     * <p>SQL 是 {@code order by id desc limit ?}，所以返回的是<b>倒序</b>；
     * 调用方（InterviewChatMemory）要自己翻回时间正序，模型很在意对话顺序。
     *
     * @param limit 最多取几条（一问一答算 2 条）
     */
    List<InterviewMessageEntity> findRecentMessages(@Param("sessionId") Long sessionId,
                                                    @Param("limit") int limit);

    /** 一场会话的全部消息，时间正序（历史回放用） */
    List<InterviewMessageEntity> findMessages(@Param("sessionId") Long sessionId);

    /**
     * 数某场会话里某个角色的消息条数。
     *
     * <p>用来算轮次：INTERVIEWER 的条数 = 已经问过几题。
     */
    int countByRole(@Param("sessionId") Long sessionId, @Param("role") String role);

    /**
     * 某个用户的全部会话，新的在前（带首句预览和消息条数）。
     *
     * <p>返回的是 VO 不是实体：XML 里用两个子查询拼出了 title 和 message_count，
     * 列名对不上字段的部分靠 {@code map-underscore-to-camel-case} 自动转 camelCase。
     */
    List<InterviewSessionVO> findSessionsByUser(@Param("userId") long userId);

    /**
     * 删除会话本身。
     *
     * <p>条件里带 user_id：不是自己的会话会删 <b>0 行</b>，
     * 天然防越权，不用"先查出来再判断是不是我的"。
     *
     * @return 0 表示不存在或不是你的
     */
    int deleteSession(@Param("id") Long id, @Param("userId") long userId);

    /**
     * 删除某场会话的全部消息。
     *
     * <p>配合 deleteSession 用，且顺序不能反：先删会话（兼越权校验），
     * 再删消息。上面没删成功时绝不能动消息。
     */
    int deleteMessages(@Param("sessionId") Long sessionId);
}
