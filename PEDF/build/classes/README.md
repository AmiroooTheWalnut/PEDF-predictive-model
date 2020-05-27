# BaggingNet
An application for process mining. Auto-generating a network of events and predicting new partial cases.
This project consists 3 branches: 1-"master" containing the source code of the project. 2-"Libs" containing the required libraries and dependancies. 3-"Test" containing the test datasets.
Weka used for this project, however, the source of weka required minor changes. Therefore, the user should change the following code in Weka classifier:

	/**
	* The dataset header for the purposes of printing out a semi-intelligible
	* model
	*/
	protected Instances m_Instances;
	
To:
	
	/**
	* The dataset header for the purposes of printing out a semi-intelligible
	* model
	*/
	public Instances m_Instances;
	
There is a compiled revised Weka in the Libs branch.

How to setup:
The "master" branch works as src folder. If you want to work with an IDE like netbeans, clone the "master" branch which should make a folder called "BaggingNet" with the sources inside. Rename the "BaggingNet" to "src" and use it in your IDE.
The "Libs" branch is required by the application. You may provide dependancies as you wish but again remember the revision in WEKA source which is currently required by the application. Again clone it and rename "BaggingNet" to "Libs" or any other name you like.
Finally, there is a "test" branch which contains the test datasets. Again clone it and rename "BaggingNet" to "TestData" or any other name you like.
