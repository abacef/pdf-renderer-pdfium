package io.github.abacef.pdf_renderer_pdfium.exceptions;

import lombok.NonNull;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class PDFiumExceptionTest {
    public static Object[][] allExceptions() {
        return new Object[][] {
                { new PdfiumFileException() },
                { new PdfiumFormatException() },
                { new PdfiumPageException() },
                { new PdfiumPasswordException() },
                { new PdfiumSecurityException() },
                { new PdfiumUnknownException() }
        };
    }

    @ParameterizedTest
    @MethodSource("allExceptions")
    public void exceptions_existsUsefulMessage(final @NonNull PdfiumException pdfiumException) {
        val message = pdfiumException.getMessage();
        assertNotNull(message);
        assertTrue(message.length() > 0);
    }

    @Test
    public void allExceptionsAreThrown() {
        val exceptionList = Arrays.stream(allExceptions())
                .map(x -> x[0])
                .map(x -> (PdfiumException) x)
                .map(Throwable::getMessage)
                .collect(Collectors.toSet());

        val numOfExceptions = exceptionList.size();

        // PDFium exceptions start from zero
        IntStream.range(1, numOfExceptions + 1).forEach(i -> {
            val exception = PdfiumException.exceptionNumberToException(i);
            assertTrue(exceptionList.contains(exception.getMessage()));
            exceptionList.remove(exception.getMessage());
        });

        assertThrows(IllegalArgumentException.class,
                () -> PdfiumException.exceptionNumberToException(numOfExceptions + 1),
                "There are only 6 exceptions that we support in the PDFium API");
    }

    @Test
    public void pdfiumException_failFastOnNullMessage() {
        assertThrows(
                NullPointerException.class,
                () -> new PdfiumException(null),
                "PDFiumException should accept a message that is not null"
        );
    }
}
