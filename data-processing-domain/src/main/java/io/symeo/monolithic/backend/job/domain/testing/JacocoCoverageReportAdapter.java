package io.symeo.monolithic.backend.job.domain.testing;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CoverageData;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.FAILED_TO_PARSE_COVERAGE_REPORT;

@Slf4j
public class JacocoCoverageReportAdapter implements CoverageReportTypeAdapter {
    @Override
    public CoverageData extractCoverageFromReport(String report) throws SymeoException {
        try {
            DocumentBuilder documentBuilder = getDocumentBuilder();

            String reportWithoutDoctype = removeDoctypeFromReport(report);
            InputSource inputSource = new InputSource(new StringReader(reportWithoutDoctype));

            Document doc = documentBuilder.parse(inputSource);

            int missedInstructions = 0;
            int coveredInstructions = 0;

            Element reportEl = (Element) doc.getElementsByTagName("report").item(0);
            NodeList counterElements = reportEl.getElementsByTagName("counter");

            for (int i = 0; i < counterElements.getLength(); i++) {
                Element el = (Element) counterElements.item(i);
                if (el.hasAttribute("type") && el.getAttribute("type").equals("BRANCH")) {
                    missedInstructions = Integer.parseInt(el.getAttribute("missed"));
                    coveredInstructions = Integer.parseInt(el.getAttribute("covered"));
                }
            }

            return CoverageData.builder()
                    .coveredBranches(coveredInstructions)
                    .totalBranchCount(coveredInstructions + missedInstructions)
                    .build();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            final String message = "Failed to parse jacoco coverage report";
            LOGGER.error(message);
            throw SymeoException.builder()
                    .message(message)
                    .code(FAILED_TO_PARSE_COVERAGE_REPORT)
                    .build();
        }
    }

    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return documentBuilderFactory.newDocumentBuilder();
    }

    private static String removeDoctypeFromReport(String report) {
        final String regex = "^<!DOCTYPE.*>$";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

        return pattern.matcher(report).replaceAll("");
    }
}
