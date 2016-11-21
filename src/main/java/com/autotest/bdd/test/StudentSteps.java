package com.autotest.bdd.test;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;

public class StudentSteps {
    @Given("There is a student")
    public void initStudent() {
        System.out.println("sssss");
    }
    @Then("he is a student")
    public void heis() {
        System.out.println("ssssssssssss");
    }
}