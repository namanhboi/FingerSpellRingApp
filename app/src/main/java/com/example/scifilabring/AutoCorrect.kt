import jbktree.BKTree
import jbktree.DiscreteDistanceFunction
import jbktree.ExtractEditDistanceSuggestion
import org.apache.commons.text.similarity.LevenshteinDistance

class AutoCorrect(val data: GenerateJSONFiles, val corpus: List<String>) {
    val bkTree : BKTree<String> = BKTree<String>(DiscreteDistanceFunction { first: String?, second: String? ->
        LevenshteinDistance.getDefaultInstance().apply(first, second)
    }, corpus)

    val nearByProbMap : Map <String, Map<String, Double>> =
        mapOf(
            "a" to mapOf("a" to 0.96, "i" to 0.04),
            "b" to mapOf("b" to 1.0),
            "c" to mapOf("c" to 1.0),
            "d" to mapOf("d" to 0.92, "l" to 0.02, "o" to 0.02, "x" to 0.04),
            "e" to mapOf("e" to 0.94, "h" to 0.02, "o" to 0.02, "r" to 0.02),
            "f" to mapOf("f" to 0.96, "c" to 0.02, "l" to 0.02),
            "g" to mapOf("g" to 0.89, "h" to 0.018, "q" to 0.036, "y" to 0.055),
            "h" to mapOf("h" to 0.88, "g" to 0.019, "m" to 0.019, "n" to 0.019, "u" to 0.019, "y" to 0.038),
            "i" to mapOf("i" to 0.86, "a" to 0.041, "g" to 0.02, "q" to 0.02, "s" to 0.02, "y" to 0.041),
            "j" to mapOf("j" to 0.96,"q" to 0.02, "z" to 0.02),
            "k" to mapOf("k" to 0.94,"p" to 0.02, "t" to 0.02, "u" to 0.02),
            "l" to mapOf("l" to 1.0),
            "m" to mapOf("m" to 0.94, "h" to 0.039, "n" to 0.02),
            "n" to mapOf("n" to 0.9 , "h" to 0.041, "m" to 0.041, "o" to 0.02),
            "o" to mapOf("o" to 0.96, "d" to 0.04),
            "p" to mapOf("p" to 0.86, "g" to 0.02, "k" to 0.02, "m" to 0.02, "t" to 0.02, "y" to 0.06),
            "q" to mapOf("q" to 0.96 , "p" to 0.021, "y" to 0.021),
            "r" to mapOf("r" to 0.92, "h" to 0.02, "p" to 0.02, "u" to 0.02, "v" to 0.02),
            "s" to mapOf("s" to 0.92, "i" to 0.02, "n" to 0.02, "q" to 0.02, "x" to 0.02),
            "t" to mapOf("t" to 0.92, "m" to 0.02, "s" to 0.06),
            "u" to mapOf("u" to 0.88, "d" to 0.02 , "h" to 0.02, "n" to 0.02, "v" to 0.02, "x" to 0.04),
            "v" to mapOf("v" to 0.92, "d" to 0.04, "h" to 0.02, "u" to 0.02),
            "w" to mapOf("w" to 0.96, "d" to 0.02, "e" to 0.02),
            "x" to mapOf("x" to 0.87, "d" to 0.02, "g" to 0.02, "h" to 0.02, "o" to 0.02, "t" to 0.02, "u" to 0.02),
            "y" to mapOf("y" to 0.9, "g" to 0.02, "k" to 0.02, "p" to 0.04, "t" to 0.02),
            "z" to mapOf("z" to 1.0)
        )
    fun suggest_this_word(word : String, top_n: Int = 3, words_model: Map<String, Double> = data.WORDS_MODEL) : List<Pair<String, Double>> {
        // cycle through nearby letters to replace the last letter
        if (word.isBlank()) return listOf()

        //val corpus : String = readFileDirectlyAsText("C:\Users\dangn\AndroidStudioProjects\ScifiLabRing\app\src\main\assets\google-10000-english.txt")

        val nearByWords = mutableListOf<String>(word)
        val len: Int = word.length
        val lastChar: String = word.substring(len - 1, len)
        val candidateLetters : Map<String, Double> = nearByProbMap[lastChar]!!
        val possibleWordsToSuggest : MutableSet<String> = mutableSetOf()
        val extraProb : MutableMap<String, Double> = mutableMapOf()
        //extraProb is an additional modifier that takes into account the probability of the last letter of the current word and the edit distance from the current word
        for (i in candidateLetters) {
            val a : String = word.substring(0, word.length - 1) + i.key
            nearByWords.add(a)
            possibleWordsToSuggest.add(a)
            extraProb[a] = i.value
        }
        val editDistance : Int = if (word.length > 4) 2 else 1
        val editDistanceWords : MutableList<String> = mutableListOf()
        for (i in nearByWords) {
            val tmpList : Map<String, Int> = ExtractEditDistanceSuggestion.nEditDistances(i, bkTree, editDistance )
            for (j in tmpList) {
                if (j.value == 0) continue
                val dd : Double = (extraProb[i] ?: 0.0) // It will never be 0, this is just to trick compiler
                var probOfEditDistance: Double = 1.0 // arbitrary number that represents a word that is 2 edit distance away is 10 times less likely than 1
                for (rubbish in 1..j.value) probOfEditDistance *= 0.1
                if (!extraProb.containsKey(j.key)) {
                    extraProb[j.key] = dd * probOfEditDistance // generate odds of an editdistance word appearing, which is the og word times the odd of the edit distance
                }
                possibleWordsToSuggest.add(j.key)
                editDistanceWords.add(j.key)
            }
        }

        val prefixed_list = mutableListOf<Pair<String, Double>>()
        /*for (i in nearByWords) {
            prefixed_list += words_model
                .filter { (key, value) -> (key.startsWith(i)) }
                .toList()
                .map{Pair(it.first, it.second * extraProb.get(i)!!)}
        }
        for (i in editDistanceWords) {
            prefixed_list += words_model
                .filter {(key, value) -> (key == i)}
                .toList()
                .map{Pair(it.first, it.second * extraProb[i]!!)}
        }*/
        //generate suggestions based on the previous word times extra_probs which
        for (i in possibleWordsToSuggest) {
            prefixed_list += words_model
                .filter { (key, value) -> (key.startsWith(i)) }
                .toList()
                .map{Pair(it.first, it.second * extraProb.get(i)!!)}
        }
        val ans = prefixed_list.distinct().sortedByDescending { it.second }.take(top_n).toMutableList()
        if (ans.size < top_n) {
            // if the above doesn't provide enough suggestions then just add additional suggestions solely based on the probability of an 2-edit distance word appearing, these added suggestions will always be lower than the above suggestions
            val tmpList : Map<String, Int> = ExtractEditDistanceSuggestion.nEditDistances(word, bkTree, 2 )
            val onlyEditDistanceSuggestion : MutableList<Pair<String, Double>> = mutableListOf()
            for (w in tmpList) {
                var dd: Double = 1.0
                for (i in 1..w.value) dd *= 0.1
                onlyEditDistanceSuggestion += data.WORDS_MODEL
                    .filter { (key, value) -> (key.startsWith(w.key)) }
                    .toList()
                    .map { Pair(it.first, dd * it.second * extraProb.get(word) !!) }
            }
            ans += onlyEditDistanceSuggestion.distinct().sortedByDescending { it.second }.take(top_n - ans.size)
            return ans//.distinct().sortedByDescending{ it.second }.take(top_n)
        }
        return ans//.distinct().sortedByDescending { it.second }.take(top_n)


    }
    fun suggest_this_word_given_last(firstWord: String, secondWord: String, top_n: Int = 3) : List<Pair<String, Double>>   {
        // generate possible second words same as suggest_this_word
        // return top_n suggestions determined by the frequency of words prefixed by the input given the
        // occurrence of last word
        val words_model = data.WORDS_MODEL
        val words_pairs_model = data.WORDS_PAIRS_MODEL

        val mapp = if (words_pairs_model.containsKey(firstWord)) words_pairs_model[firstWord]!! else words_model
        val probable_word = suggest_this_word(secondWord, words_model = mapp)

        return probable_word
    }

    //most of the bloat maybe in the bktree
}