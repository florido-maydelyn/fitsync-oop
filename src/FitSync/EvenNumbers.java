package FitSync;

public class EvenNumbers {
    public static void main(String args[]) {
        int count = 0;
        for (int evenNum = 2; evenNum <= 100; evenNum++) {
            if (evenNum % 2 != 0) {
                continue;
            }
            System.out.print(evenNum + "\t");
            count++;
            if (count % 10 == 0) {
                System.out.println();
            }
        }

        System.out.println("");
        int evenNum = 2;
        count = 0;
        boolean flag = true;
        while (flag) {
            if (evenNum % 2 == 0) {
                System.out.print(evenNum + "\t");
                count++;
                if (count % 10 == 0) {
                    System.out.println();
                }
            }
            evenNum++;
            if (evenNum > 100) {
                flag = false;
            }
        }
    }
}
