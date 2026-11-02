package zsj.mockmate.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zsj.mockmate.mapper.ResumeMapper;
import zsj.mockmate.pojo.ApiException;
import zsj.mockmate.pojo.entity.resumeEntity;
import zsj.mockmate.pojo.vo.ResumeUploadVO;
import zsj.mockmate.pojo.vo.ResumeVO;
import zsj.mockmate.service.ResumeService;
import zsj.mockmate.utils.FileNameUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ResumeServiceImpl implements ResumeService {

    /** 抽出来少于这么多字，基本就是扫描件 */
    private static final int MIN_TEXT_LENGTH = 50;

    private final ResumeMapper resumeMapper;

    public ResumeServiceImpl(ResumeMapper resumeMapper) {
        this.resumeMapper = resumeMapper;
    }

    /**
     * 获取简历
     * @return
     */
    @Override
    public List<ResumeVO> listMine() {
        return resumeMapper.findByUserId(StpUtil.getLoginIdAsLong());
    }

    /**
     * 新增简历
     * @param file
     * @return
     */
    @Override
    public ResumeUploadVO upload(MultipartFile file) {

        // ① 校验
        if (file == null || file.isEmpty()) {
            throw new ApiException("请选择要上传的文件");
        }

        // ② 取文件名和类型。先取扩展名再清洗名字：
        //    safeName() 超长会截断，先取扩展名才不会把后缀截掉
        String originalName = file.getOriginalFilename();
        String fileType = FileNameUtils.extensionUpper(originalName, "TXT");
        String fileName = FileNameUtils.safeName(originalName);

        // ③ 解析
        String rawText;
        try {
            rawText = new Tika().parseToString(file.getInputStream());
        } catch (IOException | TikaException e) {
            log.error("简历解析失败", e);
            throw new ApiException("简历解析失败：" + e.getMessage());
        }

        // ④ 扫描件判断
        if (rawText == null || rawText.trim().length() < MIN_TEXT_LENGTH) {
            throw new ApiException("这份文件里提取不到文字，可能是扫描件（图片版 PDF）");
        }

        // ⑤ 组装实体（id / createdAt 都不设）
        resumeEntity resume = new resumeEntity();
        resume.setUserId(StpUtil.getLoginIdAsLong());
        resume.setFileName(fileName);
        resume.setFileType(fileType);
        resume.setFileSize(file.getSize());
        resume.setRawText(rawText);

        // ⑥ 入库
        resumeMapper.insertResume(resume);

        log.info("简历入库: id={}, userId={}, fileName={}, 字数={}",
                resume.getId(), resume.getUserId(), fileName, rawText.length());

        return new ResumeUploadVO(resume.getId(), fileName, file.getSize(), rawText.length());
    }

    /**
     * 删除简历（目前没有绑定会话，以后要改TODO）
     * @param id
     */
    @Override
    public void deleteMine(Long id) {
        resumeMapper.deleteById(id,StpUtil.getLoginIdAsLong());
    }
}