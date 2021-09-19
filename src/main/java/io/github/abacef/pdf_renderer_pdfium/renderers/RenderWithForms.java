package io.github.abacef.pdf_renderer_pdfium.renderers;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import io.github.abacef.pdfium.Pdfium;
import io.github.abacef.pdfium.fpdf_formfill.FPDF_FORMFILLINFO;
import io.github.abacef.pdfium.fpdf_view.FPDF_LIBRARY_CONFIG;
import io.github.abacef.pdf_renderer_pdfium.exceptions.PdfiumException;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.val;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.IntStream;

/**
 * Renders a page of a PDF to a PNG image at the specified DPI
 */
public final class RenderWithForms extends PDFRenderer {

    private final Pdfium pdfium;

    private RenderWithForms() {
        this.pdfium = initLibraryWithConfig();
    }

    private Pdfium initLibraryWithConfig() {
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

    private Pointer loadDocument(
            final Pointer PdfBytes,
            final int pdfBytesLen,
            final IntByReference errorCode
    ) {
        val doc = pdfium.FPDF_LoadMemDocument(PdfBytes, pdfBytesLen, null);
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
        val pageWidthInInches = pageWidthInPoints / POINTS_PER_INCH;
        val width = (int)(pageWidthInInches * dpi);

        val pageHeightInPoints = pdfium.FPDF_GetPageHeightF(page);
        val pageHeightInInches = pageHeightInPoints / POINTS_PER_INCH;
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

    private enum OutputImageType {
        PNG,
        JPG
    }

    private byte[] bufferedImageToBytes(
            final int width,
            final int height,
            final int[] bitmap,
            final @NonNull OutputImageType imageType
    ) throws IOException {
        switch (imageType) {
            case PNG:
                val bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
                bufferedImage.setRGB(0, 0, width, height, bitmap, 0, width);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                return baos.toByteArray();
            case JPG:
                throw new IllegalArgumentException("Currently we do not support rendering an image in JPEG");
            default:
                return null;
        }
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

        val render = new RenderWithForms();

        val pdfBytesMemory = new Memory((long) pdfBytes.length * Native.getNativeSize(Byte.TYPE));
        IntStream.range(0, pdfBytes.length).forEach(i ->
                pdfBytesMemory.setByte((long) i * Native.getNativeSize(Byte.TYPE), pdfBytes[i]));

        IntByReference errorCode = new IntByReference();

        val doc = render.loadDocument(pdfBytesMemory, pdfBytes.length, errorCode);
        if (doc == null) {
            render.closeLibrary();
            throw PdfiumException.exceptionNumberToException(errorCode.getValue());
        }

        val formFillInfo = render.makeFormFillInfo();
        val form = render.loadForm(doc, formFillInfo, errorCode);
        if (form == null) {
            render.closeDocument(doc);
            render.closeLibrary();
            throw PdfiumException.exceptionNumberToException(errorCode.getValue());
        }

        val page = render.loadPage(doc, form, pageNum, errorCode);
        if (page == null) {
            render.closeForm(form);
            render.closeDocument(doc);
            render.closeLibrary();
            throw PdfiumException.exceptionNumberToException(errorCode.getValue());
        }

        val bitmapRenderReturn = render.renderPageToBitmap(page, form, dpi);
        byte[] bytesReturn = null;
        if (bitmapRenderReturn == null) {
            render.closeRenderPage(page);
        } else {
            val width = bitmapRenderReturn.getWidth();
            val height = bitmapRenderReturn.getHeight();
            val bufferPointer = bitmapRenderReturn.getBuffer();
            val pixelCount = width * height;
            val javaBuffer = bufferPointer.getIntArray(0, pixelCount);
            try {
                bytesReturn = render.bufferedImageToBytes(
                        bitmapRenderReturn.getWidth(),
                        bitmapRenderReturn.getHeight(),
                        javaBuffer,
                        OutputImageType.PNG);
            } catch (IOException ioe) {
                bytesReturn = null;
            }
        }

        render.closePage(page, form);
        render.closeForm(form);
        render.closeDocument(doc);
        render.closeLibrary();

        return bytesReturn;
    }
}
