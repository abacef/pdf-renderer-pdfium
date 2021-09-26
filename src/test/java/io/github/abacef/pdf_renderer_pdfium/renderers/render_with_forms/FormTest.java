package io.github.abacef.pdf_renderer_pdfium.renderers.render_with_forms;

import io.github.abacef.pdfium.Pdfium;
import io.github.abacef.pdfium.fpdf_view.FPDF_LIBRARY_CONFIG;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FormTest {

    private Document document;

    private Pdfium pdfium;

    private Form form;

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

        val pdfBytes = Files.readAllBytes(Paths.get("src/test/resources/sample_doc.pdf"));

        document = new Document(pdfium, pdfBytes);
    }

    @AfterEach
    public void cleanupPdfium() {
        if (form != null) {
            form.close();
        }
        document.close();
        pdfium.FPDF_DestroyLibrary();
        pdfium = null;
    }

    @Test
    public void nonNullPdfium() {
        assertThrows(
                NullPointerException.class,
                () -> form = new Form(null, document),
                "PDFium needs to be validated that it is not null"
        );
    }

    @Test
    public void nonNullDocument() {
        assertThrows(
                NullPointerException.class,
                () -> form = new Form(pdfium, null),
                "Document needs to be validated that is not null"
        );
    }

    @Test
    @SneakyThrows
    public void happyPath() {
        try (val form = new Form(pdfium, document)) {
            this.form = form;
        }
    }

    @Test
    @SneakyThrows
    public void happyPathDoubleClosed_DoesNotDoAnythingBad() {
        form = new Form(pdfium, document);
        form.close();
        form.close();
    }
}
