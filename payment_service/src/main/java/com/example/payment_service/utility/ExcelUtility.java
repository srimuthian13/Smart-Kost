package com.example.payment_service.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Component;

import com.example.payment_service.entity.PaymentEntity;

@Component

public class ExcelUtility {

    public ByteArrayInputStream exportPayments(List<PaymentEntity> payments) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Payments");
        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Tenant ID");
        header.createCell(2).setCellValue("Room ID");
        header.createCell(3).setCellValue("Amount");
        header.createCell(4).setCellValue("Status");
        header.createCell(5).setCellValue("Payment Date");
        header.createCell(6).setCellValue("Payment Method");
        header.createCell(7).setCellValue("Billing Month");
        header.createCell(8).setCellValue("Billing Year");
        int rowIdx = 1;

        for (PaymentEntity p : payments) {

            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(p.getId());

            row.createCell(1).setCellValue(p.getTenantId());

            row.createCell(2).setCellValue(p.getRoomId());

            row.createCell(3).setCellValue(p.getAmount().doubleValue());

            row.createCell(4).setCellValue(p.getStatus().name());

            row.createCell(5).setCellValue(p.getPaymentDate() != null ? p.getPaymentDate().toString() : "-");

            row.createCell(6).setCellValue(p.getPaymentMethod() != null ? p.getPaymentMethod(): "-");

            row.createCell(7).setCellValue(  p.getBillingMonth() != null ? p.getBillingMonth() : 0);

            row.createCell(8).setCellValue(p.getBillingYear() != null ? p.getBillingYear() : 0);
        }
        for (int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        workbook.write(out);

        workbook.close();
        return new ByteArrayInputStream(
                out.toByteArray());
    }
}
