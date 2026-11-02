package zsj.mockmate.utils;

import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 文件名工具。
 *
 * <p>纯函数、无状态、不依赖 Spring 容器，所以放在 utils 而不是 Service 里。
 *
 * <p>注意这里<b>不用</b> {@code Paths.get().getFileName()}：
 * 反斜杠在 Linux 上不是路径分隔符，本地 Windows 正常、部署到 Linux 就会
 * 把整条 {@code C:\fake\x.pdf} 当成文件名入库。所以手动把两种分隔符都处理掉。
 */
public final class FileNameUtils {

    /** 入库文件名的最长长度（对应 resume.file_name varchar(255)，留余量） */
    private static final int MAX_NAME_LENGTH = 200;

    /** 兜底文件名 */
    private static final String FALLBACK_NAME = "resume.txt";

    /** 工具类不允许实例化 */
    private FileNameUtils() {
    }

    /**
     * 把客户端传来的原始文件名清洗成可以安全入库的名字。
     *
     * <ol>
     *   <li>去掉路径成分（同时兼容 / 和 \）</li>
     *   <li>去掉换行符，防响应头注入</li>
     *   <li>超长时截断，但保留扩展名</li>
     *   <li>空名兜底成 resume.txt</li>
     * </ol>
     *
     * @param raw 客户端给的 FileName，可能为 null、可能带路径
     */
    public static String safeName(String raw) {
        if (!StringUtils.hasText(raw)) {
            return FALLBACK_NAME;
        }

        // 1. 两种分隔符都当成分隔符，取最后一段
        String name = raw.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }

        // 2. 去换行（Content-Disposition 里带 \r\n 可以注入响应头）
        name = name.replaceAll("[\\r\\n]", "").trim();
        if (!StringUtils.hasText(name)) {
            return FALLBACK_NAME;
        }

        // 3. 截断但保住扩展名，否则 file_name 和 file_type 会对不上
        if (name.length() > MAX_NAME_LENGTH) {
            String ext = extension(name);
            int keep = MAX_NAME_LENGTH - (ext.isEmpty() ? 0 : ext.length() + 1);
            name = name.substring(0, Math.max(keep, 1)) + (ext.isEmpty() ? "" : "." + ext);
        }

        return name;
    }

    /**
     * 取扩展名，小写。没有扩展名返回空串。
     *
     * <p>{@code .gitignore} 这种隐藏文件不算有扩展名（点在最前面）。
     */
    public static String extension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        // dot <= 0       ：没有点，或者点在首位（.gitignore）
        // dot == length-1：以点结尾（file.）
        if (dot <= 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 取大写扩展名，取不到返回 fallback。
     *
     * @param fallback 没有扩展名时的兜底值，例如 "TXT"
     */
    public static String extensionUpper(String fileName, String fallback) {
        String ext = extension(fileName);
        return ext.isEmpty() ? fallback : ext.toUpperCase(Locale.ROOT);
    }
}
