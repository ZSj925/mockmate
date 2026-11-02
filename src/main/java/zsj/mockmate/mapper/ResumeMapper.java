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

    /** 新增简历。useGeneratedKeys 会把自增 id 回填到 resume.id */
    int insertResume(resumeEntity resume);

    /**
     * 删除简历。
     */
    int deleteById(@Param("id") Long id, @Param("userId") long userId);
}