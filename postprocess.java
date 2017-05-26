//parses GLPSOL results, asks user how he/she wants results displayed, and displays results accordingly

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;
public class ReadResults {
    public static ArrayList<Major> majors=new ArrayList<Major>();
    public static ArrayList<String> majorNames=new ArrayList<String>();
    public static ArrayList<StudentGroup> studentGroups=new ArrayList<StudentGroup>();
    public static ArrayList<AdvisorGroup> advisorGroups=new ArrayList<AdvisorGroup>();
    public static int[][] assignments;
    public static int z=0;
    public static void main(String[] args) throws IOException{
        readInMajors();
        createGroups();
        readInStudents();
        readInAdvisors();
        updateGroups();
        readResults();
        System.out.println("Would you like the program to assign specific students?");
        Scanner input=new Scanner(System.in);
        if(input.next().toLowerCase().contains("n"))
            reportGeneralResults();
        else
            assignSpecificStudents();
        input.close();
        assessResults();
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
            if(numTC>Main.TC_THRESHOLD)
                highTC=true; 
            if(numTC>Main.CAC_TC_THRESHOLD)
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

    public static void readResults(){
        try{
            File results=new File("lp_results.txt");
            Scanner reader=new Scanner(results);
            assignments=new int[studentGroups.size()][advisorGroups.size()];
            for(int row=0;row<assignments.length;row++){
                for(int col=0;col<assignments[row].length;col++){
                    while(reader.hasNext()){
                        String next=reader.next();
                        if(next.equals("Compatibility")){
                            reader.next();
                            z=reader.nextInt();
                        }
                        if(next.contains("LTo")||next.contains("HTo")){
                            reader.next();
                            assignments[row][col]=reader.nextInt();
                            break;
                        }
                    }
                }
            }
            reader.close();
        }
        catch(IOException e){
            System.out.println("Error reading file");
        }
    }
    public static void reportGeneralResults(){
        try{
            File results=new File("assignment_results.txt");
            PrintWriter output=new PrintWriter(results);
            for(int row=0;row<assignments.length;row++){
                for(int col=0;col<assignments[row].length;col++){
                    if(assignments[row][col]==0)
                        continue;
                    StudentGroup sgroup=studentGroups.get(row);
                    output.print("Send "+assignments[row][col]+" student(s) from the "+sgroup.major.name+" major ");
                    if(sgroup.veryHighTC)
                        output.print("with very high transitional challenges");
                    else if(sgroup.highTC)
                        output.print("with high transitional challenges ");
                    else
                        output.print("with low transitional challenges ");
                    AdvisorGroup agroup=advisorGroups.get(col);
                    output.print("to advisors in the "+agroup.majorsNames);
                    if(agroup.hasTraining)
                        output.println(" major(s) who has training");
                    else
                        output.println(" major(s) who doesn't have training");
                }
            }
            output.close();
        }
        catch(IOException e){
            System.out.println("Error writing results to file");
        }
    }

    public static void assignSpecificStudents(){
        try{
            File results=new File("assignment_results.txt");
            PrintWriter output=new PrintWriter(results);
            for(int col=0;col<assignments[0].length;col++){
                AdvisorGroup agroup=advisorGroups.get(col);
                for(int row=0;row<assignments.length;row++){
                    StudentGroup sgroup=studentGroups.get(row);
                    for(int index=0;index<assignments[row][col];index++){
                        output.println("Assign "+sgroup.students.get(0)+" to "+agroup.advisors.get(index%agroup.numOfAdvisors));
                        sgroup.students.remove(0);
                    }
                }
            }
            output.close();
        }
        catch(IOException e){
            System.out.println("Error writing results to file");
        }
    }
    
    public static void assessResults() throws IOException{
        int numHappy1=0,num10=0,num5=0,num8=0;
        int[][] scores=Main.getScoreTable();
        for(int i=0;i<scores.length;i++){
            for(int j=0;j<scores[0].length;j++){
                StudentGroup sgroup=studentGroups.get(i);
                if(sgroup.veryHighTC||sgroup.isIntnl||sgroup.isTransfer){
                    if(scores[i][j]==10)
                        numHappy1+=assignments[i][j];
                    num10+=assignments[i][j];
                }
                else if(sgroup.major.name.equals("Undecided")){
                    if(scores[i][j]>=5)
                        numHappy1+=assignments[i][j];
                    num5+=assignments[i][j];
                }
                else{ 
                    if(scores[i][j]>=8)
                        numHappy1+=assignments[i][j];
                    num8+=assignments[i][j];
                }
            }
        }
        int numStudents=0;
        for(StudentGroup sgroup:studentGroups){
            numStudents+=sgroup.numOfStudents;
        }
        System.out.println(numHappy1*100/numStudents+"% of students with top choice.");
        System.out.println("10: "+num10+" 8: "+num8+" 5: "+num5);
        System.out.println("Max possible score: "+(10*num10+8*num8+5*num5));
        System.out.println("Percentage of max score achieved: "+(double)z/(10*num10+8*num8+5*num5)*100);
    }
}
