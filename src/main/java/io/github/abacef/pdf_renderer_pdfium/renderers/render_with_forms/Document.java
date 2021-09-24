package io.github.abacef.pdf_renderer_pdfium.renderers.render_with_forms;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import io.github.abacef.pdf_renderer_pdfium.exceptions.PdfiumException;
import io.github.abacef.pdfium.Pdfium;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;

import java.util.stream.IntStream;

public class Document implements AutoCloseable {

    @AllArgsConstructor
    static class Resources {
        private @NonNull final Pointer documentPointer;
        private @NonNull final Pdfium pdfium;
    }

    private Resources resources;

    public Document(
            @NonNull final Pdfium pdfium,
            @NonNull final byte[] pdfBytes
    ) throws PdfiumException {
        val pdfBytesMemory = new Memory((long) pdfBytes.length * Native.getNativeSize(Byte.TYPE));
        IntStream.range(0, pdfBytes.length).forEach(i ->
                pdfBytesMemory.setByte((long) i * Native.getNativeSize(Byte.TYPE), pdfBytes[i]));

        val documentPointer = pdfium.FPDF_LoadMemDocument(pdfBytesMemory, pdfBytes.length, null);
        if (documentPointer == null) {
            throw PdfiumException.exceptionNumberToException(pdfium.FPDF_GetLastError().intValue());
        }

        resources = new Resources(documentPointer, pdfium);
    }

    public Pointer getDocumentPointer() {
        return resources.documentPointer;
    }

    @Override
    public void close() {
        if (resources != null) {
            resources.pdfium.FPDF_CloseDocument(resources.documentPointer);
            resources = null;
        }
    }
}
