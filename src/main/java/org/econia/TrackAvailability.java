package org.econia;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class TrackAvailability {

    @FXML
    private JFXDatePicker pickerFrom;
    @FXML
    private  JFXDatePicker pickerTo;
    @FXML
    private JFXButton trackButton;

    @FXML
    public void initialize(){
        trackButton.setDisable(true);
        configureDatePickers();
    }

    private void configureDatePickers(){
        Controller.configureDatePickersValueBorders(pickerFrom, pickerTo);
        pickerFrom.setOnAction(fromEvent ->{
            if (pickersDateSelected()){
                trackButton.setDisable(false);
            }
        });
        pickerTo.setOnAction(toEvent ->{
            if (pickersDateSelected()){
                trackButton.setDisable(false);
            }
        });
        trackButton.setOnAction(trackEvent ->{
            long start = System.currentTimeMillis();
            List<RecordAvailability> recordAvailabilityArrayList =
                    DBProcessor.getRecordsAvailability(pickerFrom.getValue().toString(), pickerTo.getValue().toString());
            XSSFWorkbook workbook = new XSSFWorkbook();
            for (Shop shop : DBProcessor.getAllShops()){


                XSSFSheet sheet = workbook.createSheet(shop.getName());
                Map<Integer, ArrayList<Object>> data = new TreeMap<>();
                List<RecordAvailability> recordAvailabilitiesShop = new ArrayList<>();
                for (RecordAvailability recordAvailability : recordAvailabilityArrayList){
                    if (recordAvailability.getShopId()== shop.getShop_id()){
                        recordAvailabilitiesShop.add(recordAvailability);
                    }
                }
                if (recordAvailabilitiesShop.isEmpty()) {
                    workbook.removeSheetAt(workbook.getSheetIndex(shop.getName()));
                    continue;
                }

                data.put(0, new ArrayList<>(Arrays.asList("TM", "Товар")));

                sheet.addMergedRegion(new CellRangeAddress(1,48,0,0));
                sheet.addMergedRegion(new CellRangeAddress(49,51,0,0));
                sheet.addMergedRegion(new CellRangeAddress(52,55,0,0));
                sheet.addMergedRegion(new CellRangeAddress(56,60,0,0));


                List<Category> subcategories = DBProcessor.getAllSubcategories();
                List<AvailabilityConnection> availabilityConnections = DBProcessor.getAvailabilityConnections();
                for (Category subcategory : subcategories){
                    int brandId = 0;
                    for (AvailabilityConnection availabilityConnection : availabilityConnections){
                        if (availabilityConnection.getSubcatId() == subcategory.getCat_id()){
                            brandId = availabilityConnection.getBrandId();
                        }
                    }
                    String brandName = "";
                    for (Brand brand : App.getController().getBrandArrayList()){
                        if (brand.getBrand_id() == brandId){
                            brandName = brand.getName();
                            break;
                        }
                    }
                    data.put(subcategory.getCat_id(), new ArrayList<>(Arrays.asList(brandName, subcategory.getName())));
                }

                Set<Integer> keySet = data.keySet();
                int rowNum = 0;
                for (Integer key : keySet) {
                    Row row = sheet.createRow(rowNum++);
                    List<Object> objects = data.get(key);
                    int cellNum = 0;
                    for (Object obj : objects) {
                        Cell cell = row.createCell(cellNum++);
                        CellUtil.setAlignment(cell, workbook, CellStyle.ALIGN_CENTER);
                        CellUtil.setCellStyleProperty(cell, workbook, CellUtil.VERTICAL_ALIGNMENT, CellStyle.VERTICAL_CENTER);
                        if (obj instanceof String)
                            cell.setCellValue((String) obj);
                        else if (obj instanceof Integer)
                            cell.setCellValue((Integer) obj);
                    }
                }

                List<String> dates = DBProcessor.getAllDistinctDatesInRange(pickerFrom.getValue().toString(), pickerTo.getValue().toString());
                for (int i=0; i<dates.size(); i++){
                    int j=0;
                    Iterator<Row> iterator = sheet.rowIterator();
                    while(iterator.hasNext()){
                        Row currentRow = iterator.next();
                        Cell cell = currentRow.createCell(i+2, Cell.CELL_TYPE_STRING);
                        if(currentRow.getRowNum() == 0)
                            cell.setCellValue(dates.get(i));
                        else{
                            CellStyle cellStyle = workbook.createCellStyle();
                            short color = IndexedColors.RED.getIndex();
                            for (RecordAvailability recordAvailability : recordAvailabilityArrayList){
                                if (recordAvailability.getSubcatId()==j && recordAvailability.getShopId() == shop.getShop_id()
                                        && recordAvailability.getDate().toString().equals(dates.get(i))){
                                    if (recordAvailability.getAvailability().equals("Available")){
                                        color = IndexedColors.SEA_GREEN.getIndex();
                                    }else if (recordAvailability.getAvailability().equals("NotAvailable")){
                                        color = IndexedColors.LIGHT_YELLOW.getIndex();
                                    }
                                }
                            }
                            cellStyle.setFillForegroundColor(color);
                            cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                            cell.setCellStyle(cellStyle);
                        }
                        j++;
                    }
                }
            }
            autoSizeColumns(workbook);
            try (FileOutputStream out = new FileOutputStream(
                    new File(pickerFrom.getValue().toString().replace("-", ".")
                            + " - " + pickerTo.getValue().toString().replace("-", ".") + ".xlsx"))){
                workbook.write(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ((Stage) trackButton.getScene().getWindow()).close();
        });
    }

    private void autoSizeColumns(Workbook workbook) {
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() > 0) {
                Row row = sheet.getRow(sheet.getFirstRowNum());
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    if (columnIndex==0) {
                        sheet.setColumnWidth(columnIndex, 3000);
                    }
                    else {
                        sheet.autoSizeColumn(columnIndex);
                    }
                }
            }
        }
    }

    private boolean pickersDateSelected(){
        return pickerFrom.getValue() != null && pickerTo.getValue() != null;
    }
}
