package io.github.abacef.renderer;

import io.github.abacef.renderer.exceptions.PdfiumException;
import lombok.val;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PdfRendererTest {

    @Test
    public void basicRenderTest() throws IOException, PdfiumException {
        val bytes = Files.readAllBytes(Paths.get("src/test/resources/sample_doc.pdf"));

        val renderer = new PdfRenderer();
        val res = renderer.renderPdfPageToImage(bytes, 0, 300);
        File out = new File("src/test/resources/sampleOutput.png");
        ImageIO.write(res, "png", out);
    }
}
