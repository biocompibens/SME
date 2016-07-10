package ij.plugin.filter.SME_PROJECTION_SRC;

import ij.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;


/**
 * Created by rexhepaj on 16/03/16.
 */
public class SME_ENS_GUI_MAIN extends JFrame implements ActionListener {


    private static final int WIDTHSHOW  = 200;
    private static final int HEIGHTSHOW = 200;
    private static final int WIDTHGUI   = 1100;
    private static final int HEIGHTGUI  = 600;

    private static final String AVG_INT     = "Average Intensity";
    private static final String MAX_INT     = "Max Intensity";
    private static final String MIN_INT     = "MIN Intensity";
    private static final String SUM_SLICE   = "Sum Slices";
    private static final String STD_INT     = "Standard Deviation";
    private static final String MED_INT     = "Median Deviation";
    private static final String BASIC_KMEANS        = "Basic K-Means Clustering";
    private static final String BENCHMARKED_KMEANS  = "Benchmarked K-Means Clustering";
    private static final String CONCURRENT_KMEANS   = "Concurrent K-Means Clustering";

    private SME_Plugin_Get_Manifold smePlugin;

    // Set ImageJ graphical components
    private ImagePlus currentImage ;
    private ImagePlus processedImage;
    private Image outputIm,rawIm ;
    private Image manifoldSME, projectionSME;
    private ImagePlus tmpImage ;
    public ImageStack imageStack;

    private BorderLayout borderLayout1  = new BorderLayout();
    private JButton batchRunButton      = new JButton();
    private JPanel controlPanel         = new JPanel();
    private JComboBox standartProjectionMethods = new JComboBox();

    private JButton smlRunButton        = new JButton();
    private JButton kmeanRunButton      = new JButton();
    private JButton enoptRunButton      = new JButton();
    private JButton saveImButton        = new JButton();

    private GridBagLayout gridLayoutMain    = new GridBagLayout();

    private GridBagConstraints cMain    = new GridBagConstraints();
    private GridBagConstraints cTop     = new GridBagConstraints();
    private GridBagConstraints cBottom  = new GridBagConstraints();
    private GridBagConstraints cCenter  = new GridBagConstraints();

    BufferedImage imgtemplate1 = new BufferedImage(WIDTHSHOW, HEIGHTSHOW, BufferedImage.TYPE_INT_RGB);
    BufferedImage imgtemplate2 = new BufferedImage(WIDTHSHOW, HEIGHTSHOW, BufferedImage.TYPE_INT_RGB);

    // application content
    private boolean mRunning;
    private SME_KMeans_Paralel mKMeans;
    private SME_Cluster[] clustersKmeans;

    TitledBorder titledTOPLEFT ,titledTOPRIGHT,titledBOTLEFT,titledBOTRIGHT ;

    // Define standart projection methods
    //{"Average Intensity", "Max Intensity", "Min Intensity",
    // "Sum Slices", "Standard Deviation", "Median"};


    private String[] projMethods = new String[6];


    private double[][] coordinates ;

    private Boolean guiStatus = false;
    private JLabel leftImage = null;
    private JLabel centerImage = null;
    private JLabel rightImage  = null;
    private JLabel leftSME, rightSME = null;

    /**
     * Constructor called to initialize the graphical interface
     */
    public SME_ENS_GUI_MAIN(SME_Plugin_Get_Manifold sme_pluginGetManifold) {
        projMethods[0] = AVG_INT;
        projMethods[1] = MAX_INT;
        projMethods[2] = MED_INT;
        projMethods[3] = MIN_INT;
        projMethods[4] = STD_INT;
        projMethods[5] = SUM_SLICE;

        smePlugin = sme_pluginGetManifold;
        System.out.print("Debug");
        //super("FrameDemo");
    }

    public void initGUI() {
        buildGUI();
    }

    /**
     * Method to initialise all graphical components for display
     */
    public void buildGUI() {

        initTemplates();

        titledTOPLEFT  = new TitledBorder("RAW STACK - CLASSICAL PROJECTION");
        titledTOPRIGHT = new TitledBorder("RAW STACK - PREPROCESSING");
        titledBOTLEFT  = new TitledBorder("SME - 2D MANIFOLD");
        titledBOTRIGHT = new TitledBorder("SME - 3D->2D PROJECTION");

        // initialise the action listeners
        standartProjectionMethods.addActionListener(this);


        // define grid constraints
        cCenter.fill = GridBagConstraints.BOTH;
        cTop.fill = GridBagConstraints.NONE;
        cBottom.fill = GridBagConstraints.NONE;

        // Define the GUI overall panel

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        controlPanel = (JPanel) this.getContentPane();

        // Grap the screen size and set the size of the GUI to half each dimension

        GraphicsDevice gd   = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int widthScreen     = gd.getDisplayMode().getWidth();
        int heightScreen    = gd.getDisplayMode().getHeight();

        setSize(new Dimension( WIDTHGUI,HEIGHTGUI));
        setTitle("SME 3D PROJECTION METHOD - ENS COMPUTATIONAL BIOLOGY GROUP");

        // Initialise the different GUI building components

        standartProjectionMethods.addItem(AVG_INT);
        standartProjectionMethods.addItem(MAX_INT);
        standartProjectionMethods.addItem(MED_INT);
        standartProjectionMethods.addItem(MIN_INT);
        standartProjectionMethods.addItem(STD_INT);
        standartProjectionMethods.addItem(SUM_SLICE);

        batchRunButton.setText("RUN SME - BATCH MODE");
        batchRunButton.addActionListener(this);

        saveImButton.setText("RUN SME - SAVE THE MANIFOLD AS JPG");
        saveImButton.addActionListener(this);

        smlRunButton.setText("RUN SME - SML (STEP1)");
        smlRunButton.addActionListener(this);

        kmeanRunButton.setText("RUN SME - KMEAN (STEP2)");
        kmeanRunButton.addActionListener(this);

        enoptRunButton.setText("RUN SME - ENERGY OPTIMISATION (STEP3)");
        enoptRunButton.addActionListener(this);

        //Initialize the control panel
        controlPanel.setLayout(gridLayoutMain);
        cMain.fill = GridBagConstraints.HORIZONTAL;

        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 0;
        cMain.gridwidth = 1;
        cMain.gridx = 0;
        cMain.gridy = 0;
        controlPanel.add(standartProjectionMethods, cMain);

        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 0.5;
        cMain.gridwidth = 1;
        cMain.gridx = 1;
        cMain.gridy = 0;
        controlPanel.add(smlRunButton, cMain);

        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 0.5;
        cMain.gridwidth = 1;
        cMain.gridx = 2;
        cMain.gridy = 0;
        controlPanel.add(kmeanRunButton, cMain);

        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 0.5;
        cMain.gridwidth = 1;
        cMain.gridx = 3;
        cMain.gridy = 0;
        controlPanel.add(enoptRunButton, cMain);

        // add images
        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 1;
        cMain.gridwidth = 2;
        cMain.gridx = 0;
        cMain.gridy = 1;

        SME_ENS_Image_Component imcontent = new SME_ENS_Image_Component(currentImage,Boolean.TRUE, currentImage.getWidth(), currentImage.getHeight(), 0);
        leftImage = new JLabel(new ImageIcon(imcontent.getIm2Show().getScaledInstance(WIDTHSHOW,HEIGHTSHOW, Image.SCALE_DEFAULT)));
        controlPanel.add(leftImage, cMain);
        leftImage.setBorder(titledTOPLEFT);

        // add images
        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 1;
        cMain.gridwidth = 2;
        cMain.gridx = 0;
        cMain.gridy = 2;

        leftSME = new JLabel(new ImageIcon(imgtemplate1));
        controlPanel.add(leftSME, cMain);
        leftSME.setBorder(titledBOTLEFT);

        // add images
        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 1;
        cMain.gridwidth = 2;
        cMain.gridx = 2;
        cMain.gridy = 2;

        rightSME = new JLabel(new ImageIcon(imgtemplate2));
        controlPanel.add(rightSME, cMain);
        rightSME.setBorder(titledBOTRIGHT);

        // add batch run control buttons
        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 0;
        cMain.gridwidth = 2;
        cMain.gridx = 0;
        cMain.gridy = 3;
        controlPanel.add(batchRunButton, cMain);

        // add batch save control buttons
        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 0;
        cMain.gridwidth = 2;
        cMain.gridx = 2;
        cMain.gridy = 3;
        controlPanel.add(saveImButton, cMain);

        controlPanel.setVisible(Boolean.TRUE);
        this.setVisible(Boolean.TRUE);

        //TestFrame tframe = new TestFrame(imcontent.getIm2Show()) ;
        //tframe.setVisible(Boolean.TRUE);
        //tframe.repaint();
        System.out.println("new line");
        guiStatus = true;
    }

    public void initTemplates(){
        int r = 255 ;// red component 0...255
        int g = 255 ;// green component 0...255
        int b = 255 ;// blue component 0...255
        int col = (r << 16) | (g << 8) | b;

        for(int x=0; x<WIDTHSHOW;x++){
            for(int y=0; y<WIDTHSHOW;y++){
                imgtemplate1.setRGB(x, y, col);
                imgtemplate2.setRGB(x, y, col);
            }
        }

    }

    // Action listeners

    public synchronized void actionPerformed(ActionEvent e) {

        // switch depending on the source of the action button the according response
        if(guiStatus==true) {
            if (e.getSource() == standartProjectionMethods) {
                // case where action came from the drop menu
                int selIndex = standartProjectionMethods.getSelectedIndex();
                System.out.println("Selecting projection method :"+Integer.toString(selIndex));
                runNormProjection(selIndex);
                updateProjectionRaw(rawIm);

            } else if (e.getSource() == batchRunButton) {
                //run batch mode
                runBatchStep();
                updateProjectionSME(manifoldSME,projectionSME);

            } else if (e.getSource() == smlRunButton) {
                //run sml - step 1
                runSmlStep();
                updateProjectionOutput(outputIm);

            } else if (e.getSource() == kmeanRunButton) {
                //run kmean - step 2
                runKmeansStep();
                updateProjectionOutput(outputIm);

            } else if (e.getSource() == enoptRunButton) {
                //run enopt - step 3
                runEnoptStep();
                updateProjectionSME(manifoldSME,projectionSME);

            } else if (e.getSource() == saveImButton) {
                //save current output
                runSaveimStep();
            }
        }
    }

    public void runNormProjection(int projMethod){
        smePlugin.runProjection(projMethod);
        rawIm = smePlugin.getProjImage().getImage();
    }

    public void runSmlStep(){
        smePlugin.runSml(true);
        SME_ENS_Image_Component imcontent = new SME_ENS_Image_Component( smePlugin.getSmlImage(),Boolean.TRUE,
                currentImage.getWidth(), currentImage.getHeight(),6);
        outputIm = imcontent.getIm2Show();
    }

    public void runKmeansStep(){
        smePlugin.runKmeans(true);
        SME_ENS_Image_Component imcontent = new SME_ENS_Image_Component( smePlugin.getKmensImage(),Boolean.TRUE,
                currentImage.getWidth(), currentImage.getHeight(),6);
        outputIm = imcontent.getIm2Show();
    }

    public void runEnoptStep(){
        smePlugin.runEnergyOptimisation(true);
        SME_ENS_Image_Component imcontent1 = new SME_ENS_Image_Component( smePlugin.getMfoldImage(),Boolean.TRUE,
                currentImage.getWidth(), currentImage.getHeight(),6);
        manifoldSME = imcontent1.getIm2Show();
        SME_ENS_Image_Component imcontent2 = new SME_ENS_Image_Component( smePlugin.getSmeImage(),Boolean.TRUE,
                currentImage.getWidth(), currentImage.getHeight(),6);
        projectionSME = imcontent2.getIm2Show();
    }

    public void runSaveimStep(){

    }


    public void runBatchStep(){
        runSmlStep();updateProjectionOutput(outputIm);
        runKmeansStep();updateProjectionOutput(outputIm);
        smePlugin.runEnergyOptimisation(true);
        SME_ENS_Image_Component imcontent1 = new SME_ENS_Image_Component( smePlugin.getMfoldImage(),Boolean.TRUE,
                currentImage.getWidth(), currentImage.getHeight(),6);
        manifoldSME = imcontent1.getIm2Show();
        SME_ENS_Image_Component imcontent2 = new SME_ENS_Image_Component( smePlugin.getSmeImage(),Boolean.TRUE,
                currentImage.getWidth(), currentImage.getHeight(),6);
        projectionSME = imcontent2.getIm2Show();
    }

    public void updateProjectionSME(Image smeManifold, Image smeOutput){
        if( leftSME!= null) {
            controlPanel.remove(leftSME);
        }
        if( rightSME!= null) {
            controlPanel.remove(rightSME);
        }

        // add manifold image
        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 1;
        cMain.gridwidth = 2;
        cMain.gridx = 0;
        cMain.gridy = 2;

        leftSME = new JLabel(new ImageIcon(smeManifold.getScaledInstance(WIDTHSHOW, HEIGHTSHOW, Image.SCALE_DEFAULT)));
        controlPanel. add(leftSME, cMain);
        leftSME.setBorder(titledBOTLEFT);
        controlPanel.setVisible(Boolean.TRUE);
        this.repaint();
        this.setVisible(Boolean.TRUE);

        // add SME projection image
        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 1;
        cMain.gridwidth = 2;
        cMain.gridx = 2;
        cMain.gridy = 2;

        rightSME = new JLabel(new ImageIcon(projectionSME.getScaledInstance(WIDTHSHOW, HEIGHTSHOW, Image.SCALE_DEFAULT)));
        controlPanel. add(rightSME, cMain);
        rightSME.setBorder(titledBOTRIGHT);
        controlPanel.setVisible(Boolean.TRUE);
        this.repaint();
        this.setVisible(Boolean.TRUE);
    }

    public void updateProjectionRaw(Image rawIM){

        if(leftImage!=null){
            controlPanel.remove(leftImage);
        }

        // add images
        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 1;
        cMain.gridwidth = 2;
        cMain.gridx = 0;
        cMain.gridy = 1;

        if(smePlugin.getRawImage()==null){
            System.out.println("Image is NULL");
        }else{
            System.out.println("Image with data");
        }

        leftImage = new JLabel(new ImageIcon(rawIM.getScaledInstance(WIDTHSHOW, HEIGHTSHOW, Image.SCALE_DEFAULT)));
        controlPanel.add(leftImage, cMain);
        leftImage.setBorder(titledTOPLEFT);
        controlPanel.setVisible(Boolean.TRUE);
        this.repaint();
        this.setVisible(Boolean.TRUE);
    }


    public void updateProjectionOutput(Image outputIM){

        if(centerImage != null) {
            controlPanel.remove(centerImage);
        }

        // add images
        cMain.ipady = 10;
        cMain.ipadx = 10;
        cMain.weightx = 1;
        cMain.gridwidth = 2;
        cMain.gridx = 2;
        cMain.gridy = 1;

        centerImage = new JLabel(new ImageIcon(outputIM.getScaledInstance(WIDTHSHOW, HEIGHTSHOW, Image.SCALE_DEFAULT)));
        controlPanel. add(centerImage, cMain);
        centerImage.setBorder(titledTOPRIGHT);
        controlPanel.setVisible(Boolean.TRUE);
        this.repaint();
        this.setVisible(Boolean.TRUE);
    }

    /************************************************** Getter and setters********************************************/

    public ImagePlus getCurrentImage() {
        return currentImage;
    }

    public void setCurrentImage(ImagePlus currentImage) {
        this.currentImage = currentImage;
    }

    public ImagePlus getProcessedImage() {
        return processedImage;
    }

    public void setProcessedImage(ImagePlus processedImage) {
        this.processedImage = processedImage;
    }

    public ImagePlus getTmpImage() {
        return tmpImage;
    }

    public void setTmpImage(ImagePlus tmpImage) {
        this.tmpImage = tmpImage;
    }


    /*******************************************************************************************************/

    // Add a template JFrame to show images

    class TestFrame extends JFrame
    {
        public TestFrame(Image im2show) {
            // setSize(1000, 750);  <---- do not do it
            // setResizable(false); <----- do not do it either, unless any good reason

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setTitle("Test JFrame to illustrate Image");

            JLabel label = new JLabel(new ImageIcon(im2show));
            JScrollPane scrollPane = new JScrollPane(label);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            add(scrollPane, BorderLayout.CENTER);
            pack();
        }
    }
}
