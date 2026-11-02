package zsj.mockmate.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zsj.mockmate.pojo.Result;
import zsj.mockmate.pojo.vo.ResumeUploadVO;
import zsj.mockmate.pojo.vo.ResumeVO;
import zsj.mockmate.service.ResumeService;

import java.io.IOException;
import java.util.List;

/**
 * 简历相关接口
 */

@RestController
@RequestMapping("/resume")
@Slf4j
public class resumeController {

    private ResumeService resumeService;

    public resumeController(ResumeService resumeService){
        this.resumeService = resumeService;
    }


    /**
     * 新增简历（未完成）
     * @param file
     * @return
     */
    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ResumeUploadVO> upload(@RequestParam("file") MultipartFile file) throws TikaException, IOException {
        log.info("文件上传成功");
        return Result.success(resumeService.upload(file));
    }

    /**
     * 查询所有简历
     * @return
     */
    @GetMapping()
    public Result<List<ResumeVO>> list(){
        return Result.success(resumeService.listMine());
    }

    /**
     * 删除简历
     * @param id 简历 id
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        resumeService.deleteMine(id);
        return Result.success();
    }
}
