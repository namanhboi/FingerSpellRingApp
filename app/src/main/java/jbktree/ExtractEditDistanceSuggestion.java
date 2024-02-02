package jbktree;

import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class ExtractEditDistanceSuggestion {

    public static Map<String, Integer> nEditDistances(String word, BKTree<String> bkTree, int editDistance) {
        final DiscreteDistanceFunction<String> distanceFunction = (first, second) ->
                LevenshteinDistance.getDefaultInstance().apply(first, second);
        final PriorityQueue<String> results = bkTree.getNearestNeighbors(word, editDistance);
        Map<String, Integer> ans = new HashMap<>();
        while (!results.isEmpty()) {
            final String tmp = results.poll();
            ans.put(tmp, distanceFunction.getDistance(tmp, word));
        }
        return ans;
    }

}
