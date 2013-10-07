//Note: This class won't compile by design!
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class Example {

 private List<Integer> list;
 private static final int SIZE = 10;

 public Example() {
     list = new ArrayList<Integer>(SIZE);
     for (int i = 0; i < SIZE; i++) {
         list.add(new Integer(i));
     }
 }

 public void writeList() throws IOException {
     PrintWriter out = new PrintWriter(new FileWriter("OutFile.txt"));

     for (int i = 0; i < SIZE; i++) {
         out.println("Value at: " + i + " = " + list.get(i));
     }
     out.close();
 }
}