package zsj.mockmate.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zsj.mockmate.pojo.entity.resumeEntity;
import zsj.mockmate.pojo.vo.ResumeVO;

import java.util.List;

@Mapper
public interface ResumeMapper {

    /** 某个用户的简历列表，新的在前 */
    List<ResumeVO> findByUserId(@Param("userId") long userId);

    /** 按主键查（带 rawText 和 profileJson，摘要生成与注入时用） */
    resumeEntity findById(@Param("id") Long id);

    /** 新增简历。useGeneratedKeys 会把自增 id 回填到 resume.id */
    int insertResume(resumeEntity resume);

    /** 摘要生成完成后回写：摘要正文 + 状态 + 完成时间 */
    int updateSummary(@Param("id") Long id,
                      @Param("summary") String summary,
                      @Param("status") String status);

    /** 只改状态（PARSING / FAILED），不动摘要 */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 删除简历。
     */
    int deleteById(@Param("id") Long id, @Param("userId") long userId);
}
