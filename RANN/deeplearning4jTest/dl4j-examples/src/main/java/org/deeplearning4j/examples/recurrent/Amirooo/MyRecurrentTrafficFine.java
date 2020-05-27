package org.deeplearning4j.examples.recurrent.Amirooo;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MyRecurrentTrafficFine {

    public static ArrayList<Integer> transactionLengths=new ArrayList<>();
    private int numCases=14332;
    private int largestSequence=12;
    private int numInput=57;

    public TotalPredictionError totalPredictionError=new TotalPredictionError();

    public boolean newRun=false;

    private static final int HIDDEN_LAYER_WIDTH = 200;
    private static final int HIDDEN_LAYER_CONT = 4;

    private double knownTransactions=0.6;

    private double[] normalizationInformation;

    MyRecurrentTrafficFine()
    {
        NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
        builder.seed((int)(Math.random()*1000));
        builder.biasInit(1);
        builder.miniBatch(true);
        builder.updater(new RmsProp(0.001));
        builder.weightInit(WeightInit.XAVIER);

        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();

        for (int i = 0; i < HIDDEN_LAYER_CONT; i++) {
            LSTM.Builder hiddenLayerBuilder = new LSTM.Builder();
            hiddenLayerBuilder.nIn(i == 0 ? numInput : HIDDEN_LAYER_WIDTH);
            hiddenLayerBuilder.nOut(HIDDEN_LAYER_WIDTH);
            hiddenLayerBuilder.activation(Activation.SIGMOID);
            listBuilder.layer(i, hiddenLayerBuilder.build());
        }

        // we need to use RnnOutputLayer for our RNN
        RnnOutputLayer.Builder outputLayerBuilder = new RnnOutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS);
        outputLayerBuilder.activation(Activation.SIGMOID);
        outputLayerBuilder.nIn(HIDDEN_LAYER_WIDTH);
        outputLayerBuilder.nOut(numInput);
        listBuilder.layer(HIDDEN_LAYER_CONT, outputLayerBuilder.build());

        // create network
        MultiLayerConfiguration conf = listBuilder.build();
        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(5));

        INDArray input = Nd4j.zeros(numCases+1, numInput, largestSequence);
        INDArray labels = Nd4j.zeros(numCases+1, numInput, largestSequence);

        String fileName= "F:/My_software_develops/BaggingPetriNetProject/rann_baggingnet/roadTrafficFinesLogANN_DATA_50000.csv";
        File file= new File(fileName);

        Scanner inputStream;
        try{
            inputStream = new Scanner(file);
            int lastCase=0;
            int row=0;
            while(inputStream.hasNext())
            {
                String line= inputStream.next();
                if(line.contains("***"))
                {
                    lastCase=lastCase+1;
                    row=0;
                    continue;
                }else{
                    String[] values = line.split(",");
                    for(int j=0;j<values.length;j++)
                    {
                        try{
                            input.putScalar(new int[] { lastCase, row, j }, Double.valueOf(values[j]));
                            if(j!=values.length-1)
                            {
                                labels.putScalar(new int[] { lastCase, row, j }, Double.valueOf(values[j+1]));
                            }else{
                                labels.putScalar(new int[] { lastCase, row, j }, Double.valueOf(values[0]));
                            }
                            if(row==12)
                            {
                                if(values[j].equals("1"))
                                {
                                    transactionLengths.add(j);
                                }
                            }
                        }catch(Exception ex)
                        {
                            System.out.println(ex.toString());
                            System.out.println(lastCase);
                            System.out.println(row);
                            System.out.println(j);
                        }

                    }
                }
                row=row+1;
            }

            inputStream.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println(input);
        System.out.println(labels);

//        for(int caseId=0;caseId<input.size(0);caseId++) {
//            for (int rowId = 0; rowId < input.size(1); rowId++) {
//                for (int seqId = 0; seqId < input.size(2); seqId++) {
//                    System.out.println(input.getDouble(caseId, rowId, seqId));
//                }
//            }
//        }


        input=normalize(input);
        labels=normalize(labels);

        System.out.println(input);
        System.out.println(labels);

//        for(int caseId=0;caseId<input.size(0);caseId++) {
//            for (int rowId = 0; rowId < input.size(1); rowId++) {
//                for (int seqId = 0; seqId < input.size(2); seqId++) {
//                    System.out.println(input.getDouble(caseId, rowId, seqId));
//                }
//            }
//        }

        DataSet trainingData = new DataSet(input, labels);


        // ParallelWrapper will take care of load balancing between GPUs.
//        ParallelWrapper wrapper = new ParallelWrapper.Builder(net)
//            // DataSets prefetching options. Set this value proportional to number of actual devices
//            .prefetchBuffer(24)
//
//            // set number of workers equal to number of available devices. x1-x2 are good values to start with
//            .workers(2)
//
//            .trainerFactory(new SymmetricTrainerContext())
//
//            .trainingMode(ParallelWrapper.TrainingMode.SHARED_GRADIENTS)
//
//            .thresholdAlgorithm(new AdaptiveThresholdAlgorithm())
//
//            .build();

//        wrapper.setListeners(new ScoreIterationListener(10));

//        ArrayList<Pair<INDArray, INDArray>> iterable=new ArrayList<>();
//        iterable.add(new Pair<>(input, labels));

//        DataSetIterator data = new INDArrayDataSetIterator(iterable,128);

        if(newRun==true)
        {
            for (int epoch = 0; epoch < 100; epoch++) {
                System.out.println("Epoch " + epoch);

                net.fit(trainingData);
//            wrapper.fit(data);
                System.out.print("\n");
            }

            //Save the model
            File locationToSave = new File("MyMultiLayerNetwork.zip");      //Where to save the network. Note: the file is in .zip format - can be opened externally
            boolean saveUpdater = true;                                             //Updater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this if you want to train your network more in the future
            try {
                net.save(locationToSave, saveUpdater);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            File locationToSave = new File("MyMultiLayerNetwork.zip");
            boolean saveUpdater = true;
            try {
                net = MultiLayerNetwork.load(locationToSave, saveUpdater);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }







        for(int i=0;i<numCases;i++)
        {
            INDArray output=null;
            int lastKnownEvent=-1;
            net.rnnClearPreviousState();
            for(int j=0;j<Math.round((transactionLengths.get(i)+1)*knownTransactions);j++)
            {
                INDArray test = to3DMatrix(input.get(NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.point(j)),numInput);
                output = net.rnnTimeStep(test);
                if(j==Math.round((transactionLengths.get(i)+1)*knownTransactions)-1)
                {
                    lastKnownEvent=j;
                }
            }
            INDArray future = input.get(NDArrayIndex.interval(i,i+1), NDArrayIndex.all(), NDArrayIndex.interval(lastKnownEvent+1,transactionLengths.get(i)+1));
            CasePredictionError caseError = predict(net, future, output, numInput);
//            System.out.println(caseError.toString());
            totalPredictionError.caseErrors.add(caseError);
        }

        System.out.println(totalPredictionError.toString());

    }

    public CasePredictionError predict(MultiLayerNetwork net, INDArray futureTransactions, INDArray initialOutput, int numInput)
    {
        INDArray allPredictions = decodeOutput(initialOutput,numInput).dup();

        for (int i=0;i<10;i++) {
            INDArray newTransaction = decodeOutput(initialOutput,numInput);

            if(i!=0)
            {
                allPredictions = Nd4j.concat(2,allPredictions,newTransaction);
            }

//            System.out.println(initialOutput.toString());

//            System.out.println(newTransaction.toString());

            if(newTransaction.getInt(0,12,0)==1)
            {
                System.out.println("End observed");
                break;
            }

            initialOutput = net.rnnTimeStep(newTransaction);
        }
        return getError(futureTransactions,allPredictions);
    }

    public CasePredictionError getError(INDArray futureTransactions, INDArray allPredictions)
    {
        futureTransactions=denormalize(futureTransactions);
        allPredictions=denormalize(allPredictions);
        int eventPredictionError=0;
        double durationError=0;
        double featuresError=0;
        if(futureTransactions.size(2)>allPredictions.size(2))
        {
            double lastTime=0;
            for(int i=0;i<allPredictions.size(2);i++)
            {
                for(int j=0;j<13;j++)
                {
                    if(futureTransactions.getDouble(0,j,i)!=allPredictions.getDouble(0,j,i))
                    {
                        eventPredictionError=eventPredictionError+1;
                        break;
                    }
                }
                durationError=durationError+Math.abs(futureTransactions.getDouble(0,13,i)-allPredictions.getDouble(0,13,i));
                for(int j=14;j<57;j++)
                {
                    featuresError=featuresError+Math.abs(futureTransactions.getDouble(0,j,i)-allPredictions.getDouble(0,j,i));
                }
                lastTime=allPredictions.getDouble(0,13,i);
            }
            for(int i=(int)allPredictions.size(2);i<futureTransactions.size(2);i++)
            {
                eventPredictionError=eventPredictionError+1;
//                for(int j=0;j<13;j++)
//                {
//                    eventPredictionError=eventPredictionError+1;
//                }
                durationError=durationError+Math.abs(futureTransactions.getDouble(0,13,i)-lastTime);
                for(int j=14;j<57;j++)
                {
                    featuresError=featuresError+futureTransactions.getDouble(0,j,i);
                }
            }
        }else{
            double lastTime=0;
            for(int i=0;i<futureTransactions.size(2);i++)
            {
                for(int j=0;j<13;j++)
                {
                    if(futureTransactions.getInt(0,j,i)!=allPredictions.getDouble(0,j,i))
                    {
                        eventPredictionError=eventPredictionError+1;
                        break;
                    }
                }
                durationError=durationError+Math.abs(futureTransactions.getDouble(0,13,i)-allPredictions.getDouble(0,13,i));
                for(int j=14;j<57;j++)
                {
                    featuresError=featuresError+Math.abs(futureTransactions.getDouble(0,j,i)-allPredictions.getDouble(0,j,i));
                }
                lastTime=allPredictions.getDouble(0,13,i);
            }
            for(int i=(int)futureTransactions.size(2);i<allPredictions.size(2);i++)
            {
//                for(int j=0;j<13;j++)
//                {
                eventPredictionError=eventPredictionError+1;
//                }
                durationError=durationError+Math.abs(lastTime-allPredictions.getDouble(0,13,i));
                for(int j=14;j<57;j++)
                {
                    featuresError=featuresError+allPredictions.getDouble(0,j,i);
                }
            }
        }
        CasePredictionError error=new CasePredictionError(eventPredictionError,durationError,featuresError);
//        System.out.println(error.toString());
        return error;
    }

    public INDArray decodeOutput(INDArray input,int numInput)
    {
        INDArray newTransaction = Nd4j.zeros(1, numInput, 1);

        int selection = Nd4j.getExecutioner().exec(new IMax(input.get(NDArrayIndex.point(0),NDArrayIndex.interval(0,13),NDArrayIndex.point(0)), 0)).getInt(0);

        newTransaction.putScalar(new int[] { 0, selection, 0 }, 1);
        for(int i=0;i<13;i++)
        {
            if(i!=selection)
            {
                newTransaction.putScalar(new int[] { 0, i, 0 }, -1);
            }
        }

//        newTransaction.putScalar(new int[] { 0, 0, 0 }, Math.round(input.getDouble(0,0,0)));
//        newTransaction.putScalar(new int[] { 0, 1, 0 }, Math.round(input.getDouble(0,1,0)));
//        newTransaction.putScalar(new int[] { 0, 2, 0 }, Math.round(input.getDouble(0,2,0)));
//        newTransaction.putScalar(new int[] { 0, 3, 0 }, Math.round(input.getDouble(0,3,0)));
//        newTransaction.putScalar(new int[] { 0, 4, 0 }, Math.round(input.getDouble(0,4,0)));
//        newTransaction.putScalar(new int[] { 0, 5, 0 }, Math.round(input.getDouble(0,5,0)));
//        newTransaction.putScalar(new int[] { 0, 6, 0 }, Math.round(input.getDouble(0,6,0)));
//        newTransaction.putScalar(new int[] { 0, 7, 0 }, Math.round(input.getDouble(0,7,0)));
//        newTransaction.putScalar(new int[] { 0, 8, 0 }, Math.round(input.getDouble(0,8,0)));
//        newTransaction.putScalar(new int[] { 0, 9, 0 }, Math.round(input.getDouble(0,9,0)));
//        newTransaction.putScalar(new int[] { 0, 10, 0 }, Math.round(input.getDouble(0,10,0)));
//        newTransaction.putScalar(new int[] { 0, 11, 0 }, Math.round(input.getDouble(0,11,0)));
//        newTransaction.putScalar(new int[] { 0, 12, 0 }, Math.round(input.getDouble(0,12,0)));

        if(input.getDouble(0,13,0)>0)
        {
            newTransaction.putScalar(new int[] { 0, 13, 0 }, input.getDouble(0,13,0));
        }else{
            newTransaction.putScalar(new int[] { 0, 13, 0 }, 0);
        }

        if(input.getDouble(0,14,0)>0)
        {
            newTransaction.putScalar(new int[] { 0, 14, 0 }, input.getDouble(0,14,0));
        }else{
            newTransaction.putScalar(new int[] { 0, 14, 0 }, -1);
        }

        if(input.getDouble(0,15,0)>0)
        {
            newTransaction.putScalar(new int[] { 0, 15, 0 }, input.getDouble(0,15,0));
        }else{
            newTransaction.putScalar(new int[] { 0, 15, 0 }, -1);
        }

        if(input.getDouble(0,16,0)>0)
        {
            newTransaction.putScalar(new int[] { 0, 16, 0 }, input.getDouble(0,16,0));
        }else{
            newTransaction.putScalar(new int[] { 0, 16, 0 }, -1);
        }

        if(input.getDouble(0,17,0)>0)
        {
            newTransaction.putScalar(new int[] { 0, 17, 0 }, input.getDouble(0,17,0));
        }else{
            newTransaction.putScalar(new int[] { 0, 17, 0 }, -1);
        }

        if(input.getDouble(0,18,0)>0)
        {
            newTransaction.putScalar(new int[] { 0, 18, 0 }, input.getDouble(0,18,0));
        }else{
            newTransaction.putScalar(new int[] { 0, 18, 0 }, -1);
        }

        selection = Nd4j.getExecutioner().exec(new IMax(input.get(NDArrayIndex.point(0),NDArrayIndex.interval(19,21),NDArrayIndex.point(0)), 0)).getInt(0);

        newTransaction.putScalar(new int[] { 0, 19+selection, 0 }, 1);
        for(int i=19;i<21;i++)
        {
            if(i!=19+selection)
            {
                newTransaction.putScalar(new int[] { 0, i, 0 }, -1);
            }
        }

//        newTransaction.putScalar(new int[] { 0, 19, 0 }, Math.round(input.getDouble(0,19,0)));
//        newTransaction.putScalar(new int[] { 0, 20, 0 }, Math.round(input.getDouble(0,20,0)));


        if(input.getDouble(0,21,0)>0)
        {
            newTransaction.putScalar(new int[] { 0, 21, 0 }, input.getDouble(0,21,0));
        }else{
            newTransaction.putScalar(new int[] { 0, 21, 0 }, -1);
        }

        selection = Nd4j.getExecutioner().exec(new IMax(input.get(NDArrayIndex.point(0),NDArrayIndex.interval(22,26),NDArrayIndex.point(0)), 0)).getInt(0);

        newTransaction.putScalar(new int[] { 0, 22+selection, 0 }, 1);
        for(int i=22;i<26;i++)
        {
            if(i!=22+selection)
            {
                newTransaction.putScalar(new int[] { 0, i, 0 }, -1);
            }
        }

//        newTransaction.putScalar(new int[] { 0, 22, 0 }, Math.round(input.getDouble(0,22,0)));
//        newTransaction.putScalar(new int[] { 0, 23, 0 }, Math.round(input.getDouble(0,23,0)));
//        newTransaction.putScalar(new int[] { 0, 24, 0 }, Math.round(input.getDouble(0,24,0)));
//        newTransaction.putScalar(new int[] { 0, 25, 0 }, Math.round(input.getDouble(0,25,0)));

        if(input.getDouble(0,26,0)<0)
        {
            newTransaction.putScalar(new int[] { 0, 26, 0 }, -1);
        }else{
            newTransaction.putScalar(new int[] { 0, 26, 0 }, input.getDouble(0,26,0));
        }


        selection = Nd4j.getExecutioner().exec(new IMax(input.get(NDArrayIndex.point(0),NDArrayIndex.interval(27,30),NDArrayIndex.point(0)), 0)).getInt(0);

        newTransaction.putScalar(new int[] { 0, 27+selection, 0 }, 1);
        for(int i=27;i<30;i++)
        {
            if(i!=27+selection)
            {
                newTransaction.putScalar(new int[] { 0, i, 0 }, -1);
            }
        }

//        newTransaction.putScalar(new int[] { 0, 27, 0 }, Math.round(input.getDouble(0,27,0)));
//        newTransaction.putScalar(new int[] { 0, 28, 0 }, Math.round(input.getDouble(0,28,0)));
//        newTransaction.putScalar(new int[] { 0, 29, 0 }, Math.round(input.getDouble(0,29,0)));

        selection = Nd4j.getExecutioner().exec(new IMax(input.get(NDArrayIndex.point(0),NDArrayIndex.interval(30,56),NDArrayIndex.point(0)), 0)).getInt(0);

        newTransaction.putScalar(new int[] { 0, 30+selection, 0 }, 1);
        for(int i=30;i<56;i++)
        {
            if(i!=30+selection)
            {
                newTransaction.putScalar(new int[] { 0, i, 0 }, -1);
            }
        }

//        newTransaction.putScalar(new int[] { 0, 30, 0 }, Math.round(input.getDouble(0,30,0)));
//        newTransaction.putScalar(new int[] { 0, 31, 0 }, Math.round(input.getDouble(0,31,0)));
//        newTransaction.putScalar(new int[] { 0, 32, 0 }, Math.round(input.getDouble(0,32,0)));
//        newTransaction.putScalar(new int[] { 0, 33, 0 }, Math.round(input.getDouble(0,33,0)));
//        newTransaction.putScalar(new int[] { 0, 34, 0 }, Math.round(input.getDouble(0,34,0)));
//        newTransaction.putScalar(new int[] { 0, 35, 0 }, Math.round(input.getDouble(0,35,0)));
//        newTransaction.putScalar(new int[] { 0, 36, 0 }, Math.round(input.getDouble(0,36,0)));
//        newTransaction.putScalar(new int[] { 0, 37, 0 }, Math.round(input.getDouble(0,37,0)));
//        newTransaction.putScalar(new int[] { 0, 38, 0 }, Math.round(input.getDouble(0,38,0)));
//        newTransaction.putScalar(new int[] { 0, 39, 0 }, Math.round(input.getDouble(0,39,0)));
//        newTransaction.putScalar(new int[] { 0, 40, 0 }, Math.round(input.getDouble(0,40,0)));
//        newTransaction.putScalar(new int[] { 0, 41, 0 }, Math.round(input.getDouble(0,41,0)));
//        newTransaction.putScalar(new int[] { 0, 42, 0 }, Math.round(input.getDouble(0,42,0)));
//        newTransaction.putScalar(new int[] { 0, 43, 0 }, Math.round(input.getDouble(0,43,0)));
//        newTransaction.putScalar(new int[] { 0, 44, 0 }, Math.round(input.getDouble(0,44,0)));
//        newTransaction.putScalar(new int[] { 0, 45, 0 }, Math.round(input.getDouble(0,45,0)));
//        newTransaction.putScalar(new int[] { 0, 46, 0 }, Math.round(input.getDouble(0,46,0)));
//        newTransaction.putScalar(new int[] { 0, 47, 0 }, Math.round(input.getDouble(0,47,0)));
//        newTransaction.putScalar(new int[] { 0, 48, 0 }, Math.round(input.getDouble(0,48,0)));
//        newTransaction.putScalar(new int[] { 0, 49, 0 }, Math.round(input.getDouble(0,49,0)));
//        newTransaction.putScalar(new int[] { 0, 50, 0 }, Math.round(input.getDouble(0,50,0)));
//        newTransaction.putScalar(new int[] { 0, 51, 0 }, Math.round(input.getDouble(0,51,0)));
//        newTransaction.putScalar(new int[] { 0, 52, 0 }, Math.round(input.getDouble(0,52,0)));
//        newTransaction.putScalar(new int[] { 0, 53, 0 }, Math.round(input.getDouble(0,53,0)));
//        newTransaction.putScalar(new int[] { 0, 54, 0 }, Math.round(input.getDouble(0,54,0)));
//        newTransaction.putScalar(new int[] { 0, 55, 0 }, Math.round(input.getDouble(0,55,0)));

        if(input.getDouble(0,56,0)>0)
        {
            newTransaction.putScalar(new int[] { 0, 56, 0 }, input.getDouble(0,56,0));
        }else{
            newTransaction.putScalar(new int[] { 0, 56, 0 }, -1);
        }

        return newTransaction;
    }

    public INDArray to3DMatrix(INDArray test, int numInput)
    {
        INDArray converted = Nd4j.zeros(1, numInput, 1);
        for(int i=0;i<test.size(0);i++)
        {
            converted.putScalar(new int[] { 0, i, 0 }, test.getDouble(i));
        }
        return converted;
    }

    public INDArray normalize(INDArray input)
    {
        INDArray output=input.dup();
        for(int caseId=0;caseId<input.size(0);caseId++)
        {
            if(caseId%1000==0)
            {
                System.out.println(caseId);
            }

            for(int rowId=0;rowId<input.size(1);rowId++)
            {
                for(int seqId=0;seqId<input.size(2);seqId++)
                {
                    if(input.getDouble(caseId, rowId, seqId)==-1)
                    {
                        output.putScalar(new int[] { caseId, rowId, seqId }, -1);
                    }else{
                        if(rowId==13)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)/1e7d);
                        }else if(rowId==14)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)/100d);
                        }
                        else if(rowId==15)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)/100d);
                        }
                        else if(rowId==16)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)/100d);
                        }else if(rowId==17)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)/100d);
                        }else if(rowId==18)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)/100d);
                        }
                        else if(rowId==19)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)/100d);
                        }
                        else if(rowId==27)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)/1000d);
                        }else{
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId));
                        }
                    }
                }
            }
        }
        return output;
    }

    public INDArray denormalize(INDArray input)
    {
        INDArray output=input.dup();
        for(int caseId=0;caseId<input.size(0);caseId++)
        {
            for(int rowId=0;rowId<input.size(1);rowId++)
            {
                for(int seqId=0;seqId<input.size(2);seqId++)
                {
                    if(input.getDouble(caseId, rowId, seqId)<0)
                    {
                        output.putScalar(new int[] { caseId, rowId, seqId }, -1);
                    }else{
                        if(rowId==13)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)*1e7d);
                        }else if(rowId==14)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)*100d);
                        }
                        else if(rowId==15)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)*100d);
                        }
                        else if(rowId==16)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)*100d);
                        }else if(rowId==17)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)*100d);
                        }else if(rowId==18)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)*100d);
                        }
                        else if(rowId==19)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)*100d);
                        }
                        else if(rowId==27)
                        {
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId)*1000d);
                        }else{
                            output.putScalar(new int[] { caseId, rowId, seqId }, input.getDouble(caseId, rowId, seqId));
                        }
                    }
                }
            }
        }
        return output;
    }

    public static void main(String[] args) throws Exception {
        MyRecurrentTrafficFine rnn=new MyRecurrentTrafficFine();
    }

}
