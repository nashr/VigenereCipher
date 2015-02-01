/**
 * GUI & Encryption Algorithm
 */
package nashr;

import java.applet.Applet;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;

/**
 * @author Muhammad Nassirudin
 *
 */
public class Main extends Applet {
	
	private static final long serialVersionUID = -6802152294403293724L;
	
	private static final char LF = 10; // Enter
	private static final char CR = 13; // Enter
	
	private String filename; // Name of exported file
	private byte[] input; // Container of input text (plain or cipher text)
	private byte[] output; // Container of output text (plain or cipher text)
	
	private GroupLayout groupLayout;
	
	private Label labelMode;
	private Choice choiceMode;
	public static final int ENCRYPTION_MODE = 0;
	public static final int DECRYPTION_MODE = 1;
	
	private Label labelAlgorithm;
	private Choice choiceAlgorithm;
	public static final int VIGENERE = 0;
	public static final int VIGENERE_EXTENDED = 1;
	
	private Checkbox checkAutokey;
	
	private Label labelKey;
	private TextField fieldKey;
	
	private Button buttonAction; // Button to encrypt/decrypt
	private Button buttonImport;
	private Button buttonExport;

	@SuppressWarnings("unused")
	private JFileChooser chooser; // To display file browsing window
	
	private Label labelInputtext;
	private Choice choiceViewMode2;
	private TextArea textInput;
	
	private Label labelOutputtext;
	private Choice choiceViewMode;
	private TextArea textOutput;
	public static final int NO_FORMAT = 0;
	public static final int NO_SPACE = 1;
	public static final int GROUP_OF_FIVE = 2;
	
	public Main() {
		input = null;
		output = null;
		
		labelMode = new Label("Mode");
		
		choiceMode = new Choice();
		choiceMode.insert("Encryption", ENCRYPTION_MODE);
		choiceMode.insert("Decryption", DECRYPTION_MODE);
		choiceMode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int choice = choiceMode.getSelectedIndex();
				if (choice == ENCRYPTION_MODE) {
					buttonAction.setLabel("Encrypt");
					labelInputtext.setText("Plain text");
					labelOutputtext.setText("Cipher text");
					textInput.setText(textOutput.getText());
					textOutput.setText("");
					choiceViewMode.setEnabled(true);
				} else if (choice == DECRYPTION_MODE) {
					buttonAction.setLabel("Decrypt");
					labelInputtext.setText("Cipher text");
					labelOutputtext.setText("Plain text");
					textInput.setText(textOutput.getText());
					textOutput.setText("");
					choiceViewMode.setEnabled(false);
				}
			}
		});
		
		labelAlgorithm = new Label("Algorithm");
		
		choiceAlgorithm = new Choice();
		choiceAlgorithm.insert("Vigenere Cipher", VIGENERE);
		choiceAlgorithm.insert("Extended Vigenere Cipher", VIGENERE_EXTENDED);
		choiceAlgorithm.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int choice = choiceAlgorithm.getSelectedIndex();
				if (choice == VIGENERE) {
					choiceViewMode.insert("No space", NO_SPACE);
				} else if (choice == VIGENERE_EXTENDED) {
					choiceViewMode.remove(NO_SPACE);
				}
			}
		});
		
		labelKey = new Label("Key");
		fieldKey = new TextField();
		fieldKey.addTextListener(new TextListener() {
			public void textValueChanged(TextEvent e) {
				String key = fieldKey.getText();
				if (key.length() > 25) { // Ensure that key's length is at most 25 char
					fieldKey.setText(key.substring(0, 25));
					JOptionPane.showMessageDialog(null, "Key is at most 25 characters length.");
				}
				
				int choice = choiceAlgorithm.getSelectedIndex();
				if (choice == VIGENERE) { // Ensure that key is all lowercase
					if (key.length() > 0) {
						char lastChar = key.charAt(key.length() - 1);
						if (lastChar < 'a' || lastChar > 'z') {
							if (key.length() == 1) {
								fieldKey.setText("");
							} else {
								fieldKey.setText(key.substring(0, key.length() - 1));
							}
							JOptionPane.showMessageDialog(null, "Key only accepts lowercase letters.");
						}
					}
				}
				
				fieldKey.setCaretPosition(key.length());
			}
		});
		
		checkAutokey = new Checkbox("Autokey");

		buttonAction = new Button("Encrypt");
		buttonAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fieldKey.getText().isEmpty()) { // Ensure key is provided
					JOptionPane.showMessageDialog(null, "The key is empty.");
				} else if (textInput.getText().isEmpty()) { // Ensure message is provided
					JOptionPane.showMessageDialog(null, "The input text is empty.");
				} else {
					if (input == null) { // Read the message
						String s = textInput.getText();
						input = new byte[s.length()];
						for (int i = 0; i < s.length(); i++) {
							input[i] = (byte) s.charAt(i);
						}
					}
					
					if (choiceAlgorithm.getSelectedIndex() == VIGENERE) {
						if (choiceMode.getSelectedIndex() == ENCRYPTION_MODE) {
							encryptVigenere();
						} else if (choiceMode.getSelectedIndex() == DECRYPTION_MODE) {
							decryptVigenere();
						}
					} else if (choiceAlgorithm.getSelectedIndex() == VIGENERE_EXTENDED) {
						if (choiceMode.getSelectedIndex() == ENCRYPTION_MODE) {
							encryptVigenereExtended();
						} else if (choiceMode.getSelectedIndex() == DECRYPTION_MODE) {
							decryptVigenereExtended();
						}
					}
				}
			}
		});
		
		buttonImport = new Button("Import");
		buttonImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
	            chooser.setCurrentDirectory(new java.io.File("../res"));
	            chooser.setDialogTitle("Choose a file to proceed");

	            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	            	Path path = Paths.get(chooser.getSelectedFile().getAbsolutePath());
	            	try {
	            		boolean isOnlyCapital = true;
						input = Files.readAllBytes(path);
						String s = new String(input, "ISO-8859-1");
						String s2 = "";
						for (int i = 0; i < s.length(); i++) { // Read imported file
							if (s.charAt(i) < ' ') {
								isOnlyCapital = false;
								if (s.charAt(i) == LF) {
									s2 += System.lineSeparator();
								} else if (s.charAt(i) == CR) {
									if (s.charAt(i+1) == LF) {
										i++;
									}
									s2 += System.lineSeparator();
								} else {
									s2 += ' ';
								}
							} else {
								if ((s.charAt(i) < 'A' || s.charAt(i) > 'Z') && s.charAt(i) != ' ') {
									isOnlyCapital = false;
								}
								s2 += s.charAt(i);
							}
						}
						
						if (choiceAlgorithm.getSelectedIndex() == VIGENERE) {
							if (isOnlyCapital) { // Ensure the message is all uppercase
								filename = chooser.getName(chooser.getSelectedFile());
								textInput.setText(s2);
							} else {
								JOptionPane.showMessageDialog(null, "Input file has character other than capital letter and space.");
							}
						} else if (choiceAlgorithm.getSelectedIndex() == VIGENERE_EXTENDED) {
							filename = chooser.getName(chooser.getSelectedFile());
							textInput.setText(s2);
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
	            } else {
	                System.out.println("No Selection.");
	            }
			}
		});
		
		buttonExport = new Button("Export");
		buttonExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!textOutput.getText().isEmpty()) {
					if (filename == null) { // Provide filename with current time
						filename = new SimpleDateFormat("yyMMdd_HHmmss").format(Calendar.getInstance().getTime());
						filename += ".txt";
					}
					
					FileOutputStream file = null;
					
					try {
						file = new FileOutputStream("C://Development//workspace//Kripton//res//export//" + filename);
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
					
					try {
						if (choiceAlgorithm.getSelectedIndex() == VIGENERE) {
							file.write(textOutput.getText().getBytes());
						} else if (choiceAlgorithm.getSelectedIndex() == VIGENERE_EXTENDED) {
							file.write(output);
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					} finally {
						try {
							file.close();
							JOptionPane.showMessageDialog(null, "File is exported successfully.");
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					
					filename = null;
				} else {
					JOptionPane.showMessageDialog(null, "No file to export.");
				}
			}
		});
		
		labelInputtext = new Label("Plain text");
		
		textInput = new TextArea();
		textInput.addTextListener(new TextListener() {
			public void textValueChanged(TextEvent e) {
				String text = textInput.getText();
				
				int choice = choiceAlgorithm.getSelectedIndex();
				if (choice == VIGENERE) { // Ensure the input is all uppercase or space
					if (text.length() > 0) {
						char lastChar = text.charAt(text.length() - 1);
						if ((lastChar < 'A' || lastChar > 'Z') && lastChar != ' ') {
							if (text.length() == 1) {
								textInput.setText("");
							} else {
								textInput.setText(text.substring(0, text.length() - 1));
							}
							JOptionPane.showMessageDialog(null, "Vigenere algorithm only accepts capital letters and space.");
						}
					}
				}
				
				textInput.setCaretPosition(text.length());
			}
		});
		
		choiceViewMode2 = new Choice();
		choiceViewMode2.setVisible(false);
		choiceViewMode2.setEnabled(false);
		choiceViewMode2.insert("No space", NO_SPACE);
		choiceViewMode2.insert("Group of five", GROUP_OF_FIVE);
		
		labelOutputtext = new Label("Cipher text");
		
		textOutput = new TextArea();
		textOutput.setEditable(false);
		
		choiceViewMode = new Choice();
		choiceViewMode.insert("No format", NO_FORMAT);
		choiceViewMode.insert("No space", NO_SPACE);
		choiceViewMode.insert("Group of five", GROUP_OF_FIVE);
		choiceViewMode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int choice = choiceViewMode.getSelectedIndex();
				if (choice == NO_FORMAT) {
					textOutput.setText(new String(output));
				} else if (choice == NO_SPACE) {
					if (choiceAlgorithm.getSelectedIndex() == VIGENERE) {
						textOutput.setText(new String(output).replaceAll("\\s+", ""));
					} else if (choiceAlgorithm.getSelectedIndex() == VIGENERE_EXTENDED) {
						String s = new String(output);
						
						StringBuilder sb = new StringBuilder(s);
						for (int i = sb.length() - 1; i > 4; i--) {
							if (i % 5 == 0) {
								sb.insert(i, ' ');
							}
						}
						
						textOutput.setText(sb.toString());
					}
				} else if (choice == GROUP_OF_FIVE) {
					String s = new String(output).replaceAll("\\s+", "");
					
					StringBuilder sb = new StringBuilder(s);
					for (int i = sb.length() - 1; i > 4; i--) {
						if (i % 5 == 0) {
							sb.insert(i, ' ');
						}
					}
					
					textOutput.setText(sb.toString());
				}
			}
		});
		
		groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(textOutput, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(labelInputtext, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 231, Short.MAX_VALUE)
							.addComponent(choiceViewMode2, GroupLayout.PREFERRED_SIZE, 167, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(labelOutputtext, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 223, Short.MAX_VALUE)
							.addComponent(choiceViewMode, GroupLayout.PREFERRED_SIZE, 167, GroupLayout.PREFERRED_SIZE))
						.addComponent(textInput, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(labelKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(labelAlgorithm, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(labelMode, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(0, 8, Short.MAX_VALUE)
									.addComponent(buttonImport, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
									.addGap(14)
									.addComponent(buttonExport, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(buttonAction, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE))
								.addComponent(choiceAlgorithm, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(choiceMode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(fieldKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(checkAutokey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
					.addGap(12))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(12)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(labelMode, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						.addComponent(choiceMode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(12)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(choiceAlgorithm, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelAlgorithm, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
					.addGap(12)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(fieldKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelKey, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
					.addGap(12)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(checkAutokey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(12)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(buttonAction, 0, 0, Short.MAX_VALUE)
						.addComponent(buttonExport, 0, 0, Short.MAX_VALUE)
						.addComponent(buttonImport, GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE))
					.addGap(12)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(choiceViewMode2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelInputtext, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
					.addGap(10)
					.addComponent(textInput, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(labelOutputtext, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addComponent(choiceViewMode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(10)
					.addComponent(textOutput, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(21))
		);
		
		setLayout(groupLayout);
	}

	public void init() {
		// System.out.println("Init()");
	}
	
	public void stop() {
		// System.out.println("Stop()");
	}
	
	public void paint(Graphics g) {
		// System.out.println("Paint()");
	}
	
	public void destroy() {
		// System.out.println("Destroy()");
	}

	private void encryptVigenere() { // Vigenere encryption code
		String in = new String(input).replaceAll("\\s+", "");
		
		String key = fieldKey.getText().toUpperCase();
		if (checkAutokey.getState()) { // Append key with text until the length is equal
			int i = 0;
			while (key.length() < in.length()) {
				key += in.charAt(i);
			}
		}

		String out = "";
		if (checkAutokey.getState()) { // Encryption code
			for (int i = 0; i < in.length(); i++) {
				out += (char) (((in.charAt(i) + key.charAt(i)) % 26) + 'A');
			}
		} else {
			for (int i = 0; i < in.length(); i++) {
				out += (char) (((in.charAt(i) + key.charAt(i % key.length())) % 26) + 'A');
			}
		}
		
		int spaceCount = 0;
		output = null;
		output = new byte[input.length];
		for (int i = 0; i < input.length; i++) { // Restore space char to the output
			if (input[i] == ' ') {
				output[i] = ' ';
				spaceCount++;
			} else {
				output[i] = (byte) (out.charAt(i - spaceCount));
			}
		}

		textOutput.setText(new String(output));
		input = null;
	}
	
	private void decryptVigenere() { // Vigenere decryption code
		String in = new String(input).replaceAll("\\s+", "");
		
		String key = fieldKey.getText().toUpperCase();
		
		String out = "";
		if (checkAutokey.getState()) { // Decryption code
			for (int i = 0; i < in.length(); i++) {
				char ch = (char) (((in.charAt(i) - key.charAt(i) + 26) % 26) + 'A');
				out += ch;
				if (key.length() < in.length()) { // Append key with text
					key += ch;
				}
			}
		} else {
			for (int i = 0; i < in.length(); i++) {
				out += (char) (((in.charAt(i) - key.charAt(i % key.length()) + 26) % 26) + 'A');
			}
		}
		
		int spaceCount = 0;
		output = null;
		output = new byte[input.length];
		for (int i = 0; i < input.length; i++) { // Restore space char
			if (input[i] == ' ') {
				output[i] = ' ';
				spaceCount++;
			} else {
				output[i] = (byte) (out.charAt(i - spaceCount));
			}
		}

		textOutput.setText(new String(output));
		input = null;
	}

	private void encryptVigenereExtended() { // Extended Vigenere Algorithm
		String key = fieldKey.getText();
		if (checkAutokey.getState()) { // Append key with text
			int i = 0;
			while (key.length() < input.length) {
				key += (char) input[i];
				i++;
			}
		}

		output = null;
		output = new byte[input.length];
		
		if (checkAutokey.getState()) { // Encryption code
			for (int i = 0; i < input.length; i++) {
				output[i] = (byte) ((input[i] + key.charAt(i)) % 256);
			}
		} else {
			for (int i = 0; i < input.length; i++) {
				output[i] = (byte) ((input[i] + key.charAt(i % key.length())) % 256);
			}
		}

		textOutput.setText(new String(output));
		input = null;
	}
	
	private void decryptVigenereExtended() { // Extended Vigenere Algorithm
		String key = fieldKey.getText();
		
		output = null;
		output = new byte[input.length];
		
		if (checkAutokey.getState()) { // Decryption code
			for (int i = 0; i < input.length; i++) {
				output[i] = (byte) ((input[i] - key.charAt(i) + 256) % 256);
				if (key.length() < input.length) {
					key += (char) output[i];
				}
			}
		} else {
			for (int i = 0; i < input.length; i++) {
				output[i] = (byte) ((input[i] - key.charAt(i % key.length()) + 256) % 256);
			}
		}

		textOutput.setText(new String(output));
		input = null;
	}

}
