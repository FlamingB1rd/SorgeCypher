package com.flamey;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SorgeEncryptionApplication implements Runnable
{
    private static final String ETANORIS = "ETAONRIS ";
    private static final String englishAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ./ ";
    private final Scanner scanner = new Scanner(System.in);
    private static int encryptionNumbers = 80;

    @Override
    public void run()
    {
        System.out.println("Testing: ");
        //Пример от ръководството за тестване:
        //81948 18996 05763 49322 89668 93393 65029 09509 16571 28156 92931 19999 66890
        encrypt("PGP.Version 2.6.3i-Read me First, April 1996.", "SUNDAY");
        System.out.println("EO: 81948 18996 05763 49322 89668 93393 65029 09509 16571 28156 92931 19999 66890 (Expected Output)");

        System.out.println("\nTry out for yourself now! ");
        System.out.print("Please provide a plain text P1: ");
        String P1 = scanner.nextLine();
        System.out.println("\nNow please provide a key K1 with which to encrypt the plain text P1.");
        System.out.print("It must be exactly 6 unique characters put together!");
        String K1;
        encryptionNumbers = 80;
        while (true)
        {
            System.out.print("\nC1: ");
            K1 = scanner.nextLine();
            //Test to see if Key input is correct
            if (K1.length() != 6 || !isKeyWithUniqueCharacters(K1))
            {
                System.out.println("The key was either not made up of 6 characters or they were not unique! It must be a string of 6 unique characters!");
            }
            else if (!Pattern.matches("^[A-Za-z]*$", K1))
            {
                System.out.println("The key was not made of characters. It must be a string of 6 unique characters!");
            }
            else
            {
                break;
            }
        }
        System.out.println("Result: ");
        encrypt(P1, K1);
        System.out.println("Exiting...");
    }

    /**
     * Главния метод, който вика всички останали, за да изпълни цялата задача - първата част от криптирането
     * на Зорге. При подаден прост текст P1 с ключ К1, да получим криптограмата С1.
     *
     * @param plainText - Подаден прост текст P1.
     * @param key - Подаден ключ К1.
     * @author Ivelin Nikolov
     */
    private static void encrypt(String plainText, String key)
    {
        //Използваме направо Map вместо матрица след като не е нужно
        //да виждаме структурата на таблицата, а само да мапнем стойностите към
        //определените букви/символи.
        Map<String, Integer> encryptionReferenceMappingTable = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        //Създававаме нов стринг от ключа с допълнителен space
        //за да може substring да работи без да хвърля exception
        String K1 = key.toUpperCase() + " ";

        //Използваме Key String-a на върха на таблицата.
        //englishAlphabet става като array, който може да използваме
        //да назначим стойностите на буквите извън ключа
        createNewAlphabetWithoutKeyCharacters(K1, sb);
        String alphabetWithoutKeyString = sb.toString();

        //Задаваме стойности на буквите
        mapValuesToAlphabetBySorgeRules(encryptionReferenceMappingTable, K1, alphabetWithoutKeyString);

        //Подготвяме P (Plain text)
        plainText = preparePlainText(plainText) + " ";
        System.out.print("P: ");
        System.out.println(plainText);

        //Криптираме P1 -> C1
        encryptToC1(plainText, encryptionReferenceMappingTable, sb);

        //Разделя числата в С1 на групи по 5 при отпечатване
        System.out.print("C1: ");
        Arrays.stream(sb.toString().split("(?<=\\G.{5})"))
                .forEach(p -> System.out.print(p + " "));
        System.out.println();

        System.out.println(encryptionReferenceMappingTable);
    }

    /**
     * Метод за проверка дали всички букви в един стринг са уникални. Съответно връща 'вярно'
     * ако са. Използваме го за валидация на ключа.
     *
     * @param K1 - Подаден ключ за проверка.
     * @return - Дали К1 е стринг съставен с уникални букви или не. Съответно true ако е, и false ако не е.
     * @author Ivelin Nikolov
     */
    boolean isKeyWithUniqueCharacters(String K1)
    {
        char[] chArray = K1.toCharArray();
        Arrays.sort(chArray);

        for (int i = 0; i < chArray.length - 1; i++)
        {
            if (chArray[i] == chArray[i + 1])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Метод, който създава нова азбука от константата <code>alphabet</code>, която не съдържа
     * буквите от ключа.
     *
     * @param key - подаден ключ K1.
     * @param sb  - референция към StringBuilder, който държи новата азбука.
     * @author Ivelin Nikolov
     */
    private static void createNewAlphabetWithoutKeyCharacters(String key, StringBuilder sb)
    {
        for (int i = 0; i < englishAlphabet.length() - 1; i++)
        {
            if (!key.contains(englishAlphabet.substring(i, i + 1)))
            {
                sb.append(englishAlphabet, i, i + 1);
            }
        }
    }

    /**
     * Метода използва матрично итериране, за да заложи правилните стойности на съответните
     * букви. Използва метода <code>putTextLettersIntoMap</code>, за да ги запише в мап.
     * Не се използва матрица след като не е нужно да виждаме таблицата, а само да номерираме буквите.
     *
     * @param encryptionReferenceMappingTable - референция към мапа.
     * @param k1                              - ключа К1, с който ще определим стойностите.
     * @param alphabetWithoutKeyString        - Модифициарана азбука без буквите, който има К1.
     * @author Ivelin Nikolov
     */
    private static void mapValuesToAlphabetBySorgeRules(Map<String, Integer> encryptionReferenceMappingTable, String k1, String alphabetWithoutKeyString)
    {
        // Indexes:
        // K0 E1 Y2 S3 T4 R5   К -> Е -> Y -> ... +1
        // A6 B7 C8 D9 F10 G11  K -> A -> H -> ... +6
        // H12 ... Използваме метод на итерация на матрица с вложен for
        // По ред итерация +1 по колона +6

        //Назначаваме на ключа стойностите на индексите на ETANORIS
        //Итерираме отляво надясно индексите на ключа 0 -> 5
        for (int i = 0; i < k1.length() - 1; i++)
        {
            String letter = k1.substring(i, i + 1);
            int etanorisIndex = ETANORIS.indexOf(letter);

            putTextLettersIntoMap(encryptionReferenceMappingTable, letter, etanorisIndex);

            //Като срещнем други символи от ETANORIS им назначаваме съответен индекс.
            //Итерираме отгоре надолу докато не свършат буквите
            for (int j = i; j < alphabetWithoutKeyString.length(); j = j + 6)
            {
                letter = alphabetWithoutKeyString.substring(j, j + 1);
                etanorisIndex = ETANORIS.indexOf(letter);

                putTextLettersIntoMap(encryptionReferenceMappingTable, letter, etanorisIndex);
            }
        }
    }

    /**
     * Метод за криптиране на подготвения прост текст P1. Използва предварително подготвения мап, за да
     * замени буквите със съответните им стойности, след което проверява дали броя на всички цифри е кратен
     * на 5. Ако не е, се добавят нули, докато не стане.
     *
     * @param plainText                       - Прост текст P1, който ще бъде криптиран.
     * @param encryptionReferenceMappingTable - Референция към мапа със стойностите за заместване.
     * @param sb                              - референция към StringBuilder.
     * @author Ivelin Nikolov.
     */
    private static void encryptToC1(String plainText, Map<String, Integer> encryptionReferenceMappingTable, StringBuilder sb)
    {
        //Заменяме с подготвения мап и удвояваме числата.
        sb.setLength(0);
        for (int i = 0; i < plainText.length() - 1; i++)
        {
            if (Character.isDigit(plainText.charAt(i)))
            {
                sb.append(plainText.charAt(i));
                sb.append(plainText.charAt(i));
            } else
            {
                sb.append(encryptionReferenceMappingTable.get(plainText.substring(i, i + 1)));
            }
        }

        //проверяваме дали е разделено на 5 части до края. Ако не е, добавяме 0ли.
        while (sb.toString().length() % 5 != 0)
        {
            sb.append(0);
        }
    }

    /**
     * Прост метод за назначаване на съответните индекси от 0-7 за буквите отговарящи на ETANORIS
     * и от 80 - 99 за всички останали, спрямо ключа и подредбата на индексите на ETANORIS.
     *
     * @param encryptionReferenceMappingTable - референция към мапа, в който се пазят стойностите.
     * @param letter                          - буквата, на която ще се назначава стойност.
     * @param etanorisIndex                   - индекса от ETANORIS, ако има такъв.
     * @author Ivelin Nikolov
     */
    private static void putTextLettersIntoMap(Map<String, Integer> encryptionReferenceMappingTable, String letter, int etanorisIndex)
    {
        if (ETANORIS.contains(letter))
        {
            encryptionReferenceMappingTable.put(letter, etanorisIndex);
        } else
        {
            encryptionReferenceMappingTable.put(letter, encryptionNumbers);
            encryptionNumbers++;
        }
    }

    /**
     * Метод за подготовка на простия текст P1. Подава се P1 и връща същия текст
     *
     * @param plainText - Зададен PlainText (P1) за подготовка за криптиране
     * @return - Подготвен прост текст P1
     * @author Ivelin Nikolov
     */
    private static String preparePlainText(String plainText)
    {
        StringBuilder preparedPlainText = new StringBuilder();

        //Премахваме всичко освен главни и малки букви от азбуката, '.' и числата от 0 до 9
        plainText = plainText.replaceAll("[^A-Za-z.0-9]", "");

        //Използваме Pattern за да разделим текста на букви и числа
        List<String> splitPlainText = new ArrayList<String>();
        Matcher m = Pattern.compile("([\\d.]+|[A-Za-z.]+)").matcher(plainText);
        while (m.find())
        {
            splitPlainText.add(m.group());
        }

        //Слагаме '/' след всеки сплит.
        splitPlainText.forEach(e ->
        {
            preparedPlainText.append(e);
            preparedPlainText.append("/");
        });

        //Премахваме края на стринга тей като той е '/' символ, който е ненужен.
        return preparedPlainText.substring(0, preparedPlainText.length() - 1).toUpperCase();
    }
}

