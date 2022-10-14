package io.symeo.monolithic.backend.infrastructure.testing.metrics.languages;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.symeo.monolithic.backend.domain.exception.SymeoException.getSymeoException;
import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.GIT_FAILED_TO_OPEN_REPOSITORY;

public abstract class AbstractLanguageTestingMetricsAdapter {
    public List<String> getFiles(String repositoryFilesPath, String[] filesSuffixes, String[] ignoredDirectories) throws SymeoException {

        try (Stream<Path> walk = Files.walk(Paths.get(repositoryFilesPath))) {

            return walk.map(Path::toString)
                    .filter(f -> fileHasOneOfSuffixes(f, filesSuffixes) && !fileIsInFolder(f, ignoredDirectories)).toList();
        } catch (IOException e) {
            throw getSymeoException("Failed to open git repository folder at " + repositoryFilesPath, GIT_FAILED_TO_OPEN_REPOSITORY);
        }
    }

    public Integer getPatternCountInFiles(List<String> filesPaths, Pattern pattern) {
        Integer result = 0;
        for (String filePath : filesPaths) {
            try {
                String fileContent = Files.readString(Paths.get(filePath), Charset.defaultCharset());
                Matcher matcher = pattern.matcher(fileContent);

                while (matcher.find()) {
                    result++;
                }
            } catch (IOException ignored) {
            }
        }
        return result;
    }

    public Integer getLineCountForFilePaths(List<String> filesPaths) {
        int result = 0;
        for (String filePath : filesPaths) {
            try {
                Path path = Paths.get(filePath);
                result += (int) Files.lines(path).count();
            } catch (IOException ignored) {
            }
        }
        return result;
    }

    private static boolean fileHasOneOfSuffixes(String filePath, String[] suffixes) {
        return Arrays.stream(suffixes).anyMatch(filePath::endsWith);
    }

    private static boolean fileIsInFolder(String filePath, String[] folderNames) {
        return Arrays.stream(folderNames).anyMatch(folderName -> filePath.contains("/" + folderName + "/"));
    }
}
