package com.example.lizhi.service;

import com.example.lizhi.entity.StockIn;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] exportStockInToExcel(List<StockIn> stockInList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("入库单列表");

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"订单编号", "入库品种", "荔枝规格", "数量(斤)", "经办人",
                    "入库单创建时间", "入库时间", "物流状态", "保鲜状态"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建数据行样式
            CellStyle dataStyle = createDataStyle(workbook);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // 填充数据
            int rowNum = 1;
            for (StockIn stock : stockInList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(stock.getOrderNo() != null ? stock.getOrderNo() : "");
                row.createCell(1).setCellValue(stock.getLitchi_variety() != null ? stock.getLitchi_variety() : "");
                row.createCell(2).setCellValue(
                        stock.getOrder() != null && stock.getOrder().getSpecification() != null ?
                                stock.getOrder().getSpecification() : ""
                );
                row.createCell(3).setCellValue(
                        stock.getQuantity() != null ? stock.getQuantity().doubleValue() : 0.0
                );
                row.createCell(4).setCellValue(stock.getOperator_name() != null ? stock.getOperator_name() : "");

                // 格式化时间
                if (stock.getCreateTime() != null) {
                    row.createCell(5).setCellValue(stock.getCreateTime().format(formatter));
                } else {
                    row.createCell(5).setCellValue("");
                }

                if (stock.getUpdate_time() != null) {
                    row.createCell(6).setCellValue(stock.getUpdate_time().format(formatter));
                } else {
                    row.createCell(6).setCellValue("");
                }

                row.createCell(7).setCellValue(
                        stock.getStock_in_status() != null ? stock.getStock_in_status().getLabel() : ""
                );
                row.createCell(8).setCellValue(
                        stock.getFreshness_status() != null ? stock.getFreshness_status().getLabel() : ""
                );

                // 设置数据行样式
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}