package com.hierarchy.password_hierarchy_back.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class PasswordUtils {

    private static final Predicate<Character> UPPER_CASE = Character::isUpperCase;
    private static final Predicate<Character> LOWER_CASE = Character::isLowerCase;
    private static final Predicate<Character> DIGIT = Character::isDigit;
    private static final Predicate<Character> SYMBOL = ch -> !Character.isLetterOrDigit(ch);

    private static final int CHAR_LENGTH_SCORE = 4;
    private static final int UPPERCASE_BONUS = 2;
    private static final int LOWERCASE_BONUS = 2;
    private static final int DIGIT_BONUS = 4;
    private static final int SYMBOL_BONUS = 6;
    private static final int REPEAT_CHAR_DEDUCTION = 2;
    private static final int CONSECUTIVE_DEDUCTION = 2;
    private static final int SEQUENTIAL_DEDUCTION = 3;

    public static String encryptPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    // Method to calculate the password strength score
    public static long calculatePasswordStrength(String password) {
        long length = password.length();

        if (length < 8) {
            return 0;
        }

        final List<Character> chars = password.chars().mapToObj(c -> (char) c).collect(Collectors.toList());

        long upperCount = filterByCharacter(chars, UPPER_CASE);
        long lowerCount = filterByCharacter(chars, LOWER_CASE);
        long digitCount = filterByCharacter(chars, DIGIT);
        long symbolCount = filterByCharacter(chars, SYMBOL);

        long score = 0;

        // Additions
        score = additionsPasswordScores(score, chars, upperCount, lowerCount, digitCount, symbolCount, length);

        // Deductions
        score = calculatePasswordDeductions(score, chars, upperCount, lowerCount, digitCount, length);

        return scoreBetween0And100(score);
    }

    /**
     * Calculates the score for a password based on its character composition.
     *
     * @param score       the score to be processed
     * @param chars       the list of characters in the password
     * @param upperCount  the count of uppercase letters
     * @param lowerCount  the count of lowercase letters
     * @param digitCount  the count of digits
     * @param symbolCount the count of symbols
     * @param length      the length of the password
     * @return the calculated score
     */
    protected static long additionsPasswordScores(long score, List<Character> chars, long upperCount, long lowerCount,
                                                  long digitCount, long symbolCount, long length) {

        // 1. Score for Number of Characters
        score += calculateCharacterLengthScore(length);

        // 2. Score for Uppercase Letters
        score += calculateUppercaseBonus(upperCount, length);

        // 3. Score for Lowercase Letters
        score += calculateLowercaseBonus(lowerCount, length);

        // 4. Score for Digits
        score += digitCount * DIGIT_BONUS;

        // 5. Score for Symbols
        score += symbolCount * SYMBOL_BONUS;

        // 6. Score for Middle Numbers or Symbols
        score += calculateMiddleNumbersOrSymbolsScore(chars, length);

        // 7. Requirements Bonus
        score += calculateRequirementsBonus(upperCount, lowerCount, digitCount, symbolCount);

        return score;
    }

    /**
     * Calculates deductions for a password based on its character composition.
     *
     * @param score       the score to be processed
     * @param chars      the list of characters in the password
     * @param upperCount the count of uppercase letters
     * @param lowerCount the count of lowercase letters
     * @param digitCount the count of digits
     * @param length     the length of the password
     * @return the total deductions
     */
    protected static long calculatePasswordDeductions(long score, List<Character> chars, long upperCount,
                                                      long lowerCount, long digitCount, long length) {

        // 1. Deduction for Letters Only or Numbers Only
        score -= calculateLettersOrNumbersOnlyDeduction(length, upperCount, lowerCount, digitCount);

        // 2. Deduction for Repeat Characters
        score -= calculateRepeatCharacterDeduction(chars);

        // 3. Deduction for Consecutive Characters
        score -= calculateConsecutiveDeductions(chars);

        // 4. Deduction for Sequential Characters
        score -= calculateSequentialDeductions(chars);

        return score;
    }

    /**
     * Calculates the score based on the length of the password.     *
     *
     * @return the score based on password length
     */
    private static long calculateCharacterLengthScore(long length) {
        return length * CHAR_LENGTH_SCORE;
    }

    /**
     * Calculates the bonus score for the number of uppercase letters in the password.
     *
     * @return the bonus score for uppercase letters
     */
    private static long calculateUppercaseBonus(long upperCount, long length) {
        return upperCount > 0 ? (length - upperCount) * UPPERCASE_BONUS : 0;
    }

    /**
     * Calculates the bonus score for the number of lowercase letters in the password.
     *
     * @return the bonus score for lowercase letters
     */
    private static long calculateLowercaseBonus(long lowerCount, long length) {
        return lowerCount > 0 ? (length - lowerCount) * LOWERCASE_BONUS : 0;
    }

    /**
     * Calculates the score for the number of middle numbers or symbols in the password.
     *
     * @return the score for middle numbers or symbols
     */
    private static long calculateMiddleNumbersOrSymbolsScore(List<Character> chars, long length) {
        return length > 2 ? chars.subList(1, (int) (length - 1)).stream()
                .filter(c -> DIGIT.or(SYMBOL).test(c)).count() * 2 : 0;
    }

    /**
     * Calculates the bonus score based on the number of password requirements met.
     *
     * @return the bonus score for meeting password requirements
     */
    private static long calculateRequirementsBonus(long upperCount, long lowerCount, long digitCount,
                                                   long symbolCount) {
        long requirementsMet = 0;
        if (upperCount > 0) requirementsMet++;
        if (lowerCount > 0) requirementsMet++;
        if (digitCount > 0) requirementsMet++;
        if (symbolCount > 0) requirementsMet++;

        return requirementsMet >= 3 ? 2 * (requirementsMet + 1) : 0;
    }

    /**
     * Calculates the DEDUCTION for passwords with only letters or only numbers.
     *
     * @return the deduction amount for letters only or numbers only
     */
    private static long calculateLettersOrNumbersOnlyDeduction(long length, long upperCount, long lowerCount, long digitCount) {
        if (upperCount + lowerCount == length || digitCount == length) {
            return length;
        }
        return 0;
    }

    /**
     * Calculates the DEDUCTION for repeated characters.
     *
     * @return the deduction amount for repeated characters
     */
    private static long calculateRepeatCharacterDeduction(List<Character> chars) {
        Map<Character, Long> repeatChars = chars.stream()
                .map(Character::toLowerCase)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        long repeatedCount = repeatChars.values().stream().filter(count -> count > 1).count();
        return repeatedCount * REPEAT_CHAR_DEDUCTION;
    }

    /**
     * Calculates the DEDUCTION for consecutive characters.
     *
     * @return the deduction amount for consecutive characters
     */
    private static long calculateConsecutiveDeductions(List<Character> chars) {
        long deductions = 0;
        deductions += countConsecutive(chars, UPPER_CASE) * CONSECUTIVE_DEDUCTION;
        deductions += countConsecutive(chars, LOWER_CASE) * CONSECUTIVE_DEDUCTION;
        deductions += countConsecutive(chars, DIGIT) * CONSECUTIVE_DEDUCTION;
        return deductions;
    }

    /**
     * Calculates the DEDUCTION for sequential characters.
     *
     * @return the deduction amount for sequential characters
     */
    private static long calculateSequentialDeductions(List<Character> chars) {
        long deductions = 0;
        deductions += countSequential(chars, UPPER_CASE.or(LOWER_CASE)) * SEQUENTIAL_DEDUCTION;
        deductions += countSequential(chars, DIGIT) * SEQUENTIAL_DEDUCTION;
        deductions += countSequential(chars, SYMBOL) * SEQUENTIAL_DEDUCTION;
        return deductions;
    }

    /**
     * Counts consecutive characters based on the provided predicate.
     *
     * @return the count of consecutive characters matching the predicate
     */
    private static long countConsecutive(List<Character> chars, Predicate<Character> condition) {
        return IntStream.range(0, chars.size() - 1)
                .filter(i -> condition.test(chars.get(i)) && condition.test(chars.get(i + 1)))
                .count();
    }

    /**
     * Counts sequential characters based on the provided predicate.
     *
     * @return the count of sequential characters matching the predicate
     */
    private static long countSequential(List<Character> chars, Predicate<Character> condition) {
        return IntStream.range(0, chars.size() - 2)
                .filter(i -> condition.test(chars.get(i)) &&
                        chars.get(i + 1) == chars.get(i) + 1 &&
                        chars.get(i + 2) == chars.get(i + 1) + 1)
                .count();
    }

    /**
     * Filter characters.
     *
     * @return the number of character by keys: UPPER_CASE, LOWER_CASE, DIGIT, SYMBOL
     */
    private static long filterByCharacter(List<Character> chars, Predicate<Character> key) {
        return chars.stream().filter(key).count();
    }

    private static long scoreBetween0And100(long score) {
        return Math.max(0, Math.min(score, 100));
    }

}
