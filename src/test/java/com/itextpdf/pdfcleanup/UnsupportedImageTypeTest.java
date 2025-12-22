/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
    Authors: Apryse Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.pdfcleanup;

import com.itextpdf.commons.utils.StringNormalizer;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfcleanup.exceptions.CleanupExceptionMessageConstant;
import com.itextpdf.pdfcleanup.util.CleanUpImageUtil;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@Tag("IntegrationTest")
public class UnsupportedImageTypeTest extends ExtendedITextTest {

    private static final String INPUT_PATH = "./src/test/resources/com/itextpdf/pdfcleanup/UnsupportedImageTypeTest/";
    private static final String OUTPUT_PATH = "./target/test/com/itextpdf/pdfcleanup/UnsupportedImageTypeTest/";

    @BeforeAll
    public static void before() {
        createOrClearDestinationFolder(OUTPUT_PATH);
    }

    @Test
    public void checkUnSupportedImageTypeTest() throws IOException, InterruptedException {
        String input = INPUT_PATH + "JpegCmykImage.pdf";
        String output = OUTPUT_PATH + "JpegCmykImage.pdf";
        String cmp = INPUT_PATH + "cmp_JpegCmykImage.pdf";

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output, new WriterProperties()));
        PdfCleanUpTool workingTool = new PdfCleanUpTool(pdfDocument);
        int pageIndex = 1;
        Rectangle area = pdfDocument.getPage(pageIndex).getPageSize();
        workingTool.addCleanupLocation(new PdfCleanUpLocation(pageIndex, area, ColorConstants.RED));

        try {
            workingTool.cleanUp();
            pdfDocument.close();
            compareByContent(cmp, output, OUTPUT_PATH, "diff_UnsupportedImageType_");
        } catch (CleanUpImageUtil.CleanupImageHandlingUtilException e) {
            // CMYK bug https://bugs.openjdk.org/browse/JDK-8274735 in openJDK:
            // fixed for jdk8 from 351 onwards, for jdk11 from 16 onwards and for jdk17 starting from 4.
            // Amazon corretto jdk started support CMYK for JPEG from 11 version.
            // Temurin 8 does not support CMYK for JPEG either.
            Assertions.assertTrue(
                    StringNormalizer.toLowerCase(CleanupExceptionMessageConstant.UNSUPPORTED_IMAGE_TYPE)
                            .equals(StringNormalizer.toLowerCase(e.getMessage())) ||
                            "incompatible color conversion".equals(StringNormalizer.toLowerCase(e.getMessage())));
            pdfDocument.close();
        }

    }

    private void compareByContent(String cmp, String output, String targetDir, String diffPrefix)
            throws IOException, InterruptedException {
        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareByContent(output, cmp, targetDir, diffPrefix + "_");

        if (errorMessage != null) {
            Assertions.fail(errorMessage);
        }
    }
}
