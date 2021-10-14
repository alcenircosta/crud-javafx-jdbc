package gui;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entity;

	private SellerService service;

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txtId;

	@FXML
	private TextField txtName;
	
	@FXML
	private TextField txtEmail;
	
	@FXML
	private DatePicker dptxtBirhtDate;
	
	@FXML
	private TextField txtBaseSalary;

	@FXML
	private Label labelErrorName;
	
	@FXML
	private Label labelErrorEmail;
	
	@FXML
	private Label labelErrorBirhtDate;
	
	@FXML
	private Label labelErrorBaseSalary;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		if (service == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			entity = getFormData();
			service.saveOrUpdate(entity);
			notifyDataChangeListeners();
			Utils.currentStage(event).close();
		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		} catch (DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
	}

	private Seller getFormData() {

		ValidationException exception = new ValidationException("Validation Error");
		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addError("name", "Field can't be empty");
		}
		Seller obj = new Seller(Utils.tryParseToInt(txtId.getText()), txtName.getText());
		if (exception.getErrors().size() > 0) {
			throw exception;
		}
		return obj;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setSellerService(SellerService service) {
		this.service = service;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 50);
		Constraints.setTextFieldMaxLength(txtEmail, 40);
		Constraints.setTextFieldDouble(txtBaseSalary);
		Utils.formatDatePicker(dptxtBirhtDate, "dd/MM/yyyy");
	}

	public void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}

		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(entity.getName());
		txtEmail.setText(entity.getEmail());
		Locale.setDefault(Locale.US);
		if(entity.getBirthDate() != null) {
		dptxtBirhtDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
		txtBaseSalary.setText(String.format("%.2f", entity.getBaseSalary()));
		
		
	}

	private void setErrorMessages(Map<String, String> errors) {
		Set<String> field = errors.keySet();
		if (field.contains("name")) {
			labelErrorName.setText(errors.get("name"));
		}
	}
}
