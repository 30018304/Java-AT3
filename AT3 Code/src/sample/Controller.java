package sample;

import com.opencsv.CSVWriter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.ResourceBundle;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.*;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Controller implements Initializable {
    @FXML
    public Button logonButton;

    @FXML
    public TextArea ta;

    @FXML
    public Button addButton;

    @FXML
    public Button mergeButton;

    @FXML
    public Button createUserButton;

    @FXML
    public Button binarySearchButton;

    @FXML
    public Button exportCSVButton;

    @FXML
    public TextField mealNumberInput;

    @FXML
    public TextField proteinNumberInput;

    @FXML
    public TextField carbNumberInput;

    @FXML
    public TextField fatNumberInput;

    @FXML
    public TextField userNameInput;

    @FXML
    public TextField passwordInput;

    @FXML TextField binarySearchInput;

    public void binarySearchButtonClicked() {
        int searchTerm = Integer.parseInt(binarySearchInput.getText());

        int[][] searchArray = new int[foodList.size()][5];

        //convert linkedlist to array for binary search
        // loop through and put linkedlist elements into array
        for(int i = 0; i < foodList.size(); i++) {
            Food food = foodList.get(i);
            searchArray[i][0] = food.calories;
            searchArray[i][1] = food.mealNumber;
            searchArray[i][2] = food.protein;
            searchArray[i][3] = food.carbs;
            searchArray[i][4] = food.fat;
        }
        //returns true or false if found
        boolean found = linearSearch(searchArray, searchTerm);
        System.out.println(found);

        if (found == true) {
            dialog.setTitle("Found");
            dialog.setContentText("Found");
        }
        else {
            dialog.setTitle("Not Found");
            dialog.setContentText("Not Found");
        }
        ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.showAndWait();
    }

    public void createUser() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String userName = userNameInput.getText();
        String password = passwordInput.getText();
        doesUserExist(userName);

        byte[] newSalt = generateSalt();

        byte[] encryptedPassword = getEncryptedPassword(password, newSalt);

        String sEncryptedPw = Base64.getEncoder().encodeToString(encryptedPassword);

        String sSalt = Base64.getEncoder().encodeToString(newSalt);

        usersCount++;

        usersArray[0][0] = userName;
        usersArray[0][1] = sEncryptedPw;
        usersArray[0][2] = sSalt;

        //POPUP user created successfully

        dialog.setTitle("Success");
        dialog.setContentText("User successfully created");
        ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);

        dialog.showAndWait();
    }

    public class Food {
        int calories;
        int mealNumber;
        int protein;
        int carbs;
        int fat;

        public String toString() {
            return ("Meal Number: " + mealNumber + ", Calories: " + calories + ", Protein: " + protein + "grams, Carbs: " + carbs + "grams, Fat: " + fat + "grams\n");
        }
    }

    //Linked list to store food objects
    LinkedList<Food> foodList = new LinkedList<Food>();
    //2d array to convert from linkedlist in order to merge sort
    public static String[][] usersArray = new String[20][3];
    public static boolean isUserNameMatched;
    static int usersCount = 0;
    public static int foundUserIndex;
    public static String[][] foundUserArray = new String[1][3];
    //Dialog popup for displaying messages
    Dialog<String> dialog = new Dialog<String>();
    ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);

    public void exportCSVButtonClicked() throws IOException, CsvException {
        //filename to export entries to CSV
        var fileName = "src/foodEntries.csv";

        //string array to store linkedlist entries as CSV
        String[] entries = new String[foodList.size()];

        //loop through linkedlist entries and assign them to the new array
        for(int i = 0; i < foodList.size(); i++) {
            Food food = foodList.get(i);
            entries[i] = food.toString();
        }

        //write the string array to csv
        try (var fos = new FileOutputStream(fileName);
             var osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             var writer = new CSVWriter(osw)) {

            writer.writeNext(entries);
        }
    }


    public boolean linearSearch(int[][] a, int value) {
        int i = 0, j = a[0].length - 1; // start from top right corner

        while (i < a.length && j >= 0) {
            if (a[i][j] == value) {
                return true;

            } else if (a[i][j] > value) {
                j--; // move left
            } else {
                i++; // move down
            }
        }
        // element not found
        return false;


    }

    public void logonButtonClick() throws Exception {
        String userNameLogon = userNameInput.getText();
        System.out.println("username is:" + userNameLogon);
        String passwordLogon = passwordInput.getText();
        System.out.println("password is:" + passwordLogon);

        //check if user exists already
        doesUserExist(userNameLogon);

        //if the user exists, check password against stored hash and salts
        if (foundUserArray[0][0].equals("true")) {
            int foundUserIndex = Integer.parseInt(foundUserArray[0][1]);

            String encPwString = usersArray[foundUserIndex][1];

            String saltString = usersArray[foundUserIndex][2];

            byte[] decodedPw = Base64.getDecoder().decode(encPwString.getBytes("UTF-8"));

            byte[] decodedSalt = Base64.getDecoder().decode(saltString.getBytes("UTF-8"));

            boolean authenticated = authenticate(passwordLogon, decodedPw, decodedSalt);

            //if the logon is successful, show the main application window
            if(authenticated) {
                Stage stage1 = (Stage) logonButton.getScene().getWindow();
                stage1.close();
                sample.Main main = new Main();
                main.showWindow();
            }
            //if the password was wrong, display popup message
            else {
                dialog.setTitle("Incorrect password");
                dialog.setContentText("Incorrect password");
                ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
                dialog.showAndWait();
            }
        }
        //if the user does not exist, display popup message
        else {
            System.out.println("user does not exist");
            dialog.setTitle("No user by that username exists");
            dialog.setContentText("No user by that username exists");

            ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);

            dialog.showAndWait();
        }



    }

    //handler for the add button being clicked
    public void addButtonClicked() throws Exception {
        //creates a new food object from the textfield inputs
        Food newFood = new Food();

        //convert the textfield inputs to integers and calculate the number of calories from the meal
        newFood.mealNumber = Integer.parseInt(mealNumberInput.getText());
        newFood.protein = Integer.parseInt(proteinNumberInput.getText());
        newFood.carbs = Integer.parseInt(carbNumberInput.getText());
        newFood.fat = Integer.parseInt(fatNumberInput.getText());
        newFood.calories = (newFood.protein * 4) + (newFood.carbs * 4) + (newFood.fat * 9);

        //add the newly created food object to the linkedlist
        foodList.addLast(newFood);

        //set the text of the textarea to display the new meal
        ta.setText(foodList.toString());
    }

    //handler for the merge button being clicked
    public void mergeButtonClicked() throws Exception {
        //clear the text area to display the sorted results
        ta.clear();
        //declare a new array for merge sorting
        int[][] mergeArray = new int[foodList.size()][5];
        // loop through convert linked list to array
        for(int i = 0; i < foodList.size(); i++) {
            Food food = foodList.get(i);
            mergeArray[i][0] = food.calories;
            mergeArray[i][1] = food.mealNumber;
            mergeArray[i][2] = food.protein;
            mergeArray[i][3] = food.carbs;
            mergeArray[i][4] = food.fat;

        }
        //merge sort the array
        MergeSort(mergeArray);
        //clear the linkedlist to reinsert the sorted objects
        foodList.clear();

        //convert back to linkedlist
        for(int i = 0; i < mergeArray.length; i++) {
            Food newFood = new Food();
            newFood.calories = mergeArray[i][0];
            newFood.mealNumber = mergeArray[i][1];
            newFood.protein = mergeArray[i][2];
            newFood.carbs = mergeArray[i][3];
            newFood.fat = mergeArray[i][4];
            foodList.addLast(newFood);

        }
        //update the text area to display the sorted results
        ta.setText(foodList.toString());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dialog.getDialogPane().getButtonTypes().add(type);
    }

    public static void CopyLine(int[][] destArr, int destIndex, int[][] sourceArr, int sourceIndex)
    {
        for (int i = 0; i < destArr[1].length; ++i)
        {
            destArr[destIndex][i] = sourceArr[sourceIndex][i];
        }
    }

    //array merging
    static void Merge(int[][] num, int lowIndex, int middleIndex, int highIndex)
    {
        var left = lowIndex;
        var right = middleIndex + 1;
        var tempArray = new int[highIndex - lowIndex + 1][num[1].length];

        var index = 0;

        while ((left <= middleIndex) && (right <= highIndex))
        {
            if (num[left][0] < num[right][0])
            {
                CopyLine(tempArray, index, num, left);
                left++;
            }
            else
            {
                CopyLine(tempArray, index, num, right);
                right++;
            }

            index++;
        }

        for (var j = left; j <= middleIndex; j++)
        {
            CopyLine(tempArray, index, num, j);
            index++;
        }

        for (var j = right; j <= highIndex; j++)
        {
            CopyLine(tempArray, index, num, j);
            index++;
        }

        for (var j = 0; j < tempArray.length; j++)
        {
            CopyLine(num, lowIndex + j, tempArray, j);
        }
    }

    //merge sorting
    static void MergeSort(int[][] num, int lowIndex, int highIndex)
    {
        if (lowIndex < highIndex)
        {
            var middleIndex = (lowIndex + highIndex) / 2;
            MergeSort(num, lowIndex, middleIndex);
            MergeSort(num, middleIndex + 1, highIndex);
            Merge(num, lowIndex, middleIndex, highIndex);
        }
    }

    public static void MergeSort(int[][] num)
    {
        MergeSort(num, 0, num.length - 1);

    }

    static void WriteArray(String description, int[][] arr)
    {
        System.out.println(description);
        for (int i = 0; i < arr.length; i++)
        {
            for (int j = 0; j < arr[1].length; j++)
            {
                System.out.println(arr[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static boolean authenticate(String attemptedPassword, byte[] encryptedPassword, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Encrypt the clear-text password using the same salt that was used to
        // encrypt the original password
        byte[] encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);

        // Authentication succeeds if encrypted password that the user entered
        // is equal to the stored hash
        return Arrays.equals(encryptedPassword, encryptedAttemptedPassword);
    }

    public static byte[] getEncryptedPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {

        String algorithm = "PBKDF2WithHmacSHA1";

        int derivedKeyLength = 160;

        int iterations = 1000;

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);

        SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);

        return f.generateSecret(spec).getEncoded();
    }

    public static byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        // Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
        byte[] salt = new byte[8];
        random.nextBytes(salt);

        return salt;
    }

    public static String[][] doesUserExist(String userName) {
        //check if the username exists, if it does logon

        //****return the index the user was found in the form of an array
        for(int i = 0; i < usersCount; i++) {
            if (userName.equals(usersArray[i][0])) {
                foundUserIndex = i;
                isUserNameMatched = true;
                System.out.println(foundUserIndex);
                System.out.println("Success");
                foundUserArray[0][0] = "true";
                foundUserArray[0][1] = Integer.toString(foundUserIndex);
                foundUserArray[0][2] = userName;
            }
            else {
                foundUserArray[0][0]= "false";
            }
        }
        return foundUserArray;
    }
}