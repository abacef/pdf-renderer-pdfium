package io.github.abacef.pdf_renderer_pdfium;

import io.github.abacef.pdf_renderer_pdfium.exceptions.PdfiumException;
import io.github.abacef.pdf_renderer_pdfium.renderers.RenderWithForms;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PdfRendererTest {

    @Test
    public void basicRenderWithFormsTest() throws IOException, PdfiumException {
        val bytes = Files.readAllBytes(Paths.get("src/test/resources/sample_doc.pdf"));

        RenderWithForms.render(bytes, 0, 300);
    }
}
