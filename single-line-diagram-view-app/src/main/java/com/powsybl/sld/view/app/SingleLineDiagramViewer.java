/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.view.app;

import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SingleLineDiagramViewer extends AbstractSingleLineDiagramViewer {

    private static final String CASE_PATH_PROPERTY = "casePath";

    private final Button caseLoadingStatus = new Button("  ");
    private final TextField casePathTextField = new TextField();

    @Override
    protected Node createCasePane(Stage primaryStage) {
        caseLoadingStatus.setStyle("-fx-background-color: red");
        casePathTextField.setEditable(false);

        HBox.setHgrow(casePathTextField, Priority.ALWAYS);

        Button caseButton = new Button("...");
        caseButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open case File");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                loadNetwork(file.toPath());
            }
        });
        HBox casePane = new HBox(3, caseLoadingStatus, casePathTextField, caseButton);
        BorderPane.setMargin(casePane, new Insets(3, 3, 3, 3));
        return casePane;
    }

    @Override
    protected void loadNetworkFromPreferences() {
        String casePathPropertyValue = preferences.get(CASE_PATH_PROPERTY, null);
        if (casePathPropertyValue != null) {
            loadNetwork(Paths.get(casePathPropertyValue));
        }
    }

    private void loadNetwork(Path file) {
        Service<Network> networkService = new Service<Network>() {
            @Override
            protected Task<Network> createTask() {
                return new Task<Network>() {
                    @Override
                    protected Network call() {
                        Properties properties = new Properties();
                        properties.put("iidm.import.cgmes.post-processors", "cgmesDLImport");
                        return Importers.loadNetwork(file, LocalComputationManager.getDefault(), new ImportConfig(), properties);
                    }
                };
            }
        };
        networkService.setOnRunning(event -> {
            caseLoadingStatus.setStyle("-fx-background-color: yellow");
            casePathTextField.setText(file.toAbsolutePath().toString());
        });
        networkService.setOnSucceeded(event -> {
            setNetwork((Network) event.getSource().getValue());
            caseLoadingStatus.setStyle("-fx-background-color: green");
            preferences.put(CASE_PATH_PROPERTY, file.toAbsolutePath().toString());
        });
        networkService.setOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            LOGGER.error(exception.toString(), exception);
            casePathTextField.setText("");
            caseLoadingStatus.setStyle("-fx-background-color: red");
        });
        networkService.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
