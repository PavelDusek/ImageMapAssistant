package imagemapassistant;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Pavel Dušek
 * licence: public domain
 * for any questions mail me: pavel.dusek [at] gmail.com
 */
public class ImageMapAssistant extends JFrame implements ActionListener {
    private JFileChooser fileChooser;
    private JCanvas canvas;
    private JScrollPane imageScrollPane, outputScrollPane;
    private JPanel toolkitPanel;
    private JButton magnifierPlusButton, magnifierMinusButton,
            rectangleButton, circleButton, polygonButton, finishedButton, doneButton,
            openTagButton, closeTagButton;
    private JTextArea outputArea, doneArea;
    private Cursor zoomInCursor, zoomOutCursor;
    private Toolkit toolkit;
    private JMenuBar menuBar;
    private JMenu souborMenu, upravitMenu, nastaveniMenu, barvaMenu;
    private JMenuItem otevritMenu, otevritUrlMenu, copyMenuItem;
    private JRadioButtonMenuItem imapRBMItem, xmlRBMItem;
    private PressButton downButton, upButton, leftButton, rightButton;
    boolean formatXml = false;
    boolean copyOtherArea = false;
    private int lastRBMIIndex;
    Area activeAreaObject;
    List<Area> areaObjectsList = new ArrayList<Area>();
    List<Color> colors = new ArrayList<Color>();
    List<JRadioButtonMenuItem> colorRBMItems = new ArrayList<JRadioButtonMenuItem>();
    Color color = Color.GREEN;

    public ImageMapAssistant() {
        //Setting all the Swing components:

        //properties of the frame
        super("ImageMapAssistant");
        //try different look&feels if they are available according to this preference (from least favorite to most favorite):
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("com.apple.mrj.swing.MacLookAndFeel");
        } catch (ClassNotFoundException excp1) {
            excp1.printStackTrace();
        } catch (InstantiationException excp2) {
            excp2.printStackTrace();
        } catch (IllegalAccessException excp3) {
            excp3.printStackTrace();
        } catch (UnsupportedLookAndFeelException excp4) {
            excp4.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(this);
        setIconImage(createImageIcon("images/map.png", null).getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        getContentPane().setLayout(new BorderLayout());

        //custom cursors
        toolkit = Toolkit.getDefaultToolkit();
        try {
            zoomInCursor = toolkit.createCustomCursor(
                    createImageIcon("images/magPlus.png", "").getImage(),
                    new Point(0,0),
                    "zoom in");
            zoomOutCursor = toolkit.createCustomCursor(
                    createImageIcon("images/magMinus.png", "").getImage(),
                    new Point(0,0),
                    "zoom out");
        } catch (IndexOutOfBoundsException ioob) {
            zoomOutCursor = Cursor.getDefaultCursor();
            zoomInCursor = Cursor.getDefaultCursor();
            ioob.printStackTrace();
        } catch (HeadlessException hl) {
            hl.printStackTrace();
        }

        //menu
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        souborMenu = new JMenu("Soubor");
        otevritMenu = new JMenuItem("Otevřít");
        otevritMenu.setActionCommand("loadImage");
        otevritMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        otevritMenu.addActionListener(this);
        souborMenu.add(otevritMenu);
        otevritUrlMenu = new JMenuItem("Otevřít z url");
        otevritUrlMenu.setActionCommand("loadUrl");
        otevritUrlMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK));
        otevritUrlMenu.addActionListener(this);
        souborMenu.add(otevritUrlMenu);
        menuBar.add(souborMenu);
        upravitMenu = new JMenu("Upravit");
        copyMenuItem = new JMenuItem("Kopírovat");
        copyMenuItem.addActionListener(this);
        copyMenuItem.setActionCommand("copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
        upravitMenu.add(copyMenuItem);
        menuBar.add(upravitMenu);

        //vyber barev
        nastaveniMenu = new JMenu("Nastavení");
        barvaMenu = new JMenu("Barva");
        Color[] colorsArray = {Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY,
                          Color.GRAY, Color.GREEN, Color.LIGHT_GRAY, Color.MAGENTA,
                          Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW
        };
        String[] colorNamesArray = {"Black", "Blue", "Cyan", "Dark gray", "Gray", "Green",
                               "Light gray", "Magenta", "Orange", "Pink",
                               "Red", "White", "Yellow"
        };
        List<String> colorNames = new ArrayList<String>();
        Collections.addAll(colors, colorsArray);
        Collections.addAll(colorNames, colorNamesArray);
        JRadioButtonMenuItem rBMItem;
        for (Iterator<String> it = colorNames.iterator(); it.hasNext(); ) {
            rBMItem = new JRadioButtonMenuItem(it.next());
            rBMItem.setActionCommand("color");
            rBMItem.addActionListener(this);
            barvaMenu.add(rBMItem);
        }
        lastRBMIIndex = 5;

        JMenu formatMenu = new JMenu("Formát");
        imapRBMItem = new JRadioButtonMenuItem("Formát Image Map");
        imapRBMItem.addActionListener(this);
        imapRBMItem.setActionCommand("setImap");
        imapRBMItem.setSelected(true);
        xmlRBMItem = new JRadioButtonMenuItem("Formát xml");
        xmlRBMItem.addActionListener(this);
        xmlRBMItem.setActionCommand("setXml");
        menuBar.add(nastaveniMenu);
        nastaveniMenu.add(barvaMenu);
        nastaveniMenu.add(formatMenu);
        formatMenu.add(imapRBMItem);
        formatMenu.add(xmlRBMItem);

        //other components
        canvas = new JCanvas(null, this);

        fileChooser = new JFileChooser();
        toolkitPanel = new JPanel(new FlowLayout());

        rectangleButton = new JButton(createImageIcon("images/rectangle.png", "čtverec"));
        circleButton = new JButton(createImageIcon("images/circle.png", "kruh"));
        polygonButton = new JButton(createImageIcon("images/polygon.png", "polygon"));
        finishedButton = new JButton(createImageIcon("images/finished.png", "konec"));
        magnifierPlusButton = new JButton(createImageIcon("images/magPlus.png", "zoom +"));
        magnifierMinusButton = new JButton(createImageIcon("images/magMinus.png", "zoom -"));
        leftButton = new PressButton(createImageIcon("images/leftArrow.png", "doleva"), canvas, -5, 0);
        downButton = new PressButton(createImageIcon("images/downArrow.png", "dolu"), canvas, 0, 5);
        upButton = new PressButton(createImageIcon("images/upArrow.png", "nahoru"), canvas, 0, -5);
        rightButton = new PressButton(createImageIcon("images/rightArrow.png", "doprava"), canvas, 5, 0);
        doneButton = new JButton(createImageIcon("images/done.png", "hotovo"));
        openTagButton = new JButton("<…>");
        closeTagButton = new JButton("</…>");

        rectangleButton.setActionCommand("rectangle");
        circleButton.setActionCommand("circle");
        polygonButton.setActionCommand("polygon");
        finishedButton.setActionCommand("finished");
        magnifierPlusButton.setActionCommand("zoomIn");
        magnifierMinusButton.setActionCommand("zoomOut");
        doneButton.setActionCommand("done");
        rectangleButton.addActionListener(this);
        circleButton.addActionListener(this);
        polygonButton.addActionListener(this);
        finishedButton.addActionListener(this);
        magnifierPlusButton.addActionListener(this);
        magnifierMinusButton.addActionListener(this);
        doneButton.addActionListener(this);
        openTagButton.setVisible(false);
        openTagButton.setActionCommand("openTag");
        openTagButton.addActionListener(this);
        closeTagButton.setVisible(false);
        closeTagButton.setActionCommand("closeTag");
        closeTagButton.addActionListener(this);
        finishedButton.setEnabled(false);
        toolkitPanel.add(rectangleButton);
        toolkitPanel.add(circleButton);
        toolkitPanel.add(polygonButton);
        toolkitPanel.add(finishedButton);
        toolkitPanel.add(doneButton);
        toolkitPanel.add(magnifierPlusButton);
        toolkitPanel.add(magnifierMinusButton);
        toolkitPanel.add(leftButton);
        toolkitPanel.add(downButton);
        toolkitPanel.add(upButton);
        toolkitPanel.add(rightButton);
        toolkitPanel.add(openTagButton);
        toolkitPanel.add(closeTagButton);
        add(toolkitPanel, BorderLayout.PAGE_START);
        //add(topPanel, BorderLayout.PAGE_START);
        imageScrollPane = new JScrollPane(canvas);
        add(imageScrollPane, BorderLayout.CENTER);

        outputArea = new JTextArea(5, 60);
        outputArea.setText("<imagemap>\nSoubor:Jméno.přípona | thumb | velikost px| align | text");
        outputScrollPane = new JScrollPane(outputArea);
        add(outputScrollPane, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        ImageMapAssistant mainFrame = new ImageMapAssistant();
        mainFrame.setSize(Constants.getScreenWidth(), Constants.getScreenHeight());
        mainFrame.setVisible(true);
    }

    public void addPoint(int x, int y) {
        //JOptionPane.showMessageDialog(this, String.valueOf(x) + "," + String.valueOf(y), "Coordinates", JOptionPane.INFORMATION_MESSAGE);
        activeAreaObject.addPoint(new Dimension(x, y));
        copyOtherArea = false;
        if (imapRBMItem.isSelected()) {
            formatXml = false;
        } else {
            formatXml = true;
        }
        if (activeAreaObject.isFinished()) {
            outputArea.setText(outputArea.getText() + "\n" + activeAreaObject.getText(formatXml));
            areaObjectsList.add(activeAreaObject);
            enableButtons();
            finishedButton.setEnabled(false);
            canvas.setCursor(Cursor.getDefaultCursor());
            canvas.clearPoints();
            canvas.repaint();
        }
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected ImageIcon createImageIcon(String path,
                                           String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    private void showResult() {
        copyOtherArea = true;
        JFrame resultFrame = new JFrame("ImageMap kód");
        resultFrame.setJMenuBar(menuBar);
        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        doneArea = new JTextArea();
        doneArea.setText(outputArea.getText());
        JScrollPane scrollPane = new JScrollPane(doneArea);
        resultFrame.add(scrollPane);
        resultFrame.pack();
        resultFrame.setVisible(true);
    }

    private void disableButtons() {
        rectangleButton.setEnabled(false);
        circleButton.setEnabled(false);
        polygonButton.setEnabled(false);
        finishedButton.setEnabled(false);
        magnifierPlusButton.setEnabled(false);
        magnifierMinusButton.setEnabled(false);
        doneButton.setEnabled(false);
    }
    private void enableButtons() {
        rectangleButton.setEnabled(true);
        circleButton.setEnabled(true);
        polygonButton.setEnabled(true);
        finishedButton.setEnabled(true);
        magnifierPlusButton.setEnabled(true);
        magnifierMinusButton.setEnabled(true);
        doneButton.setEnabled(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("loadImage")) {
            barvaMenu.getItem(lastRBMIIndex).setSelected(true);
            int returnVal = fileChooser.showOpenDialog(this);
            String path = "";
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                path = file.getPath();
            }
            try {
                BufferedImage image = ImageIO.read(new File(path));
                canvas.setImage(image);
                areaObjectsList = new ArrayList<Area>();
                canvas.repaint();
            } catch (IOException excp) {
                JOptionPane.showMessageDialog(this, excp.getLocalizedMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }
        if (e.getActionCommand().equals("loadUrl")) {
            barvaMenu.getItem(lastRBMIIndex).setSelected(true);
            try {
                String urlString = JOptionPane.showInputDialog(this, "http://", "Otevřít obrázek z url", JOptionPane.PLAIN_MESSAGE);
                if (!urlString.substring(0, 7).equals("http://")) {
                    //add "http://"
                    urlString = "http://" + urlString;
                }
                URL url = new URL(urlString);
                BufferedImage image = Constants.toBufferedImage(java.awt.Toolkit.getDefaultToolkit().createImage(url));
                canvas.setImage(image);
                areaObjectsList = new ArrayList<Area>();
                canvas.repaint();
            } catch (MalformedURLException mue) {
                JOptionPane.showMessageDialog(this, mue.getLocalizedMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                mue.printStackTrace();
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, ioe.getLocalizedMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                ioe.printStackTrace();
            } catch (Exception excp) {
                JOptionPane.showMessageDialog(this, excp.getLocalizedMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                JOptionPane.showMessageDialog(this, "Prosím, zkontrolujte, zdali jste zadali url png/jpg/gif obrázku.", "Error!", JOptionPane.ERROR_MESSAGE);
            }

        }
        if (e.getActionCommand().equals("openTag")) {
            outputArea.setText(outputArea.getText() + "\n<testobject>");
        }
        if (e.getActionCommand().equals("closeTag")) {
            outputArea.setText(outputArea.getText() + "\n</testobject>");
        }
        if (e.getActionCommand().equals("setXml")) {
            openTagButton.setVisible(true);
            closeTagButton.setVisible(true);
            imapRBMItem.setSelected(false);
            formatXml = true;
            String header = "<root>\n\t<addChromosome>chromozom, co není v defaultním seznamu</addChromosome>\n" +
                "\t<intro>\n" +
                "\t\t<question type=\"multiplecorrect\">\n" +
                "\t\t<!--<question type=\"onecorrect\">-->\n" +
                "\t\t<!--<question type=\"fillin\">-->\n" +
                "\t\t\t<questionText>Znění otázky</questionText>\n" +
                "\t\t\t<choice>špatná odpověď</choice>\n" +
                "\t\t\t<choice correct=\"true\">správná odpověď</choice>\n" +
                "\t\t\t<wrongAnswerReply>Reakce na špatnou odpověď.</wrongAnswerReply>\n" +
                "\t\t</question>\n" +
                "\t\t</intro>\n" +
                "\t\t<settings>\n" +
                "\t\t\t<picture>cesta/k/obrázku.png</picture>\n" +
                "\t\t\t<autoCorrect value=\"off\" />\n" +
                "\t\t\t<!--<autoCorrect value=\"on\" />-->\n" +
                "\t\t</settings>\n";
            outputArea.setText(header);
        }
        if (e.getActionCommand().equals("setImap")) {
            openTagButton.setVisible(false);
            closeTagButton.setVisible(false);
            xmlRBMItem.setSelected(false);
            formatXml = false;
        }
        if (e.getActionCommand().equals("copy")) {
            if (copyOtherArea) {
                doneArea.requestFocus();
                doneArea.selectAll();
                doneArea.copy();
            } else {
                outputArea.requestFocus();
                outputArea.selectAll();
                outputArea.copy();
            }
        }
        if (e.getActionCommand().equals("rectangle")) {
            activeAreaObject = new Area("rectangle");
            disableButtons();
            canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
        if (e.getActionCommand().equals("circle")) {
            activeAreaObject = new Area("circle");
            disableButtons();
            canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
        if (e.getActionCommand().equals("polygon")) {
            activeAreaObject = new Area("polygon");
            disableButtons();
            finishedButton.setEnabled(true);
            canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
        if (e.getActionCommand().equals("finished")) {
            activeAreaObject.finished = true;
            outputArea.setText(outputArea.getText() + "\n" + activeAreaObject.getText(formatXml));
            areaObjectsList.add(activeAreaObject);
            enableButtons();
            finishedButton.setEnabled(false);
            canvas.setCursor(Cursor.getDefaultCursor());
            canvas.clearPoints();
            canvas.repaint();
        }
        if (e.getActionCommand().equals("zoomIn")) {
            canvas.magnifierPlus = true;
            canvas.setCursor(zoomInCursor);
        }
        if (e.getActionCommand().equals("zoomOut")) {
            canvas.magnifierMinus = true;
            canvas.setCursor(zoomOutCursor);
        }
        if (e.getActionCommand().equals("done")) {
            if (formatXml) {
                String outputText = outputArea.getText();
                outputText += "\n\t<outro>\n\t\t<question type=\"fillin\">\n" +
                    "\t\t\t<questionText>Text otázky</questionText>\n" +
                    "\t\t\t<choice correct=\"true\">Jedna správná odpověď</choice>\n" +
                    "\t\t\t<choice correct=\"true\">Jiná správná odpověď</choice>\n" +
                    "\t\t\t<wrongAnswerReply>Reakce, pokud je odpověď špatně</wrongAnswerReply>\n" +
                    "\t\t</question>\n" +
                    "\t</outro>\n" +
                    "</root>";
                outputArea.setText(outputText);
            } else {
                outputArea.setText(outputArea.getText() + "\ndefault [[Link|Link text]]\n</imagemap>");
            }
            showResult();
        }
        if (e.getActionCommand().equals("color")) {
            setColor();
        }
    }

    private void setColor() {
        int rBMItemsCount = barvaMenu.getItemCount();
        barvaMenu.getItem(lastRBMIIndex).setSelected(false);
        for (int i = 0; i < rBMItemsCount; i++) {
            if (i != lastRBMIIndex && barvaMenu.getItem(i).isSelected()) {
                lastRBMIIndex = i;
                color = colors.get(i);
            }
        }
    }
}

class JCanvas extends JPanel {
    //Class that enables to use canvas in Swing applications
    BufferedImage image;
    ImageMapAssistant parent;
    double ratio;
    int x, y, imageWidth, imageHeight, width, height,
            labelWidth, labelHeight;
    boolean magnifierPlus, magnifierMinus;
    List<Dimension> points = new ArrayList();

    public JCanvas(BufferedImage imageObj, ImageMapAssistant parentPanel) {
        setBorder(BorderFactory.createLineBorder(Color.black));
        ratio = 1;
        image = imageObj;
        parent = parentPanel;
        if (image != null) {
            setImage(image);
        }
        magnifierPlus = false;
        magnifierMinus = false;
        setSizes();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouse(e.getX(), e.getY());
            }
        });
    }
    public void setSizes() {
        if (image != null) {
            Dimension labelDimensions = this.getSize();
            labelWidth = labelDimensions.width;
            labelHeight = labelDimensions.height;

            //Both width and height is changed in the same ratio:
            height = double2int(ratio * int2double(image.getHeight()));
            width = double2int(ratio * int2double(image.getWidth()));
        }
    }

    private void mouse(int xCoordinate, int yCoordinate) {
        if (magnifierPlus) {
            magnifierPlus = false;
            zoomIn(xCoordinate, yCoordinate);
            setCursor(Cursor.getDefaultCursor());
        } else if (magnifierMinus) {
            magnifierMinus = false;
            zoomOut(xCoordinate, yCoordinate);
            setCursor(Cursor.getDefaultCursor());
        } else if ((parent.activeAreaObject != null) && (!parent.activeAreaObject.isFinished())) {
            getPointXY(xCoordinate, yCoordinate);
        }
    }

    private void getPointXY(int xCoordinate, int yCoordinate) {
        points.add(new Dimension(xCoordinate, yCoordinate));
        repaint();
        int imageX = xCanvasToImage(xCoordinate);
        int imageY = yCanvasToImage(yCoordinate);
        parent.addPoint(imageX, imageY);
    }

    public void clearPoints() {
        points = new ArrayList<Dimension>();
    }
    private int xImageToCanvas(int imageX) {
        return double2int(int2double(imageX) * ratio) + x;
    }
    private int yImageToCanvas(int imageY) {
        return double2int(int2double(imageY) * ratio) + y;
    }
    private int xCanvasToImage(int canvasX) {
        return double2int(int2double(canvasX - x)/ratio);
    }
    private int yCanvasToImage(int canvasY) {
        return double2int(int2double(canvasY - y)/ratio);
    }
    private void zoomIn(int xCoordinate, int yCoordinate) {
        ratio *= 1.2;
        setSizes();
        x += (labelWidth/2) - xCoordinate;
        y += (labelHeight/2) - yCoordinate;
        repaint();
    }
    private void zoomOut(int xCoordinate, int yCoordinate) {
        ratio /= 1.2;
        setSizes();
        x += (labelWidth/2) - xCoordinate;
        y += (labelHeight/2) - yCoordinate;
        repaint();
    }

    public void moveImage(int xMove, int yMove) {
        x += xMove;
        y += yMove;
        repaint();
    }

    public void setImage(BufferedImage imageObj) {
        image = imageObj;
        if (image != null) {
            Dimension labelDimensions = this.getSize();
            labelWidth = labelDimensions.width;
            labelHeight = labelDimensions.height;
            width = image.getWidth();
            height = image.getHeight();
            //Both width and height is changed in the same ratio:
            if (width >= height) {
                ratio = int2double(labelWidth) / int2double(width);
            } else {
                ratio = int2double(labelHeight) / int2double(height);
            }
            //Do not magnify smaller image.
            if (ratio > 1.0) {
                ratio = 1.0;
            }
            //Put the picture in the middle of the JLabel object
            x = (labelWidth - double2int(int2double(width) * ratio))/2;
            y = (labelHeight - double2int(int2double(height) * ratio))/2;
        }
        setSizes();
    }
    public Dimension getPrefferedSize() {
        if (image == null) {
            return new Dimension(Constants.width, Constants.height);
        } else {
            return new Dimension(image.getWidth(), image.getHeight());
        }
    }
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Color defaultColor = graphics.getColor();

        //draw the image
        if (image != null) {
            graphics.drawImage(image, x, y, width, height, null);
        }
        graphics.setColor(parent.color);

        //draw all points that have been actually clicked on the canvas
        Dimension point; int pointX; int pointY;
        for (Iterator<Dimension> it = points.iterator(); it.hasNext(); ) {
            point = it.next();
            pointX = double2int(point.getWidth());
            pointY = double2int(point.getHeight());
            graphics.drawOval(pointX, pointY, 5, 5);
        }

        //draw all the objects that have been created
        Area area;
        List<Area> areas = parent.areaObjectsList;
        for (Iterator<Area> it = areas.iterator(); it.hasNext(); ) {
            area = it.next();
            if (area.isFinished()) {

                if (area.type.equals("polygon")) {
                    //draw polygon
                    Dimension point0 = new Dimension(0,0);
                    Dimension point1 = new Dimension(0,0);
                    Dimension point2 = new Dimension(0,0);
                    Iterator<Dimension> pointIt = area.points.iterator();
                    if (pointIt.hasNext()) {
                        point0 = pointIt.next();
                        point1 = point0;
                    }
                    while (pointIt.hasNext()) {
                            point2 = pointIt.next();
                            //draw line between two edges of the polygon
                            graphics.drawLine(
                                    xImageToCanvas(point1.width), yImageToCanvas(point1.height),
                                    xImageToCanvas(point2.width), yImageToCanvas(point2.height));
                            //move to next point:
                            point1 = point2;
                    }
                    //close the polygon
                    graphics.drawLine(xImageToCanvas(point0.width), yImageToCanvas(point0.height),
                            xImageToCanvas(point1.width), yImageToCanvas(point1.height));
                } else {
                    //draw rectangle or cicrle
                    Dimension point1 = area.getPoint1();
                    Dimension point2 = area.getPoint2();
                    double radiusDouble = Math.sqrt( Math.pow((point2.getWidth() - point1.getWidth()),2) + Math.pow((point2.getHeight() - point2.getHeight()), 2) );
                    int x1 = xImageToCanvas(point1.width);
                    int y1 = yImageToCanvas(point1.height);
                    int x2 = xImageToCanvas(point2.width);
                    int y2 = yImageToCanvas(point2.height);
                    int radius = double2int(radiusDouble);
                    if (area.type.equals("rectangle")) {
                        graphics.drawRect(x1, y1, x2 - x1, y2 - y1);
                    }
                    if (area.type.equals("circle")) {
                        graphics.drawOval(x1-radius, y1-radius, 2*radius, 2*radius);
                    }
                }
            }
        }
        graphics.setColor(defaultColor);
    }


    double int2double(int value) {
        return Double.parseDouble(String.valueOf(value));
    }
    int double2int(double value) {
        return (int) Math.rint(value);
    }
}

final class Constants {
    public static final int width = 600;
    public static final int height = 500;
    public static final int xOffset = 20;
    public static final int yOffset = 60;

    public static int getScreenWidth() {
        java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int screenWidth = (int) Math.rint(screenSize.getWidth());
        return screenWidth;
    }
    public static int getScreenHeight() {
        java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int screenHeight = (int) Math.rint(screenSize.getHeight());
        return screenHeight;
    }

    // This method returns a buffered image with the contents of an image
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see Determining If an Image Has Transparent Pixels
       // boolean hasAlpha = hasAlpha(image);
        boolean hasAlpha = false;

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }
}

class Area {
    String type;
    List<Dimension> points = new ArrayList<Dimension>();
    boolean finished = false;

    Area(String areaType) {
        type = areaType;
    }
    public void addPoint(Dimension d) {
        points.add(d);
    }
    public boolean isFinished() {
        if (type.equals("rectangle")) {
            if (points.size() == 2) {
                finished = true;
                return true;
            } else {
                return false;
            }
        } else if (type.equals("circle")) {
            if (points.size() == 2) {
                finished = true;
                return true;
            } else {
                return false;
            }
        } else if (type.equals("polygon")) {
            return finished;
        } else {
            return false;
        }
    }

    public Dimension getPoint1() {
        return points.get(0);
    }
    public Dimension getPoint2() {
        return points.get(1);
    }

    public List<Dimension> getPoints() {
        return points;
    }

    public String getText(boolean xmlFormat) {
        if (type.equals("circle")) {
            Dimension point = points.get(0);
            double x = point.getWidth();
            double y = point.getHeight();
            point = points.get(1);
            double x2 = point.getWidth();
            double y2 = point.getHeight();
            double r = Math.sqrt( Math.pow((x2 - x),2) + Math.pow((y2 - y), 2) );

            int xInt = (int) Math.rint(x);
            int yInt = (int) Math.rint(y);
            int rInt = (int) Math.rint(r);
            String output = "circle";
            output += " " + Integer.toString(xInt);
            output += " " + Integer.toString(yInt);
            output += " " + Integer.toString(rInt);
            output += " [[Link|Link text]]";
            return output;
        } else if (type.equals("rectangle")) {
            Dimension point = points.get(0);
            Dimension point2 = points.get(1);
            int xInt = (int) Math.rint(point.getWidth());
            int yInt = (int) Math.rint(point.getHeight());
            int xInt2 = (int) Math.rint(point2.getWidth());
            int yInt2 = (int) Math.rint(point2.getHeight());

            String output = "";
            if (xmlFormat) {
                output += "<area>\n";
                output += "<x1>" + Integer.toString(xInt) + "</x1>\n";
                output += "<y1>" + Integer.toString(yInt) + "</y1>\n";
                output += "<x2>" + Integer.toString(xInt2) + "</x2>\n";
                output += "<y2>" + Integer.toString(yInt2) + "</y2>\n";
                output += "</area>\n";
            } else {
                output += "rect";
                output += " " + Integer.toString(xInt);
                output += " " + Integer.toString(yInt);
                output += " " + Integer.toString(xInt2);
                output += " " + Integer.toString(yInt2);
                output += " [[Link|Link text]]";
            }
            return output;
        } else if (type.equals("polygon")) {
            String output = "poly";
            for (Iterator<Dimension> it = points.iterator(); it.hasNext(); ) {
                Dimension point = it.next();
                int xInt = (int) Math.rint(point.getWidth());
                int yInt = (int) Math.rint(point.getHeight());
                output += " " + Integer.toString(xInt);
                output += " " + Integer.toString(yInt);
            }
            output += " [[Link|Link text]]";
            return output;
        } else {
            return "";
        }
    }
}

class PressButton extends JLabel implements ActionListener {
    private Timer timer;
    private MouseListener mouseAdapter;
    boolean mousepressed = false;
    private int xMove, yMove;
    JCanvas canvas;
    ImageMapAssistant parent;
    public PressButton(ImageIcon icon, JCanvas jcanvas, int xMovePixels, int yMovePixels) {
        super(icon);
        xMove = xMovePixels; yMove = yMovePixels;
        timer = new Timer(0, this);
        timer.setActionCommand("move");
        timer.setDelay(100);
        canvas = jcanvas;
        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                timer.start();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                timer.stop();
            }
        };
        addMouseListener(mouseAdapter);
    }
    public void actionPerformed(ActionEvent e) {
        canvas.moveImage(xMove, yMove);
    }
}