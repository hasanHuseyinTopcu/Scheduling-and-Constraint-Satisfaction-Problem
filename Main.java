import java.io.*;
import java.util.*;

public class Main {
	
	public static void main(String[] args){
		
		List<VariableNode[]> scheduleMatrix = new ArrayList<VariableNode[]>();	/* scheduleMatrix must be dynamic so we use array list that type is array */
		List<Course> courseList = new ArrayList<Course>();
		List<String> timeslotList = new ArrayList<String>();
		String line = null, courseLevel = null;
		boolean result = false;
		
		try{
			
			BufferedReader timeslotBufferedReader = new BufferedReader( new FileReader("Timeslots.txt") );		/* reading Timeslots.txt file */
			BufferedReader coursesBufferedReader = new BufferedReader( new FileReader("Courses.txt") );			/* reading Courses.txt file */
			BufferedWriter scheduleBufferedWriter = new BufferedWriter( new FileWriter("Schedule.txt") );		/* writing Schedule.txt file */
			
			
			while( ( line = timeslotBufferedReader.readLine() ) != null ){		/* for time slots*/
				
				if( !line.equals("") )
					timeslotList.add(line);
			}

			while( ( line = coursesBufferedReader.readLine() ) != null ){		/* for courses */
				
				if( line.matches("[0-9]+") )			/* if line is equal any course level then set courseLevel */
					courseLevel = line;
				else									/* if line is equal any course then add course and its level in courseList */
					if(line.equals("") != true)
						createCourseList( courseList, line, courseLevel);
			}
			
			createScheduleMatrix(scheduleMatrix, timeslotList, courseList);
			
			for(int i=0; i<100; i++){						/* The order of courses is very important for our algorithm because this algorithm run by sequence of courses */
															/* For example if the last 4,5 or 6... courses are four class level then algorithm can't found result for this sequence */
				changeOrderOfCourseList(courseList);		/* Therefore we change course list's order before call backtrackingForwardChecking function */
				result = backtrackingForwardChecking(scheduleMatrix, courseList, timeslotList);/* we try 100 times because we think it is enough if there is a result */
				
				if(result)		/* if result is true then find a exam schedule */
					break;
			}
			
			displayExamSchedule(scheduleMatrix, scheduleBufferedWriter, result);
			
			scheduleBufferedWriter.close();		/* always close files ;) */
			coursesBufferedReader.close();
			timeslotBufferedReader.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	public static void createCourseList(List<Course> courseList, String line, String courseLevel){
		
		Course newCourse = new Course();			/* create Course object */
		
		newCourse.setCourseName(line);				/* set object's variables */
		newCourse.setCourseLevel(courseLevel);
		newCourse.setAvailability(true);
		
		courseList.add(newCourse);					/* add object in list */
		
	}
	
	public static void createScheduleMatrix(List<VariableNode[]> scheduleMatrix, List<String> timeslotList, List<Course> courseList){
		
		int weekNumber = calculateWeekNumber(courseList, timeslotList);
		String[] weekdaysArray = {"Monday" , "Tuesday" , "Wednesday" , "Thursday" , "Friday"};
		
		for(int i=0; i<weekNumber; i++){						/* each weeks */
			
			for(int j=0; j<weekdaysArray.length; j++){				/* each weekdays */
				
				VariableNode[] columnVariableNodeArray = new VariableNode[timeslotList.size()];
				
				for(int k=0; k<timeslotList.size(); k++){				/* each time slots */
					
					VariableNode newNode = new VariableNode();
					
					newNode.setWeekday(weekdaysArray[j]);		 /* set VariableNode variables */
					newNode.setTimeslot(timeslotList.get(k));
					newNode.setDomain(null);
					
					columnVariableNodeArray[k] = newNode;		/* add newNode in array */
				}
				
				scheduleMatrix.add(columnVariableNodeArray);	/* add array in array list */
				
			}
			
		}
				
	}
	
	public static int calculateWeekNumber(List<Course> courseList, List<String> timeslotList){
		
		double weekNumber = (double)( courseList.size() )  / (double)( timeslotList.size()*5 ) ;	/* calculate week number */
		weekNumber = Math.ceil(weekNumber);
				
		return (int)weekNumber;
	}
	
	public static boolean backtrackingForwardChecking(List<VariableNode[]> scheduleMatrix, List<Course> courseList, List<String> timeslotList){
		
		boolean constraint1 = false, constraint2 = false , constraint3 = false;
		int firstClassExamCount = 0, secondClassExamCount = 0, thirdClassExamCount = 0, fourthClassExamCount = 0, lastRow, lastColumn;	
		
		lastRow = (int)Math.ceil( (double)courseList.size() / (double)timeslotList.size() )-1;		/* calculate last  */
		lastColumn = ( courseList.size() - (lastRow)*4 )-1;
		
		for(int i=0; i<scheduleMatrix.size(); i++){					//each column as weekdays
			
			for(int j=0; j<scheduleMatrix.get(0).length; j++){		//each row as time slots
				
				
				if(j == 0){		//if time slot is first then three is no constraint so we set first available course 
					
					for(Course course : courseList){
						
						if(course.isAvailability() == true){			/* set the first suitable course */
							
							scheduleMatrix.get(i)[j].setDomain(course);
							course.setAvailability(false);
							break;
						}
					}
					
				}
				else{
					
					for(int k=0; k<courseList.size(); k++){
						
						if(courseList.get(k).isAvailability() == true){
							
							//First Constraint
							if(scheduleMatrix.get(i)[j-1].getDomain().getCourseLevel().equals("4") == false){			//constraint1Control => The exams of the same level classes shouldn’t be placed
																														//in consecutive time slots on the same weekday(Except fourth classes exams)
								if( scheduleMatrix.get(i)[j-1].getDomain().getCourseLevel().equals( courseList.get(k).getCourseLevel() ) == false )
									constraint1 = true;
								
							}else			//if class is fourth then true because constraint1 is not valid for fourth class
								constraint1=true;
							
							//Second and Third Constraint
							for(int l=0; l<j; l++){
								
								if(scheduleMatrix.get(i)[l].getDomain().getCourseLevel().equals("1"))
									firstClassExamCount++;
								
								else if(scheduleMatrix.get(i)[l].getDomain().getCourseLevel().equals("2"))
									secondClassExamCount++;
								
								else if(scheduleMatrix.get(i)[l].getDomain().getCourseLevel().equals("3"))
									thirdClassExamCount++;
								
								else if(scheduleMatrix.get(i)[l].getDomain().getCourseLevel().equals("4"))
									fourthClassExamCount++;
								
							}
							
							if(courseList.get(k).getCourseLevel().equals("1"))
								firstClassExamCount++;
							
							else if(courseList.get(k).getCourseLevel().equals("2"))
								secondClassExamCount++;
							
							else if(courseList.get(k).getCourseLevel().equals("3"))
								thirdClassExamCount++;
							
							else if(courseList.get(k).getCourseLevel().equals("4"))
								fourthClassExamCount++;
							
							
							if( firstClassExamCount < 3 && secondClassExamCount < 3 && thirdClassExamCount < 3 )	//The exams of the first, second and third classes shouldn’t be placed
								constraint2 = true;																	//in more than two slots on the same weekday(Except fourth classes exams).
								
							if( fourthClassExamCount < 4 )															//The exams of the fourth class shouldn’t be placed in more than three
								constraint3 = true;																	//slots on the same weekday.
								
							if(constraint1 && constraint2 && constraint3){
								scheduleMatrix.get(i)[j].setDomain(courseList.get(k));
								courseList.get(k).setAvailability(false);
								
								break;
							}
							
						}
						
						if( k == courseList.size()-1 ){
							if( constraint1 == false || constraint2 == false || constraint3 == false )
								return false;
						}
						
						firstClassExamCount=0;			//update variables for use again
						secondClassExamCount=0;
						thirdClassExamCount=0;
						fourthClassExamCount=0;
						
						constraint1 = false;
						constraint2= false;
						constraint3 = false;
						
					}
					
				}
				
				if(i==lastRow && j==lastColumn && constraint1 && constraint2 && constraint3)
					return true;
				
			}
			
		}
		return false;
	}
	
	public static void changeOrderOfCourseList(List<Course> courseList){
		
		int index1=0, index2=0, i;
		
		for(i=0; i<100; i++){			/* do changing operation 100 times, we think enough 100 times */
			
			Random random = new Random();
			
			index1 = random.nextInt(courseList.size()-1)+0 ;	/* generate random two index that between 0 and courseList's size */
			index2 = random.nextInt(courseList.size()-1)+0 ;
			
			Course temp = courseList.get(index1);				/* change any two courseList's element */
			courseList.set(index1, courseList.get(index2));
			courseList.set(index2, temp);
			
		}
		
	}
	
	public static void displayExamSchedule(List<VariableNode[]> scheduleMatrix, BufferedWriter scheduleBufferedWriter, boolean result){
		
		try{
			
			if(result == false)
				scheduleBufferedWriter.write("There is not any schedule for this exams list");
			
			else{
				
				for(VariableNode[] variableNodeArray:scheduleMatrix){		/* for each loops to all scheduleMatrix's nodes */
					
					for(VariableNode node : variableNodeArray){
						
						if(node.getDomain() != null){
							
							scheduleBufferedWriter.write( node.getDomain().getCourseName()+" "+node.getWeekday()+" "+node.getTimeslot());
							scheduleBufferedWriter.newLine();
						}
						
					}
					scheduleBufferedWriter.newLine();
				}
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}

}
