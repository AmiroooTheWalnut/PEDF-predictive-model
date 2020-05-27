/* *****************************************************************************
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.deeplearning4j.examples.recurrent.basic;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * This example trains a RNN. When trained we only have to put the first
 * character of LEARNSTRING to the RNN, and it will recite the following chars
 *
 * @author Peter Grossmann
 */
public class BasicRNNExampleMultipleStrings {

	// define a sentence to learn.
    // Add a special character at the beginning so the RNN learns the complete string and ends with the marker.
	private static final char[] LEARNSTRING = "*Der Cottbuser Postkutscher putzt den Cottbuser Postkutschkasten.".toCharArray();

	public static ArrayList<char[]> multiStrings=new ArrayList<>();
    public static ArrayList<char[]> multiCategory=new ArrayList<>();
	public static ArrayList<double[]> multiRegFeatures=new ArrayList<>();
//    public static ArrayList<int[]> multiCategoryFeatures=new ArrayList<>();

	// a list of all possible characters
	private static final List<Character> LEARNSTRING_CHARS_LIST = new ArrayList<>();

    // a list of all possible characters
    private static final List<Character> LEARNCATEGORY_CHARS_LIST = new ArrayList<>();

	// RNN dimensions
	private static final int HIDDEN_LAYER_WIDTH = 200;
	private static final int HIDDEN_LAYER_CONT = 3;

	public static int largestSequence=-1;

	public static void main(String[] args) {

        multiStrings.add("AEFBD".toCharArray());
        multiRegFeatures.add(new double[]{1,2,2,3,4});
        multiCategory.add("AAABB".toCharArray());

        multiStrings.add("ABFEBD".toCharArray());
        multiRegFeatures.add(new double[]{1,2,2,2,4,4});
        multiCategory.add("BAABBA".toCharArray());

        multiStrings.add("AEEFBD".toCharArray());
        multiRegFeatures.add(new double[]{2,2,2,3,3,3});
        multiCategory.add("AAABBB".toCharArray());

        multiStrings.add("AEBBFFED".toCharArray());
        multiRegFeatures.add(new double[]{1,2,2,3,5,5,5,6});
        multiCategory.add("ABBAAABA".toCharArray());

//		// create a dedicated list of possible chars in LEARNSTRING_CHARS_LIST
//		LinkedHashSet<Character> LEARNSTRING_CHARS = new LinkedHashSet<>();
//		for (char c : LEARNSTRING)
//			LEARNSTRING_CHARS.add(c);
		LEARNSTRING_CHARS_LIST.add('A');
        LEARNSTRING_CHARS_LIST.add('B');
        LEARNSTRING_CHARS_LIST.add('C');
        LEARNSTRING_CHARS_LIST.add('D');
        LEARNSTRING_CHARS_LIST.add('E');
        LEARNSTRING_CHARS_LIST.add('F');

        LEARNCATEGORY_CHARS_LIST.add('A');
        LEARNCATEGORY_CHARS_LIST.add('B');

		// some common parameters
		NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
		builder.seed((int)(Math.random()*1000));
		builder.biasInit(1);
		builder.miniBatch(false);
		builder.updater(new RmsProp(0.001));
		builder.weightInit(WeightInit.XAVIER);

		ListBuilder listBuilder = builder.list();

		int numInput=LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.size()+1;

		// first difference, for rnns we need to use LSTM.Builder
		for (int i = 0; i < HIDDEN_LAYER_CONT; i++) {
			LSTM.Builder hiddenLayerBuilder = new LSTM.Builder();
			hiddenLayerBuilder.nIn(i == 0 ? numInput : HIDDEN_LAYER_WIDTH);
			hiddenLayerBuilder.nOut(HIDDEN_LAYER_WIDTH);
			// adopted activation function from LSTMCharModellingExample
			// seems to work well with RNNs
			hiddenLayerBuilder.activation(Activation.TANH);
			listBuilder.layer(i, hiddenLayerBuilder.build());
		}

		// we need to use RnnOutputLayer for our RNN
		RnnOutputLayer.Builder outputLayerBuilder = new RnnOutputLayer.Builder(LossFunction.MSE);
		// softmax normalizes the output neurons, the sum of all outputs is 1
		// this is required for our sampleFromDistribution-function
		outputLayerBuilder.activation(Activation.ELU);
		outputLayerBuilder.nIn(HIDDEN_LAYER_WIDTH);
		outputLayerBuilder.nOut(numInput);
		listBuilder.layer(HIDDEN_LAYER_CONT, outputLayerBuilder.build());

		// create network
		MultiLayerConfiguration conf = listBuilder.build();
		MultiLayerNetwork net = new MultiLayerNetwork(conf);
		net.init();
		net.setListeners(new ScoreIterationListener(1));

        largestSequence=8;

		/*
		 * CREATE OUR TRAINING DATA
		 */
		// create input and output arrays: SAMPLE_INDEX, INPUT_NEURON,
		// SEQUENCE_POSITION
		INDArray input = Nd4j.zeros(multiStrings.size(), numInput, largestSequence);
		INDArray labels = Nd4j.zeros(multiStrings.size(), numInput, largestSequence);

        for(int i=0;i<multiStrings.size();i++)
        {
            // loop through our sample-sentence
            int samplePos = 0;
            for (char currentChar : multiStrings.get(i)) {
                // small hack: when currentChar is the last, take the first char as
                // nextChar - not really required. Added to this hack by adding a starter first character.
                char nextChar = multiStrings.get(i)[(samplePos + 1) % (multiStrings.get(i).length)];
                // input neuron for current-char is 1 at "samplePos"
                input.putScalar(new int[] { i, LEARNSTRING_CHARS_LIST.indexOf(currentChar), samplePos }, 1);
                // output neuron for next-char is 1 at "samplePos"
                labels.putScalar(new int[] { i, LEARNSTRING_CHARS_LIST.indexOf(nextChar), samplePos }, 1);
                samplePos++;
            }

            // loop through our sample-sentence
            samplePos = 0;
            for (char currentChar : multiCategory.get(i)) {
                // small hack: when currentChar is the last, take the first char as
                // nextChar - not really required. Added to this hack by adding a starter first character.
                char nextChar = multiCategory.get(i)[(samplePos + 1) % (multiCategory.get(i).length)];
                // input neuron for current-char is 1 at "samplePos"
                input.putScalar(new int[] { i, (LEARNSTRING_CHARS_LIST.size())+LEARNCATEGORY_CHARS_LIST.indexOf(currentChar), samplePos }, 1);
                // output neuron for next-char is 1 at "samplePos"
                labels.putScalar(new int[] { i, (LEARNSTRING_CHARS_LIST.size())+LEARNCATEGORY_CHARS_LIST.indexOf(nextChar), samplePos }, 1);
                samplePos++;
            }
//            if(multiCategory.get(i).length<largestSequence)
//            {
//                for(int m=multiCategory.get(i).length;m<largestSequence;m++)
//                {
//                    for(int k=0;k<numInput;k++)
//                    {
//                        input.putScalar(new int[] { i, k, m }, -1);
//                        labels.putScalar(new int[] { i, k, m }, -1);
//                    }
//                }
//            }

            // loop through our sample-sentence
            samplePos = 0;
            for (double currentValue : multiRegFeatures.get(i)) {
                // small hack: when currentChar is the last, take the first char as
                // nextChar - not really required. Added to this hack by adding a starter first character.
                double nextValue = multiRegFeatures.get(i)[(samplePos + 1) % (multiRegFeatures.get(i).length)];
                // input neuron for current-char is 1 at "samplePos"
                input.putScalar(new int[] { i, (LEARNSTRING_CHARS_LIST.size())+(LEARNCATEGORY_CHARS_LIST.size()), samplePos }, currentValue);
                // output neuron for next-char is 1 at "samplePos"
                labels.putScalar(new int[] { i, (LEARNSTRING_CHARS_LIST.size())+(LEARNCATEGORY_CHARS_LIST.size()), samplePos }, nextValue);
                samplePos++;
            }
        }

        for(int i=0;i<multiStrings.size();i++)
        {
            for(int j=0;j<numInput;j++)
            {
                for(int k=0;k<largestSequence;k++)
                {
                    double value = input.get(NDArrayIndex.point(i), NDArrayIndex.point(j), NDArrayIndex.point(k)).getDouble(0, 0, 0);
                    System.out.print(value);
                    System.out.print(",");
                }
                System.out.println();
            }
            System.out.println("***");
        }

        System.out.println(input);
        System.out.println(labels);

		DataSet trainingData = new DataSet(input, labels);

		// some epochs
		for (int epoch = 0; epoch < 400; epoch++) {

			System.out.println("Epoch " + epoch);

			// train the data
			net.fit(trainingData);

//			if(epoch>195)
//            {
//                System.out.println("DEBUG!!! BREAKPOINT!");
//            }

            for(int k=0;k<multiStrings.size();k++)
            {
                // clear current stance from the last example
                net.rnnClearPreviousState();

                // put the first character into the rrn as an initialisation
                INDArray testInit = Nd4j.zeros(1,numInput, 1);
                testInit.putScalar(LEARNSTRING_CHARS_LIST.indexOf(multiStrings.get(k)[0]), 1);

                testInit.putScalar(LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.indexOf(multiCategory.get(k)[0]), 1);

                testInit.putScalar(LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.size(), multiRegFeatures.get(k)[0]);

                // run one step -> IMPORTANT: rnnTimeStep() must be called, not
                // output()
                // the output shows what the net thinks what should come next
                INDArray output = net.rnnTimeStep(testInit);

                // now the net should guess LEARNSTRING.length more characters
//            for (char ignored : LEARNSTRING) {
                for (int i=0;i<10;i++) {
                    INDArray outputSequence=output.get(NDArrayIndex.all(),NDArrayIndex.interval(0,LEARNSTRING_CHARS_LIST.size()),NDArrayIndex.all());
                    int sampledCharacterIdx = Nd4j.getExecutioner().exec(new IMax(outputSequence, 1)).getInt(0);

                    // print the chosen output
                    System.out.print(LEARNSTRING_CHARS_LIST.get(sampledCharacterIdx));

                    // use the last output as input
                    INDArray nextInput = Nd4j.zeros(1, numInput, 1);
                    nextInput.putScalar(sampledCharacterIdx, 1);


                    INDArray outputCategory=output.get(NDArrayIndex.all(),NDArrayIndex.interval(LEARNSTRING_CHARS_LIST.size(),LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.size()),NDArrayIndex.all());
                    int sampledCategoryIdx = Nd4j.getExecutioner().exec(new IMax(outputCategory, 1)).getInt(0);

                    // print the chosen output
                    System.out.print(LEARNCATEGORY_CHARS_LIST.get(sampledCategoryIdx));

                    // use the last output as input
                    nextInput.putScalar(LEARNSTRING_CHARS_LIST.size()+sampledCategoryIdx, 1);

                    // print the chosen output
                    INDArray continuousOutput = output.get(NDArrayIndex.all(), NDArrayIndex.point(LEARNSTRING_CHARS_LIST.size() + LEARNCATEGORY_CHARS_LIST.size()), NDArrayIndex.all());
                    System.out.print(continuousOutput);

                    // use the last output as input
                    nextInput.putScalar(LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.size(), continuousOutput.getDouble(0,0,0));

                    output = net.rnnTimeStep(nextInput);

                    if(LEARNSTRING_CHARS_LIST.get(sampledCharacterIdx).equals('D'))
                    {
                        break;
                    }

                }
                System.out.print("\n");
            }
		}
		//ADDED BY AMIROOO
        net.rnnClearPreviousState();
        INDArray testInit = Nd4j.zeros(1,numInput, 1);

        testInit.putScalar(LEARNSTRING_CHARS_LIST.indexOf('A'), 1);

        testInit.putScalar(LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.indexOf('A'), 1);

        testInit.putScalar(LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.size(), 2);


//        testInit.putScalar(new int[] { 0, LEARNSTRING_CHARS_LIST.indexOf('A'), 0 }, 1);
//        testInit.putScalar(new int[] { 0, (LEARNSTRING_CHARS_LIST.size())+LEARNCATEGORY_CHARS_LIST.indexOf('A'), 0 }, 1);
//        testInit.putScalar(new int[] { 0, (LEARNSTRING_CHARS_LIST.size())+(LEARNCATEGORY_CHARS_LIST.size()), 0 }, 2);

        net.rnnTimeStep(testInit);

        testInit = Nd4j.zeros(1,numInput, 1);

        testInit.putScalar(LEARNSTRING_CHARS_LIST.indexOf('E'), 1);

        testInit.putScalar(LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.indexOf('A'), 1);

        testInit.putScalar(LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.size(), 2);

//        testInit = Nd4j.zeros(1,numInput, 1);
//        testInit.putScalar(new int[] { 0, LEARNSTRING_CHARS_LIST.indexOf('E'), 0 }, 1);
//        testInit.putScalar(new int[] { 0, (LEARNSTRING_CHARS_LIST.size())+LEARNCATEGORY_CHARS_LIST.indexOf('A'), 0 }, 1);
//        testInit.putScalar(new int[] { 0, (LEARNSTRING_CHARS_LIST.size())+(LEARNCATEGORY_CHARS_LIST.size()), 0 }, 2);

        INDArray output = net.rnnTimeStep(testInit);

//        INDArray nextInput = testInit.dup();
        
//        testInit = Nd4j.zeros(1,LEARNSTRING_CHARS_LIST.size(), 1);
//        testInit.putScalar(LEARNSTRING_CHARS_LIST.indexOf('B'), 1);
//        output = net.rnnTimeStep(testInit);
        for (int i=0;i<10;i++) {
            int sampledCharacterIdx = Nd4j.getExecutioner().exec(new IMax(output.get(NDArrayIndex.point(0),NDArrayIndex.interval(0,LEARNSTRING_CHARS_LIST.size()),NDArrayIndex.point(0)), 0)).getInt(0);
            System.out.print(LEARNSTRING_CHARS_LIST.get(sampledCharacterIdx));

            int sampledCategoryIdx = Nd4j.getExecutioner().exec(new IMax(output.get(NDArrayIndex.point(0),NDArrayIndex.interval(LEARNSTRING_CHARS_LIST.size(),LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.size()),NDArrayIndex.point(0)), 0)).getInt(0);
            System.out.print(LEARNCATEGORY_CHARS_LIST.get(sampledCategoryIdx));

            int point=LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.size();
            System.out.print(output.getDouble(point));

//            INDArray temp = nextInput.dup();
//            nextInput = Nd4j.zeros(1, numInput, temp.size(2)+1);

            INDArray newTransaction = Nd4j.zeros(1, numInput, 1);

            newTransaction.putScalar(sampledCharacterIdx, 1);
            newTransaction.putScalar(LEARNSTRING_CHARS_LIST.size()+sampledCategoryIdx, 1);
            newTransaction.putScalar(LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.size(), output.getDouble(0,LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.size(),0));

//            newTransaction.putScalar(new int[] { 0, LEARNSTRING_CHARS_LIST.indexOf(LEARNSTRING_CHARS_LIST.get(sampledCharacterIdx)), 0 }, 1);
//            newTransaction.putScalar(new int[] { 0, (LEARNSTRING_CHARS_LIST.size())+LEARNCATEGORY_CHARS_LIST.indexOf(LEARNCATEGORY_CHARS_LIST.get(sampledCategoryIdx)), 0 }, 1);
//            newTransaction.putScalar(new int[] { 0, (LEARNSTRING_CHARS_LIST.size())+(LEARNCATEGORY_CHARS_LIST.size()), 0 }, output.getDouble(0,LEARNSTRING_CHARS_LIST.size()+LEARNCATEGORY_CHARS_LIST.size(),0));

//            for(int j=0;j<output.size(1);j++)
//            {
//                newTransaction.put(new int[] { 0, j, 0 }, output.get(NDArrayIndex.point(0),NDArrayIndex.point(j),NDArrayIndex.point(0)));
//            }

//            nextInput=Nd4j.concat(2,temp,newTransaction);
            output = net.rnnTimeStep(newTransaction);

            if(LEARNSTRING_CHARS_LIST.get(sampledCharacterIdx).equals('D'))
            {
                System.out.println("Finished sequence");
                break;
            }
        }
        //ADDED BY AMIROOO
	}
}
