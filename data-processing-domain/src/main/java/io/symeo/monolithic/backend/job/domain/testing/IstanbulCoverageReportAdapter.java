package io.symeo.monolithic.backend.job.domain.testing;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.StringReader;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.FAILED_TO_PARSE_COVERAGE_REPORT;

@Slf4j
public class IstanbulCoverageReportAdapter implements CoverageReportTypeAdapter {
    @Override
    public Float extractCoverageFromReport(String report) throws SymeoException {
        try {
            DocumentBuilder documentBuilder = getDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(report));

            Document doc = documentBuilder.parse(inputSource);

            Element coverageEl = (Element) doc.getElementsByTagName("coverage").item(0);
            Element projectEl = (Element) coverageEl.getElementsByTagName("project").item(0);
            Element metricsEl = (Element) projectEl.getElementsByTagName("metrics").item(0);

            int conditionals = Integer.parseInt(metricsEl.getAttribute("conditionals"));
            int coveredConditionals = Integer.parseInt(metricsEl.getAttribute("coveredconditionals"));

            return (float) coveredConditionals / conditionals;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            final String message = "Failed to parse istanbul coverage report";
            LOGGER.warn(message);
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
}
