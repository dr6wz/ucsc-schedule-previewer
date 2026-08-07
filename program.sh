javac -d bin  -cp ./gson-2.12.1.jar src/main/*.java
java -cp bin:./gson-2.12.1.jar main.ScheduleParser
