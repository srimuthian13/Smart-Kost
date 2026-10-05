package com.example.payment_service.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.example.payment_service.entity.PaymentEntity;
import com.example.payment_service.payload.res.TenantRes;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;

@Component
public class PdfGeneratorUtil {

    public File generateInvoicePdf(PaymentEntity payment, TenantRes tenant, String roomType) throws Exception {
        Path folder = Paths.get("uploads/invoices");
        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }

        String fileName = "Invoice_" + payment.getOrderId() + ".pdf";
        File file = new File(folder.toFile(), fileName);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        Paragraph title = new Paragraph("INVOICE - SMART KOST", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        document.add(new Paragraph("Order ID: " + payment.getOrderId(), normalFont));
        document.add(new Paragraph("Tanggal Pembayaran: " + (payment.getPaymentDate() != null ? payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "-"), normalFont));
        document.add(new Paragraph("Status: " + payment.getStatus(), boldFont));
        
        document.add(new Paragraph(" ")); // empty line

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        
        addCellToTable(table, "Nama Tenant", true, boldFont);
        addCellToTable(table, tenant.getName(), false, normalFont);
        
        addCellToTable(table, "Kamar", true, boldFont);
        addCellToTable(table, roomType != null ? roomType : "Room ID: " + payment.getRoomId(), false, normalFont);

        NumberFormat idFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        
        addCellToTable(table, "Total Tagihan", true, boldFont);
        addCellToTable(table, idFormat.format(payment.getAmount()), false, normalFont);
        
        addCellToTable(table, "Metode Pembayaran", true, boldFont);
        addCellToTable(table, payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "-", false, normalFont);

        document.add(table);

        Paragraph footer = new Paragraph("Terima kasih telah menggunakan layanan Smart Kost.", normalFont);
        footer.setAlignment(Paragraph.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);

        document.close();

        return file;
    }

    private void addCellToTable(PdfPTable table, String text, boolean isHeader, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setPadding(8);
        if (isHeader) {
            cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
        }
        table.addCell(cell);
    }
}
