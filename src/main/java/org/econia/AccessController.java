package org.econia;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AccessController {

    @FXML
    private ToggleButton unlockToggleButton;
    @FXML
    private ToggleButton addSKUToggleButton;
    @FXML
    private ToggleButton forceUpdateToggleButton;
    @FXML
    private ToggleButton autoUpdateToggleButton;
    @FXML
    private JFXSlider sliderFrequency;
    @FXML
    private JFXButton saveSettingsButton;

    public ToggleButton getUnlockToggleButton() {
        return unlockToggleButton;
    }

    public ToggleButton getAddSKUToggleButton() {
        return addSKUToggleButton;
    }

    public ToggleButton getForceUpdateToggleButton() {
        return forceUpdateToggleButton;
    }

    public ToggleButton getAutoUpdateToggleButton() {
        return autoUpdateToggleButton;
    }

    public JFXSlider getSliderFrequency() {
        return sliderFrequency;
    }

    private static final String APP_ICON = "appIcon1.jpg";
    private static final int FREQUENCY_DEFAULT = 3;

    @FXML
    public void initialize() {
        DBProcessor.AccessLevel accessLevel = App.getController().getAccessLevel();

        if (accessLevel.isUnlockToggleButtonEnabled()){
            unlockToggleButton.setDisable(false);
            unlockToggleButton.setSelected(true);
            if (accessLevel.isAddSKUToggleButtonEnabled()){
                addSKUToggleButton.setDisable(false);
                addSKUToggleButton.setSelected(true);
            }else{
                addSKUToggleButton.setSelected(false);
            }
            if (accessLevel.isForceUpdateToggleButtonEnabled()){
                forceUpdateToggleButton.setDisable(false);
                forceUpdateToggleButton.setSelected(true);
            }else{
                forceUpdateToggleButton.setSelected(false);
            }
            if (accessLevel.isAutoUpdateToggleButtonEnabled()){
                autoUpdateToggleButton.setDisable(false);
                autoUpdateToggleButton.setSelected(true);
                sliderFrequency.setValue(accessLevel.getPeriod());
            }else{
                autoUpdateToggleButton.setSelected(false);
                sliderFrequency.setValue(FREQUENCY_DEFAULT);
                sliderFrequency.setDisable(true);
            }
        }else {
            addSKUToggleButton.setDisable(true);
            forceUpdateToggleButton.setDisable(true);
            autoUpdateToggleButton.setDisable(true);
            sliderFrequency.setDisable(true);
        }

        unlockToggleButton.setOnAction(unlockEvent -> {
            if (unlockToggleButton.isSelected()) {
                unlockToggleButton.setSelected(false);
                try {
                    LoginController loginController = new LoginController(this);
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
                    fxmlLoader.setController(loginController);
                    Parent root = fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.setTitle("Вхід до Бази Даних");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.getIcons().add(new Image(getClass().getResourceAsStream(APP_ICON)));
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                addSKUToggleButton.setDisable(true);
                addSKUToggleButton.setSelected(false);
                forceUpdateToggleButton.setDisable(true);
                forceUpdateToggleButton.setSelected(false);
                autoUpdateToggleButton.setDisable(true);
                autoUpdateToggleButton.setSelected(false);
                sliderFrequency.setDisable(true);
                sliderFrequency.setValue(FREQUENCY_DEFAULT);
            }
        });

        autoUpdateToggleButton.setOnAction(autoUpdEvent ->{
            if (autoUpdateToggleButton.isSelected()){
                sliderFrequency.setDisable(false);
            }else{
                sliderFrequency.setDisable(true);
                sliderFrequency.setValue(FREQUENCY_DEFAULT);
            }
        });
    }
}
