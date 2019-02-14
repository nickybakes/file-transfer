import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.image.*;
import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Map;
import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.Border;

public class Window extends JPanel implements ActionListener{
	private JFrame frame;
	private JPanel contentPane;
	private JLabel title = new JLabel("File Transfer Tool");
	private JLabel author = new JLabel("by Nick Baker");
	private JLabel subtitle = new JLabel("This tool will copy files to a new destination, and sort them into folders based on year/month.");
	private JLabel subtitle2 = new JLabel("Simply add the folders you want to copy files from, and then select the folder you want to copy them to. Then click Start Process!");
	private JLabel subtitle3 = new JLabel("NOTE: If you quit the process, some files might have started copying, but not all of their data was copied. Be sure to clear out the export folder afterwards.");
	private JLabel instructions1 = new JLabel("Folders to copy files from:");
	private JLabel instructions2 = new JLabel("Folder to copy files to:");
	private JLabel empty = new JLabel(" ");
	private JPanel importDirPanel;
	private JButton addDir;
	private JTextField exportDir;
	private JButton exportBrowse;
	private JCheckBox addSubfolders;
	private JButton startButton;
	private final JFileChooser fc = new JFileChooser();
	private ArrayList<Directory> importDirectories = new ArrayList();
	private ArrayList<Integer> indexes = new ArrayList();
	private SwingWorker<Void, String> worker;
	private Thread filecopyingThread;

	public Window() {
		start();
	}

	public void start() {
		empty.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		frame = new JFrame("Photo Transfer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		contentPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		
		title.setFont(new Font(title.getFont().getName(), Font.PLAIN, 20));
		Font font = title.getFont();
		Map attributes = font.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		title.setFont(font.deriveFont(attributes));
		title.setBorder(BorderFactory.createEmptyBorder(10, 0, 3, 0));
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		contentPane.add(title);
		
		author.setFont(new Font(author.getFont().getName(), Font.PLAIN, 14));
		author.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		author.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		contentPane.add(author);
		
		subtitle.setFont(new Font(subtitle.getFont().getName(), Font.PLAIN, 13));
		subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		subtitle.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		contentPane.add(subtitle);
		
		subtitle2.setFont(new Font(subtitle2.getFont().getName(), Font.PLAIN, 13));
		subtitle2.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		subtitle2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		contentPane.add(subtitle2);
		
		subtitle3.setFont(new Font(subtitle3.getFont().getName(), Font.PLAIN, 13));
		subtitle3.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		subtitle3.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		contentPane.add(subtitle3);

		instructions1.setFont(new Font(instructions1.getFont().getName(), Font.PLAIN, 13));
		instructions1.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		instructions1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		contentPane.add(instructions1);

		frame.setContentPane(contentPane);

		importDirPanel = new JPanel();
		//importDirPanel.setLayout(new GridLayout(2, importDirectories.size(), 10, 5));
		importDirPanel.setLayout(new BoxLayout(importDirPanel, BoxLayout.PAGE_AXIS));
		importDirPanel.setBorder(BorderFactory.createTitledBorder("Added Folders: " + importDirectories.size()));
		importDirPanel.add(Box.createRigidArea(new Dimension(500, 0)));
		frame.add(importDirPanel);

		addDir = new JButton("Add folder");
		addDir.setFont(new Font(addDir.getFont().getName(), Font.PLAIN, 13));
		addDir.setAlignmentX(JButton.CENTER_ALIGNMENT);
		addDir.addActionListener(this);
		contentPane.add(addDir);

		addSubfolders = new JCheckBox("Automatically add all folders within the folder you add (if any are found)");
		addSubfolders.setFont(new Font(addSubfolders.getFont().getName(), Font.PLAIN, 13));
		addSubfolders.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		addSubfolders.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		addSubfolders.setSelected(true);
		contentPane.add(addSubfolders);

		instructions2.setFont(new Font(instructions2.getFont().getName(), Font.PLAIN, 13));
		instructions2.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		instructions2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		contentPane.add(instructions2);

		exportDir = new JTextField(5);
		exportDir.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
		contentPane.add(exportDir);

		exportBrowse = new JButton("Select folder");
		exportBrowse.setFont(new Font(exportBrowse.getFont().getName(), Font.PLAIN, 13));
		exportBrowse.setAlignmentX(JButton.CENTER_ALIGNMENT);
		exportBrowse.addActionListener(this);
		contentPane.add(exportBrowse);

		contentPane.add(empty);

		startButton = new JButton("Start process!");
		startButton.setFont(new Font(startButton.getFont().getName(), Font.BOLD, 13));
		//startButton.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		startButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		startButton.addActionListener(this);
		contentPane.add(startButton);

		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		frame.pack();
		frame.setVisible(true);

	}

	public void checkForErrors() throws Exception {
		if(importDirectories.size() == 0) {
			throw new Exception("Please add folders to import photos from.");
		}
		for(Directory d : importDirectories) {
			File importFolder = new File(d.textField().getText());
			if(!importFolder.isDirectory()) {
				throw new Exception("Import folder: \"" + d.textField().getText() + "\" does not exist");
			}
			if(d.textField().getText().equals(exportDir.getText())) {
				throw new Exception("An import folder cannot be the same as the export folder!");
			}
		}
		File exportFolder = new File(exportDir.getText());
		if(!exportFolder.isDirectory()) {
			throw new Exception("Export folder does not exist!");
		}
	}

	public int countFiles() throws Exception {
		int numOfFiles = 0;
		for(Directory d : importDirectories) {
			File importFolder = new File(d.textField().getText());
			File[] listOfFiles = importFolder.listFiles();
			ArrayList<File> actualFiles = new ArrayList();
			for(File f : listOfFiles) {
				if(f.isFile()) {
					actualFiles.add(f);
				}
			}
			numOfFiles = numOfFiles + actualFiles.size();
		}
		if(numOfFiles == 0) {
			throw new Exception("Import folder does not contain any files!");
		}
		return numOfFiles;
	}

	public void copyFiles(int numOfFiles) {

	}

	public void addDirectory(String directory) {
		boolean addDirectory = true;
		for(Directory d : importDirectories) {
			if(directory.equals(d.textField().getText())) {
				addDirectory = false;
				JOptionPane.showMessageDialog(frame, "This import folder has already been added!", "Error", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		if(addDirectory) {
			if(importDirectories.size() < 14) {
				importDirectories.add(new Directory(directory, importDirectories.size()));
				indexes.add(importDirectories.get(importDirectories.size()-1).id());
				importDirPanel.add(importDirectories.get(importDirectories.size() -1).textField());
				importDirPanel.add(importDirectories.get(importDirectories.size() -1).removeButton());
				importDirPanel.setBorder(BorderFactory.createTitledBorder("Added Folders: " + importDirectories.size()));
				if(addSubfolders.isSelected()) {
					File importFolder = new File(directory);
					File[] listOfFiles = importFolder.listFiles();
					ArrayList<File> subFolders = new ArrayList();
					for(File f : listOfFiles) {
						if(f.isDirectory()){
							subFolders.add(f);
						}
					}
					for(File f : subFolders) {
						addDirectory(f.getPath());
					}
				}
				if(importDirectories.size() >= 14) {
					addDir.setEnabled(false);
				}
				frame.pack();
			}
		}
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == exportBrowse) {
			int returnVal = fc.showOpenDialog(Window.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				exportDir.setText( fc.getSelectedFile().getPath());
			}
		}else if (event.getSource() == addDir) {
			int returnVal = fc.showOpenDialog(Window.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				this.addDirectory(fc.getSelectedFile().getPath());
			}
		}else if (event.getSource() == startButton) {
			try {
				this.checkForErrors();
				int num = this.countFiles();
				Thread filecopyingThread = new Thread(new FileCopier(num, importDirectories, exportDir.getText()));
				filecopyingThread.start();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	class FileCopier implements ActionListener, Runnable{
		private int numOfFiles;
		private int currentFile;
		private JFrame frame2;
		private JPanel contentPane;
		private JLabel status;
		private JProgressBar progressBar;
		private JButton cancelButton;
		private JButton okButton;
		private ArrayList<Directory> theseDirectories;
		private String exportFolder;
		private boolean finished = false;
		public FileCopier(int num, ArrayList<Directory> newDirectories, String exp) {
			numOfFiles = num;
			theseDirectories = newDirectories;
			exportFolder = exp;
			currentFile = 1;
			frame2 = new JFrame("Copying files...");
			frame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			contentPane = new JPanel();
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
			contentPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
			contentPane.add(Box.createRigidArea(new Dimension(500, 0)));

			status = new JLabel("Copying 0/" + numOfFiles + "...");
			status.setFont(new Font(status.getFont().getName(), Font.PLAIN, 13));
			status.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
			status.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			contentPane.add(status);

			progressBar = new JProgressBar();
			progressBar.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
			progressBar.setMinimum(0);
			progressBar.setMaximum(numOfFiles);
			contentPane.add(progressBar);

			cancelButton = new JButton("Quit Process");
			cancelButton.setFont(new Font(cancelButton.getFont().getName(), Font.PLAIN, 13));
			cancelButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
			cancelButton.addActionListener(this);
			contentPane.add(cancelButton);

			frame2.setContentPane(contentPane);
			frame2.pack();
			frame2.setVisible(true);

		}

		public void actionPerformed(ActionEvent event) {
			if (event.getSource() == cancelButton) {
				System.exit(0);
			}
		}

		private void updateStatus() {
			if(finished) {
				status.setText("Finished copying "+numOfFiles+"/" + numOfFiles + " files.");
				frame2.repaint();
				frame2.pack();
			}else {
				status.setText("Copying "+currentFile+"/" + numOfFiles + " files...");
				progressBar.setValue(currentFile);
				frame2.repaint();
				frame2.pack();
			}
		}

		private void copyFile(File source, File dest){
			try {
				Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
				BasicFileAttributes metadata = Files.readAttributes(source.toPath(), BasicFileAttributes.class);
				BasicFileAttributeView destinationMetaData = Files.getFileAttributeView(dest.toPath(), BasicFileAttributeView.class);
				destinationMetaData.setTimes(metadata.lastModifiedTime(), metadata.lastAccessTime(), metadata.creationTime());
				//System.out.println("" + currentFile + ": " + metadata.lastModifiedTime());
			}catch(Exception e) {}
		}

		public void run() {
			for(Directory d : theseDirectories) {
				File importFolder = new File(d.textField().getText());
				File[] listOfFiles = importFolder.listFiles();
				ArrayList<File> actualFiles = new ArrayList();
				for(File f : listOfFiles) {
					if(f.isFile()) {
						actualFiles.add(f);
					}
				}
				for(File f: actualFiles) {
					try {
						BasicFileAttributes metadata = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
						if(exportFolder.substring(exportFolder.length()-1).equals("\\")) {
							File destination = new File(exportFolder + metadata.lastModifiedTime().toString().substring(0, 7));
							if(!Files.isDirectory(destination.toPath(), LinkOption.NOFOLLOW_LINKS)) {
								Files.createDirectory(destination.toPath());
							}
							worker = new SwingWorker<Void, String>(){
								protected Void doInBackground() throws Exception {
									copyFile(f, new File(destination.toString() + "\\" + f.getName()));
									return null;
								}
								protected void done() {
									currentFile++;
									updateStatus();
									if(currentFile >= numOfFiles && !finished) {
										finished = true;
										status.setText("Finished copying "+numOfFiles+"/" + numOfFiles + " files.");
										contentPane.remove(cancelButton);
										okButton = new JButton("OK");
										okButton.setFont(new Font(okButton.getFont().getName(), Font.PLAIN, 13));
										okButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
										okButton.addActionListener(new ActionListener(){
											public void actionPerformed(ActionEvent e){frame2.dispose();}});
										contentPane.add(okButton);
										frame2.repaint();
										frame2.pack();
									}
								}
							};
							worker.execute();
						}else {
							File destination = new File(exportFolder + "\\" + metadata.lastModifiedTime().toString().substring(0, 7));
							if(!Files.isDirectory(destination.toPath(), LinkOption.NOFOLLOW_LINKS)) {
								Files.createDirectory(destination.toPath());
							}
							worker = new SwingWorker<Void, String>(){
								protected Void doInBackground() throws Exception {
									copyFile(f, new File(destination.toString() + "\\" + f.getName()));
									return null;
								}
								protected void done() {
									currentFile++;
									updateStatus();
									if(currentFile >= numOfFiles && !finished) {
										finished = true;
										status.setText("Finished copying "+currentFile+"/" + numOfFiles + " files.");
										contentPane.remove(cancelButton);
										okButton = new JButton("OK");
										okButton.setFont(new Font(okButton.getFont().getName(), Font.PLAIN, 13));
										okButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
										okButton.addActionListener(new ActionListener(){
											public void actionPerformed(ActionEvent e){frame2.dispose();}});
										contentPane.add(okButton);
										frame2.repaint();
										frame2.pack();
									}
								}
							};
							worker.execute();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	class Directory {
		private JButton removeButton = new JButton("Remove");
		private JTextField textField;
		private String dir;
		private int ID;

		public Directory(String text, int i) {
			textField = new JTextField(text);
			textField.addKeyListener(new KeyListener(){
				public void keyPressed(KeyEvent arg0) {} public void keyReleased(KeyEvent arg0) {}
				public void keyTyped(KeyEvent arg0) {
					dir = textField.getText();
				}
			});
			dir = text;
			ID = i;
			removeButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
			removeButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					addDir.setEnabled(true);
					importDirPanel.remove(textField);
					importDirPanel.remove(removeButton);
					for(int x = 0; x < importDirectories.size(); x++) {
						if(indexes.get(x) == ID) {
							importDirectories.remove(x);
							indexes.remove(x);
						}
					}
					importDirPanel.setBorder(BorderFactory.createTitledBorder("Added Folders: " + importDirectories.size()));
					frame.pack();
				}
			});
		}

		public JTextField textField() {
			return textField;
		}

		public JButton removeButton() {
			return removeButton;
		}

		public int id() {
			return ID;
		}

	}

	private static void runGUI() {
		//JFrame.setDefaultLookAndFeelDecorated(true);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (UnsupportedLookAndFeelException e) {
		}
		new Window();
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				runGUI();
			}
		});
	}
}
