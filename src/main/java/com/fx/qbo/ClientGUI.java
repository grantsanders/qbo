package com.fx.qbo;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.io.IOException;

import com.intuit.ipp.exception.FMSException;
import com.intuit.oauth2.exception.InvalidRequestException;
import com.intuit.oauth2.exception.OAuthException;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

public class ClientGUI {

	public ClientGUI() {
		buildConnectPanel();
	}

	private static JFrame frame = new JFrame("beansquad importer");
	private final static String homeDirectory = System.getProperty("user.home");
	private static String authCode = "";
	private static String realmId = "";
	public static APIController api = new APIController();
	private static String filePath = "";
	private static String fileName = "";
	private static JFrame browser = new JFrame("beansquad importer");

	private static void buildConnectPanel() {

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints mgr = new GridBagConstraints();
		mgr.gridwidth = 3;
		mgr.gridheight = 3;
		frame.setSize(350, 250);
		JButton connectButton = new JButton("Connect to Quickbooks");
		mgr.gridx = 1;
		mgr.gridy = 1;
		panel.add(connectButton, mgr);
		frame.add(panel);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		connectButton.addActionListener(e -> {
			try {
				api.setOAuthUrl();
			} catch (InvalidRequestException e1) {
				Popup invalidRequest = new Popup("InvalidRequestException", "Error: Invalid Request");
				invalidRequest.setVisible(true);
			} catch (OAuthException e1) {
				Popup OAuthException = new Popup("OAuthException", "Error: Invalid OAuth2 Request");
				OAuthException.setVisible(true);
			} catch (IOException e1) {
				Popup IOException = new Popup("IOException", "Error: Invalid Input/Output");
				IOException.setVisible(true);
			}
			buildAuthBrowser();
		});

	}

	private static void buildAuthBrowser() {

		// JFrame browser = new JFrame("beansquad importer");
		final JFXPanel fxPanel = new JFXPanel();
		browser.add(fxPanel);
		browser.setSize(800, 600);
		browser.setLocationRelativeTo(null);
		browser.setVisible(true);
		// browser.addWindowListener(new java.awt.event.WindowAdapter() {

		// @Override
		// public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		// api.setAuthCode(authCode);
		// api.setRealmId(realmId);
		// buildUserPlatform();
		// }
		// });
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(fxPanel);
			}
		});

	}

	private static void setCode(String location) {
		authCode = location.substring(location.indexOf("code=") + 5, location.indexOf("&state"));
		System.out.println(location);
		System.out.println("Auth code : " + authCode);

	}

	private static void setRealmId(String location) {
		realmId = location.substring(location.lastIndexOf("&") + 9, location.length());
		System.out.println("Realm ID : " + realmId);
	}

	private static void initFX(JFXPanel fxPanel) {
		// fx thread
		Scene scene = createScene();
		fxPanel.setScene(scene);
	}

	private static Scene createScene() {
		WebView view = new WebView();
		WebEngine authBrowser = view.getEngine();
		Group root = new Group();
		Scene scene = new Scene(root);
		authBrowser.load(api.getUrl());

		authBrowser.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
			if (Worker.State.SUCCEEDED.equals(newValue)) {
				setCode(authBrowser.getLocation());
				setRealmId(authBrowser.getLocation());
				if (realmId.equals(API_Constants.getRealmId())) {
					browser.setVisible(false);
					api.setAuthCode(authCode);
					api.setRealmId(realmId);
					buildUserPlatform();
				}
			}
		});
		root.getChildren().add(view);
		return (scene);
	}

	public static void buildUserPlatform() {
		frame.setVisible(false);

		JFileChooser csvSelector = new JFileChooser(homeDirectory + "/Downloads/");
		csvSelector.setApproveButtonText("Select");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
		csvSelector.setFileFilter(filter);

		JFrame csvFrame = new JFrame("what up jerome");
		JPanel main = new JPanel();
		JLabel selectPrompt = new JLabel("Select a csv file:");
		JButton selectButton = new JButton("bing bong");
		JButton importerButton = new JButton("Import");
		JLabel importerLabel = new JLabel(fileName);

		main.setLayout(new GridBagLayout());
		GridBagConstraints mgr = new GridBagConstraints();
		mgr.insets = new Insets(3, 1, 3, 1);
		csvFrame.setSize(300, 200);

		main.setLayout(new GridBagLayout());
		mgr.gridx = 1;
		mgr.gridy = 0;
		main.add(selectPrompt, mgr);
		mgr.gridy = 1;
		main.add(selectButton, mgr);

		selectButton.addActionListener(e -> {
			csvSelector.setDialogTitle("Select file for import");
			int o = csvSelector.showOpenDialog(null);

			if (o == JFileChooser.APPROVE_OPTION) {

				filePath = csvSelector.getSelectedFile().getAbsolutePath();
				csvFrame.setSize(400, 350);
				fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
				importerLabel.setText("Importing " + fileName);

				csvFrame.setSize(450, 350);
				csvFrame.setLocationRelativeTo(null);

				mgr.gridy = 2;
				main.add(importerLabel, mgr);
				mgr.gridy = 3;
				main.add(importerButton, mgr);

			}
			importerButton.addActionListener(x -> {
				importerLabel.setText("Formatting and posting invoices...");
			});
		});

		importerButton.addActionListener(x -> {

			importerLabel.setText("Formatting and posting invoices...");

			// api.setAuthCode(authCode);

			if ((fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).equals("csv")) == false) {

				importerLabel.setText("does that look like a csv to you??");

			} else {

				api.getServiceHandler();
				FileHandler handler = new FileHandler(filePath, api);

				try {

					int invoiceCounter = handler.formatData();
					if (invoiceCounter == 1) {
						importerLabel.setText("Done- created " + invoiceCounter + " invoice");
					} else {
						importerLabel.setText("Done- created " + invoiceCounter + " invoices");
					}

				} catch (OAuthException e1) {
					Popup OAuthException = new Popup("OAuthException", "Error: Invalid OAuth Request");
					OAuthException.setVisible(true);
					e1.printStackTrace();
				} catch (FMSException e1) {
					Popup FMSException = new Popup("FMSException", "Error: Invalid Request.");
					FMSException.setVisible(true);
					e1.printStackTrace();
				}
			}
		});

		csvFrame.add(main);
		csvFrame.setLocationRelativeTo(null);
		csvFrame.setVisible(true);
		csvFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

}
