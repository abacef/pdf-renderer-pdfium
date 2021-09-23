package io.github.abacef.pdf_renderer_pdfium.renderers.render_with_forms;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import io.github.abacef.pdf_renderer_pdfium.renderers.PDFRenderer;
import io.github.abacef.pdfium.Pdfium;
import io.github.abacef.pdfium.fpdf_view.FPDF_LIBRARY_CONFIG;
import io.github.abacef.pdf_renderer_pdfium.exceptions.PdfiumException;
import lombok.NonNull;
import lombok.val;

import java.util.stream.IntStream;

/**
 * Renders a page of a PDF to a PNG image at the specified DPI, rendering any acro-forms as it would be seen by a user
 */
public final class RenderWithForms extends PDFRenderer {

    private static Pdfium initLibraryWithConfig() {
        val pdfiumLocal = Pdfium.newInstance();

        val config = new FPDF_LIBRARY_CONFIG();
        config.version = 2;
        config.m_pUserFontPaths = null;
        config.m_pIsolate = null;
        config.m_v8EmbedderSlot = 0;
        config.m_pPlatform = null;
        pdfiumLocal.FPDF_InitLibraryWithConfig(config);

        return pdfiumLocal;
    }

    private static void closeLibrary(final Pdfium pdfium) {
        pdfium.FPDF_DestroyLibrary();
    }

    public static byte[] render(
            final @NonNull byte[] pdfBytes,
            final int pageNum,
            final int dpi
    ) throws PdfiumException {
        if (pdfBytes.length == 0) {
            throw new IllegalArgumentException("There must be bytes in the passed in byte array");
        } else if (pageNum < 0) {
            throw new IllegalArgumentException("Page number to render must not be negative");
        } else if (dpi <= 0) {
            throw new IllegalArgumentException("DPI must be greater than zero");
        }

        val pdfium = initLibraryWithConfig();

        val pdfBytesMemory = new Memory((long) pdfBytes.length * Native.getNativeSize(Byte.TYPE));
        IntStream.range(0, pdfBytes.length).forEach(i ->
                pdfBytesMemory.setByte((long) i * Native.getNativeSize(Byte.TYPE), pdfBytes[i]));

        try (val document = new Document(pdfium, pdfBytesMemory, pdfBytes.length)) {
            try (val form = new Form(pdfium, document)) {
                try (val page = new Page(pdfium, document, form, pageNum)) {
                    try (val bitmap = new Bitmap(pdfium, page, form, dpi, Bitmap.OutputImageType.PNG)) {
                        return bitmap.convertToImage();
                    }
                }
            }
        } finally {
            closeLibrary(pdfium);
        }
    }
}
