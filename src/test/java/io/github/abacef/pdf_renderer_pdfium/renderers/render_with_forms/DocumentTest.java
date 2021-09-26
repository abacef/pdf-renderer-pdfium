package io.github.abacef.pdf_renderer_pdfium.renderers.render_with_forms;

import io.github.abacef.pdf_renderer_pdfium.exceptions.PdfiumException;
import io.github.abacef.pdfium.Pdfium;
import io.github.abacef.pdfium.fpdf_view.FPDF_LIBRARY_CONFIG;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Paths;

public class DocumentTest {

    private Pdfium pdfium;

    private byte[] pdfBytes;

    private Document document;

    @BeforeEach
    @SneakyThrows
    public void setupPdfium() {
        pdfium = Pdfium.newInstance();

        val config = new FPDF_LIBRARY_CONFIG();
        config.version = 2;
        config.m_pUserFontPaths = null;
        config.m_pIsolate = null;
        config.m_v8EmbedderSlot = 0;
        config.m_pPlatform = null;
        pdfium.FPDF_InitLibraryWithConfig(config);

        pdfBytes = Files.readAllBytes(Paths.get("src/test/resources/sample_doc.pdf"));
    }

    @AfterEach
    public void cleanupPdfium() {
        if (document != null) {
            document.close();
        }
        pdfium.FPDF_DestroyLibrary();
        pdfium = null;
    }

    @Test
    public void nonNullPdfium() {
        assertThrows(
                NullPointerException.class,
                () -> document = new Document(null, pdfBytes),
                "PDFium needs to be validated that it is not null");
    }

    @Test
    public void nonNullBytes() {
        assertThrows(
                NullPointerException.class,
                () -> document = new Document(pdfium, null),
                "PDF bytes needs to be validated that it is not null"
        );
    }

    @Test
    @SneakyThrows
    public void happyPath() {
        try (val doc = new Document(pdfium, pdfBytes)) {
            document = doc;
        }
    }

    @Test
    @SneakyThrows
    public void happyPathWithCloseAfterClose_doesNotDoAnythingBad() {
        document = new Document(pdfium, pdfBytes);
        document.close();
        document.close();
    }

    @Test
    public void randomBytes_throwsException() {
        val localBytes = new byte[] { 23, 45, 0 };
        assertThrows(
                PdfiumException.class,
                () -> document = new Document(pdfium, localBytes),
                "Random bytes should make Pdfium throw an exception"
        );
    }
}
