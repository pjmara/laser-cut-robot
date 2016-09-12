import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import org.apache.commons.io.IOUtils;
import com.neuronrobotics.bowlerstudio.vitamins.*;
import eu.mihosoft.vrl.v3d.parametrics.*;
import javafx.scene.paint.Color;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;


class HeadOnNeck implements ICadGenerator, IParameterChanged{
	//First we load teh default cad generator script 
	ICadGenerator defaultCadGen=(ICadGenerator) ScriptingEngine
	                    .gitScriptRun(
                                "https://github.com/madhephaestus/laser-cut-robot.git", // git location of the library
	                              "laserCutCad.groovy" , // file to load
	                              null
                        )
	LengthParameter thickness 		= new LengthParameter("Material Thickness",3.15,[10,1])
	LengthParameter headDiameter 		= new LengthParameter("Head Dimeter",100,[200,50])
	LengthParameter snoutLen 		= new LengthParameter("Snout Length",63,[200,50])
	LengthParameter jawHeight 		= new LengthParameter("Jaw Height",32,[200,10])
	LengthParameter leyeDiam 		= new LengthParameter("Left Eye Diameter",35,[headDiameter.getMM()/2,29])
	LengthParameter reyeDiam 		= new LengthParameter("Right Eye Diameter",35,[headDiameter.getMM()/2,29])
	LengthParameter eyeCenter 		= new LengthParameter("Eye Center Distance",headDiameter.getMM()/2,[headDiameter.getMM(),headDiameter.getMM()/2])
	StringParameter servoSizeParam 			= new StringParameter("hobbyServo Default","towerProMG91",Vitamins.listVitaminSizes("hobbyServo"))
	StringParameter boltSizeParam 			= new StringParameter("Bolt Size","M3",Vitamins.listVitaminSizes("capScrew"))

	HashMap<String, Object>  boltMeasurments = Vitamins.getConfiguration( "capScrew",boltSizeParam.getStrValue())
	HashMap<String, Object>  nutMeasurments = Vitamins.getConfiguration( "nut",boltSizeParam.getStrValue())
	//println boltMeasurments.toString() +" and "+nutMeasurments.toString()
	double boltDimeMeasurment = boltMeasurments.get("outerDiameter")
	double nutDimeMeasurment = nutMeasurments.get("width")
	double nutThickMeasurment = nutMeasurments.get("height")
	private TransformNR offset =BowlerStudio3dEngine.getOffsetforvisualization().inverse();
	ArrayList<CSG> headParts =null
	@Override 
	public ArrayList<CSG> generateCad(DHParameterKinematics d, int linkIndex) {
		ArrayList<CSG> allCad=defaultCadGen.generateCad(d,linkIndex);
		ArrayList<DHLink> dhLinks=d.getChain().getLinks();
		DHLink dh = dhLinks.get(linkIndex)
		//If you want you can add things here
		//allCad.add(myCSG);
		if(linkIndex ==dhLinks.size()-1){
			println "Found neck limb" 
			headDiameter.setMM(180)
			snoutLen.setMM(150)
			eyeCenter.setMM(100)
			leyeDiam.setMM(60)
			reyeDiam.setMM(60)
			if(headParts==null)
				headParts = (ArrayList<CSG> )ScriptingEngine.gitScriptRun("https://gist.github.com/e67b5f75f23c134af5d5054106e3ec40.git", "AnimatronicHead.groovy" ,  null )
			TransformNR initialState = offset.times(d.getRobotToFiducialTransform())
			RotationNR rot = initialState.getRotation();
			for(int i=0;i<headParts.size()-1;i++){
				CSG part = headParts.get(i)
				Color color= part.getColor()
				part=	part	
						.movez(-jawHeight.getMM())
						.rotx(-90)
						.rotz(-Math.toDegrees(rot.getRotationElevation()))
						
				part.setColor(color)
				defaultCadGen.add(allCad ,part, dh.getListener() )
				for(String p:part .getParameters()){
					CSGDatabase.addParameterListener(p,this);
				}
			}
		}
		return allCad;
	}
	@Override 
	public ArrayList<CSG> generateBody(MobileBase b ) {
		ArrayList<CSG> allCad=defaultCadGen.generateBody(b);
		//If you want you can add things here
		//allCad.add(myCSG);
		return allCad;
	}
	/**
	 * This is a listener for a parameter changing
	 * @param name
	 * @param p
	 */
	 
	public void parameterChanged(String name, Parameter p){
		//new RuntimeException().printStackTrace(System.out);
		//println "All Parts was set to null"
		headParts=null
	}
};

return new HeadOnNeck()