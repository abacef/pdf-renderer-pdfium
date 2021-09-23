package io.github.abacef.pdf_renderer_pdfium.renderers.render_with_forms;

import com.sun.jna.Pointer;
import io.github.abacef.pdf_renderer_pdfium.exceptions.PdfiumException;
import io.github.abacef.pdfium.Pdfium;
import lombok.Getter;

public class Document implements AutoCloseable {

    @Getter
    private Pointer documentPointer;

    private Pdfium pdfium;

    public Document(final Pdfium pdfium, final Pointer PdfBytes, final int pdfBytesLen) throws PdfiumException {
        documentPointer = pdfium.FPDF_LoadMemDocument(PdfBytes, pdfBytesLen, null);
        if (documentPointer == null) {
            throw PdfiumException.exceptionNumberToException(pdfium.FPDF_GetLastError().intValue());
        }
        this.pdfium = pdfium;
    }

    @Override
    public void close() {
        if (pdfium != null && documentPointer != null) {
            pdfium.FPDF_CloseDocument(documentPointer);
            pdfium = null;
            documentPointer = null;
        }
    }
}
