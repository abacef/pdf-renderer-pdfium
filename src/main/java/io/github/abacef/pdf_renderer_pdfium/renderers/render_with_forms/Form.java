package io.github.abacef.pdf_renderer_pdfium.renderers.render_with_forms;

import com.sun.jna.Pointer;
import io.github.abacef.pdf_renderer_pdfium.exceptions.PdfiumException;
import io.github.abacef.pdfium.Pdfium;
import io.github.abacef.pdfium.fpdf_formfill.FPDF_FORMFILLINFO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;

public class Form implements AutoCloseable {

    @AllArgsConstructor
    private static class Resources {
        private @NonNull final Pointer formPointer;
        private @NonNull final Pdfium pdfium;
    }

    private Resources resources;

    public Form(final Pdfium pdfium, final Document document) throws PdfiumException {
        val formPointer = pdfium.FPDFDOC_InitFormFillEnvironment(document.getDocumentPointer(), makeFormFillInfo());
        if (formPointer == null) {
            throw PdfiumException.exceptionNumberToException(pdfium.FPDF_GetLastError().intValue());
        }

        pdfium.FORM_DoDocumentOpenAction(formPointer);
        resources = new Resources(formPointer, pdfium);
    }

    public Pointer getFormPointer() {
        return resources.formPointer;
    }

    private static FPDF_FORMFILLINFO makeFormFillInfo() {
        val formFillInfo = new FPDF_FORMFILLINFO();
        formFillInfo.version = 2;
        return formFillInfo;
    }

    @Override
    public void close() {
        if (resources != null) {
            resources.pdfium.FPDFDOC_ExitFormFillEnvironment(resources.formPointer);
            resources = null;
        }
    }
}
