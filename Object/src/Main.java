import java.io.*;
public class Main {
	 public static void main(String args[]) {
	      
	      Employee empOne = new Employee("James Smith");
	      Employee empTwo = new Employee("Mary");

	      empOne.empAge(26);
	      empOne.empDesignation("Senior Software Engineer");
	      empOne.empSalary(1000);
	      empOne.printEmployee();

	      empTwo.empAge(21);
	      empTwo.empDesignation("Software Engineer");
	      empTwo.empSalary(400);
	      empTwo.printEmployee();
	   }

}