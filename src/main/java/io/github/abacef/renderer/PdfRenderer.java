package io.github.abacef.renderer;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import io.github.abacef.pdfium.Pdfium;
import io.github.abacef.pdfium.fpdf_formfill.FPDF_FORMFILLINFO;
import io.github.abacef.renderer.exceptions.PdfiumException;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.val;

import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

public class PdfRenderer {

    private final Pdfium pdfium;

    public PdfRenderer() {
        this.pdfium = Pdfium.newInstance();
    }

    private Pointer loadDocument(
            final Pointer PdfBytes,
            final int pdfBytesLen,
            final IntByReference errorCode
    ) {
        val doc = pdfium.FPDF_LoadMemDocument(PdfBytes, pdfBytesLen, Pointer.NULL);
        if (doc == null) {
            errorCode.setValue(pdfium.FPDF_GetLastError().intValue());
            return Pointer.NULL;
        }

        return doc;
    }

    private FPDF_FORMFILLINFO makeFormFillInfo() {
        val formFillInfo = new FPDF_FORMFILLINFO();
        formFillInfo.version = 2;
        return formFillInfo;
    }

    private Pointer loadForm(
            final Pointer doc,
            final FPDF_FORMFILLINFO formFillInfo,
            final IntByReference errorCode) {
        val form = pdfium.FPDFDOC_InitFormFillEnvironment(doc, formFillInfo);
        if (form == null) {
            errorCode.setValue(pdfium.FPDF_GetLastError().intValue());
            return null;
        }

        pdfium.FPDF_SetFormFieldHighlightColor(form, Pdfium.FPDF_FORMFIELD_UNKNOWN, new NativeLong(0xFFE4dd));
        pdfium.FPDF_SetFormFieldHighlightAlpha(form, (byte)100);
        pdfium.FORM_DoDocumentOpenAction(form);

        return form;
    }

    private void closeDocument(final Pointer doc) {
        pdfium.FPDF_CloseDocument(doc);
    }

    private Pointer loadPage(final Pointer doc, final Pointer form, final int pageNum, final IntByReference errorCode) {
        val page = pdfium.FPDF_LoadPage(doc, pageNum);
        if (page == null) {
            errorCode.setValue(pdfium.FPDF_GetLastError().intValue());
            return null;
        }

        pdfium.FORM_OnAfterLoadPage(page, form);
        pdfium.FORM_DoPageAAction(page, form, Pdfium.FPDFPAGE_AACTION_OPEN);

        return page;
    }

    private void closeForm(final Pointer form) {
        pdfium.FPDFDOC_ExitFormFillEnvironment(form);
    }

    private void closePage(final Pointer page, final Pointer form) {
        pdfium.FORM_DoPageAAction(page, form, Pdfium.FPDFPAGE_AACTION_CLOSE);
        pdfium.FORM_OnBeforeClosePage(page, form);
        pdfium.FPDF_ClosePage(page);
    }

    private void closeRenderPage(final Pointer page) {
        pdfium.FPDF_RenderPage_Close(page);
    }

    private void closeLibrary() {
        pdfium.FPDF_DestroyLibrary();
    }

    @Data
    @Builder
    private static class RenderBitmapReturn {
        private Pointer buffer;
        private int width;
        private int height;
    }

    private RenderBitmapReturn renderPageToBitmap(
            final Pointer page,
            final Pointer form,
            final int dpi
    ) {
        val pageWidthInPoints = pdfium.FPDF_GetPageWidthF(page);
        val pageWidthInInches = pageWidthInPoints / 72;
        val width = (int)(pageWidthInInches * dpi);

        val pageHeightInPoints = pdfium.FPDF_GetPageHeightF(page);
        val pageHeightInInches = pageHeightInPoints / 72;
        val height = (int)(pageHeightInInches * dpi);

        val alpha = pdfium.FPDFPage_HasTransparency(page);

        Pointer bitmap = pdfium.FPDFBitmap_Create(width, height, alpha);
        // returns null for parameter error or out of memory
        if (bitmap == null) {
            return null;
        }

        val fillColor = new NativeLong(alpha == 1 ? 0x00000000 : 0xffffffff);
        pdfium.FPDFBitmap_FillRect(bitmap, 0, 0, width, height, fillColor);
        pdfium.FPDF_RenderPageBitmap(bitmap, page, 0, 0, width, height, 0, Pdfium.FPDF_ANNOT);
        pdfium.FPDF_FFLDraw(form, bitmap, page, 0, 0, width, height, 0, Pdfium.FPDF_ANNOT);

        val buffer = pdfium.FPDFBitmap_GetBuffer(bitmap);

        return RenderBitmapReturn.builder()
                .buffer(buffer)
                .width(width)
                .height(height)
                .build();
    }

    public BufferedImage renderPdfPageToImage(
            final @NonNull byte[] pdfBytes,
            final int pageNum,
            final int dpi
    ) throws PdfiumException {
        val pdfBytesMemory = new Memory((long) pdfBytes.length * Native.getNativeSize(Byte.TYPE));
        IntStream.range(0, pdfBytes.length).forEach(i ->
                pdfBytesMemory.setByte((long) i * Native.getNativeSize(Byte.TYPE), pdfBytes[i]));

        IntByReference errorCode = new IntByReference();

        val doc = loadDocument(pdfBytesMemory, pdfBytes.length, errorCode);
        if (doc == null) {
            closeLibrary();
            throw PdfiumException.exceptionNumberToException(errorCode.getValue());
        }

        val formFillInfo = makeFormFillInfo();
        val form = loadForm(doc, formFillInfo, errorCode);
        if (form == null) {
            closeDocument(doc);
            closeLibrary();
            throw PdfiumException.exceptionNumberToException(errorCode.getValue());
        }

        val page = loadPage(doc, form, pageNum, errorCode);
        if (page == null) {
            closeForm(form);
            closeDocument(doc);
            closeLibrary();
            throw PdfiumException.exceptionNumberToException(errorCode.getValue());
        }

        val bitmapRenderReturn = renderPageToBitmap(page, form, dpi);
        BufferedImage image = null;
        if (bitmapRenderReturn == null) {
            closeRenderPage(page);
        } else {
            val width = bitmapRenderReturn.getWidth();
            val height = bitmapRenderReturn.getHeight();
            val buffer = bitmapRenderReturn.getBuffer();

            val bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            val pixelCount = width * height;
            val javaBuffer = buffer.getIntArray(0, pixelCount);
            bufferedImage.setRGB(0, 0, width, height, javaBuffer, 0, width);
            image = bufferedImage;
        }

        closePage(page, form);
        closeForm(form);
        closeDocument(doc);
        closeLibrary();
        return image;
    }
}
