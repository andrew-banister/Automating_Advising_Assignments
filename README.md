Automating Academic Advising Assignments
Andrew Banister, Salma Chiheb, John Daniels, Heather Gronewald, Jordan King, Paris Nelson 

Sample Student Data.xls is created from a random name generator with an expected distribution of majors for students entering Southwestern. StudentList.txt is the version to be read by the program.

preprocess.java reads in StudentList.txt and MajorsList.txt to calculate assignment scores and set up the linear program (advising.lp).

GLPSOL from the GLPK (GNU Linear Programming Kit) then needs to be run from the command line.

Then postprocess.java parses and displays the results based on the user specifications.