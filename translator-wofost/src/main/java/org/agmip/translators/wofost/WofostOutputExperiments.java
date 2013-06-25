package org.agmip.translators.wofost;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agmip.util.MapUtil;
import aquacrop_utils.Experiment;
import aquacrop_utils.ManagementEvent;
import org.agmip.common.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WofostOutputExperiments extends WofostOutput {
	
	ArrayList<String> runNames = new ArrayList<String>();
        private static final Logger LOG = LoggerFactory.getLogger(WofostOutputExperiments.class);
	
	private void createBatchRun(String filePath) throws IOException
	{
		String fName = filePath + "runs.bat";
		FileOutputStream fstream = new FileOutputStream(fName);
		DataOutputStream out = new DataOutputStream(fstream);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));		
		
		
		for (String runName: runNames)
		{		
			bw.write(String.format("..\\wofost %s\\%s.ini\n", runName, runName));
		}
		
		bw.write("\ndel *.tmp\n\n");
		bw.write("pause\\");
		
		bw.close();
		out.close();
	}
	
	public void writeFile(String filePath, Map input) {
		Section = "Experiment";       
		
		// get all experiments
		ArrayList<HashMap<String, Object>> experimentsPackage = MapUtil.getRawPackageContents(input, "experiments");
		
		for (HashMap<String, Object> experiment: experimentsPackage)
		{	
			try
			{
				//HashMap<String, String> experimentValues = experiment.getValues();
				expName = (String) experiment.get("exname");
				runNames.add(ReplaceIllegalChars(expName));
				LOG.debug("Experiment: " + expName);
				
				String expDirName = filePath + ReplaceIllegalChars(expName) + "\\";
				File expDir = new File(expDirName);
				expDir.mkdir();
				
				String expDirInputName = filePath + expName + "\\input\\";
				File expDirInput = new File(expDirInputName);
				expDirInput.mkdir();
				
				String expDirOutputName = filePath + expName + "\\output\\";
				File expDirOutput = new File(expDirOutputName);
				expDirOutput.mkdir();
				
				String expFileName = getRunIniFileName(expName, expDirName);
				
				FileOutputStream fstream = new FileOutputStream(expFileName);
				DataOutputStream out = new DataOutputStream(fstream);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));		
				
				bw.write(String.format("** generated file for experiment %s\n", expName));
				bw.write("[Directory settings for WOFOST]\n");
				bw.write("DBMDIR='-'\n");
				bw.write("DBRDIR='-'\n");
				bw.write("WTRDIR='..\\meteo\\cabowe\\'\n");
				bw.write("SOLDIR='..\\soild\\'\n");
				bw.write("CRPDIR='..\\cropd\\'\n");
				bw.write("CLMDIR='-'\n");
				bw.write(String.format("RUNDIR='%s'\n", expName + "\\input\\"));
				bw.write(String.format("OUTDIR='%s'\n", expName + "\\output\\"));
				bw.write("DCGDIR='-'\n");
				bw.write("DRVDIR='-'\n");
				bw.write("GEODIR='-'\n");
				bw.write("TMPDIR='-'\n");
				bw.write("CURDIR='-'\n");
				bw.write("EUSDIR='-'\n");
				
				bw.close();
				out.close();
				
				Experiment exp = Experiment.create(experiment);
				@SuppressWarnings("rawtypes")
				Map<Class<? extends ManagementEvent>, List<ManagementEvent>> eventMap = exp.getEvents();
				
				new WofostOutputRunopt().writeFile(expDirInputName, experiment);				
				new WofostOutputTimer().writeFile(expDirInputName, experiment, eventMap); 
				new WofostOutputSite().writeFile(expDirInputName, experiment);
			}
			catch (FileNotFoundException e) {
				LOG.error("file not found");
                                LOG.error(Functions.getStackTrace(e));
			} catch (IOException e) {
				LOG.error("IO error");
                                LOG.error(Functions.getStackTrace(e));
			} 
		}
		
		try {
			createBatchRun(filePath);
		} catch (IOException e) {
			LOG.error("IO error");
                        LOG.error(Functions.getStackTrace(e));
		}
	}
	

}
