package com.example.posfunnyjumping;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class HomeController {


    private Stage stage;
    private Scene scene;
    private Parent root;


    @FXML
    private TableColumn<DatabaseConnection.Producto, Integer> claveColumn;

    @FXML
    private TableColumn<DatabaseConnection.Producto, String> descripcionColumn;

    @FXML
    private TableColumn<DatabaseConnection.Producto, Double> precioColumn;

    @FXML
    private TableColumn<DatabaseConnection.Producto, Double> existenciaColumn;


    @FXML
    private TableColumn<DatabaseConnection.Producto, Void> editarColumn; // Define as Void for buttons

    @FXML
    private TableColumn<DatabaseConnection.Producto, Void> eliminarColumn;


    @FXML
    private TextField ProductoClaveTextField;

    @FXML
    private TextField ProductoDescripcionTextField;
    @FXML
    private TextField ProductoPrecioTextField;
    @FXML
    private TextField ProductoExistenciaTextField;

    @FXML
    private TableView<DatabaseConnection.Producto> productosTable;

    private List<DatabaseConnection.Producto> productosList;

    @FXML
    private void handleTabOpen(Event event) {
        Tab tab = (Tab) event.getSource();
        if (tab.isSelected()) {
            switch (tab.getId()) {

                case "Productos": {


                    configureProductosTableColumns(); // Ensure columns are configured correctly

                    claveColumn.setCellValueFactory(new PropertyValueFactory<>("clave"));
                    descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
                    precioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
                    existenciaColumn.setCellValueFactory(new PropertyValueFactory<>("existencia"));

                    productosList = DatabaseConnection.getAllProductos();
                    ObservableList<DatabaseConnection.Producto> data = FXCollections.observableArrayList(productosList);
                    productosTable.setItems(data);

                    System.out.println(productosTable.getItems());
                    break;
                }

                case "Tiempos": {
                    System.out.println("Opened tab tiempos");
                    break;
                }

                case "Usuarios": {
                    System.out.println("Opened tab Usuarios");
                    break;
                }

            }
        }
    }

    @FXML
    private void handleRegistrarProducto(Event event) {

        if (ProductoDescripcionTextField.getText().isEmpty() ||
                ProductoPrecioTextField.getText().isEmpty() || ProductoExistenciaTextField.getText().isEmpty()) {
            System.out.println("Please fill in all the fields.");
        } else {

            String descripcion = ProductoDescripcionTextField.getText();
            double precio = Double.parseDouble(ProductoPrecioTextField.getText());
            double existencia = Double.parseDouble(ProductoExistenciaTextField.getText());


            if (ProductoClaveTextField.getText().isEmpty()) {
                // Insert the new product into the database
                DatabaseConnection.insertProducto(descripcion, precio, existencia);
            } else {
                int clave = Integer.parseUnsignedInt(ProductoClaveTextField.getText());
                DatabaseConnection.updateProducto(clave, descripcion, precio, existencia);
            }
            // Clear input fields after insertion
            ProductoClaveTextField.clear();
            ProductoDescripcionTextField.clear();
            ProductoPrecioTextField.clear();
            ProductoExistenciaTextField.clear();

            // Refresh the TableView with updated data
            productosList = DatabaseConnection.getAllProductos();
            ObservableList<DatabaseConnection.Producto> data = FXCollections.observableArrayList(productosList);
            productosTable.setItems(data);


        }

    }

    private void deleteProduct(DatabaseConnection.Producto producto) {
        // Implement your delete logic here
        // For example, you might have a method in DatabaseConnection class to delete the product
        DatabaseConnection.deleteProducto(producto.getClave());
    }

    private void configureProductosTableColumns() {
        // Clear input fields after insertion
        ProductoClaveTextField.clear();
        ProductoDescripcionTextField.clear();
        ProductoPrecioTextField.clear();
        ProductoExistenciaTextField.clear();


        claveColumn.setCellValueFactory(new PropertyValueFactory<>("clave"));
        descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        precioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        existenciaColumn.setCellValueFactory(new PropertyValueFactory<>("existencia"));

        // Define how the editColumn should be rendered
        editarColumn.setCellFactory(param -> new TableCell<DatabaseConnection.Producto, Void>() {
                    private final Button editButton = new Button("Editar");

                    {
                        editButton.setOnAction(event -> {
                            DatabaseConnection.Producto producto = getTableView().getItems().get(getIndex());

                            ProductoClaveTextField.setText(String.valueOf(producto.getClave()));
                            ProductoDescripcionTextField.setText(producto.getDescripcion());
                            ProductoPrecioTextField.setText(String.valueOf(producto.getPrecio()));
                            ProductoExistenciaTextField.setText(String.valueOf(producto.getExistencia()));

                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(editButton);
                        }
                    }
                }
        );

        eliminarColumn.setCellFactory(new Callback<TableColumn<DatabaseConnection.Producto, Void>, TableCell<DatabaseConnection.Producto, Void>>() {
            @Override
            public TableCell<DatabaseConnection.Producto, Void> call(final TableColumn<DatabaseConnection.Producto, Void> param) {
                return new TableCell<DatabaseConnection.Producto, Void>() {
                    private final Button deleteButton = new Button("Eliminar");

                    {
                        deleteButton.setOnAction(event -> {
                            DatabaseConnection.Producto producto = getTableView().getItems().get(getIndex());
                            // Code to delete the product
                            deleteProduct(producto);
                            // Refresh the table view
                            getTableView().getItems().remove(producto);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(deleteButton);
                        }
                    }
                };
            }
        });

    }


    @FXML
    protected void onRegistrarVentaButtonClick(ActionEvent event) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("RegistrarVenta.fxml")));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onConsultaVentasButtonClick(ActionEvent event) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ConsultaVentas.fxml")));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onConsultaComprasButtonClick(ActionEvent event) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ConsultaCompras.fxml")));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onConsultaCortesButtonClick(ActionEvent event) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ConsultaCortes.fxml")));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onConfiguracionButtonClick(ActionEvent event) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Configuracion.fxml")));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onRegresarAlHomeButtonClick(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Temporizador.fxml")));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


}

