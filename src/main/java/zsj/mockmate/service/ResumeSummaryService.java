package zsj.mockmate.service;

/**
 * 简历摘要：把简历原文压成一份"面试提问素材清单"，面试时注入 system。
 *
 * <p>摘要是异步生成的（本地模型一次十几秒），所以有状态字段：
 * PENDING（刚入库）→ PARSING（生成中）→ READY（可用）/ FAILED（失败）。
 *
 * <p>调用方注意：{@link #getSummary} 是纯读的，没生成好就返回 null，
 * 需要补触发请显式调 {@link #generateAsync}（必须跨 bean 调用，否则 @Async 失效）。
 */
public interface ResumeSummaryService {

    /** 刚入库，还没开始生成 */
    String STATUS_PENDING = "PENDING";

    /** 正在生成 */
    String STATUS_PARSING = "PARSING";

    /** 摘要可用 */
    String STATUS_READY = "READY";

    /** 生成失败（可重试） */
    String STATUS_FAILED = "FAILED";

    /**
     * 后台生成摘要（异步，不阻塞调用方）。
     *
     * <p>内部兜住所有异常并置 FAILED —— 注意 @Async 方法里抛出的异常会被 Spring 吞掉，
     * 不自己 catch 的话状态会永远停在 PARSING。
     */
    void generateAsync(Long resumeId);

    /**
     * 同步生成摘要。已经 READY 的直接返回，不重复烧算力（幂等）。
     *
     * @return 生成的摘要；失败或没内容时返回 null
     */
    String generate(Long resumeId);

    /**
     * 取摘要（面试注入时用）。纯读，没有副作用。
     *
     * @return 摘要文本；还没生成好返回 null
     */
    String getSummary(Long resumeId);

    /** 摘要还没生成好、且不是正在生成中 —— 用来判断要不要补触发一次 */
    boolean needGenerate(Long resumeId);
}
