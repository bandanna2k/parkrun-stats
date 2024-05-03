package dnt.parkrun.menu;

import dnt.parkrun.stats.invariants.ParsersTest;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        new Main().go();
    }

    private void go() throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String choice = "";
        while(!choice.equals("X"))
        {
            displayOptions();

            choice = reader.readLine().toUpperCase(Locale.ROOT);

            action(choice);
        }
    }

    private void action(String choice)
    {
        switch (choice)
        {
            case "X": break;
            case "Q":
                runInvariantsQuick();
                break;
            default:
                System.out.printf("No action for '%s'%n", choice);
        }
    }

    private void runInvariantsQuick()
    {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));

        Result result = junit.run(
                ParsersTest.class
        );

        resultReport(result);
    }

    private void resultReport(Result result)
    {
        int passCount = result.getRunCount() - result.getFailureCount() - result.getAssumptionFailureCount() - result.getIgnoreCount();
        System.out.println("Passed: " + passCount);
        System.out.println("Failed: " + result.getFailureCount());
        result.getFailures().forEach(failure -> {
            System.out.println(failure.getTestHeader() + "\t" + failure.getMessage());
        });
    }

    private void displayOptions()
    {
        System.out.println("----------------------");
        System.out.println("Q) Quick run invariants (re-runnable, takes 1 minute");
        System.out.println("I) run Invariants (re-runnable, takes 5 minutes)");
        System.out.println("X) eXit");
        System.out.println("----------------------");
    }
}