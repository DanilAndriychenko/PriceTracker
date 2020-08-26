package org.econia;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;

public class LoginController {

    @FXML
    public JFXTextField loginField;
    @FXML
    public JFXPasswordField passwordField;
    @FXML
    public Label labelWrongInput;
    @FXML
    public JFXButton signIn;

    private AccessController accessController;

    public LoginController(AccessController accessController){
        this.accessController = accessController;
    }

    @FXML
    public void initialize(){
        signIn.setOnAction(signInEvent ->{
            if (loginField.getText().equals("1") && passwordField.getText().equals("1")){
                accessController.getUnlockToggleButton().setSelected(true);
                accessController.getAddSKUToggleButton().setDisable(false);
                accessController.getForceUpdateToggleButton().setDisable(false);
                accessController.getAutoUpdateToggleButton().setDisable(false);
                signIn.getScene().getWindow().hide();
            }else {
                new Thread(() -> {
                    Platform.runLater(() -> {
                        if (!loginField.getText().equals(DBProcessor.getUSERNAME())){
                            loginField.setUnFocusColor(Paint.valueOf("red"));
                        }
                        if (!passwordField.getText().equals(DBProcessor.getPASSWORD())){
                            passwordField.setUnFocusColor(Paint.valueOf("red"));
                        }
                        labelWrongInput.setVisible(true);
                    });
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    Platform.runLater(() -> {
                        loginField.setUnFocusColor(Paint.valueOf("blue"));
                        passwordField.setUnFocusColor(Paint.valueOf("blue"));
                        labelWrongInput.setVisible(false);
                    });
                }).start();
            }
        });
    }
}
