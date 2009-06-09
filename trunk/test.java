import java.io.*;
import java.util.*;

class test {
    public static void main(String [] args) {
	int [] a = new int [100];
	for (int i = 0; i < 100; i++)
	    a[i] = 0;


	for (int j = 0; j < 20; j++) {
	    bitOp.Set(a, j);
	    for (int i = 0; i < 20; i++)
		System.out.print((bitOp.on(a, i) ? 1 : 0) + " ");
	    System.out.println();
	}

	System.out.println("Unset") ;

	for (int j = 0; j < 20; j++) {
	    bitOp.Unset(a, j);

	    for (int i = 0; i < 20; i++)
		System.out.print((bitOp.on(a, i) ? 1 : 0) + " ");
	    System.out.println();
	}
    }
}
