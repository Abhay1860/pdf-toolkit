package com.project.pdf_toolkit.Controller;

import com.project.pdf_toolkit.Service.PdfService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TestController {

    private final PdfService pdfService;

    public TestController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

//    @PostMapping("/upload")
//    public String uploadFile(@RequestParam("file") MultipartFile file){
//
//        try {
//            String projectPath = System.getProperty("user.dir");
//            String uploadDir = projectPath + File.separator + "uploads";
//
//            File dir = new File(uploadDir);
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//
//            String fileName = file.getOriginalFilename();
//            File destination = new File(dir, fileName);
//            file.transferTo(destination);
//
//            return "File saved at: " + destination.getAbsolutePath();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "Error uploading file";
//        }
//    }

    @PostMapping("/page-count")
    public String pageCount(@RequestParam("file")  MultipartFile file){
        try {

            int pages = pdfService.getPageCount(file.getBytes());

            return "Total pages: "+pages;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading PDF";

        }
    }

    @GetMapping("/pdf-test")
    public String pdfTest() {
        return "PDFBox installed successfully";
    }

    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePDFs(
            @RequestParam("files") MultipartFile[] files) {

        try {

            byte[] merged = pdfService.mergePDFs(files);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merged.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(merged);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/split")
    public String splitPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("ranges") String ranges,
            Model model,
            HttpServletResponse response) {

        try {
            byte[] zipData = pdfService.splitPdfByRanges(file, ranges);

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=split.zip");

            response.getOutputStream().write(zipData);
            response.getOutputStream().flush();

            return null; // important

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "split";
        }
    }

    @PostMapping("/reorder")
    public ResponseEntity<byte[]> reorderPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("order") String order) throws IOException {

        byte[] data = pdfService.reorderPdf(file, order);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reordered.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @PostMapping("/rotate")
    public ResponseEntity<byte[]> rotatePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("angle") int angle) throws IOException {

        byte[] data = pdfService.rotatePdf(file, angle);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rotated.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @PostMapping("/lock")
    public ResponseEntity<byte[]> lockPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) throws IOException {

        byte[] data = pdfService.lockPdf(file, password);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=locked.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
//        return ResponseEntity.badRequest().body(ex.getMessage());
//    }
}
