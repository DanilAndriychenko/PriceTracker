package org.econia;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class ChangeDateFormatController {

    @FXML
    public JFXRadioButton radButMonthLetters;
    @FXML
    public JFXRadioButton radButMonthNums;
    @FXML
    public JFXRadioButton radButYearNums;
    @FXML
    public JFXRadioButton radButYearNone;
    @FXML
    public JFXButton butSave;

    @FXML
    public void initialize(){
        String currPattern = App.getController().getDateFormatPattern();
        switch (currPattern) {
            case "dd MMM":
                radButMonthLetters.setSelected(true);
                radButYearNone.setSelected(true);
                break;
            case "dd.MM.yy":
                radButMonthNums.setSelected(true);
                radButYearNums.setSelected(true);
                break;
            case "dd.MM":
                radButMonthNums.setSelected(true);
                radButYearNone.setSelected(true);
                break;
            case "dd MMM yy":
            default:
                radButMonthLetters.setSelected(true);
                radButYearNums.setSelected(true);
                break;
        }

        ToggleGroup togGroupMonth = new ToggleGroup();
        ToggleGroup togGroupYear = new ToggleGroup();
        radButMonthLetters.setToggleGroup(togGroupMonth);
        radButMonthNums.setToggleGroup(togGroupMonth);
        radButYearNums.setToggleGroup(togGroupYear);
        radButYearNone.setToggleGroup(togGroupYear);

        butSave.setOnAction(saveEvent -> {
            StringBuilder dateFormatPatternBuilder = new StringBuilder("dd");
            if (radButMonthLetters.isSelected() && radButYearNums.isSelected()){
                dateFormatPatternBuilder.append(" MMM yy");
            }else if (radButMonthLetters.isSelected() && radButYearNone.isSelected()){
                dateFormatPatternBuilder.append(" MMM");
            }else if (radButMonthNums.isSelected() && radButYearNums.isSelected()){
                dateFormatPatternBuilder.append(".MM.yy");
            }else if (radButMonthNums.isSelected() && radButYearNone.isSelected()){
                dateFormatPatternBuilder.append(".MM");
            }
            App.getController().setDateFormatPattern(dateFormatPatternBuilder.toString());

            Stage stage = (Stage) butSave.getScene().getWindow();
            stage.close();
        });
    }
}
