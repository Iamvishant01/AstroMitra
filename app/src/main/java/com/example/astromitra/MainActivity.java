package com.example.astromitra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText nameInput, dayInput, yearInput;
    private Spinner monthSpinner;
    private TextView generatedTextView;
    private Executor executor = Executors.newSingleThreadExecutor();
    private GenerativeModelFutures geminiModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        nameInput = findViewById(R.id.textInputEditText);
        dayInput = findViewById(R.id.dayEditText);
        yearInput = findViewById(R.id.yearEditText);
        monthSpinner = findViewById(R.id.monthSpinner);
        generatedTextView = findViewById(R.id.generatedText1);
        Button showInsightsButton = findViewById(R.id.filledButton);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setupAppBarMenu(topAppBar);
        setupMonthSpinner();

        // Initialize Gemini API
        setupGeminiModel();

        // Set onClickListener for the button
        showInsightsButton.setOnClickListener(v -> showInsights());
    }
    /**
     * Configures the AppBar menu to open a browser link when the menu item is clicked.
     */

    private void setupAppBarMenu(MaterialToolbar topAppBar) {
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.item_profile) {
                // Open the developer's profile link in a browser
                String url = "https://linktr.ee/iamvishant02"; // Replace with your desired URL
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupMonthSpinner() {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, months);
        monthSpinner.setAdapter(adapter);
    }

    private void setupGeminiModel() {
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.15f;
        configBuilder.topK = 32;
        configBuilder.topP = 1f;
        configBuilder.maxOutputTokens = 4096;

        geminiModel = GenerativeModelFutures.from(new GenerativeModel(
                "gemini-1.5-flash-001",
                BuildConfig.apikey,
                configBuilder.build(),
                null // No custom safety settings
        ));
    }

    private void showInsights() {
        runOnUiThread(() -> generatedTextView.setText("Loading ..."));
        // Retrieve user input
        String name = getTextInput(nameInput);
        String day = getTextInput(dayInput);
        String year = getTextInput(yearInput);
        String month = monthSpinner.getSelectedItem().toString();

        // Validate input
        if (name.isEmpty() || day.isEmpty() || year.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse day and year to integers
        int dayValue;
        int yearValue;
        try {
            dayValue = Integer.parseInt(day);
            yearValue = Integer.parseInt(year);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numerical values for day and year", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate content using Gemini API
        generateInsightsFromGemini(name, dayValue, getMonthNumber(month), yearValue);
    }

    private void generateInsightsFromGemini(String name, int day, int monthNumber, int year) {
        // Calculate Mulank
        int mulank = calculateMulank(day);

        // Calculate Bhagyank
        int bhagyank = calculateBhagyank(mulank, monthNumber, year);
        // Prepare the content request
        String prompt = String.format(
                "Generate an astrology insight for:\n" +
                        "Name: %s\nDay: %d\nMonth: %d\nYear: %d.\n with " +
                        "Mulank: %d\nBhagyank: %d\n"+"you have to provide prediction in two one by using numerology and second is by lal kitab on this topics" +
                        "1 Career Guidance and Success:\n" +
                        "Description: Advice on career choices, suitable fields, or professional opportunities.\n" +
                        "Curiosity: People want to know whether they're on the right career path, when is the right time to change jobs, or how they can achieve professional success.\n" +
                        "\n" +
                        "2. Love, Relationship, and Marriage Predictions:\n" +
                        "Description: Insights into romantic relationships, marriage compatibility, and predictions.\n" +
                        "Curiosity: People often seek to know their ideal partner or when they might get married, or understand their relationship dynamics better (like compatibility with a partner).\n" +
                        "3. Health and Well-being:\n" +
                        "Description: Personal health insights, recommendations for better well-being based on numerology.\n" +
                        "Curiosity: How to maintain good health or when they may face certain health challenges.\n" +
                        "4. Personality and Traits Analysis:\n" +
                        "Description: Detailed personality insights based on numerology, including strengths, weaknesses, and hidden traits.\n" +
                        "Curiosity: People are eager to understand their true nature, quirks, or tendencies, and how they are perceived by others.\n" +
                        "5. Wealth, Prosperity, and Financial Predictions:\n" +
                        "Description: Predictions related to financial success, business growth, or making investments.\n" +
                        "Curiosity: Users often seek advice on how to attract wealth or which financial decisions may lead to prosperity.\n" +
                        "6. Family and Children Predictions:\n" +
                        "Description: Future predictions about family dynamics, children, or how to improve family relationships.\n" +
                        "Curiosity: People often wonder about having children, their family life, or the type of parents they might become.\n" +
                        "7. Luck, Destiny, and Divine Timing:\n" +
                        "Description: When certain events (like a big breakthrough, chance meeting, or turning point) might take place based on numerological cycles.\n" +
                        "Curiosity: They want to know when \"their luck\" will change or whether they’ll soon be presented with life-altering opportunities.\n" +
                        "8. Life Challenges and Overcoming Obstacles:\n" +
                        "Description: Guidance on dealing with upcoming challenges or life lessons.\n" +
                        "Curiosity: People may look for advice on how to navigate life’s difficulties, either internal struggles (e.g., self-doubt) or external issues (e.g., relationships).\n" +
                        "9. Future Life Milestones and Events:\n" +
                        "Description: General predictions about major life milestones such as moving to a new place, change in career, or financial windfalls.\n" +
                        "Curiosity: They may ask about key events like promotions, relocations, or any significant shifts in their future.\n" +
                        "10. Current Year or Monthly Predictions:\n" +
                        "Description: Predictions based on the person’s birthdate for the current running year and month\n" +
                        "Curiosity: What the upcoming year/month will look like in terms of luck, success, and personal growth.\n" +
                        "11. Best Time for Important Decisions:\n" +
                        "Description: Help determine when is the best time to start a new venture, business, or make big life changes.\n" +
                        "Curiosity: People often want to time their major decisions, like marriage, investment, or launching a new career.\n" +
                        "12. Compatibility Analysis (Friends/Girlfreind/Colleagues/Business Partners):\n" +
                        "Description: How well they connect with others in terms of friendships, professional collaborations, or business partnerships. and people with which mulanks are good with user \n" +
                        "Curiosity: Will a partnership be successful? Is it a good time to collaborate with someone new?\n" +
                        "13. Soul’s Purpose and Life’s Mission:\n" +
                        "Description: Insights on their life mission, spiritual purpose, or calling.\n" +
                        "Curiosity: People often seek clarity on their deeper life purpose or what they’re meant to do in life.\n" +
                        "14. Psychic and Intuition Insights:\n" +
                        "Description: Offering intuitive insights based on the user’s numerology chart.\n" +
                        "Curiosity: People may wish to know how intuitive they are and how they can tap into their own psychic abilities or inner wisdom.\n" +
                        "15. Relationships with Parents or Siblings:\n" +
                        "Description: Provide insights on family dynamics, especially relating to their bond with parents or siblings.\n" +
                        "Curiosity: Some users are curious about how to improve familial relationships or understand their role in the family better.\n" +
                        "\n",
                name, day, monthNumber, year, mulank, bhagyank
        );

        Content content = new Content.Builder().addText(prompt).build();

        // Call Gemini's generateContent
        ListenableFuture<GenerateContentResponse> response = geminiModel.generateContent(content);

        // Handle the response asynchronously
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> generatedTextView.setText(result.getText()));
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    t.printStackTrace();
                    Toast.makeText(MainActivity.this, "Failed to generate insights", Toast.LENGTH_SHORT).show();
                });
            }
        }, executor);

    }
    // Function to calculate Mulank
    private int calculateMulank(int day) {
        // If the day is greater than 9, sum its digits
        if (day > 9) {
            day = sumDigits(day);
        }
        return day;
    }

    // Function to sum digits of a number until it is a single digit
    private int sumDigits(int number) {
        int sum = 0;
        while (number > 0) {
            sum += number % 10;
            number /= 10;
        }
        // Keep summing digits until single digit
        if (sum > 9) {
            sum = sumDigits(sum);
        }
        return sum;
    }

    // Function to calculate Bhagyank
    private int calculateBhagyank(int mulank, int monthNumber, int year) {
        // Reduce monthNumber to a single digit if it's two digits
        if (monthNumber > 9) {
            monthNumber = sumDigits(monthNumber);
        }

        // Reduce year to a single digit
        int yearSum = sumDigits(year);

        // Sum all components: mulank + month number + reduced year
        int bhagyank = mulank + monthNumber + yearSum;

        // Reduce the resulting Bhagyank to a single digit
        return sumDigits(bhagyank);
    }
    private String getTextInput(TextInputEditText inputField) {
        return inputField.getText() != null ? inputField.getText().toString().trim() : "";
    }

    private int getMonthNumber(String month) {
        switch (month) {
            case "January": return 1;
            case "February": return 2;
            case "March": return 3;
            case "April": return 4;
            case "May": return 5;
            case "June": return 6;
            case "July": return 7;
            case "August": return 8;
            case "September": return 9;
            case "October": return 10;
            case "November": return 11;
            case "December": return 12;
            default: return 0;
        }
    }
}
