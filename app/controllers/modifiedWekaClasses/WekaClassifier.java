package controllers.modifiedWekaClasses;


import java.util.HashMap;
import java.util.Map;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.weka.WekaException;
import weka.core.Instances;

/**
 * This class is a modified version of the original class from java machine
 * learning implementation. Change the ToWekaUtils data field to use own modified class.
 * (https://svn.code.sf.net/p/java-ml/code/trunk/src/net/sf/javaml/tools/weka/)
 * 
 */
public class WekaClassifier implements Classifier {

    private static final long serialVersionUID = -4607698346509036963L;

    private weka.classifiers.Classifier wekaClass;

    private ToWekaUtils utils;

    public WekaClassifier(weka.classifiers.Classifier wekaClass) {
        this.wekaClass = wekaClass;
    }

    public void buildClassifier(Dataset data) {
        utils = new ToWekaUtils(data);
        Instances inst = utils.getDataset();
        try {
            wekaClass.buildClassifier(inst);
        } catch (Exception e) {
            throw new WekaException(e);
        }

    }

    @Override
    public Object classify(Instance instance) {
        try {        		
        	if(wekaClass != null)
        	{
        		weka.core.Instance i = utils.instanceToWeka(instance);
        		double classValue = wekaClass.classifyInstance(i);
                return utils.convertClass(classValue);
        	}
        	else return null;
        } catch (Exception e) {
            throw new WekaException(e);
        }
    }

    @Override
    public Map<Object, Double> classDistribution(Instance instance) {
        try {
            Map<Object, Double> out = new HashMap<Object, Double>();
            double[] distr = wekaClass.distributionForInstance(utils.instanceToWeka(instance));
            for (int i = 0; i < distr.length; i++)
                out.put(utils.convertClass(i), distr[i]);
            return out;
        } catch (Exception e) {
            throw new WekaException(e);
        }
    }

}
