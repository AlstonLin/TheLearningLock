package io.alstonlin.thelearninglock;


import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * The Object that represents the Machine Learning portion of the app,
 * which predicts impostors by applying the training data on a Gaussian distribution
 * and looking at the product of probabilities for the user to be the real one
 */
public class ML implements Serializable {
    // Constants
    private static final long serialVersionUID = 19981017L;
    // Fields
    private transient Context context;
    private ArrayList<double[]> trainingSet = new ArrayList<>();
    private double[] muArr;
    private double[] sigmaSquaredArr;
    private int n;
    private double epsilon;


    public ML(Context context, int n){
        this.context = context;
        this.n = n;
        muArr = new double[n];
        sigmaSquaredArr = new double[n];
    }

    /**
     * Attempts to load from file if exists.
     * @return The loaded object from file, or null if does not exist
     */
    public static ML loadFromFile(Context context){
        FileInputStream fis = null;
        ObjectInputStream is = null;
        ML result = null;
        try {
            fis = context.openFileInput(Const.ML_FILENAME);
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
     * Adds an entry to the System, and if indicated, retrains the algorithm
     * @param data The data (layout_pattern cell times) of the user as input
     * @param retrain If the algorithm should be retrained
     */
    public void addEntry(double[] data, boolean retrain){
        if (data.length != n) {
            throw new IllegalArgumentException("N is set to " + n + " but given data of size " + data.length);
        }
        trainingSet.add(data);
        if (retrain) train();
    }

    /**
     * Sets the muArr and sigmaSquaredArr based on the current trainingSet.
     */
    public void train(){
        // Sums all the inputs per feature
        double[] featureSums = new double[n];
        int m = trainingSet.size() - Const.NUM_VALIDATION_ENTRIES;
        for (double[] entry : trainingSet){
            for (int i = 0; i < entry.length; i++){
                featureSums[i] += entry[i];
            }
        }
        // Calculates mu
        for (int i = 0; i < n; i++){
            double mu = featureSums[i] / m;
            muArr[i] = mu;
            // Calculates Sigma squared
            double sum = 0;
            for (int j = 0; j < m; j++){
                sum += Math.pow((trainingSet.get(j)[i] - mu), 2);
            }
            sigmaSquaredArr[i] += sum / m;
        }
        // Last 3 for cross-validation to calculate an epsilon by averaging the prediction
        // and exponentiation it by some constant
        // TODO: Can increase this accuracy if we do a k-fold cross validation instead
        double sum = 0;
        for (int i = m; i < trainingSet.size(); i++){
            sum += getPrediction(trainingSet.get(i));
        }
        epsilon = sum / Const.NUM_VALIDATION_ENTRIES;
        // Saves everything this is trained
        save();
    }

    /**
     * Uses the given time data to predict if they're the real owner or not
     * @param x The time data
     */
    public boolean predictImposter(double[] x){
        if (x.length != n) {
            throw new IllegalArgumentException("N is set to " + n + " but given data of size " + x.length);
        }
        double prediction = getPrediction(x);
        Log.d("ML", "PREDICTED " + prediction + " with an epsilon " + epsilon);
        return prediction < epsilon;
    }

    private double getPrediction(double[] x){
        double product = 1;
        for (int i = 0; i < n; i++){
            product *= p(x[i], muArr[i], sigmaSquaredArr[i]);
        }
        return product;
    }

    private static double p(double x, double mu, double sigmaSquared){
        double sigma = Math.sqrt(sigmaSquared);
        // Formula: p = (sqrt(2pi) * sigma) ^ -1 * exp(-(x - mu) ^ 2 / (2 *  sigma^2))
        double exp = Math.exp(- Math.pow(x - mu, 2) / (2 * sigmaSquared));
        return (1 / (Math.sqrt(2 * Math.PI) * sigma)) * exp;
    }

    /**
     * Saves ML data to file
     */
    private void save() {
        FileOutputStream fos = null;
        ObjectOutputStream os = null;
        try {
            fos = context.openFileOutput(Const.ML_FILENAME, Context.MODE_PRIVATE);
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
}
