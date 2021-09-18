package io.github.abacef.pdf_renderer_pdfium;

import io.github.abacef.pdf_renderer_pdfium.exceptions.PdfiumException;
import io.github.abacef.pdf_renderer_pdfium.renderers.PdfRenderer;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PdfRendererTest {

    @Test
    public void basicRenderTest() throws IOException, PdfiumException {
        val bytes = Files.readAllBytes(Paths.get("src/test/resources/sample_doc.pdf"));

        val renderer = new PdfRenderer();

        renderer.render(bytes, 0, 300);
    }
}
