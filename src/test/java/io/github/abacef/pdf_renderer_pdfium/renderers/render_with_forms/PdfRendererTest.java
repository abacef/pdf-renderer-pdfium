package io.github.abacef.pdf_renderer_pdfium.renderers.render_with_forms;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Paths;

public class PdfRendererTest {

    @SneakyThrows
    @Test
    public void render_invalidBytes() {
        val bytes = new byte[] {};

        assertThrows(
                IllegalArgumentException.class,
                () -> RenderWithForms.render(bytes, 0, 300),
                "Supposed to complain because there are no bytes"
        );
    }

    @Test
    @SneakyThrows
    public void basicRenderWithFormsTest() {
        val bytes = Files.readAllBytes(Paths.get("src/test/resources/sample_doc.pdf"));

        val output = RenderWithForms.render(bytes, 0, 300);
        Files.write(Paths.get("src/test/resources/sample_doc.png"), output);
    }
}
