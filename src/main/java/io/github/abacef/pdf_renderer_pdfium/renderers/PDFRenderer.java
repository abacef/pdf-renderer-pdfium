package io.github.abacef.pdf_renderer_pdfium.renderers;

import io.github.abacef.pdf_renderer_pdfium.exceptions.PdfiumException;
import lombok.NonNull;

public interface PDFRenderer {
    byte[] render(
            @NonNull byte[] pdfBytes,
            int pageNum,
            int dpi
    ) throws PdfiumException;
}
