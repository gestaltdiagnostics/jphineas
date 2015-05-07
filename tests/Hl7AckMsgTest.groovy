/*
 *  Copyright (c) 2015 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
 *  
 *  This file is part of jPhineas
 *
 *  jPhineas is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  jPhineas is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with jPhineas.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *  Copyright (c) 2012 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
 *  
 *  This file is part of PhinmsX.
 *
 *  PhinmsX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Foobar is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */



import junit.framework.TestCase
import tdunnick.jphineas.util.*


/**
 * @author user
 *
 */
public class Hl7AckMsgTest extends TestCase{
	String msg = "" +
  "MSH|^~\\&|AHS-SLI|ST ELIZABETH HOSPITAL LAB^^CLIA|vCMR|WEDSS|200907081020||ORU^R01|67d50db3-c111-4247-8099-9807c8a10098|T|2.3.1\n" +
  "PID|1|000000000420894|X311520:999||Kermit^Andrea^T||19781022080001|F||WN^WHITE/NON-HISPANIC^L|2771 Walnut Street^^Unity^WI^54488|||||M^MARRIED^L\n" +
  "NK1|1|Kermit^John^M|H^HUSBAND^L|2771 Walnut Street^^Unity^WI^54488|^^^^^920^7661736|^^^^^920^5853113\n" +
  "ORC|RE|09:B0036303S|09:B0036303S^ST ELIZABETH HOSPITAL LAB^^CLIA||||||200907081020|||YOUB^YOUNG MD^BRETT^D||^^^^^920^9963700|||||||ST ELIZABETH HOSPITAL LAB|1506 S ONEIDA ST^^APPLETON^WI^54915|^^^^^920^7382141|1531 S MADISON ST 4TH FLOOR^^APPLETON^WI^54915\n" +
  "OBR|1|63ZZ00259000|63ZZ00259000^ST ELIZABETH HOSPITAL LAB^^CLIA|^^^107.550^BODY FLUID AEROB CULT RPT^L|||200907061645|||||||200907061703|JF&JOINT FLUID&L|YOUB^YOUNG MD^BRETT^D|^^^^^920^9963700|||09:B0036303S|||||F\n" +
  "OBX|1|ST|^^^107.550^BODY FLUID AEROB CULT RPT^L|1|SPECIMEN COMMENTS:||||||F|||200907080733|SEH^ST ELIZABETH HOSPITAL LAB^L\n" +
  "OBX|2|ST|^^^107.551^BODY FLUID AEROB CULT RPT-1^L|2|PAGE DR YOUNG W/ RESULTS 554-1309||||||F|||200907080733|SEH^ST ELIZABETH HOSPITAL LAB^L\n" +
  "MSH|^~\\&|RD|MAYO CLINIC DEPT. OF LAB MED AND PATHOLOGY^24D0404292^CLIA|WIDOH|WI|200910010408||ORU^R01|2009100104084438632|P|2.3.1\n" +
  "PID|1||X507771:999~X492862:999||Bigbird^Jack||19800701101126|M||U^^HL7 005^^^L\n" +
  "NK1|1\n" +
  "ORC|||||||||||||||||||||THEDACARE LABS|130 2ND ST^^NEENAH^WI^54956|^^^^^920^7292079|130 2ND ST^^NEENAH^WI^54956\n" +
  "OBR|1||33ZZ00874000|^^^81096^CHLAMYDIA TRACHOMATIS AMPLIFIED DNA^L|||200909272345||||||STRAND DISPLACEMENT AMPLIFICATION|200909301202|^^URINE|^CAROTHERS, KELLY||||||200909302341|||F\n" +
  "OBX|1|CE|6357-8^C TRACH DNA UR QL PCR^LN^24075^CHLAMYDIA TRACHOMATIS AMPLIFIED DNA^L||G-A200^POSITIVE^SNM||NEGATIVE||||F|||20090930114100|24D0404292^MAYO CLINIC DEPT. OF LAB MED AND PATHOLOGY^CLIA\n" +
  "NTE|1|C|REPORTABLE DISEASE\n" +
  "MSH|^~\\&|RD|MAYO CLINIC DEPT. OF LAB MED AND PATHOLOGY^24D0404292^CLIA|WIDOH|WI|200910010408||ORU^R01|2009100104084438633|P|2.3.1\n" +
  "PID|1||X728026:999~X201448:999||Labtest^Sebastian||19800112220310|M||U^^HL7 005^^^L\n" +
  "NK1|1\n" +
  "ORC|||||||||||||||||||||DEAN CLINIC|1313 FISH HATCHERY ROAD^^MADISON^WI^53715|^^^^^608^2528025|1313 FISH HATCHERY ROAD^^MADISON^WI^53715\n" +
  "OBR|1||14ZZ00367000|^^^81958^HIV-1 RNA QUANTIFICATION, P^L|||200909291116||||||REVERSE TRANSCRIPTION-POLYMERASE CHAIN REACTION (RT-PCR) (PCR IS UTILIZED PURSUANT TO A LICENSE AGREEMENT WITH ROCHE MOLECULAR SYSTEMS, INC.) NOTE: SEE HIV TREATMENT MONITORING ALGORITHM IN SPECIAL INSTRUCTIONS.|200909300945|^^PLASMA|3310^LEVIN,JAMES M.||||||200909302053|||F\n" +
  "OBX|1|SN|21008-8^HIV 1 RNA^LN^81958^HIV-1 RNA QUANTIFICATION, P^L||^53|COPIES/ML|||||F|||20090930085300|24D1040592^MAYO CLINIC DPT OF LAB MED PATHOLOGY SUPERIOR DR\n" +
  "NTE|1|C\n" +
  "NTE|2||RESULT IN LOG COPIES/ML IS 1.72\n" +
  "NTE|3|C\n" +
  "NTE|4|C|QUANTIFICATION RANGE OF THIS ASSAY IS 48 TO 10,000,000\n" +
  "NTE|5|C|COPIES/ML (1.68 TO 7.00 LOG COPIES/ML).\n" +
  "NTE|6|C\n" +
  "NTE|7|C|TESTING WAS DONE BY THE COBAS AMPLIPREP/COBAS\n" +
  "NTE|8|C|TAQMAN HIV-1 TEST (ROCHE MOLECULAR SYSTEMS, INC.).\n";

  String msgack = "" +
 "MSH|^~\\&|vCMR|WEDSS|AHS-SLI|ST ELIZABETH HOSPITAL LAB^^CLIA|20120331131530||ACK||T|2.3.1\r" +
 "MSA|AA|67d50db3-c111-4247-8099-9807c8a10098|MSG OK\r" +
 "MSH|^~\\&|WIDOH|WI|RD|MAYO CLINIC DEPT. OF LAB MED AND PATHOLOGY^24D0404292^CLIA|20120331131530||ACK||P|2.3.1\r" +
 "MSA|AA|2009100104084438632|MSG OK\r" +
 "MSH|^~\\&|WIDOH|WI|RD|MAYO CLINIC DEPT. OF LAB MED AND PATHOLOGY^24D0404292^CLIA|20120331131530||ACK||P|2.3.1\r" +
 "MSA|AA|2009100104084438633|MSG OK\r";

  String seg = "MSH|^~\\&|AHS-SLI|ST ELIZABETH HOSPITAL LAB^^CLIA|vCMR|WEDSS|200907081020||ORU^R01|67d50db3-c111-4247-8099-9807c8a10098|T|2.3.1"	  
  String segack = "" +
  "MSH|^~\\&|vCMR|WEDSS|AHS-SLI|ST ELIZABETH HOSPITAL LAB^^CLIA|20120331131530||ACK||T|2.3.1\r" +
  "MSA|AE|67d50db3-c111-4247-8099-9807c8a10098|Application Error\r";

   Hl7AckMsg ack = new Hl7AckMsg ();
   
   boolean msgcmp (String m1, String m2) {
  	 String pat = "CLIA\\|[0-9]+\\|"
  	 m1 = m1.replaceAll(pat, "")
  	 m2 = m2.replaceAll(pat, "")
  	 //println m1 + "\n" + m2
  	 return m1.equals (m2);
   }
   
	void testGenAck(){
		String s = ack.genAck (seg, "AE", "Application Error");
		assert s != null : "failed to generate seg ack"
		//println s;
		assert msgcmp (s, segack) : "didn't match expected ack"
	}
	
	void testGetAck(){
		String s = ack.getAck (msg);
		assert s != null : "failed to generate msg ack"
		//println s;
	}
	
	void testHl7AckMsg(){
		String s = new Hl7AckMsg (msg);
		//println new Hl7AckMsg (s);
		assert msgcmp (s, msgack) : "didn't match message ack"
	}
	
	
}
