import java.io.File
import java.util.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.collections.toSortedMap
import java.util.SortedMap

@Serializable
class GenerateJSONFiles {
    var WORDS: List<String> = mutableListOf<String>()
    var WORDS_MODEL : Map<String, Double> = mutableMapOf<String, Double>()
    var WORDS_PAIRS : List<List<String>> = mutableListOf()
    var WORDS_PAIRS_MODEL : Map<String, Map<String, Double>> = mutableMapOf()

    fun generateFiles(corpus : String) {
        val reg = Regex("[a-z]+")
        val allWords = reg.findAll(corpus.lowercase())
        WORDS = allWords.map{it.value}.toList()

        WORDS_MODEL = generateWORDSMODEL()//.toList().sortedByDescending{it.second}.toMap()

        WORDS_PAIRS = generateWORDPAIRS()

        WORDS_PAIRS_MODEL = generateWORDSPAIRSMODEL()

    }
    fun generateWORDSMODEL() : MutableMap<String, Double> {
        var tmp : MutableMap<String, Double> = mutableMapOf<String, Double>()
        for (i in WORDS.distinct()) {
            tmp[i] = 1.0 * Collections.frequency( WORDS, i) / WORDS.size
            //print('h')
        }
        return tmp
    }

    fun generateWORDPAIRS() : MutableList<List<String>> {
        var tmp : MutableList<List<String>> = mutableListOf()
        for (i in 0..WORDS.size - 2) {
            tmp.add(listOf(WORDS[i], WORDS[i + 1]))
            //print('h')
        }
        return tmp
    }

    fun generateWORDSPAIRSMODEL() : MutableMap<String, MutableMap<String, Double>> {
        var tmp : MutableMap<String, MutableMap<String, Int>> = mutableMapOf()
        for (i in WORDS_PAIRS) {
            val firstWord : String = i[0]
            val secondWord : String = i[1]
            if (!tmp.containsKey(firstWord)) {
                tmp.put(firstWord, mutableMapOf())
            }
            if (!tmp[firstWord]!!.containsKey(secondWord)) {
                tmp[firstWord]!!.put(secondWord, 0)
            }
            tmp[firstWord]!!.set(secondWord, (tmp[firstWord]!!.get(secondWord) ?: 0) + 1)
        }
        var probTmp : MutableMap<String, MutableMap<String, Double>> = mutableMapOf()
        for (entry in tmp) {
            val freqMapSecondWord : Map<String, Int> = entry.value
            val firstWordInEntry: String = entry.key
            val sum = freqMapSecondWord.values.sum()
            if (!probTmp.containsKey(firstWordInEntry)) {
                probTmp.put(firstWordInEntry, mutableMapOf())
            }
            for (secondWordEntry in freqMapSecondWord) {
                probTmp[firstWordInEntry]!!.put(secondWordEntry.key, 1.0 * secondWordEntry.value / sum)
            }
        }
        return probTmp
    }


    fun readFileAsTextUsingInputStream(fileName: String)
            = File(fileName).inputStream().readBytes().toString(Charsets.UTF_8)
}