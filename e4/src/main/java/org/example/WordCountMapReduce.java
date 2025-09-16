package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WordCountMapReduce implements MapReduceInterface {
    private static final Pattern WORD_PATTERN = Pattern.compile("[^\\p{L}\\p{Nd}]+");

    @Override
    public List<KeyValue> map(String fileName, String content) {
        List<KeyValue> words = new ArrayList<>();
        
        String lowerContent = content.toLowerCase();
        
        String[] wordArray = WORD_PATTERN.split(lowerContent);
        
        for (String word : wordArray) {
            if (!word.isEmpty()) {
                words.add(new KeyValue(word, "1"));
            }
        }
        
        System.out.println("MAP: Найдено " + words.size() + " слов в файле " + fileName);
        return words;
    }

    @Override
    public String reduce(String word, List<String> counts) {
        int totalCount = counts.size();
        
        System.out.println("REDUCE: Слово '" + word + "' встречается " + totalCount + " раз");
        return String.valueOf(totalCount);
    }
}

