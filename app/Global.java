import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import controllers.managers.ClassificationManager;
import controllers.managers.ClassifierCollection;
import play.*;
import tools.Utils;
 
public class Global extends GlobalSettings {
	@Override
	  public void onStart(Application app) {
		if(Utils.loadObject("default_class") == null)
		{
		ClassificationManager classificationManager = new ClassificationManager();
		classificationManager
		.setClassifier(ClassifierCollection.SVM_CLASSIFIER);
		File trainingDataFile = new File("public/datasets/Datensatz 1_v1.csv");
		try {
			classificationManager.setData(trainingDataFile, 0,
					ClassificationManager.SET_TRAINING_DATA, ";",
					true);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		System.out.println("training default classifier");
		classificationManager.trainClassifier();
		Utils.saveObject("default_class", classificationManager);
		}
	  }  
	   
	  @Override
	  public void onStop(Application app) {
	        // code 
	  }  
}