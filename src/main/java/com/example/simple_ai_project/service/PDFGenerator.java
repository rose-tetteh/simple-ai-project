package com.example.simple_ai_project.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfException;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class PDFGenerator {

    public byte[] generatePdf(String summary, String feedback, Double score) throws PdfException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("SUMMARY:"));
            document.add(new Paragraph(summary != null ? summary : "No summary available"));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("FEEDBACK:"));
            document.add(new Paragraph(feedback != null ? feedback : "No feedback available"));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Score: " + (score != null ? score + "%" : "No score available")));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new PdfException(e);
        }
    }
}
