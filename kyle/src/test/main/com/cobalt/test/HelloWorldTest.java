package com.cobalt.test;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by K25627 on 10/24/2015.
 */
@RunWith(Parameterized.class)
public class HelloWorldTest {

    @Parameterized.Parameters(name = "{index}: input string \"{0}\"")
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][]{
                {"test input"},
                {"test"},
                {"test method"},
                {"input"},
                {"method"},
                {"hello world"}
        });
    }

    @Parameterized.Parameter
    public String in;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testTestMethod() throws Throwable {
        try {
            System.out.println(HelloWorld.testMethod(in));
        } catch (Throwable t){
            thrown.expect(Exception.class);
            throw t;
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.flush();
    }
}