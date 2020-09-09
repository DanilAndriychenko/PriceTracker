package org.econia;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
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
            List<RecordAvailability> recordAvailabilityArrayList =
                    DBProcessor.getRecordsAvailability(pickerFrom.getValue().toString(), pickerTo.getValue().toString());

            XSSFWorkbook workbook = new XSSFWorkbook();
            for (Brand brand : DBProcessor.getAllBrands()){
                XSSFSheet sheet = workbook.createSheet(brand.getName());
                Map<Integer, ArrayList<Object>> data = new TreeMap<>();
                List<RecordAvailability> recordAvailabilitiesBrand = new ArrayList<>();
                for (RecordAvailability recordAvailability : recordAvailabilityArrayList){
                    if (recordAvailability.getBrand_id()==brand.getBrand_id()){
                        recordAvailabilitiesBrand.add(recordAvailability);
                    }
                }
                if (recordAvailabilitiesBrand.isEmpty()) {
                    workbook.removeSheetAt(workbook.getSheetIndex(brand.getName()));
                    continue;
                }
                sheet.addMergedRegion(new CellRangeAddress(1,48,0,0));
                data.put(0, new ArrayList<>(Arrays.asList("TM", "Товар")));

            }
            try (FileOutputStream out = new FileOutputStream(
                    new File(pickerFrom.getValue().toString() + pickerTo.getValue().toString() + ".xlsx"))){
                workbook.write(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ((Stage) trackButton.getScene().getWindow()).close();
        });
    }

    private boolean pickersDateSelected(){
        return pickerFrom.getValue() != null && pickerTo.getValue() != null;
    }
}
