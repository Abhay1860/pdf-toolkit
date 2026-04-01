package com.project.pdf_toolkit.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
public class PdfService {

    public int getPageCount(byte[] fileBytes) throws IOException {

        PDDocument document = PDDocument.load(fileBytes);
        int pages = document.getNumberOfPages();

        document.close();

        return pages;
    }

    public byte[] mergePDFs(MultipartFile[] files) throws IOException {

        PDFMergerUtility merger = new PDFMergerUtility();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (MultipartFile file : files) {
            merger.addSource(new ByteArrayInputStream(file.getBytes()));
        }

        merger.setDestinationStream(outputStream);

        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        return outputStream.toByteArray();
    }

    private List<int[]> mergeOverlappingRanges(List<int[]> ranges) {

        if (ranges.isEmpty()) return ranges;

        // Sort by start
        ranges.sort(Comparator.comparingInt(a -> a[0]));

        List<int[]> merged = new ArrayList<>();

        int[] current = ranges.get(0);

        for (int i = 1; i < ranges.size(); i++) {
            int[] next = ranges.get(i);

            if (next[0] <= current[1] + 1) {
                // Overlapping or adjacent → merge
                current[1] = Math.max(current[1], next[1]);
            } else {
                merged.add(current);
                current = next;
            }
        }

        merged.add(current);

        return merged;
    }

    private List<int[]> parseAndValidateRanges(String ranges, int totalPages) {

        if (ranges == null || ranges.trim().isEmpty()) {
            throw new IllegalArgumentException("Ranges cannot be empty");
        }

        List<int[]> result = new ArrayList<>();

        String[] rangeArray = ranges.split(",");

        for (String range : rangeArray) {

            if (!range.contains("-")) {
                throw new IllegalArgumentException("Invalid range format: " + range);
            }

            String[] parts = range.split("-");

            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid range format: " + range);
            }

            int start, end;

            try {
                start = Integer.parseInt(parts[0].trim());
                end = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Range contains non-numeric values: " + range);
            }

            if (start < 1 || end < 1) {
                throw new IllegalArgumentException("Page numbers must be >= 1");
            }

            if (start > end) {
                throw new IllegalArgumentException("Start page cannot be greater than end page: " + range);
            }

            if (end > totalPages) {
                throw new IllegalArgumentException("Range exceeds total pages: " + range);
            }

            // convert to 0-based
            result.add(new int[]{start - 1, end - 1});
        }

        return result;
    }


    public byte[] splitPdfByRanges(MultipartFile file, String ranges) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try (PDDocument document = PDDocument.load(file.getInputStream());
             ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(zipBaos)) {

            int totalPages = document.getNumberOfPages();

            List<int[]> validatedRanges = parseAndValidateRanges(ranges, totalPages);

            validatedRanges = mergeOverlappingRanges(validatedRanges);

            int fileIndex = 1;

            for (int[] r : validatedRanges) {
                int start = r[0];
                int end = r[1];

                PDDocument newDoc = new PDDocument();

                for (int i = start; i <= end; i++) {
                    newDoc.addPage(document.getPage(i));
                }

                ByteArrayOutputStream pdfBaos = new ByteArrayOutputStream();
                newDoc.save(pdfBaos);
                newDoc.close();

                ZipEntry entry = new ZipEntry("split_" + fileIndex + ".pdf");
                zipOut.putNextEntry(entry);
                zipOut.write(pdfBaos.toByteArray());
                zipOut.closeEntry();

                fileIndex++;
            }

            zipOut.finish();
            return zipBaos.toByteArray();
        }
    }

    public byte[] reorderPdf(MultipartFile file, String order) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (order == null || order.trim().isEmpty()) {
            throw new IllegalArgumentException("Order cannot be empty");
        }

        try (PDDocument document = PDDocument.load(file.getInputStream());
             PDDocument newDoc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            int totalPages = document.getNumberOfPages();

            String[] parts = order.split(",");

            for (String part : parts) {
                int pageNum;

                try {
                    pageNum = Integer.parseInt(part.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid page number: " + part);
                }

                if (pageNum < 1 || pageNum > totalPages) {
                    throw new IllegalArgumentException("Page number out of range: " + pageNum);
                }

                newDoc.addPage(document.getPage(pageNum - 1));
            }

            newDoc.save(baos);
            return baos.toByteArray();
        }
    }

    public byte[] rotatePdf(MultipartFile file, int angle) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (angle != 90 && angle != 180 && angle != 270) {
            throw new IllegalArgumentException("Angle must be 90, 180, or 270");
        }

        try (PDDocument document = PDDocument.load(file.getInputStream());
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            for (PDPage page : document.getPages()) {
                int currentRotation = page.getRotation();
                page.setRotation((currentRotation + angle) % 360);
            }

            document.save(baos);
            return baos.toByteArray();
        }
    }

    public byte[] lockPdf(MultipartFile file, String password) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        try (PDDocument document = PDDocument.load(file.getInputStream());
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            AccessPermission ap = new AccessPermission();
            StandardProtectionPolicy policy =
                    new StandardProtectionPolicy(password, password, ap);

            policy.setEncryptionKeyLength(128);
            policy.setPermissions(ap);

            document.protect(policy);
            document.save(baos);

            return baos.toByteArray();
        }
    }
}