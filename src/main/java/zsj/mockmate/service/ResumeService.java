package zsj.mockmate.service;

import org.apache.tika.exception.TikaException;
import org.springframework.web.multipart.MultipartFile;
import zsj.mockmate.pojo.vo.ResumeUploadVO;
import zsj.mockmate.pojo.vo.ResumeVO;

import java.io.IOException;
import java.util.List;

public interface ResumeService {

    /** 当前登录用户的简历列表 */
    List<ResumeVO> listMine();

    /** 新增简历 */
    ResumeUploadVO upload(MultipartFile file) throws IOException, TikaException;

    /** 删除简历 */
    void deleteMine(Long id);
}