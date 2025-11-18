package com.example.lizhi.controller;

import com.example.lizhi.entity.StockIn;
import com.example.lizhi.entity.User;
import com.example.lizhi.service.ExcelExportService;
import com.example.lizhi.service.ReturnOrderService;
import com.example.lizhi.service.StockInService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
public class StockInController {

    private final StockInService stockInService;
    private final ReturnOrderService returnOrderService;
    private final ExcelExportService excelExportService;

    public StockInController(StockInService stockInService,
                             ReturnOrderService returnOrderService,
                             ExcelExportService excelExportService) {
        this.stockInService = stockInService;
        this.returnOrderService = returnOrderService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/stock")
    public String getStockInPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("currentUser");
        Page<StockIn> stockRecordPage = stockInService.findAllStockIn(page, size, currentUser);

        model.addAttribute("stockRecords", stockRecordPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", stockRecordPage.getTotalPages());
        model.addAttribute("totalItems", stockRecordPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "stock";
    }

    @GetMapping("/stock/search")
    public String searchStock(
            @RequestParam String orderNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("currentUser");
        Page<StockIn> stockRecordPage = stockInService.searchByOrderNo(orderNo, page, size, currentUser);

        model.addAttribute("stockRecords", stockRecordPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", stockRecordPage.getTotalPages());
        model.addAttribute("totalItems", stockRecordPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "stock :: #stock-table-body";
    }

    //批量删除入库单
    @DeleteMapping("/stock/delete/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteStock(@RequestBody Map<String, List<Integer>> request) {
        List<Integer> ids = request.get("ids");
        try {
            stockInService.batchDelete(ids);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "批量删除成功"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }

    @PutMapping("/api/stock/{stockId}/status")
    public ResponseEntity<?> updateStockStatus(
            @PathVariable Integer stockId,
            @RequestBody Map<String, Object> request) {
        try {
            String newStatus = (String) request.get("newStatus");
            String rejectionReason = (String) request.get("rejectionReason");

            StockIn updatedStock = stockInService.updateStatus(stockId, newStatus, rejectionReason);

            return ResponseEntity.ok(updatedStock);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("修改失败：" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("服务器错误：" + e.getMessage());
        }
    }

    // 导出所有入库单
    @GetMapping("/stock/export")
    public ResponseEntity<byte[]> exportAllStockIn() throws IOException {
        List<StockIn> stockInList = stockInService.getAllStockInForExport();
        byte[] excelBytes = excelExportService.exportStockInToExcel(stockInList);

        String fileName = "入库单列表_" + System.currentTimeMillis() + ".xlsx";
        String encodedFileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .body(excelBytes);
    }

    // 根据搜索条件导出入库单
    @GetMapping("/stock/export/search")
    public ResponseEntity<byte[]> exportStockInBySearch(@RequestParam String orderNo) throws IOException {
        List<StockIn> stockInList = stockInService.getStockInForExportByOrderNo(orderNo);
        byte[] excelBytes = excelExportService.exportStockInToExcel(stockInList);

        String fileName = "入库单搜索列表_" + System.currentTimeMillis() + ".xlsx";
        String encodedFileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .body(excelBytes);
    }
}