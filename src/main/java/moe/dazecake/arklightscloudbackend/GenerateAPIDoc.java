package moe.dazecake.arklightscloudbackend;

import io.github.yedaxia.apidocs.Docs;
import io.github.yedaxia.apidocs.DocsConfig;
import io.github.yedaxia.apidocs.plugin.markdown.MarkdownDocPlugin;

public class GenerateAPIDoc {
    public static void main(String[] args) {
        DocsConfig config = new DocsConfig();
        config.setProjectPath("./"); // 项目根目录
        config.setProjectName("ArkLightsCloudBackEnd"); // 项目名称
        config.setApiVersion("Alpha");       // 声明该API的版本
        config.setDocsPath("./docs"); // 生成API 文档所在目录
        config.setAutoGenerate(Boolean.TRUE);  // 配置自动生成
        config.addPlugin(new MarkdownDocPlugin());
        config.addJavaSrcPath("./src/main/java");
        Docs.buildHtmlDocs(config); // 执行生成文档
    }
}
