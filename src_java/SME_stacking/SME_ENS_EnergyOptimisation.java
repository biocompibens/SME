package ij.plugin.filter.SME_PROJECTION_SRC;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.plugin.ZAxisProfiler;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.awt.*;

/**
 * Created by rexhepaj on 17/03/16.
 */
public class SME_ENS_EnergyOptimisation {
    private SME_Plugin_Get_Manifold sme_pluginGetManifold = null;
    private final int KMEAN_NORM    = 2;
    private double ENERGY_STEP      = 0.000001;
    private RealMatrix kmeanOutput ;
    private RealMatrix rawdata2D    = null;
    private RealMatrix tmpProcess   = null;
    private double totalz           = 0;
    private double step ;

    private int stepNumber          = 100;
    private RealMatrix idmax        = null;
    private RealMatrix idmaxini     = null;
    private RealMatrix idmaxk       = null;
    private RealMatrix idmaxki      = null;
    private RealVector cost         = null;
    private RealMatrix edgeflag2B   = null;
    private RealMatrix edgeflag2IB  = null;
    private RealMatrix movmat       = null;
    private RealMatrix mink         = null;
    private int iter                = 1;
    private RealMatrix edgeflag     = null;
    private RealMatrix edgeflag2    = null;
    private RealMatrix edgeflag3k   = null;
    private RealMatrix idmaxkB      = null;
    private RealMatrix  valk        = null;
    private RealVector  sftz        = MatrixUtils.createRealVector(new double[0]);
    private double KE               = 0;
    private int nt                  = 0;
    private double ht               = 0;
    private double WW               = 9;

    private RealVector sg           =   MatrixUtils.createRealVector(new double[0]);
    private RealVector sgain        =   MatrixUtils.createRealVector(new double[0]);
    private RealVector dg           =   MatrixUtils.createRealVector(new double[0]);
    private RealVector sgk          =   MatrixUtils.createRealVector(new double[0]);
    private RealVector WA           =   MatrixUtils.createRealVector(new double[0]);

    private RealVector foregroundPixelVal     = null;

    public SME_ENS_EnergyOptimisation(SME_Plugin_Get_Manifold refplugin){
        //TODO fix issues with the centriole image, 111111 at the last coloumns
        // for the idmaxk, sum of the Gx and Gy not correct, number of entries not equal to the number
        // of the stack slices.
        sme_pluginGetManifold = refplugin;
        initOptimisation();
    }

    public void initOptimisation(){
        ImagePlus   smlProjection = sme_pluginGetManifold.getSmlImage();
        kmeanOutput = MatrixUtils.createRealMatrix(SME_ENS_Utils.convertFloatMatrixToDoubles(
                sme_pluginGetManifold.getMap2d().getProcessor().getFloatArray(),
                smlProjection.getWidth(),smlProjection.getHeight()));
        kmeanOutput = kmeanOutput.transpose();

        edgeflag = kmeanOutput;
        //SME_ENS_Utils.printRealMatrix(edgeflag.getData());
        double normFactor = 1.0/KMEAN_NORM;
        edgeflag2  = MatrixUtils.createRealMatrix(edgeflag.scalarMultiply(normFactor).getData());
        edgeflag3k = MatrixUtils.createRealMatrix(edgeflag.scalarMultiply(normFactor).getData());

        //SME_ENS_Utils.printRealMatrix(edgeflag2.getData());
        //SME_ENS_Utils.printRealMatrixStats(edgeflag2,"edgeflag2");
        //[valk,idmax]=max(timk,[],3);
        idmax       = SME_ENS_Utils.getMaxProjectionIndex(smlProjection.getImageStack()).scalarAdd(1);

        ZProjector zproject = new ZProjector();
        zproject.setMethod(ZProjector.MAX_METHOD);
        zproject.setImage(new ImagePlus("IterativeProjection", smlProjection.getImageStack()));
        zproject.doProjection();

        valk            =   MatrixUtils.createRealMatrix(
                        SME_ENS_Utils.convertFloatMatrixToDoubles(
                        zproject.getProjection().getImageStack().getProcessor(1).getFloatArray(),
                        smlProjection.getImageStack().getWidth(), smlProjection.getImageStack().getHeight()));
        valk            =   valk.transpose();

        zproject.setMethod(ZProjector.SUM_METHOD);
        zproject.setImage(new ImagePlus("IterativeProjection", smlProjection.getImageStack()));
        zproject.doProjection();
        RealMatrix valkSum      =   MatrixUtils.createRealMatrix(
                SME_ENS_Utils.convertFloatMatrixToDoubles(
                        zproject.getProjection().getImageStack().getProcessor(1).getFloatArray(),
                        smlProjection.getImageStack().getWidth(), smlProjection.getImageStack().getHeight()));
        RealVector valkSumvec            =   SME_ENS_Utils.realmat2vector(valkSum,0);

        for(int vecIndx=0;vecIndx<valkSumvec.getDimension();vecIndx++){
            totalz              =   totalz + valkSumvec.getEntry(vecIndx);
        }

        initStepEnergyOpt();
        initWparam();

        // save tmp sml max projection and kmeans projection

        //IJ.saveAsTiff(new ImagePlus("SML_Projection",smlProjection.getImageStack()),"smlResult.tiff");
        //IJ.saveAsTiff(sme_pluginGetManifold.getKmensImage(),"kmeansResult.tiff");

        //SME_ENS_Utils.printRealMatrix(idmax.getData(),"idmax");
        SME_ENS_Utils.printRealMatrixStats(idmax,"idmax");

        idmaxk      = idmax.copy();
        idmaxki     = idmax.copy();
        mink        = idmax.copy().scalarMultiply(0).scalarAdd(1);

        // TODO code function to automatically update the step size

        cost     = MatrixUtils.createRealVector(new double[2]);
        cost.setEntry(0, Integer.MAX_VALUE);cost.setEntry(1,1000);
    }

    public void initWparam(){

        RealVector edgeflag2Cond1 = SME_ENS_Utils.realmatSelectVector(edgeflag2,valk,1); // TODO Check definition of edgeflag2 no equal to matlab
        RealVector edgeflag2Cond2 = SME_ENS_Utils.realmatSelectVector(edgeflag2,valk,0);
        RealVector edgeflag2Cond3 = SME_ENS_Utils.realmatSelectVector(edgeflag2,valk,0.5);

        RealVector valkVec        = SME_ENS_Utils.realmat2vector(valk,0);
        int histNmbBins           = 100;
        RealVector hcb            = MatrixUtils.createRealVector(SME_ENS_Utils.linspace(
                valkVec.getMinValue(),valkVec.getMaxValue(),histNmbBins));
        RealVector hcf            = hcb.copy();

        RealVector ncf     = SME_ENS_Utils.getHistogramRealvec(edgeflag2Cond1, hcb);
        //TODO: replace edgeflag2Cond3 with edgeflag2Cond2 if going back to previous version
        RealVector ncb     = SME_ENS_Utils.getHistogramRealvec(edgeflag2Cond3, hcb);

        nt      =   SME_ENS_Utils.getLastindComp(ncb,ncf);
        ht      =   hcb.getEntry(nt);

        idmaxini        =   idmax.copy();
        edgeflag2Cond1 = SME_ENS_Utils.realmatSelectVector(edgeflag2,valk,1);

        double overlap2 = findOverlap2(edgeflag2Cond1); // TODO : check this as not equal to the matlab output

        edgeflag2B  = SME_ENS_Utils.padSymetricMatrix(edgeflag2, Boolean.TRUE);
        edgeflag2IB = SME_ENS_Utils.padSymetricMatrix(edgeflag2B, Boolean.TRUE);

        ImageStack base1 =  SME_ENS_Utils.find_base(edgeflag2IB, 3,Boolean.FALSE);
        base1            =  SME_ENS_Utils.findElementStack(base1,1);

        ZProjector zproject = new ZProjector();
        zproject.setMethod(0);
        zproject.setImage(new ImagePlus("IterativeProjection", base1));
        zproject.setMethod(ZProjector.SUM_METHOD);
        zproject.doProjection();

        int nrowsIB     = base1.getHeight();
        int ncolsIB     = base1.getWidth();

        RealMatrix class3   =   MatrixUtils.createRealMatrix(
                                SME_ENS_Utils.convertFloatMatrixToDoubles(
                                zproject.getProjection().getImageStack().getProcessor(1).getFloatArray(),
                                ncolsIB, nrowsIB));
        class3              =   class3.transpose();

        idmaxk              =   idmax;
        idmaxkB             =   SME_ENS_Utils.padSymetricMatrix(idmaxk, Boolean.TRUE);
        RealMatrix  IB      =   SME_ENS_Utils.padSymetricMatrix(idmaxkB, Boolean.TRUE);
        ImageStack  base    =   SME_ENS_Utils.find_base(IB, 3, Boolean.TRUE);

        zproject.setMethod(0);
        zproject.setImage(new ImagePlus("IterativeProjection", base));
        zproject.doProjection();
        RealMatrix Mold     =   MatrixUtils.createRealMatrix(SME_ENS_Utils.convertFloatMatrixToDoubles(
                                zproject.getProjection().getImageStack().getProcessor(1).getFloatArray(),
                                ncolsIB, nrowsIB));
        Mold                =   Mold.transpose();

        ImageStack varoldStack = SME_ENS_Utils.repmatMatrixVar(Mold, base);

        zproject.setImage(new ImagePlus("IterativeProjection", varoldStack));
        zproject.setMethod(3);
        zproject.doProjection();
        int rowDim          = varoldStack.getHeight();
        int colDim          = varoldStack.getWidth();

        RealMatrix varold2  = MatrixUtils.createRealMatrix(
                SME_ENS_Utils.convertFloatMatrixToDoubles(
                        zproject.getProjection().getImageStack().getProcessor(1).getFloatArray(),
                        colDim, rowDim));
        varold2             = varold2.transpose();

        RealMatrix M10      =   idmaxk.subtract(Mold);
        RealMatrix MD       =   M10.subtract(M10);
        RealMatrix s01      =   SME_ENS_Utils.realmatrixDoublepow(varold2.add(
                                SME_ENS_Utils.elementMultiply(M10, idmaxk.subtract(Mold.add(M10.scalarMultiply(1 / (double) 9))),Boolean.FALSE)
                                ).scalarMultiply(1 / (double) 8), 0.5);
        RealMatrix sD       =   SME_ENS_Utils.realmatrixDoublepow(varold2.add(
                                SME_ENS_Utils.elementMultiply(MD, Mold.subtract(Mold.add(MD.scalarMultiply(1 / (double) 9))),Boolean.FALSE)
                                ).scalarMultiply(1 / (double) 8), 0.5);

        RealMatrix sgain    =   s01.subtract(sD);
        RealMatrix dD       =   idmax.subtract(Mold);

        for(int j=0;j<edgeflag2.getColumnDimension();j++){
            for(int i=0;i<edgeflag2.getRowDimension();i++){
                if((class3.getEntry(i,j)>8)&(edgeflag2.getEntry(i,j)==1)){
                    if(sgain.getEntry(i,j)>0) {
                        sg = sg.append(sgain.getEntry(i, j));
                        dg = dg.append(Math.abs(dD.getEntry(i, j)));
                    }
                }
            }
        }

        WA                  =   dg.ebeDivide(sg);
        Percentile quantEng =   new Percentile();

        // TODO: Remplace the following code with uncommented block in case of returning to
        // the non smooth manifold
/*        if(dg.getDimension()>0)
            if(overlap2==0)
                WW                  =  WA.getMinValue();
            else
                WW                  =   Math.abs(quantEng.evaluate(WA.toArray(),overlap2*100));*/

        double lambda1  =   Math.abs(quantEng.evaluate(WA.toArray(),overlap2*100));

        double meanfg   =   SME_ENS_Utils.realvectorMean(edgeflag2Cond1);
        double meansfg  =   SME_ENS_Utils.realvectorMean(edgeflag2Cond3);
        double meanbg   =   SME_ENS_Utils.realvectorMean(edgeflag2Cond2);

        double RT       =   (meansfg-meanbg)/(meanfg-meanbg);

        double C1       =   1/lambda1;
        double C2       =   RT/lambda1;
        double C3       =   0/lambda1;

        SME_ENS_Utils.realmatSetVector(edgeflag3k,edgeflag2,1,C1);
        SME_ENS_Utils.realmatSetVector(edgeflag3k,edgeflag2,0.5,C2);
        SME_ENS_Utils.realmatSetVector(edgeflag3k,edgeflag2,0,C3);

        WW=1;
    }

    public double findOverlap2(RealVector edgeFlagCond){

        double sum1         = 0;
        double sum2         = 0;

        for(int i=0;i<edgeFlagCond.getDimension();i++){
            if(edgeFlagCond.getEntry(i)>ht){
                sum2++;
            }else{
                sum1++;
            }
        }

        return(sum1/sum2);
    }

    public void initStepEnergyOpt(){
        foregroundPixelVal      = MatrixUtils.createRealVector(new double[1]);
        Boolean vecInitialised  = Boolean.FALSE;

        for(int i=0;i<edgeflag2.getRowDimension();i++){
            for(int j=0;j<edgeflag2.getColumnDimension();j++){
                if(edgeflag2.getEntry(i,j)>0){
                    if(!vecInitialised){
                        foregroundPixelVal.setEntry(0,idmax.getEntry(i,j));
                        vecInitialised = Boolean.TRUE;
                    }else {
                        foregroundPixelVal = foregroundPixelVal.append(idmax.getEntry(i, j));
                    }
                }

            }
        }


        KE= foregroundPixelVal.getMaxValue()-foregroundPixelVal.getMinValue()+1;
        step=KE/100;

        //step     = sme_pluginGetManifold.getStack1().getSize()/(double)stepNumber;
    }

    public void computePSIprojection(){

        /**
         *
         Gx = imfilter(zprojf1, M, 'replicate', 'conv');
         Gy = imfilter(zprojf1, M', 'replicate', 'conv');
         */

        int[] imageSize = new int[3];
        float[] M       = { -1, 2, -1};

        RealMatrix qzr2  = idmaxk.copy();

        edgeflag2 = edgeflag.scalarAdd(1);
        ImageStack A = sme_pluginGetManifold.getStack();
        imageSize[0] = A.getHeight();imageSize[1] = A.getWidth();imageSize[2] = A.getSize();

        qzr2.walkInOptimizedOrder(new SetVisitorRound());
        qzr2.walkInOptimizedOrder(new SetVisitorTestValreset(3,imageSize[2],imageSize[2]));
        qzr2.walkInOptimizedOrder(new SetVisitorTestValreset(4,1,1));

        for(int k=0;k<imageSize[2];k++){
            int sft             =   k;
            RealMatrix qzr3     =   qzr2.scalarAdd(-sft);

            qzr3.walkInOptimizedOrder(new SetVisitorTestValreset(3,imageSize[2],imageSize[2]));
            qzr3.walkInOptimizedOrder(new SetVisitorTestValreset(4,1,1));
            RealMatrix zprojf1 = MatrixUtils.createRealMatrix(
                    qzr3.getRowDimension(),qzr3.getColumnDimension());
            RealVector qzr3Vec  =   SME_ENS_Utils.realmat2vector(qzr3,0);

            for(int kin=(int)qzr3Vec.getMinValue();kin<qzr3Vec.getMaxValue();kin++){
                RealMatrix temp =   MatrixUtils.createRealMatrix(
                        SME_ENS_Utils.convertFloatMatrixToDoubles(
                        sme_pluginGetManifold.getStack().getProcessor(kin).getFloatArray(),
                        imageSize[1],imageSize[0])).transpose();

                        for(int i1=0;i1<temp.getRowDimension();i1++){
                            for(int j1=0;j1<temp.getColumnDimension();j1++){
                                if(qzr3.getEntry(i1,j1)==kin){
                                    zprojf1.setEntry(i1,j1,temp.getEntry(i1,j1));
                                }
                            }
                        }
            }

            SME_ENS_Convolver sme_convolver = new SME_ENS_Convolver();
            FloatProcessor Gx = new FloatProcessor(SME_ENS_Utils.convertDoubleMatrixToFloat(
                    zprojf1.getData(),
                    sme_pluginGetManifold.getStack().getHeight(),
                    sme_pluginGetManifold.getStack().getWidth()));
            FloatProcessor Gy = new FloatProcessor(SME_ENS_Utils.convertDoubleMatrixToFloat(
                    zprojf1.getData(),
                    sme_pluginGetManifold.getStack().getHeight(),
                    sme_pluginGetManifold.getStack().getWidth()));

            sme_convolver.convolveFloat(Gx,M,1,3);
            sme_convolver.convolveFloat(Gy,M,3,1);

            Gx.abs(); Gy.abs();

            zprojf1 = MatrixUtils.createRealMatrix(SME_ENS_Utils.convertFloatMatrixToDoubles(Gx.getFloatArray(),
                    Gx.getWidth(),Gx.getHeight())).add( MatrixUtils.createRealMatrix(SME_ENS_Utils.convertFloatMatrixToDoubles(Gy.getFloatArray(),
                    Gy.getWidth(),Gy.getHeight())));
            RealMatrix  zprojf2 =   zprojf1.copy();

            /*************************************************/

            qzr3     =   qzr2.scalarAdd(sft);


            qzr3.walkInOptimizedOrder(new SetVisitorTestValreset(3,imageSize[2],imageSize[2]));
            qzr3.walkInOptimizedOrder(new SetVisitorTestValreset(4,1,1));
            zprojf1 = MatrixUtils.createRealMatrix(
                    qzr3.getRowDimension(),qzr3.getColumnDimension());

            qzr3Vec  =   SME_ENS_Utils.realmat2vector(qzr3,0);

            for(int kin=(int)qzr3Vec.getMinValue();kin<qzr3Vec.getMaxValue();kin++){
                RealMatrix temp =   MatrixUtils.createRealMatrix(
                        SME_ENS_Utils.convertFloatMatrixToDoubles(
                                sme_pluginGetManifold.getStack().getProcessor(kin).getFloatArray(),
                                imageSize[1],imageSize[0])).transpose();

                for(int i1=0;i1<temp.getRowDimension();i1++){
                    for(int j1=0;j1<temp.getColumnDimension();j1++){
                        if(qzr3.getEntry(i1,j1)==kin){
                            zprojf1.setEntry(i1,j1,temp.getEntry(i1,j1));
                        }
                    }
                }
            }

            sme_convolver = new SME_ENS_Convolver();
            Gx = new FloatProcessor(SME_ENS_Utils.convertDoubleMatrixToFloat(
                    zprojf1.getData(),
                    sme_pluginGetManifold.getStack().getHeight(),
                    sme_pluginGetManifold.getStack().getWidth()));
            Gy = new FloatProcessor(SME_ENS_Utils.convertDoubleMatrixToFloat(
                    zprojf1.getData(),
                    sme_pluginGetManifold.getStack().getHeight(),
                    sme_pluginGetManifold.getStack().getWidth()));

            sme_convolver.convolveFloat(Gx,M,1,3);
            sme_convolver.convolveFloat(Gy,M,3,1);

            Gx.abs(); Gy.abs();

            zprojf1 = MatrixUtils.createRealMatrix(SME_ENS_Utils.convertFloatMatrixToDoubles(Gx.getFloatArray(),
                    Gx.getWidth(),Gx.getHeight())).add( MatrixUtils.createRealMatrix(SME_ENS_Utils.convertFloatMatrixToDoubles(Gy.getFloatArray(),
                    Gy.getWidth(),Gy.getHeight())));

            RealVector zprojf1Vec = SME_ENS_Utils.realmat2vector(zprojf1,0);
            RealVector zprojf2Vec = SME_ENS_Utils.realmat2vector(zprojf2,0);
            double sumVec1 = 0;
            double sumVec2 = 0;

            for(int vecIndex=0;vecIndex<zprojf1Vec.getDimension();vecIndex++){
                sumVec1 = sumVec1 + zprojf1Vec.getEntry(vecIndex);
                sumVec2 = sumVec2 + zprojf2Vec.getEntry(vecIndex);
            }

            sftz = sftz.append((sumVec1+sumVec2)/totalz);
        }
    }

    public void applyEnergyOptimisation(Boolean showResults) {

        //initWparam();

        RealMatrix idmax1 = idmaxk.copy();
        RealMatrix idmax2 = idmaxk.copy();
        RealMatrix idmaxkB, IB = null;
        ZProjector zproject = new ZProjector();
        zproject.setMethod(0);
        double dist2goal = 0;
        double startProgressBar = 0 ;

        ENERGY_STEP = ENERGY_STEP*KE;
        startProgressBar = sme_pluginGetManifold.getProgressbar();
        double oldDistance = Integer.MAX_VALUE;
        double costIterStep=0;

        while (Math.abs(cost.getEntry(iter) - cost.getEntry((iter - 1))) > (ENERGY_STEP)) {

            iter++;
            idmax1 = idmaxk.scalarAdd(step).copy();
            idmax2 = idmaxk.scalarAdd(-step).copy();

            idmaxkB = SME_ENS_Utils.padSymetricMatrix(idmaxk, Boolean.TRUE);
            IB = SME_ENS_Utils.padSymetricMatrix(idmaxkB, Boolean.TRUE);

            ImageStack base = SME_ENS_Utils.find_base(IB, 3,Boolean.TRUE);
            zproject.setImage(new ImagePlus("IterativeProjection", base));
            zproject.setMethod(0);
            zproject.doProjection();
            int nrowsIB     = base.getHeight();
            int ncolsIB     = base.getWidth();

            //TODO remove this initialisation of Imagestac
            //ImageStack zprojStack = zproject.getProjection().getImageStack();
            //float[][] dataArray   = zproject.getProjection().getProcessor().getFloatArray();

            RealMatrix Mold =   MatrixUtils.createRealMatrix(
                    SME_ENS_Utils.convertFloatMatrixToDoubles(
                            zproject.getProjection().getImageStack().getProcessor(1).getFloatArray(),
                            ncolsIB, nrowsIB));
            Mold            =   Mold.transpose();

            ImageStack varoldStack = SME_ENS_Utils.repmatMatrixVar(Mold, base);

            zproject.setImage(new ImagePlus("IterativeProjection", varoldStack));
            zproject.setMethod(3);
            zproject.doProjection();
            int rowDim = varoldStack.getHeight();
            int colDim = varoldStack.getWidth();

            RealMatrix varold2 = MatrixUtils.createRealMatrix(
                    SME_ENS_Utils.convertFloatMatrixToDoubles(
                            zproject.getProjection().getImageStack().getProcessor(1).getFloatArray(),
                            colDim, rowDim));
            varold2             = varold2.transpose();

            RealMatrix d1 = SME_ENS_Utils.elementMultiply(idmax.subtract(idmax1),edgeflag3k,Boolean.TRUE);
            RealMatrix d2 = SME_ENS_Utils.elementMultiply(idmax.subtract(idmax2),edgeflag3k,Boolean.TRUE);
            RealMatrix d0 = SME_ENS_Utils.elementMultiply(idmax.subtract(idmaxk),edgeflag3k,Boolean.TRUE);

            RealMatrix M11 = idmax1.subtract(Mold);
            RealMatrix M12 = idmax2.subtract(Mold);
            RealMatrix M10 = idmaxk.subtract(Mold);

            RealMatrix s1 = SME_ENS_Utils.realmatrixDoublepow(varold2.add(
                    SME_ENS_Utils.elementMultiply(M11, idmax1.subtract(Mold.add(M11.scalarMultiply(1 / (double) 9))),Boolean.FALSE)
            ).scalarMultiply(1 / (double) 8), 0.5).scalarMultiply(WW);
            RealMatrix s2 = SME_ENS_Utils.realmatrixDoublepow(varold2.add(
                    SME_ENS_Utils.elementMultiply(M12, idmax2.subtract(Mold.add(M12.scalarMultiply(1 / (double) 9))),Boolean.FALSE)
            ).scalarMultiply(1 / (double) 8), 0.5).scalarMultiply(WW);
            RealMatrix s0 = SME_ENS_Utils.realmatrixDoublepow(varold2.add(
                    SME_ENS_Utils.elementMultiply(M10, idmaxk.subtract(Mold.add(M10.scalarMultiply(1 / (double) 9))),Boolean.FALSE)
            ).scalarMultiply(1 /(double) 8), 0.5).scalarMultiply(WW);

            RealMatrix c1 = d1.add(s1);
            RealMatrix c2 = d2.add(s2);
            RealMatrix c0 = d0.add(s0);

            ImageStack catStack = new ImageStack( c0.getColumnDimension() , c0.getRowDimension());
            catStack = SME_ENS_Utils.realmatrixCat(
                    SME_ENS_Utils.realmatrixCat(
                            SME_ENS_Utils.realmatrixCat(catStack,
                                    c0), c1), c2);

            zproject.setImage(new ImagePlus("IterativeProjection", catStack));
            zproject.setMethod(2);
            zproject.doProjection();
            RealMatrix minc = MatrixUtils.createRealMatrix(
                    SME_ENS_Utils.convertFloatMatrixToDoubles(
                            zproject.getProjection().getImageStack().getProcessor(1).getFloatArray(),
                            colDim, rowDim));
            minc    =   minc.transpose();

            RealMatrix shiftc   =   SME_ENS_Utils.getMinProjectionIndex(catStack);
            //shiftc              =   shiftc.transpose();
            //shiftc              =   shiftc.scalarAdd(-1);
            SME_ENS_Utils.replaceRealmatElements(shiftc,1,step);
            SME_ENS_Utils.replaceRealmatElements(shiftc,2,-step);

            idmaxk  =   idmaxk.add(shiftc);
            RealVector costIter = SME_ENS_Utils.realmat2vector(minc,0);
            costIterStep = costIter.getL1Norm()/(minc.getRowDimension()*minc.getColumnDimension());
            cost = cost.append(costIterStep);
            step = step * 0.99;

            if(iter>2) {
                dist2goal = startProgressBar+0.8*(((cost.getEntry(2)-costIterStep)/(cost.getEntry(2))));

                //System.out.println(Integer.toString(iter));
                //System.out.println(Double.toString(cost.getEntry(iter-1)));
                //System.out.println(Double.toString(cost.getEntry(iter)));
                //System.out.println(Double.toString(cost.getEntry(iter-1)-cost.getEntry(iter)));
                //IJ.showStatus("ENS PLUGIN ENERGY OPTIMISATION - STEP :: "+
                //        Integer.toString(iter) + " - COST = " + Double.toString(costIterStep));
                //System.out.println("Progress Bar :: " + Double.toString(dist2goal));
                if(dist2goal<1) {
                    sme_pluginGetManifold.updateProgressbar(dist2goal);
                }

                IJ.showStatus("                                 ");
            }else{
                sme_pluginGetManifold.updateProgressbar(startProgressBar);
                IJ.showStatus("                                 ");
            }
        }

        sme_pluginGetManifold.setCostData(SME_ENS_Utils.realvec2Stack(cost));
        if(showResults) {sme_pluginGetManifold.setSmePlotmaker(new SME_Data_Profiler(
                "ENERGY OPTIMISATION COST OPTIMISATION","ENERGY OPTIMISATION :: ITERATION","COST VALUE"
        ));
        ((SME_Data_Profiler) sme_pluginGetManifold.getSmePlotmaker()).run(new ImagePlus("Cost data",sme_pluginGetManifold.getCostData()));}

        computePSIprojection();

        sme_pluginGetManifold.setCostData(SME_ENS_Utils.realvec2Stack(sftz));
        if(showResults) {sme_pluginGetManifold.setSmePlotmaker(new SME_Data_Profiler(
                "ENERGY OPTIMISATION PROJECTION SUITABILITY INDEX","STACK INDEX","PSI VALUE"
        ));
        ((SME_Data_Profiler) sme_pluginGetManifold.getSmePlotmaker()).
                run(new ImagePlus("PSI data",sme_pluginGetManifold.getCostData()));}

    }

    public void setOutputManifold(Boolean showResult){
        double norm_factor  =   sme_pluginGetManifold.getStack1().getSize();
        int dimW            =   sme_pluginGetManifold.getStack1().getWidth();
        int dimH            =   sme_pluginGetManifold.getStack1().getHeight();

        RealMatrix normMnold = idmaxk.scalarMultiply(1/norm_factor).scalarMultiply(255);
        float[][] mfoldFlaot = SME_ENS_Utils.convertDoubleMatrixToFloat(normMnold.transpose().getData(),dimW,dimH);
        ImagePlus smeManifold = new ImagePlus("Manifold",((ImageProcessor) new FloatProcessor(mfoldFlaot)));
        sme_pluginGetManifold.setMfoldImage(smeManifold);
        if(showResult) sme_pluginGetManifold.getMfoldImage().show();
    }

    public void setOutputSME(Boolean showResult){
        double norm_factor  =   sme_pluginGetManifold.getStack1().getSize();
        int dimW            =   sme_pluginGetManifold.getStack1().getWidth();
        int dimH            =   sme_pluginGetManifold.getStack1().getHeight();

        ImageStack rawStack  = sme_pluginGetManifold.getStack();
        RealMatrix projMnold = MatrixUtils.createRealMatrix(dimH,dimW);

        for(int i=0;i<dimH;i++){
            for(int j=0;j<dimW;j++){
                int zIndex = ((int) Math.round(idmaxk.getEntry(i,j)))-1;
                projMnold.setEntry (i,j,rawStack.getVoxel(j,i,zIndex));
            }
        }

        float[][] mfoldFlaot = SME_ENS_Utils.convertDoubleMatrixToFloat(projMnold.transpose().getData(),dimW,dimH);
        ImagePlus smeManifold = new ImagePlus("ProjectionSME",((ImageProcessor) new FloatProcessor(mfoldFlaot)));
        sme_pluginGetManifold.setSmeImage(smeManifold);
        if(showResult) sme_pluginGetManifold.getSmeImage().show();
    }

    private static class SetVisitorRound extends DefaultRealMatrixChangingVisitor {
        @Override
        public double visit(int i, int j, double value) {
            return Math.round(value);
        }
    }

    private static class SetVisitorTestValreset extends DefaultRealMatrixChangingVisitor {

        double compareValue = 0;
        double resetValue   = 0;
        int compOperator    = 5;

        public SetVisitorTestValreset(int opVal, double valComp, double valReset){
            compareValue = valComp;
            resetValue   = valReset;
            compOperator = opVal ;
        }

        @Override
        public double visit(int i, int j, double value) {
            double returnVal = value;
            switch (compOperator) {
                case 0: // ==
                    if(value==compareValue) {returnVal=resetValue;}
                    break;
                case 1: // >=
                    if(value>=compareValue) {returnVal=resetValue;}
                    break;
                case 2: // <=
                    if(value<=compareValue) {returnVal=resetValue;}
                case 3: // >
                    if(value>compareValue) {returnVal=resetValue;}
                    break;
                case 4: // <
                    if(value<compareValue) {returnVal=resetValue;}
                    break;
                case 5: // !=
                    if(value!=compareValue) {returnVal=resetValue;}
                    break;
            }
            return returnVal;
        }
    }
}