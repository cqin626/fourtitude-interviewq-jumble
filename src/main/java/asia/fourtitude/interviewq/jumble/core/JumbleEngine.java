package asia.fourtitude.interviewq.jumble.core;

import java.io.*;
import java.util.*;

public class JumbleEngine {
    private static final Random RANDOM = new Random();

    private List<String> words;
    private List<String> palindromeWordList;
    private Map<Integer, List<String>> wordLengthMap;
    private Set<String> wordSet;

    private void ensureWordsLoaded() {
        if (words == null) {
            words = new ArrayList<>();
            loadWords();
        }
    }

    private void ensurePalindromeWordsLoaded() {
        if (palindromeWordList == null) {
            ensureWordsLoaded();
            palindromeWordList = new ArrayList<>();
            loadPalindromeWords();
        }
    }

    private void ensureWordLengthMapInitialized() {
        if (wordLengthMap == null) {
            ensureWordsLoaded();
            wordLengthMap = new HashMap<>();
            for (String word : words) {
                int wordLength = word.length();

                if (!wordLengthMap.containsKey(wordLength)) {
                    List<String> wordsByLength = new ArrayList<>();
                    wordsByLength.add(word);
                    wordLengthMap.put(wordLength, wordsByLength);
                } else {
                    wordLengthMap.get(wordLength).add(word);
                }
            }
        }
    }

    private void ensureWordSetInitialized() {
        if (wordSet == null) {
            ensureWordsLoaded();
            wordSet = new HashSet<>();
            for (String word : words) {
                wordSet.add(word);
            }
        }
    }

    private void loadWords() {
        String resourcePath = "words.txt";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new RuntimeException("Resource not found: " + resourcePath);
        }

        try (
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String word;
            while ((word = bufferedReader.readLine()) != null) {
                words.add(getNormalizedWord(word));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load from words", e);
        }
    }

    private void loadPalindromeWords() {
        for (String word : words) {
            if (isPalindromeWord(word)) {
                palindromeWordList.add(word);
            }
        }
    }

    private String getNormalizedWord(String word) {
        return word.trim().toLowerCase();
    }

    /**
     * From the input `word`, produces/generates a copy which has the same
     * letters, but in different ordering.
     *
     * Example: from "elephant" to "lehnaetp".
     *
     * Evaluation/Grading:
     * a) pass unit test: JumbleEngineTest#scramble()
     * b) scrambled letters/output must not be the same as input
     *
     * @param word The input word to scramble the letters.
     * @return The scrambled output/letters.
     */
    public String scramble(String word) {
        if (word == null) {
            return word;
        }
        String normalizedWord = getNormalizedWord(word);

        if (normalizedWord.chars().distinct().count() == 1 || normalizedWord.length() < 2) {
            return normalizedWord;
        }

        char[] charArr = normalizedWord.toCharArray();
        String scrambledWord;

        do {
            for (int i = charArr.length - 1; i > 0; i--) {
                int randInt = RANDOM.nextInt(i + 1);
                char temp = charArr[i];
                charArr[i] = charArr[randInt];
                charArr[randInt] = temp;
            }
            scrambledWord = String.valueOf(charArr);
        } while (scrambledWord.equals(normalizedWord));

        return scrambledWord;
    }

    /**
     * Retrieves the palindrome words from the internal
     * word list/dictionary ("src/main/resources/words.txt").
     *
     * Word of single letter is not considered as valid palindrome word.
     *
     * Examples: "eye", "deed", "level".
     *
     * Evaluation/Grading:
     * a) able to access/use resource from classpath
     * b) using inbuilt Collections
     * c) using "try-with-resources" functionality/statement
     * d) pass unit test: JumbleEngineTest#palindrome()
     *
     * @return The list of palindrome words found in system/engine.
     * @see https://www.google.com/search?q=palindrome+meaning
     */
    public Collection<String> retrievePalindromeWords() {
        ensurePalindromeWordsLoaded();
        return Collections.unmodifiableCollection(palindromeWordList);
    }

    private boolean isPalindromeWord(String word) {
        if (word == null || word.length() == 1) {
            return false;
        }
        int start = 0;
        int end = word.length() - 1;

        while (start <= end) {
            if (word.charAt(start) != word.charAt(end)) {
                return false;
            }
            start++;
            end--;
        }
        return true;
    }

    /**
     * Picks one word randomly from internal word list.
     *
     * Evaluation/Grading:
     * a) pass unit test: JumbleEngineTest#randomWord()
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param length The word picked, must of length.
     *               When length is null, then return random word of any length.
     * @return One of the word (randomly) from word list.
     *         Or null if none matching.
     */
    public String pickOneRandomWord(Integer length) {
        if (length == null) {
            ensureWordsLoaded();
            return words.get(RANDOM.nextInt(words.size()));
        }
        ensureWordLengthMapInitialized();

        List<String> matchingWords = wordLengthMap.get(length);

        return matchingWords == null ? null : matchingWords.get(RANDOM.nextInt(matchingWords.size()));
    }

    /**
     * Checks if the `word` exists in internal word list.
     * Matching is case insensitive.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param word The input word to check.
     * @return true if `word` exists in internal word list.
     */
    public boolean exists(String word) {
        if (word == null) {
            return false;
        }
        String normalizedWord = getNormalizedWord(word);
        ensureWordSetInitialized();
        return wordSet.contains(normalizedWord);
    }

    /**
     * Finds all the words from internal word list which begins with the
     * input `prefix`.
     * Matching is case insensitive.
     *
     * Invalid `prefix` (null, empty string, blank string, non letter) will
     * return empty list.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param prefix The prefix to match.
     * @return The list of words matching the prefix.
     */
    public Collection<String> wordsMatchingPrefix(String prefix) {
        /*
         * Refer to the method's Javadoc (above) and implement accordingly.
         * Must pass the corresponding unit tests.
         */
        throw new UnsupportedOperationException("to be implemented");
    }

    /**
     * Finds all the words from internal word list that is matching
     * the searching criteria.
     *
     * `startChar` and `endChar` must be 'a' to 'z' only. And case insensitive.
     * `length`, if have value, must be positive integer (>= 1).
     *
     * Words are filtered using `startChar` and `endChar` first.
     * Then apply `length` on the result, to produce the final output.
     *
     * Must have at least one valid value out of 3 inputs
     * (`startChar`, `endChar`, `length`) to proceed with searching.
     * Otherwise, return empty list.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param startChar The first character of the word to search for.
     * @param endChar   The last character of the word to match with.
     * @param length    The length of the word to match.
     * @return The list of words matching the searching criteria.
     */
    public Collection<String> searchWords(Character startChar, Character endChar, Integer length) {
        /*
         * Refer to the method's Javadoc (above) and implement accordingly.
         * Must pass the corresponding unit tests.
         */
        throw new UnsupportedOperationException("to be implemented");
    }

    /**
     * Generates all possible combinations of smaller/sub words using the
     * letters from input word.
     *
     * The `minLength` set the minimum length of sub word that is considered
     * as acceptable word.
     *
     * If length of input `word` is less than `minLength`, then return empty list.
     *
     * The sub words must exist in internal word list.
     *
     * Example: From "yellow" and `minLength` = 3, the output sub words:
     * low, lowly, lye, ole, owe, owl, well, welly, woe, yell, yeow, yew, yowl
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param word      The input word to use as base/seed.
     * @param minLength The minimum length (inclusive) of sub words.
     *                  When zero, return empty list.
     *                  Default is 3.
     * @return The list of sub words constructed from input `word`.
     */
    public Collection<String> generateSubWords(String word, Integer minLength) {
        /*
         * Refer to the method's Javadoc (above) and implement accordingly.
         * Must pass the corresponding unit tests.
         */
        throw new UnsupportedOperationException("to be implemented");
    }

    /**
     * Creates a game state with word to guess, scrambled letters, and
     * possible combinations of words.
     *
     * Word is of length 6 characters.
     * The minimum length of sub words is of length 3 characters.
     *
     * @param length    The length of selected word.
     *                  Expects >= 3.
     * @param minLength The minimum length (inclusive) of sub words.
     *                  Expects positive integer.
     *                  Default is 3.
     * @return The game state.
     */
    public GameState createGameState(Integer length, Integer minLength) {
        Objects.requireNonNull(length, "length must not be null");
        if (minLength == null) {
            minLength = 3;
        } else if (minLength <= 0) {
            throw new IllegalArgumentException("Invalid minLength=[" + minLength + "], expect positive integer");
        }
        if (length < 3) {
            throw new IllegalArgumentException("Invalid length=[" + length + "], expect greater than or equals 3");
        }
        if (minLength > length) {
            throw new IllegalArgumentException(
                    "Expect minLength=[" + minLength + "] greater than length=[" + length + "]");
        }
        String original = this.pickOneRandomWord(length);
        if (original == null) {
            throw new IllegalArgumentException("Cannot find valid word to create game state");
        }
        String scramble = this.scramble(original);
        Map<String, Boolean> subWords = new TreeMap<>();
        for (String subWord : this.generateSubWords(original, minLength)) {
            subWords.put(subWord, Boolean.FALSE);
        }
        return new GameState(original, scramble, subWords);
    }

}
