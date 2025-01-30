package com.shakespeare.new_app;

public class GlobalClass {
    public static int fontsizesp = 16;

    public static String system_prompt = "Assume this query relates to William Shakespeare's plays.";

    public static String selectedPlay = "";
    public static String selectedPlayCode = "";
    public static String selectedPlayFilename = "";
//    public static String user_prompt = "";
//
//    public static String userPromptPlay = "";

    public static Integer selectedActNumber = 0; // added for database navigation
    public static Integer selectedSceneNumber = 0; // added for database navigation
    public static Integer numberOfScenesInAct = 0; // added for database navigation
    public static Integer numberOfActsInPlay = 0; // added for database navigation

    public static Integer intAboutYouScreenSource = 0; // source of About You click Main = 0, Settings Home = 1.
    public static Integer intShowLineNumbers = 1; // Show line numbers = 1, and hide line numbers = 0.


}
