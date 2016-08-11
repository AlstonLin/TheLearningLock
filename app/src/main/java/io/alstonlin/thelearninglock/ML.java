package io.alstonlin.thelearninglock;


import android.content.Context;
import android.util.Log;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationStep;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * The Object that represents the Machine Learning portion of the app, which handles the
 * underlying Neural Network.
 */
public class ML implements Serializable {
    // Constants
    public static final String FILENAME = "learning_lock_saved.eg";
    private static final double TRAIN_CONVERGENCE_THRESHOLD = 0.005;
    private static final int MAX_EPOCHS = 10000;
    private static final long serialVersionUID = 19981017L;
    // Singleton
    private static ML instance;
    // Fields
    private transient Context context;
    private ArrayList<double[]> valid;
    private ArrayList<double[]> invalid;
    private BasicNetwork network;
    private int inputLayerCount;


    private ML(Context context){
        this.context = context;
        this.inputLayerCount = -1;
        this.network = new BasicNetwork();
    }

    /**
     * Gets the Singleton of this class. Note that setup() must be called first before this.
     * @return The Singleton instance
     */
    public static ML getInstance(){
        if (instance == null) throw new IllegalStateException("setup() must be called before getInstance()");
        return instance;
    }

    /**
     * This function must be called before getInstance() can be called, and this function may not
     * be called twice.
     * @param context The context this is being created in
     * @return If it was loaded from a file
     */
    public static boolean setup(Context context){
        if (instance != null) throw new IllegalStateException("Cannot call setup() twice");
        instance = loadFromFile(context);
        if (instance == null) { // First time
            instance = new ML(context);
            return false;
        }
        return true;
    }

    /**
     * Attemps to load from file if exists.
     * @return The loaded object from file, or null if does not exist
     */
    private static ML loadFromFile(Context context){
        FileInputStream fis = null;
        ObjectInputStream is = null;
        ML result = null;
        try {
            fis = context.openFileInput(FILENAME);
            is = new ObjectInputStream(fis);
            ML loaded = (ML) is.readObject();
            loaded.context = context;
            result = loaded;
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (fis != null) fis.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Sets the array size of the data sent to the network to train. Must be called before the
     * network is actually trained and used
     * @param inputLayerCount The value
     */
    public void setInputLayerCount(int inputLayerCount){
        this.inputLayerCount = inputLayerCount;
        this.valid = new ArrayList<>();
        this.invalid = new ArrayList<>();
        this.network.addLayer(new BasicLayer(new ActivationSigmoid(), true, inputLayerCount)); // Input
        this.network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 6 * inputLayerCount)); // Hidden
        this.network.addLayer(new BasicLayer(new ActivationStep(), false, 1)); // Output
        this.network.getStructure().finalizeStructure();
        this.network.reset();
    }

    /**
     * Adds an entry to the System, and if indicated, retrain the network.
     * @param data The data (layout_pattern cell times) of the user as input
     * @param validity If the data was valid, the output
     * @param retrain If the network should be retrained
     */
    public void addEntry(double[] data, boolean validity, boolean retrain){
        ArrayList<double[]> list = validity ? valid : invalid;
        list.add(data);
        if (retrain) train();
    }

    /**
     * Uses the given time data to predict if they're the real owner or not
     * @param times The time data
     */
    public boolean predict(double[] times){
        MLData input = new BasicMLData(times);
        MLData output =  network.compute(input);
        return output.getData()[0]  == 1;
    }

    /**
     *
     */
    public void train(){
        double[][] validArray = new double[valid.size()][inputLayerCount];
        double[][] invalidArray = new double[invalid.size()][inputLayerCount];
        for (int i = 0; i < valid.size(); i++){
            validArray[i] = valid.get(i);
        }
        for (int i = 0; i < invalid.size(); i++){
            invalidArray[i] = invalid.get(i);
        }
        train(validArray, invalidArray);
    }

    /**
     * Takes in data to train the Neural Network.
     *
     * @param validTimes Inputs of times that would be valid and should unlock. Should be in a
     *                   n * inputLayerCount double array of times
     * @param invalidTimes Similar to the first parameter, but with data that should not result in
     *                     an unlock
     */
    private void train(double[][] validTimes, double[][] invalidTimes){
        // Creates input and output arrays
        int total = validTimes.length + invalidTimes.length;
        double[][] output = new double[total][1];
        double[][] input = new double[total][inputLayerCount];
        for (int i = 0; i < validTimes.length; i++){
            input[i] = validTimes[i];
            output[i][0] = 1;
        }
        for (int i = 0; i < invalidTimes.length; i++){
            input[i + validTimes.length] = invalidTimes[i];
            output[i + validTimes.length][0] = 0;
        }
        NeuralDataSet trainingSet = new BasicNeuralDataSet(input, output);
        // Starts training
        final Train train = new ResilientPropagation(network, trainingSet);
        int counter = 0;
        double error;
        do {
            train.iteration();
            counter++;
            error = train.getError();
            if (counter % 10 == 0) Log.i("Blah", "ERROR =" + error);
        } while(error > TRAIN_CONVERGENCE_THRESHOLD && counter < MAX_EPOCHS);
        // Saves everything this is trained
        save();
    }

    /**
     * Saves the neural network into a file.
     */
    private void save() {
        FileOutputStream fos = null;
        ObjectOutputStream os = null;
        try {
            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(this);
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException e){
                e.printStackTrace();
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static boolean isSetup(){
        return instance != null && instance.invalid != null;
    }

}
