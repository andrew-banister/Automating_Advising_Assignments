//Program that parses input data, calculates assignment scores, and writes LP file for GLPSOL 

import java.io.*;
import java.util.*;


public class Main {
    //Class variables. Do not touch
    public static ArrayList<Major> majors=new ArrayList<Major>();
    public static ArrayList<String> majorNames=new ArrayList<String>();
    public static ArrayList<StudentGroup> studentGroups=new ArrayList<StudentGroup>();
    public static ArrayList<AdvisorGroup> advisorGroups=new ArrayList<AdvisorGroup>();
    public static String[][] variableNames;
    public static int[][] scores;
    
    //Program parameters. May be adjusted
    public static int TC_THRESHOLD=2;
    public static int CAC_TC_THRESHOLD=6;
    public static int POINTS_FOR_MDD=1;
    public static int POINTS_FOR_TRAINING=5;
    public static int FIXED_UNIFORM_CAPACITY=5;
    
    public static void main(String[] args) throws IOException{
        readInMajors();
        createGroups();
        readInStudents();
        readInAdvisors();
        updateGroups();
        createVariablesAndscores();
        writeLPFile();
    }
    
    //Read from file the list of majors and their corresponding divisions
    public static void readInMajors() throws IOException{
        File majorsFile=new File("MajorsList.txt");
        Scanner reader=new Scanner(majorsFile);
        while(reader.hasNextLine()){
            String currentLine=reader.nextLine();
            StringTokenizer tokenizer=new StringTokenizer(currentLine,",");
            if(tokenizer.countTokens()!=3)
                throw new IllegalArgumentException("Error in format of majors file");
            Major major=new Major(tokenizer.nextToken(),tokenizer.nextToken(),tokenizer.nextToken());
            majors.add(major);
            majorNames.add(major.name);
        }
        reader.close();
    }
    
    //Based on the majors, create student groups for each
    public static void createGroups(){
        for(Major major:majors){
            if(!major.name.equals("Transfer")&&!major.name.equals("Intnl")){
                StudentGroup lowTC=new StudentGroup(major,false,false,false,false);
                studentGroups.add(lowTC);
                StudentGroup highTC=new StudentGroup(major,true,false,false,false);
                studentGroups.add(highTC);
                StudentGroup veryHTC=new StudentGroup(major,false,false,false,true);
                studentGroups.add(veryHTC);
                StudentGroup transfer=new StudentGroup(major,true,true,false,false);
                studentGroups.add(transfer);
                StudentGroup intnl=new StudentGroup(major,true,false,true,false);
                studentGroups.add(intnl);
                StudentGroup transferIntnl=new StudentGroup(major,true,true,true,false);
                studentGroups.add(transferIntnl);
                AdvisorGroup noTraining=new AdvisorGroup(false);
                noTraining.addMajor(major);
                advisorGroups.add(noTraining);
            }
            AdvisorGroup hasTraining=new AdvisorGroup(true);
            hasTraining.addMajor(major);
            advisorGroups.add(hasTraining);
        }
    }
    
    //Read from file the list of students and assigns them to a group based on major and TC's
    public static void readInStudents() throws IOException{
        File studentFile=new File("StudentList.txt");
        Scanner reader=new Scanner(studentFile);
        while(reader.hasNextLine()){
            String currentLine=reader.nextLine();
            StringTokenizer tokenizer=new StringTokenizer(currentLine,",");
            String name=tokenizer.nextToken()+tokenizer.nextToken();
            if(name.contains("-"))
                name=name.replace("-","");
            String major=tokenizer.nextToken().replace(" ","");
            String isTransfer=tokenizer.nextToken().toLowerCase();
            String isIntnl=tokenizer.nextToken().toLowerCase();
            int numTC=Integer.parseInt(tokenizer.nextToken());
            boolean highTC=false,veryHighTC=false;
            if(numTC>TC_THRESHOLD)
                highTC=true; 
            if(numTC>CAC_TC_THRESHOLD)
                veryHighTC=true;
            for(StudentGroup sgroup:studentGroups){
                if(sgroup.major.name.equals(major)){
                    if(isIntnl.contains("y")&&isTransfer.contains("y")){
                        if(sgroup.isIntnl&&sgroup.isTransfer){
                            sgroup.addStudent(name);
                            break;
                        }
                        continue;
                    }
                    if(isIntnl.contains("y")){
                        if(sgroup.isIntnl){
                            sgroup.addStudent(name);
                            break;
                        }
                        continue;
                    }
                    if(isTransfer.contains("y")){
                        if(sgroup.isTransfer){
                            sgroup.addStudent(name);
                            break;
                        }
                        continue;
                    }
                    if(veryHighTC){
                        if(sgroup.veryHighTC){
                            sgroup.addStudent(name);
                            break;
                        }
                        continue;
                    }
                    if(sgroup.highTC==highTC){
                        sgroup.addStudent(name);
                        break;
                    }

                }
            }
        }
    }
    
    //Read from file the list of advisors
    public static void readInAdvisors() throws IOException{
        File facultyFile=new File("FacultyList.txt");
        Scanner reader=new Scanner(facultyFile);
        while(reader.hasNextLine()){
            String currentLine=reader.nextLine();
            StringTokenizer tokenizer=new StringTokenizer(currentLine,",");
            String name=tokenizer.nextToken();
            if(name.contains("-"))
                name=name.replace("-","");
            String major_s=tokenizer.nextToken();
            String hasTrainingString=tokenizer.nextToken();
            boolean hasTraining=false;
            if(hasTrainingString.equals("1"))
                hasTraining=true;
            if(major_s.contains("/")){
                StringTokenizer majorTokenizer=new StringTokenizer(major_s,"/");
                ArrayList<Major> m=new ArrayList<Major>();
                for(int i=0;i<majorTokenizer.countTokens();i++){
                    String majorString=majorTokenizer.nextToken();
                    if(!majorNames.contains(majorString))
                        throw new IllegalArgumentException("Unrecognized major for advisor "+name);
                    m.add(majors.get(majorNames.indexOf(majorString)));
                }
                boolean added=false;
                for(AdvisorGroup agroup:advisorGroups){
                    if(agroup.majors.equals(m)&&agroup.hasTraining==hasTraining){
                        agroup.addAdvisor(name);
                        added=true;
                        break;
                    }
                }
                if(!added){
                    AdvisorGroup newGroup=new AdvisorGroup(hasTraining);
                    for(Major major:m){
                        newGroup.addMajor(major);
                    }
                    newGroup.addAdvisor(name);
                    advisorGroups.add(newGroup);
                }
            }
            else{
                if(!majorNames.contains(major_s))
                    throw new IllegalArgumentException("Unrecognized major for advisor "+name);
                ArrayList<Major> m=new ArrayList<Major>();
                m.add(majors.get(majorNames.indexOf(major_s)));
                for(AdvisorGroup agroup:advisorGroups){
                    if(agroup.majors.equals(m)&&agroup.hasTraining==hasTraining){
                        agroup.addAdvisor(name);
                        break;
                    }
                }
            }
        }
        reader.close();
    }
    
    //Delete any groups that had low students added
    public static void updateGroups(){
        for(int i=0;i<studentGroups.size();i++){
            if(studentGroups.get(i).numOfStudents==0){
                studentGroups.remove(i);
                i--;
            }
        }
        for(int i=0;i<advisorGroups.size();i++){
            if(advisorGroups.get(i).numOfAdvisors==0){
                advisorGroups.remove(i);
                i--;
            }
        }
    }
    
    //Create variable names and calculate associated scores based on group/advisor compatibilities
    public static int[][] createVariablesAndscores(){
        variableNames=new String[studentGroups.size()][advisorGroups.size()];
        scores=new int[studentGroups.size()][advisorGroups.size()];
        for(int row=0;row<variableNames.length;row++){
            for(int col=0;col<variableNames[row].length;col++){
                StudentGroup sgroup=studentGroups.get(row);
                String variableName=sgroup.major.name;
                if(sgroup.isIntnl)
                    variableName+="Intnl";
                if(sgroup.isTransfer)
                    variableName+="Transfer";
                if(sgroup.veryHighTC)
                    variableName+="VH";
                else if(sgroup.highTC)
                    variableName+="H";
                else
                    variableName+="L";
                variableName+="To"+advisorGroups.get(col).majorsNames;
                if(advisorGroups.get(col).hasTraining)
                    variableName+="H";
                else
                    variableName+="L";
                variableNames[row][col]=variableName;
            }
        }

        for(int row=0;row<variableNames.length;row++){
            for(int col=0;col<variableNames[row].length;col++){
                StudentGroup sgroup=studentGroups.get(row);
                AdvisorGroup agroup=advisorGroups.get(col);
                int maxScore=0;
                for(Major major:agroup.majors){
                    int score=0;
                    if(major.equals(sgroup.major))
                        score+=POINTS_FOR_MDD;
                    if(major.department.equals(sgroup.major.department))
                        score+=POINTS_FOR_MDD;
                    if(major.division.equals(sgroup.major.division))
                        score+=POINTS_FOR_MDD;
                    //Special Cases
                    if(sgroup.major.name.equals("AnimalBehavior")&&(major.department.equals("Biology")||major.department.equals("Psychology")))
                        score+=POINTS_FOR_MDD;
                    if(sgroup.veryHighTC&&major.name.equals("AcademicSuccess"))
                        score+=POINTS_FOR_TRAINING;
                    if(sgroup.isIntnl&&major.equals("Intnl"))
                        score+=POINTS_FOR_TRAINING;
                    if(sgroup.isTransfer&&major.name.equals("Transfer"))
                        score+=POINTS_FOR_TRAINING;
                    if(score>maxScore)
                        maxScore=score;
                }
                //Special Cases
                if(sgroup.highTC&&!agroup.hasTraining)
                    maxScore+=0;
                else
                    maxScore+=POINTS_FOR_TRAINING;
                scores[row][col]=maxScore;
            }
        }
        return scores;
    }
    
    //Write glp file using variable names and corresponding scores.
    public static void writeLPFile() throws IOException{
            File glpFile=new File("advising.lp");
            PrintWriter output=new PrintWriter(glpFile);
            output.println("Maximize");
            output.print("Compatibility:");
            for(int row=0;row<scores.length;row++){
                for(int col=0;col<scores[row].length;col++){
                    output.print(" "+scores[row][col]+" "+variableNames[row][col]);
                    if(col!=scores[row].length-1||row!=scores.length-1)
                        output.println(" +");
                }
            }
            output.println();
            output.println("\nsubject to");
            for(int row=0;row<scores.length;row++){
                StudentGroup sgroup=studentGroups.get(row);
                output.print(sgroup.major.name);
                if(sgroup.isIntnl&&sgroup.isTransfer)
                    output.println("IntnlTransferStudents");
                else if(sgroup.isIntnl)
                    output.print("IntnlStudents:");
                else if(sgroup.isTransfer)
                    output.print("TransferStudents:");
                else if(sgroup.veryHighTC)
                    output.print("VHStudents:");
                else if(sgroup.highTC)
                    output.print("HStudents:");
                else
                    output.print("LStudents:");
                for(int col=0;col<scores[row].length;col++){
                    output.print(" "+variableNames[row][col]);
                    if(col!=scores[row].length-1)
                        output.print(" +");
                    else
                        output.println(" = "+sgroup.numOfStudents);
                }
            }
            for(int col=0;col<scores[0].length;col++){
                output.print(advisorGroups.get(col).majorsNames);
                if(advisorGroups.get(col).hasTraining)
                    output.print("HAdvCap:");
                else
                    output.print("LAdvCap:");
                for(int row=0;row<scores.length;row++){
                    output.print(" "+variableNames[row][col]);
                    if(row!=scores.length-1)
                        output.print(" +");
                    else
                        output.println(" <= "+advisorGroups.get(col).totalCapacity);
                }
            }
            output.println();
            output.println("bounds");
            for(int row=0;row<scores.length;row++){
                for(int col=0;col<scores[row].length;col++){
                    output.println(variableNames[row][col]+" >= 0");
                }
            }
            output.println();
            output.println("integer");
            for(int row=0;row<scores.length;row++){
                for(int col=0;col<scores[row].length;col++){
                    output.println(variableNames[row][col]);
                }
            }
            output.println("\nEnd");
            output.close();
    }

public static int[][] getScoreTable() throws IOException{
    readInMajors();
    createGroups();
    readInStudents();
    readInAdvisors();
    updateGroups();
    return createVariablesAndscores();
}
}
