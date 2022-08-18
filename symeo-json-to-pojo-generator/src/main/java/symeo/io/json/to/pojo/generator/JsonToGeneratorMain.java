package symeo.io.json.to.pojo.generator;

import com.sun.codemodel.JCodeModel;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.rules.RuleFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class JsonToGeneratorMain {


    public static void main(String[] args) throws IOException {
        final Converter converter = new Converter();

        final URL inputJsonUrl = new URL("file:///Users/ilysse/Workspace/CATLEAN/symeo-monolithic-backend/symeo-json-to-pojo-generator/src/main/resources/single_github_repo.json");
        final File outputJavaClassDirectory = new File("/Users/ilysse/Workspace/CATLEAN/symeo-monolithic-backend" +
                "/infrastructure/github-adapter/src/main/java");
        final String packageName = "io.symeo.monolithic.backend.infrastructure.github.adapter.dto.repo";
        final String javaClassName = "GithubRepositoryDTO";
        converter.convertJsonToJavaClass(
                inputJsonUrl, outputJavaClassDirectory, packageName, javaClassName);
    }


    static class Converter {

        public void convertJsonToJavaClass(URL inputJsonUrl, File outputJavaClassDirectory, String packageName,
                                           String javaClassName)
                throws IOException {
            JCodeModel jcodeModel = new JCodeModel();

            GenerationConfig config = new DefaultGenerationConfig() {
                @Override
                public boolean isGenerateBuilders() {
                    return true;
                }

                @Override
                public SourceType getSourceType() {
                    return SourceType.JSON;
                }
            };

            SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new Jackson2Annotator(config),
                    new SchemaStore()), new SchemaGenerator());
            mapper.generate(jcodeModel, javaClassName, packageName, inputJsonUrl);

            jcodeModel.build(outputJavaClassDirectory);
        }
    }
}
