/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.view.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.GraphBuilder;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.SubstationDiagram;
import com.powsybl.sld.VoltageLevelDiagram;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.sld.cgmes.layout.CgmesSubstationLayoutFactory;
import com.powsybl.sld.cgmes.layout.CgmesVoltageLevelLayoutFactory;
import com.powsybl.sld.force.layout.ForceSubstationLayoutFactory;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.layout.positionfromextension.PositionFromExtension;
import com.powsybl.sld.layout.positionbyclustering.PositionByClustering;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.util.NominalVoltageDiagramStyleProvider;
import com.powsybl.sld.util.TopologicalStyleProvider;
import com.powsybl.sld.view.AbstractContainerDiagramView;
import com.powsybl.sld.view.DisplayVoltageLevel;
import com.powsybl.sld.view.SubstationDiagramView;
import com.powsybl.sld.view.VoltageLevelDiagramView;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
public abstract class AbstractSingleLineDiagramViewer extends Application implements DisplayVoltageLevel {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractSingleLineDiagramViewer.class);

    private static final String SELECTED_VOLTAGE_LEVEL_IDS_PROPERTY = "selectedVoltageLevelIds";
    private static final String SELECTED_SUBSTATION_IDS_PROPERTY = "selectedSubstationIds";

    private Map<String, VoltageLevelLayoutFactory> voltageLevelsLayouts = new LinkedHashMap<>();

    private Map<String, DiagramStyleProvider> styles = new LinkedHashMap<>();

    private Map<String, SubstationLayoutFactory> substationsLayouts = new LinkedHashMap<>();

    private final ComponentLibrary convergenceComponentLibrary = new ResourcesComponentLibrary("/ConvergenceLibrary");

    private final Map<String, ComponentLibrary> svgLibraries
            = ImmutableMap.of("CVG Design", convergenceComponentLibrary);

    private final ObservableList<SelectableSubstation> selectableSubstations = FXCollections.observableArrayList();

    private final ObservableList<SelectableVoltageLevel> selectableVoltageLevels = FXCollections.observableArrayList();

    private final TextField filterInput = new TextField();

    private final TreeView<Container> substationsTree = new TreeView<>();

    private final TabPane diagramsPane = new TabPane();
    private Tab tabSelected;
    private Tab tabChecked;
    private final BorderPane selectedDiagramPane = new BorderPane();
    private final TabPane checkedDiagramsPane = new TabPane();
    private GridPane parametersPane;

    private final ObjectProperty<Network> networkProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<LayoutParameters> layoutParameters = new SimpleObjectProperty<>(new LayoutParameters()
            .setShowGrid(true)
            .setAdaptCellHeightToContent(true));

    protected final Preferences preferences = Preferences.userNodeForPackage(VoltageLevelDiagramView.class);

    private final ObjectMapper objectMapper = JsonUtil.createObjectMapper();

    protected final ComboBox<String> voltageLevelLayoutComboBox = new ComboBox<>();

    private final ComboBox<String> substationLayoutComboBox = new ComboBox<>();

    private final ComboBox<String> styleComboBox = new ComboBox<>();

    private final ComboBox<String> svgLibraryComboBox = new ComboBox<>();

    private final CheckBox showNames = new CheckBox("Show names");

    private final CheckBox hideSubstations = new CheckBox("Hide substations");

    private final ComboBox<String> diagramNamesComboBox = new ComboBox<>();

    private class ContainerDiagramPane extends BorderPane {
        private final ScrollPane flowPane = new ScrollPane();

        private final TextArea infoArea = new TextArea();

        private final VBox svgArea = new VBox();
        private final TextField svgSearchField = new TextField();
        private final Button svgSearchButton = new Button("Search");
        private final TextArea svgTextArea = new TextArea();
        private AtomicReference<Integer> svgSearchStart = new AtomicReference<>(0);
        private final Button svgSaveButton = new Button("Save");

        private final VBox metadataArea = new VBox();
        private final TextField metadataSearchField = new TextField();
        private final Button metadataSearchButton = new Button("Search");
        private final TextArea metadataTextArea = new TextArea();
        private final AtomicReference<Integer> metadataSearchStart = new AtomicReference<>(0);
        private final Button metadataSaveButton = new Button("Save");

        private final VBox jsonArea = new VBox();
        private final TextField jsonSearchField = new TextField();
        private final Button jsonSearchButton = new Button("Search");
        private final TextArea jsonTextArea = new TextArea();
        private final AtomicReference<Integer> jsonSearchStart = new AtomicReference<>(0);
        private final Button jsonSaveButton = new Button("Save");

        private final Tab tab1 = new Tab("Diagram", flowPane);

        private final Tab tab2 = new Tab("SVG", svgArea);

        private final Tab tab3 = new Tab("Metadata", metadataArea);

        private final Tab tab4 = new Tab("Graph", jsonArea);

        private final TabPane tabPane = new TabPane(tab1, tab2, tab3, tab4);

        private final TitledPane titledPane = new TitledPane("Infos", infoArea);

        private final ChangeListener<LayoutParameters> listener;

        ContainerDiagramPane(Container c) {
            createArea(svgSearchField, svgSearchButton, svgSaveButton, "SVG file", "*.svg", svgTextArea, svgArea, svgSearchStart);
            createArea(metadataSearchField, metadataSearchButton, metadataSaveButton, "JSON file", "*.json", metadataTextArea, metadataArea, metadataSearchStart);
            createArea(jsonSearchField, jsonSearchButton, jsonSaveButton, "JSON file", "*.json", jsonTextArea, jsonArea, jsonSearchStart);

            infoArea.setEditable(false);
            infoArea.setText(String.join(System.lineSeparator(),
                    "id: " + c.getId(),
                    "name: " + c.getName()));
            tabPane.setSide(Side.BOTTOM);
            tab1.setClosable(false);
            tab2.setClosable(false);
            tab3.setClosable(false);
            tab4.setClosable(false);
            setCenter(tabPane);
            setBottom(titledPane);
            listener = (observable, oldValue, newValue) -> loadDiagram(c);
            layoutParameters.addListener(new WeakChangeListener<>(listener));
            loadDiagram(c);
        }

        class ContainerDiagramResult {

            private final AbstractContainerDiagramView view;

            private final String svgData;

            private final String metadataData;

            private final String jsonData;

            ContainerDiagramResult(AbstractContainerDiagramView view, String svgData, String metadataData, String jsonData) {
                this.view = view;
                this.svgData = svgData;
                this.metadataData = metadataData;
                this.jsonData = jsonData;
            }

            AbstractContainerDiagramView getView() {
                return view;
            }

            String getSvgData() {
                return svgData;
            }

            String getMetadataData() {
                return metadataData;
            }

            String getJsonData() {
                return jsonData;
            }
        }

        private ScrollPane getFlowPane() {
            return flowPane;
        }

        private String getSelectedDiagramName() {
            return diagramNamesComboBox.getSelectionModel().getSelectedItem();
        }

        private ContainerDiagramResult createContainerDiagramView(Container c) {
            String svgData;
            String metadataData;
            String jsonData;
            try (StringWriter svgWriter = new StringWriter();
                 StringWriter metadataWriter = new StringWriter();
                 StringWriter jsonWriter = new StringWriter()) {
                DiagramStyleProvider styleProvider = styles.get(styleComboBox.getSelectionModel().getSelectedItem());

                String dName = getSelectedDiagramName();
                LayoutParameters diagramLayoutParameters = new LayoutParameters(layoutParameters.get()).setDiagramName(dName);
                diagramLayoutParameters.setComponentsSize(getComponentLibrary().getComponentsSize());

                DiagramLabelProvider initProvider = new DefaultDiagramLabelProvider(networkProperty.get(), getComponentLibrary(), diagramLayoutParameters);
                GraphBuilder graphBuilder = new NetworkGraphBuilder(networkProperty.get());

                if (c.getContainerType() == ContainerType.VOLTAGE_LEVEL) {
                    VoltageLevelDiagram diagram = VoltageLevelDiagram.build(graphBuilder, c.getId(), getVoltageLevelLayoutFactory(), showNames.isSelected());
                    diagram.writeSvg("",
                            new DefaultSVGWriter(getComponentLibrary(), diagramLayoutParameters),
                            initProvider,
                            styleProvider,
                            svgWriter,
                            metadataWriter);
                    diagram.getGraph().writeJson(jsonWriter);
                } else if (c.getContainerType() == ContainerType.SUBSTATION) {
                    SubstationDiagram diagram = SubstationDiagram.build(graphBuilder, c.getId(), getSubstationLayoutFactory(), getVoltageLevelLayoutFactory(), showNames.isSelected());
                    diagram.writeSvg("",
                            new DefaultSVGWriter(getComponentLibrary(), diagramLayoutParameters),
                            initProvider,
                            styleProvider,
                            svgWriter,
                            metadataWriter);
                    diagram.getSubGraph().writeJson(jsonWriter);
                }

                svgWriter.flush();
                metadataWriter.flush();
                svgData = svgWriter.toString();
                metadataData = metadataWriter.toString();
                jsonData = jsonWriter.toString();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            AbstractContainerDiagramView diagramView = null;
            try (InputStream svgInputStream = new ByteArrayInputStream(svgData.getBytes(StandardCharsets.UTF_8));
                 InputStream metadataInputStream = new ByteArrayInputStream(metadataData.getBytes(StandardCharsets.UTF_8))) {
                if (c.getContainerType() == ContainerType.VOLTAGE_LEVEL) {
                    diagramView = VoltageLevelDiagramView.load(svgInputStream, metadataInputStream, switchId -> handleSwitchPositionchange(c, switchId), AbstractSingleLineDiagramViewer.this);
                } else if (c.getContainerType() == ContainerType.SUBSTATION) {
                    diagramView = SubstationDiagramView.load(svgInputStream, metadataInputStream, switchId -> handleSwitchPositionchange(c, switchId), AbstractSingleLineDiagramViewer.this);
                } else {
                    throw new AssertionError();
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return new ContainerDiagramResult(diagramView, svgData, metadataData, jsonData);
        }

        private void handleSwitchPositionchange(Container c, String switchId) {
            Switch sw = null;
            if (c.getContainerType() == ContainerType.VOLTAGE_LEVEL) {
                VoltageLevel v = (VoltageLevel) c;
                sw = v.getSubstation().getNetwork().getSwitch(switchId);
            } else if (c.getContainerType() == ContainerType.SUBSTATION) {
                Substation s = (Substation) c;
                sw = s.getNetwork().getSwitch(switchId);
            }
            if (sw != null) {
                sw.setOpen(!sw.isOpen());
                DiagramStyleProvider styleProvider = styles.get(styleComboBox.getSelectionModel().getSelectedItem());
                styleProvider.reset();
                loadDiagram(c);
            }
        }

        private void loadDiagram(Container c) {
            Service<ContainerDiagramResult> loader = new Service<ContainerDiagramResult>() {
                @Override
                protected Task<ContainerDiagramResult> createTask() {
                    return new Task<ContainerDiagramResult>() {
                        @Override
                        protected ContainerDiagramResult call() {
                            return createContainerDiagramView(c);
                        }
                    };
                }
            };
            loader.setOnScheduled(event -> {
                Text loading = new Text("Loading...");
                loading.setFont(Font.font(30));
                flowPane.setContent(loading);
                svgTextArea.setText("");
                metadataTextArea.setText("");
                jsonTextArea.setText("");
            });
            loader.setOnSucceeded(event -> {
                ContainerDiagramResult result = (ContainerDiagramResult) event.getSource().getValue();
                if (result.getView() != null) {
                    flowPane.setContent(result.getView());
                }
                svgTextArea.setText(result.getSvgData());
                metadataTextArea.setText(result.getMetadataData());
                jsonTextArea.setText(result.getJsonData());
            });
            loader.setOnFailed(event -> {
                Throwable e = event.getSource().getException();
                LOGGER.error(e.toString(), e);
            });
            loader.start();
        }

        private ComponentLibrary getComponentLibrary() {
            String selectedItem = svgLibraryComboBox.getSelectionModel().getSelectedItem();
            return svgLibraries.get(selectedItem);
        }

        private SubstationLayoutFactory getSubstationLayoutFactory() {
            String selectedItem = substationLayoutComboBox.getSelectionModel().getSelectedItem();
            return substationsLayouts.get(selectedItem);
        }

        private void createArea(TextField searchField, Button searchButton, Button saveButton,
                                String descrSave, String extensionSave,
                                TextArea textArea, VBox area,
                                AtomicReference<Integer> searchStart) {
            HBox searchBox = new HBox();
            searchBox.setSpacing(20);
            searchBox.setPadding(new Insets(10));
            searchField.setPrefColumnCount(35);
            searchBox.getChildren().add(searchField);
            searchBox.getChildren().add(searchButton);
            searchBox.getChildren().add(saveButton);

            searchStart.set(0);
            searchButton.setOnAction(evh -> {
                String txtPattern = searchField.getText();
                Pattern pattern = Pattern.compile(txtPattern);
                Matcher matcher = pattern.matcher(textArea.getText());
                boolean found = matcher.find(searchStart.get());
                if (found) {
                    textArea.selectRange(matcher.start(), matcher.end());
                    searchStart.set(matcher.end());
                } else {
                    textArea.deselect();
                    searchStart.set(0);
                    found = matcher.find(searchStart.get());
                    if (found) {
                        textArea.selectRange(matcher.start(), matcher.end());
                        searchStart.set(matcher.end());
                    }
                }
            });
            searchField.textProperty().addListener((observable, oldValue, newValue) ->
                searchStart.set(0)
            );

            saveButton.setOnAction(evh -> {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(descrSave, extensionSave);
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showSaveDialog(getScene().getWindow());

                if (file != null) {
                    try {
                        PrintWriter writer;
                        writer = new PrintWriter(file);
                        writer.println(textArea.getText());
                        writer.close();
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            });

            area.setSpacing(8);
            area.getChildren().add(searchBox);
            area.getChildren().add(textArea);
            VBox.setVgrow(searchBox, Priority.NEVER);
            VBox.setVgrow(textArea, Priority.ALWAYS);
            textArea.setEditable(false);
        }
    }

    abstract class AbstractSelectableContainer {

        protected final String id;

        protected final String name;

        protected final BooleanProperty checkedProperty = new SimpleBooleanProperty();

        protected boolean saveDiagrams = true;

        AbstractSelectableContainer(String id, String name) {
            this.id = id;
            this.name = name;
            checkedProperty.addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    addDiagramTab();
                } else {
                    removeDiagramTab();
                }
                if (saveDiagrams) {
                    saveSelectedDiagrams();
                }
            });
        }

        private void removeDiagramTab() {
            checkedDiagramsPane.getTabs().removeIf(tab -> tab.getText().equals(id));
        }

        abstract void addDiagramTab();

        protected String getId() {
            return id;
        }

        protected String getIdOrName() {
            return showNames.isSelected() ? name : id;
        }

        public BooleanProperty checkedProperty() {
            return checkedProperty;
        }

        public void setCheckedProperty(Boolean b) {
            checkedProperty.setValue(b);
        }

        public void setSaveDiagrams(boolean saveDiagrams) {
            this.saveDiagrams = saveDiagrams;
        }

        @Override
        public String toString() {
            return getIdOrName();
        }
    }

    private class SelectableVoltageLevel extends AbstractSelectableContainer {

        SelectableVoltageLevel(String id, String name) {
            super(id, name);
        }

        @Override
        protected void addDiagramTab() {
            VoltageLevel vl = networkProperty.get().getVoltageLevel(id);
            if (vl != null) {
                Tab tab = new Tab(id, new ContainerDiagramPane(vl));
                tab.setTooltip(new Tooltip(vl.getName()));
                tab.setOnCloseRequest(e -> closeTab());

                ContextMenu menu = new ContextMenu();
                MenuItem itemCloseTab = new MenuItem("Close tab");
                itemCloseTab.setOnAction(e -> closeTab());
                MenuItem itemCloseAllTabs = new MenuItem("Close all tabs");
                itemCloseAllTabs.setOnAction(e -> closeAllTabs());

                menu.getItems().add(itemCloseTab);
                menu.getItems().add(itemCloseAllTabs);
                tab.setContextMenu(menu);
                checkedDiagramsPane.getTabs().add(tab);
                checkedDiagramsPane.getSelectionModel().select(tab);
            } else {
                LOGGER.warn("Voltage level {} not found", id);
            }
        }

        public void closeTab() {
            checkedProperty.set(false);
            checkvItemTree(id, false);
        }
    }

    private class SelectableSubstation extends AbstractSelectableContainer {
        SelectableSubstation(String id, String name) {
            super(id, name);
        }

        @Override
        protected void addDiagramTab() {
            Substation s = networkProperty.get().getSubstation(id);
            if (s != null) {
                Tab tab = new Tab(id, new ContainerDiagramPane(s));
                tab.setTooltip(new Tooltip(s.getName()));
                tab.setOnCloseRequest(e -> closeTab());

                ContextMenu menu = new ContextMenu();
                MenuItem itemCloseTab = new MenuItem("Close tab");
                itemCloseTab.setOnAction(e -> closeTab());

                MenuItem itemCloseAllTabs = new MenuItem("Close all tabs");
                itemCloseAllTabs.setOnAction(e -> closeAllTabs());

                menu.getItems().add(itemCloseTab);
                menu.getItems().add(itemCloseAllTabs);
                tab.setContextMenu(menu);
                checkedDiagramsPane.getTabs().add(tab);
                checkedDiagramsPane.getSelectionModel().select(tab);
            } else {
                LOGGER.warn("Substation {} not found", id);
            }
        }

        private void checksItemTree(String id, boolean selected) {
            substationsTree.getRoot().getChildren().stream().forEach(child -> {
                if (child.getValue().getId().equals(id)) {
                    ((CheckBoxTreeItem) child).setSelected(selected);
                }
            });
        }

        public void closeTab() {
            checkedProperty.set(false);
            checksItemTree(id, false);
        }
    }

    private VoltageLevelLayoutFactory getVoltageLevelLayoutFactory() {
        String selectedItem = voltageLevelLayoutComboBox.getSelectionModel().getSelectedItem();
        return voltageLevelsLayouts.get(selectedItem);
    }

    private void setParameters(LayoutParameters layoutParameters) {
        this.layoutParameters.set(new LayoutParameters(layoutParameters));
    }

    private void addSpinner(String label, double min, double max, double amountToStepBy, int row,
                            ToDoubleFunction<LayoutParameters> initializer,
                            BiFunction<LayoutParameters, Double, LayoutParameters> updater) {
        Spinner<Double> spinner = new Spinner<>(min, max, initializer.applyAsDouble(layoutParameters.get()), amountToStepBy);
        spinner.setEditable(true);
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> setParameters(updater.apply(layoutParameters.get(), newValue)));
        parametersPane.add(new Label(label), 0, row);
        parametersPane.add(spinner, 0, row + 1);
    }

    private void addCheckBox(String label, int row,
                             Predicate<LayoutParameters> initializer,
                             BiFunction<LayoutParameters, Boolean, LayoutParameters> updater) {
        CheckBox cb = new CheckBox(label);
        cb.setSelected(initializer.test(layoutParameters.get()));
        cb.selectedProperty().addListener((observable, oldValue, newValue) -> setParameters(updater.apply(layoutParameters.get(), newValue)));
        parametersPane.add(cb, 0, row);
    }

    private void initPositionLayoutCheckBox(Predicate<PositionVoltageLevelLayoutFactory> initializer, CheckBox stackCb) {
        VoltageLevelLayoutFactory layoutFactory = getVoltageLevelLayoutFactory();
        stackCb.setSelected(layoutFactory instanceof PositionVoltageLevelLayoutFactory && initializer.test((PositionVoltageLevelLayoutFactory) layoutFactory));
        stackCb.setDisable(!(layoutFactory instanceof PositionVoltageLevelLayoutFactory));
    }

    private void addPositionLayoutCheckBox(String label, int rowIndex, Predicate<PositionVoltageLevelLayoutFactory> initializer,
                                           BiFunction<PositionVoltageLevelLayoutFactory, Boolean, PositionVoltageLevelLayoutFactory> updater) {
        CheckBox stackCb = new CheckBox(label);
        initPositionLayoutCheckBox(initializer, stackCb);
        voltageLevelLayoutComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> initPositionLayoutCheckBox(initializer, stackCb));
        stackCb.selectedProperty().addListener((observable, oldValue, newValue) -> {
            VoltageLevelLayoutFactory layoutFactory = getVoltageLevelLayoutFactory();
            if (layoutFactory instanceof PositionVoltageLevelLayoutFactory) {
                updater.apply((PositionVoltageLevelLayoutFactory) layoutFactory, newValue);
                // just to trigger diagram update
                refreshDiagram();
            }
        });

        parametersPane.add(stackCb, 0, rowIndex);
    }

    private void createParametersPane() {
        parametersPane = new GridPane();
        parametersPane.setHgap(5);
        parametersPane.setVgap(5);
        parametersPane.setPadding(new Insets(5, 5, 5, 5));

        int rowIndex = 0;

        Button fitToContent = new Button("Fit to content");
        fitToContent.setOnAction(event -> {
            ContainerDiagramPane pane = null;
            Tab tab = diagramsPane.getSelectionModel().getSelectedItem();
            if (tab != null) {
                if (tab == tabChecked) {
                    if (checkedDiagramsPane.getSelectionModel().getSelectedItem() != null) {
                        pane = (ContainerDiagramPane) checkedDiagramsPane.getSelectionModel().getSelectedItem().getContent();
                    }
                } else {
                    pane = (ContainerDiagramPane) selectedDiagramPane.getCenter();
                }
                if (pane != null) {
                    ((AbstractContainerDiagramView) pane.getFlowPane().getContent()).fitToContent(
                            pane.getFlowPane().getViewportBounds().getWidth(), 20.,
                            pane.getFlowPane().getViewportBounds().getHeight(), 20.);
                    pane.getFlowPane().setHvalue(pane.getFlowPane().getHmin());
                    pane.getFlowPane().setVvalue(pane.getFlowPane().getVmin());
                }
            }
        });

        parametersPane.add(fitToContent, 0, rowIndex++);

        // svg library list
        svgLibraryComboBox.getItems().addAll(svgLibraries.keySet());
        svgLibraryComboBox.getSelectionModel().selectFirst();
        svgLibraryComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshDiagram());
        parametersPane.add(new Label("Design:"), 0, rowIndex++);
        parametersPane.add(svgLibraryComboBox, 0, rowIndex++);

        styleComboBox.getItems().addAll(styles.keySet());
        styleComboBox.getSelectionModel().select(1);
        styleComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshDiagram());
        parametersPane.add(new Label("Style:"), 0, rowIndex++);
        parametersPane.add(styleComboBox, 0, rowIndex++);

        // substation layout list
        substationLayoutComboBox.getItems().addAll(substationsLayouts.keySet());
        substationLayoutComboBox.getSelectionModel().selectFirst();
        substationLayoutComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshDiagram());
        parametersPane.add(new Label("Substation Layout:"), 0, rowIndex++);
        parametersPane.add(substationLayoutComboBox, 0, rowIndex++);

        // voltageLevel layout list
        voltageLevelLayoutComboBox.getItems().addAll(voltageLevelsLayouts.keySet());
        voltageLevelLayoutComboBox.getSelectionModel().selectFirst();
        voltageLevelLayoutComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshDiagram());

        parametersPane.add(new Label("VoltageLevel Layout:"), 0, rowIndex++);
        parametersPane.add(voltageLevelLayoutComboBox, 0, rowIndex++);

        //CGMES-DL diagrams names list
        parametersPane.add(new Label("CGMES-DL Diagrams:"), 0, ++rowIndex);
        parametersPane.add(diagramNamesComboBox, 0, ++rowIndex);
        diagramNamesComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshDiagram());
        diagramNamesComboBox.setDisable(true);
        voltageLevelLayoutComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> setDiagramsNamesContent(networkProperty.get(), false));
        rowIndex += 1;

        addSpinner("Horizontal substation padding:", 50, 300, 5, rowIndex, LayoutParameters::getHorizontalSubstationPadding, LayoutParameters::setHorizontalSubstationPadding);
        rowIndex += 2;
        addSpinner("Vertical substation padding:", 50, 300, 5, rowIndex, LayoutParameters::getVerticalSubstationPadding, LayoutParameters::setVerticalSubstationPadding);
        rowIndex += 2;
        addSpinner("Initial busbar X:", 0, 100, 5, rowIndex, LayoutParameters::getInitialXBus, LayoutParameters::setInitialXBus);
        rowIndex += 2;
        addSpinner("Initial busbar Y:", 0, 500, 5, rowIndex, LayoutParameters::getInitialYBus, LayoutParameters::setInitialYBus);
        rowIndex += 2;
        addSpinner("Busbar vertical space:", 10, 100, 5, rowIndex, LayoutParameters::getVerticalSpaceBus, LayoutParameters::setVerticalSpaceBus);
        rowIndex += 2;
        addSpinner("Horizontal busbar padding:", 10, 100, 5, rowIndex, LayoutParameters::getHorizontalBusPadding, LayoutParameters::setHorizontalBusPadding);
        rowIndex += 2;
        addSpinner("Cell width:", 10, 100, 5, rowIndex, LayoutParameters::getCellWidth, LayoutParameters::setCellWidth);
        rowIndex += 2;
        addSpinner("Extern cell height:", 100, 500, 10, rowIndex, LayoutParameters::getExternCellHeight, LayoutParameters::setExternCellHeight);
        rowIndex += 2;
        addSpinner("Intern cell height:", 10, 100, 5, rowIndex, LayoutParameters::getInternCellHeight, LayoutParameters::setInternCellHeight);
        rowIndex += 2;
        addSpinner("Stack height:", 10, 100, 5, rowIndex, LayoutParameters::getStackHeight, LayoutParameters::setStackHeight);
        rowIndex += 2;
        addCheckBox("Show grid", rowIndex, LayoutParameters::isShowGrid, LayoutParameters::setShowGrid);
        rowIndex += 1;
        addCheckBox("Show internal nodes", rowIndex, LayoutParameters::isShowInternalNodes, LayoutParameters::setShowInternalNodes);
        rowIndex += 1;
        addCheckBox("Draw straight wires", rowIndex, LayoutParameters::isDrawStraightWires, LayoutParameters::setDrawStraightWires);
        rowIndex += 1;
        addPositionLayoutCheckBox("Stack feeders", rowIndex, PositionVoltageLevelLayoutFactory::isFeederStacked, PositionVoltageLevelLayoutFactory::setFeederStacked);
        rowIndex += 1;
        addPositionLayoutCheckBox("Remove fictitious nodes", rowIndex, PositionVoltageLevelLayoutFactory::isRemoveUnnecessaryFictitiousNodes, PositionVoltageLevelLayoutFactory::setRemoveUnnecessaryFictitiousNodes);
        rowIndex += 1;
        addPositionLayoutCheckBox("Substitute singular fictitious nodes", rowIndex, PositionVoltageLevelLayoutFactory::isSubstituteSingularFictitiousByFeederNode, PositionVoltageLevelLayoutFactory::setSubstituteSingularFictitiousByFeederNode);
        rowIndex += 1;
        addCheckBox("Shift feeders height", rowIndex, LayoutParameters::isShiftFeedersPosition, LayoutParameters::setShiftFeedersPosition);
        rowIndex += 1;
        addSpinner("Shift feeders height scale factor:", 1, 10, 1, rowIndex, LayoutParameters::getScaleShiftFeedersPosition, LayoutParameters::setScaleShiftFeedersPosition);
        rowIndex += 2;
        addSpinner("Scale factor:", 1, 20, 1, rowIndex, LayoutParameters::getScaleFactor, LayoutParameters::setScaleFactor);
        rowIndex += 2;
        addSpinner("Arrows distance:", 0, 200, 1, rowIndex, LayoutParameters::getArrowDistance, LayoutParameters::setArrowDistance);
        rowIndex += 2;
        addCheckBox("Avoid SVG components duplication", rowIndex, LayoutParameters::isAvoidSVGComponentsDuplication, LayoutParameters::setAvoidSVGComponentsDuplication);

        rowIndex += 1;
        addCheckBox("Adapt cell height to content", rowIndex, LayoutParameters::isAdaptCellHeightToContent, LayoutParameters::setAdaptCellHeightToContent);
        rowIndex += 2;
        addSpinner("Min space between components:", 8, 60, 1, rowIndex, LayoutParameters::getMinSpaceBetweenComponents, LayoutParameters::setMinSpaceBetweenComponents);
        rowIndex += 2;
        addSpinner("Minimum extern cell height:", 80, 300, 10, rowIndex, LayoutParameters::getMinExternCellHeight, LayoutParameters::setMinExternCellHeight);

        rowIndex += 2;
        addCheckBox("Center label:", rowIndex, LayoutParameters::isLabelCentered, LayoutParameters::setLabelCentered);
        rowIndex += 2;
        addSpinner("Angle Label:", -360, 360, 1, rowIndex, LayoutParameters::getAngleLabelShift, LayoutParameters::setAngleLabelShift);

        rowIndex += 2;
        addCheckBox("HighLight line state", rowIndex, LayoutParameters::isHighlightLineState, LayoutParameters::setHighlightLineState);
    }

    private void setDiagramsNamesContent(Network network, boolean setValues) {
        if (network != null && NetworkDiagramData.checkNetworkDiagramData(network)) {
            if (setValues) {
                diagramNamesComboBox.getItems().setAll(NetworkDiagramData.getDiagramsNames(network));
                diagramNamesComboBox.getSelectionModel().clearSelection();
                diagramNamesComboBox.setValue(null);
            }
            diagramNamesComboBox.setDisable(!(getVoltageLevelLayoutFactory() instanceof CgmesVoltageLevelLayoutFactory));
        } else {
            diagramNamesComboBox.getItems().clear();
            diagramNamesComboBox.setDisable(true);
        }
    }

    private void refreshDiagram() {
        layoutParameters.set(new LayoutParameters(layoutParameters.get()));
    }

    private void loadSelectedVoltageLevelsDiagrams() {
        String selectedIdsPropertyValue = preferences.get(SELECTED_VOLTAGE_LEVEL_IDS_PROPERTY, null);
        if (selectedIdsPropertyValue != null) {
            try {
                Set<String> selectedIds = new HashSet<>(objectMapper.readValue(selectedIdsPropertyValue, new TypeReference<List<String>>() {
                }));
                selectableVoltageLevels.stream()
                        .filter(selectableObject -> selectedIds.contains(selectableObject.getId()))
                        .forEach(selectableVoltageLevel -> {
                            selectableVoltageLevel.setSaveDiagrams(false);
                            selectableVoltageLevel.checkedProperty().set(true);
                            selectableVoltageLevel.setSaveDiagrams(true);
                        });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void loadSelectedSubstationsDiagrams() {
        String selectedPropertyValue = preferences.get(SELECTED_SUBSTATION_IDS_PROPERTY, null);

        if (selectedPropertyValue != null) {
            try {
                Set<String> selectedSubstationIds = new HashSet<>(objectMapper.readValue(selectedPropertyValue, new TypeReference<List<String>>() {
                }));
                selectableSubstations.stream()
                        .filter(selectableSubstation -> selectedSubstationIds.contains(selectableSubstation.getId()))
                        .forEach(selectableSubstation -> {
                            selectableSubstation.setSaveDiagrams(false);
                            selectableSubstation.checkedProperty().set(true);
                            selectableSubstation.setSaveDiagrams(true);
                        });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /*
        Handling the display of names/id in the substations tree
     */
    private void initTreeCellFactory() {
        substationsTree.setCellFactory(param -> {
            CheckBoxTreeCell<Container> treeCell = new CheckBoxTreeCell<>();
            StringConverter<TreeItem<Container>> strConvert = new StringConverter<TreeItem<Container>>() {
                @Override
                public String toString(TreeItem<Container> c) {
                    if (c.getValue() != null) {
                        return showNames.isSelected() ? c.getValue().getName() : c.getValue().getId();
                    } else {
                        return "";
                    }
                }

                @Override
                public TreeItem<Container> fromString(String string) {
                    return null;
                }
            };
            treeCell.setConverter(strConvert);
            return treeCell;
        });
    }

    @Override
    public void start(Stage primaryStage) {
        initLayoutsFactory();
        initStylesProvider();

        initTreeCellFactory();

        showNames.setSelected(true);
        showNames.selectedProperty().addListener((observable, oldValue, newValue) -> {
            substationsTree.refresh();
            refreshDiagram();
        });

        hideSubstations.selectedProperty().addListener((observable, oldValue, newValue) -> {
            initSubstationsTree();
            substationsTree.refresh();
        });

        filterInput.textProperty().addListener(obs ->
            initSubstationsTree()
        );

        // handling the change of the network
        networkProperty.addListener((observable, oldNetwork, newNetwork) -> {
            if (newNetwork == null) {
                selectableVoltageLevels.clear();
                selectableSubstations.clear();
            } else {
                selectableVoltageLevels.setAll(newNetwork.getVoltageLevelStream()
                        .map(vl -> new SelectableVoltageLevel(vl.getId(), vl.getName()))
                        .collect(Collectors.toList()));
                selectableSubstations.setAll(newNetwork.getSubstationStream()
                        .map(s -> new SelectableSubstation(s.getId(), s.getName()))
                        .collect(Collectors.toList()));
            }
        });
        diagramsPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabSelected = new Tab("Selected", selectedDiagramPane);
        tabChecked = new Tab("Checked", checkedDiagramsPane);
        diagramsPane.getTabs().setAll(tabSelected, tabChecked);

        createParametersPane();

        BorderPane voltageLevelPane = new BorderPane();
        Label filterLabel = new Label("Filter:");
        filterLabel.setMinWidth(40);
        GridPane voltageLevelToolBar = new GridPane();
        voltageLevelToolBar.setHgap(5);
        voltageLevelToolBar.setVgap(5);
        voltageLevelToolBar.setPadding(new Insets(5, 5, 5, 5));
        voltageLevelToolBar.add(showNames, 0, 0, 2, 1);
        voltageLevelToolBar.add(hideSubstations, 0, 1, 2, 1);
        voltageLevelToolBar.add(filterLabel, 0, 2);
        voltageLevelToolBar.add(filterInput, 1, 2);
        ColumnConstraints c0 = new ColumnConstraints();
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        voltageLevelToolBar.getColumnConstraints().addAll(c0, c1);
        voltageLevelPane.setTop(voltageLevelToolBar);
        voltageLevelPane.setCenter(substationsTree);

        SplitPane splitPane = new SplitPane(voltageLevelPane, diagramsPane, new ScrollPane(parametersPane));
        splitPane.setDividerPositions(0.2, 0.7, 0.1);

        Node casePane = createCasePane(primaryStage);
        BorderPane.setMargin(casePane, new Insets(3, 3, 3, 3));
        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(splitPane);
        mainPane.setTop(casePane);

        // selected voltegeLevels diagrams reloading
        selectableVoltageLevels.addListener(new ListChangeListener<SelectableVoltageLevel>() {
            @Override
            public void onChanged(Change<? extends SelectableVoltageLevel> c) {
                loadSelectedVoltageLevelsDiagrams();
                selectableVoltageLevels.remove(this);
            }
        });

        // selected substation diagrams reloading
        selectableSubstations.addListener(new ListChangeListener<SelectableSubstation>() {
            @Override
            public void onChanged(Change<? extends SelectableSubstation> c) {
                loadSelectedSubstationsDiagrams();
                selectableSubstations.remove(this);
            }
        });

        // Handling selection of a substation or a voltageLevel in the substations tree
        substationsTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Container>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<Container>> observable, TreeItem<Container> oldValue, TreeItem<Container> newValue) {
                if (newValue == null) {
                    return;
                }
                Container c = newValue.getValue();
                selectedDiagramPane.setCenter(new ContainerDiagramPane(c));
            }
        });

        // case reloading
        loadNetworkFromPreferences();

        Scene scene = new Scene(mainPane, 1000, 800);
        primaryStage.setTitle("Substation diagram viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    protected void loadNetworkFromPreferences() {
    }

    protected abstract Node createCasePane(Stage primaryStage);

    /*
        check/uncheck a voltageLevel in the substations tree
     */
    private void checkVoltageLevel(VoltageLevel v, Boolean checked) {
        selectableVoltageLevels.stream()
                .filter(selectableVoltageLevel -> selectableVoltageLevel.getIdOrName().equals(showNames.isSelected() ? v.getName() : v.getId()))
                .forEach(selectableVoltageLevel -> selectableVoltageLevel.setCheckedProperty(checked));
    }

    /*
        check/uncheck a substation in the substations tree
     */
    private void checkSubstation(Substation s, Boolean checked) {
        selectableSubstations.stream()
                .filter(selectableSubstation -> selectableSubstation.getIdOrName().equals(showNames.isSelected() ? s.getName() : s.getId()))
                .forEach(selectableSubstation -> selectableSubstation.setCheckedProperty(checked));
    }

    private void initVoltageLevelsTree(TreeItem<Container> rootItem,
                                       Substation s, String filter, boolean emptyFilter,
                                       Map<String, SelectableSubstation> mapSubstations,
                                       Map<String, SelectableVoltageLevel> mapVoltageLevels) {
        boolean firstVL = true;
        CheckBoxTreeItem<Container> sItem = null;

        for (VoltageLevel v : s.getVoltageLevels()) {
            boolean vlOk = showNames.isSelected() ? v.getName().contains(filter) : v.getId().contains(filter);

            if (!emptyFilter && !vlOk) {
                continue;
            }

            CheckBoxTreeItem<Container> vItem = new CheckBoxTreeItem<>(v);
            vItem.setIndependent(true);
            if (mapVoltageLevels.containsKey(v.getId()) && mapVoltageLevels.get(v.getId()).checkedProperty().get()) {
                vItem.setSelected(true);
            }

            if (firstVL && !hideSubstations.isSelected()) {
                sItem = new CheckBoxTreeItem<>(s);
                sItem.setIndependent(true);
                sItem.setExpanded(true);
                if (mapSubstations.containsKey(s.getId()) && mapSubstations.get(s.getId()).checkedProperty().get()) {
                    sItem.setSelected(true);
                }
                rootItem.getChildren().add(sItem);
                sItem.selectedProperty().addListener((obs, oldVal, newVal) ->
                    checkSubstation(s, newVal)
                );
            }

            firstVL = false;

            if (sItem != null) {
                sItem.getChildren().add(vItem);
            } else {
                rootItem.getChildren().add(vItem);
            }

            vItem.selectedProperty().addListener((obs, oldVal, newVal) ->
                    checkVoltageLevel(v, newVal));
        }
    }

    private void initSubstationsTree() {
        String filter = filterInput.getText();
        boolean emptyFilter = StringUtils.isEmpty(filter);

        Network n = networkProperty.get();
        TreeItem<Container> rootItem = new TreeItem<>();
        rootItem.setExpanded(true);

        Map<String, SelectableSubstation> mapSubstations = selectableSubstations.stream()
                .collect(Collectors.toMap(SelectableSubstation::getId, Function.identity()));
        Map<String, SelectableVoltageLevel> mapVoltageLevels = selectableVoltageLevels.stream()
                .collect(Collectors.toMap(SelectableVoltageLevel::getId, Function.identity()));

        for (Substation s : n.getSubstations()) {
            initVoltageLevelsTree(rootItem, s, filter, emptyFilter, mapSubstations, mapVoltageLevels);
        }

        if (substationsTree.getRoot() != null) {
            substationsTree.getRoot().getChildren().clear();
        }

        substationsTree.setRoot(rootItem);
        substationsTree.setShowRoot(false);
    }

    @Override
    public void display(String voltageLevelId) {
        VoltageLevel v = networkProperty.get().getVoltageLevel(voltageLevelId);
        if (diagramsPane.getSelectionModel().getSelectedItem() == tabChecked) {
            checkVoltageLevel(v, true);
            checkvItemTree(voltageLevelId, true);
            checkedDiagramsPane.getTabs().stream().forEach(tab -> {
                if (tab.getText().equals(voltageLevelId)) {
                    checkedDiagramsPane.getSelectionModel().select(tab);
                }
            });
        } else if (diagramsPane.getSelectionModel().getSelectedItem() == tabSelected) {
            selectedDiagramPane.setCenter(new ContainerDiagramPane(v));
        }
    }

    private void checkvItemTree(String id, boolean selected) {
        substationsTree.getRoot().getChildren().stream().forEach(childS ->
                childS.getChildren().stream().forEach(childV -> {
                    if (childV.getValue().getId().equals(id)) {
                        ((CheckBoxTreeItem) childV).setSelected(selected);
                    }
                })
        );
    }

    public void saveSelectedDiagrams() {
        try {
            String selectedVoltageLevelIdsPropertyValue = objectMapper.writeValueAsString(selectableVoltageLevels.stream()
                    .filter(selectableVoltageLevel -> selectableVoltageLevel.checkedProperty().get())
                    .map(SelectableVoltageLevel::getId)
                    .collect(Collectors.toList()));
            preferences.put(SELECTED_VOLTAGE_LEVEL_IDS_PROPERTY, selectedVoltageLevelIdsPropertyValue);

            String selectedSubstationIdsPropertyValue = objectMapper.writeValueAsString(selectableSubstations.stream()
                    .filter(selectableSubstation -> selectableSubstation.checkedProperty().get())
                    .map(SelectableSubstation::getId)
                    .collect(Collectors.toList()));
            preferences.put(SELECTED_SUBSTATION_IDS_PROPERTY, selectedSubstationIdsPropertyValue);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void closeAllTabs() {
        selectableVoltageLevels.stream().filter(s -> s.checkedProperty().get()).forEach(s -> s.closeTab());
        selectableSubstations.stream().filter(s -> s.checkedProperty().get()).forEach(s -> s.closeTab());
    }

    protected void setNetwork(Network network) {
        closeAllTabs();
        updateLayoutsFactory(network);
        updateStylesProvider(network);
        networkProperty.setValue(network);
        initSubstationsTree();
        setDiagramsNamesContent(network, true);
    }

    private void initLayoutsFactory() {
        voltageLevelsLayouts.put("Smart", null);
        voltageLevelsLayouts.put("Auto extensions", new PositionVoltageLevelLayoutFactory(new PositionFromExtension()));
        voltageLevelsLayouts.put("Auto without extensions", new PositionVoltageLevelLayoutFactory(new PositionFree()));
        voltageLevelsLayouts.put("Auto without extensions Clustering", new PositionVoltageLevelLayoutFactory(new PositionByClustering()));
        voltageLevelsLayouts.put("Random", new RandomVoltageLevelLayoutFactory(500, 500));
        voltageLevelsLayouts.put("Cgmes", null);

        substationsLayouts.put("Horizontal", new HorizontalSubstationLayoutFactory());
        substationsLayouts.put("Vertical", new VerticalSubstationLayoutFactory());
        substationsLayouts.put("Cgmes", null);
        substationsLayouts.put("Smart", new ForceSubstationLayoutFactory(ForceSubstationLayoutFactory.CompactionType.NONE));
        substationsLayouts.put("Smart with horizontal compaction", new ForceSubstationLayoutFactory(ForceSubstationLayoutFactory.CompactionType.HORIZONTAL));
        substationsLayouts.put("Smart with vertical compaction", new ForceSubstationLayoutFactory(ForceSubstationLayoutFactory.CompactionType.VERTICAL));
    }

    private void updateLayoutsFactory(Network network) {
        voltageLevelsLayouts.put("Smart", new SmartVoltageLevelLayoutFactory(network));
        voltageLevelsLayouts.put("Cgmes", new CgmesVoltageLevelLayoutFactory(network));

        substationsLayouts.put("Cgmes", new CgmesSubstationLayoutFactory(network));
    }

    private void initStylesProvider() {
        styles.put("Default", new DefaultDiagramStyleProvider());
        styles.put("Nominal voltage", null);
        styles.put("Topology", null);
    }

    private void updateStylesProvider(Network network) {
        styles.put("Nominal voltage", new NominalVoltageDiagramStyleProvider(network));
        styles.put("Topology", new TopologicalStyleProvider(network));
    }
}
