package phonebook

import java.io.File
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

const val MSG_START_SEARCHING_LINEAR = "Start searching (linear search)..."
const val MSG_START_SEARCHING_BUBBLE = "Start searching (bubble sort + jump search)..."
const val MSG_START_SEARCHING_QUICK = "Start searching (quick sort + binary search)..."
const val MSG_START_SEARCHING_HASH = "Start searching (hash table)..."

fun main() {

    //------------------------------------------------------------------------------------------------------------------
    // Get data from files and split + some base variables
    //------------------------------------------------------------------------------------------------------------------
//    val allRecordsFile = File("C:\\tmp\\small_directory.txt")
//    val toFindFile = File("C:\\tmp\\small_find.txt")
    val allRecordsFile = File("C:\\tmp\\directory.txt")
    val toFindFile = File("C:\\tmp\\find.txt")

    val allRecordsSplit = allRecordsFile.readLines().map { with(it.split(" ", limit = 2)) { Pair(this[0], this[1]) } }
    val toFind = toFindFile.readLines()
    val toFindNo = toFind.size
    var foundNo: Int

    //------------------------------------------------------------------------------------------------------------------
    // Start searching linear for comparison
    //------------------------------------------------------------------------------------------------------------------
    MSG_START_SEARCHING_LINEAR.let(::println)

    val timeLinear = measureTimeMillis {
        foundNo = linearSearch(toFind, allRecordsSplit)
    }

    var timeDetails = getTimeDetails(timeLinear)
    val timeDetailsLinear = "${timeDetails.first} min. ${timeDetails.second} sec. ${timeDetails.third} ms."
    println("Found $foundNo / $toFindNo entries. Time taken: $timeDetailsLinear")
    println()

    //------------------------------------------------------------------------------------------------------------------
    // Perform bubble sort and go with jump search
    //------------------------------------------------------------------------------------------------------------------
    MSG_START_SEARCHING_BUBBLE.let(::println)

    val allRecordsSorted = allRecordsSplit.toMutableList()
    var marker = allRecordsSorted.size - 1
    var timeBubbleSort = 0L
    var stopped = false
    for (i in 0 until allRecordsSorted.size - 1) {
        timeBubbleSort += measureTimeMillis { marker = bubbleSearchComponent(marker, allRecordsSorted) }
        if (timeBubbleSort > 20 * timeLinear) {
            timeDetails = getTimeDetails(timeBubbleSort + timeLinear)
            println("Found $foundNo / $toFindNo entries. Time taken: ${timeDetails.first} min. ${timeDetails.second} sec. ${timeDetails.third} ms.")
            timeDetails = getTimeDetails(timeBubbleSort)
            println("Sorting time: ${timeDetails.first} min. ${timeDetails.second} sec. ${timeDetails.third} ms. - STOPPED, moved to linear search")
            println("Searching time: $timeDetailsLinear")
            stopped = true
            break
        }
    }

    if (!stopped) {

        val timeJumpSearch = measureTimeMillis {
            foundNo = jumpSearch(toFind, allRecordsSorted)
        }

        timeDetails = getTimeDetails(timeBubbleSort + timeJumpSearch)
        println("Found $foundNo / $toFindNo entries. Time taken: ${timeDetails.first} min. ${timeDetails.second} sec. ${timeDetails.third} ms.")
        timeDetails = getTimeDetails(timeBubbleSort)
        println("Sorting time: ${timeDetails.first} min. ${timeDetails.second} sec. ${timeDetails.third} ms.")
        timeDetails = getTimeDetails(timeJumpSearch)
        println("Searching time: ${timeDetails.first} min. ${timeDetails.second} sec. ${timeDetails.third} ms.")

    }
    println()

    //------------------------------------------------------------------------------------------------------------------
    // Perform quick sort and go with binary search
    //------------------------------------------------------------------------------------------------------------------
    MSG_START_SEARCHING_QUICK.let(::println)

    val allRecordsSorted2 = allRecordsSplit.toMutableList()
    val timeQuickSort = measureTimeMillis { quickSort(allRecordsSorted2, 0, allRecordsSorted2.lastIndex) }

    val timeBinarySearch = measureTimeMillis {
        foundNo = binarySearch(toFind, allRecordsSorted2)
    }

    timeDetails = getTimeDetails(timeQuickSort + timeBinarySearch)
    println("Found $foundNo / $toFindNo entries. Time taken: ${timeDetails.first} min. ${timeDetails.second} sec. ${timeDetails.third} ms.")
    timeDetails = getTimeDetails(timeQuickSort)
    println("Sorting time: ${timeDetails.first} min. ${timeDetails.second} sec. ${timeDetails.third} ms.")
    timeDetails = getTimeDetails(timeBinarySearch)
    println("Searching time: ${timeDetails.first} min. ${timeDetails.second} sec. ${timeDetails.third} ms.")
    println()


    //------------------------------------------------------------------------------------------------------------------
    // Hash tables
    //------------------------------------------------------------------------------------------------------------------
    MSG_START_SEARCHING_HASH.let(::println)

    val allRecordsMap: Map<String, String>
    val timeCreation = measureTimeMillis {
        allRecordsMap =
            allRecordsFile.readLines().associate { line -> line.split(" ", limit = 2).run { Pair(this[1], this[0]) } }
    }
    val timeHashSearch = measureTimeMillis {
        foundNo = hashSearch(toFind, allRecordsMap)
    }
    timeDetails = getTimeDetails(timeCreation + timeHashSearch)
    println("Found $foundNo / $toFindNo entries. Time taken: ${timeDetails.first} min. ${timeDetails.second} sec. ${timeDetails.third} ms.")
    timeDetails = getTimeDetails(timeCreation)
    println("Creating time: ${timeDetails.first} min. ${timeDetails.second} sec. ${timeDetails.third} ms.")
    timeDetails = getTimeDetails(timeHashSearch)
    println("Searching time: ${timeDetails.first} min. ${timeDetails.second} sec. ${timeDetails.third} ms.")

}

fun hashSearch(toFind: List<String>, allRecordsMap: Map<String, String>): Int {
    var foundNo = 0
    for (i in toFind.indices) {
        if (allRecordsMap.contains(toFind[i])) {
            foundNo++
        }
    }
    return foundNo
}

private fun jumpSearch(toFind: List<String>, allRecordsSorted: List<Pair<String, String>>): Int {
    var foundNo = 0
    for (i in toFind.indices) {
        if (singleJumpSearch(allRecordsSorted, toFind[i]) != -1) {
            foundNo++
        }
    }
    return foundNo
}

private fun linearSearch(toFind: List<String>, allRecordsSplit: List<Pair<String, String>>): Int {
    var foundNo = 0
    for (i in toFind.indices) {
        if (allRecordsSplit.firstOrNull { it.second == toFind[i] } != null) {
            foundNo++
        }
    }
    return foundNo
}

private fun binarySearch(toFind: List<String>, allRecordsSorted: List<Pair<String, String>>): Int {
    var foundNo = 0
    for (i in toFind.indices) {
        if (singleBinarySearch(allRecordsSorted, toFind[i]) != -1) {
            foundNo++
        }
    }
    return foundNo
}

fun singleJumpSearch(allRecordsSorted: List<Pair<String, String>>, recordToFind: String): Int {
    var curr = 0
    var prev: Int
    val step = sqrt(allRecordsSorted.size.toDouble()).toInt()

    while (allRecordsSorted[curr].second < recordToFind) {
        if (curr == allRecordsSorted.size) {
            return -1
        }
        prev = curr

        curr = (curr + step).coerceAtMost(allRecordsSorted.lastIndex)

        while (allRecordsSorted[curr].second > recordToFind) {
            curr--
            if (curr <= prev) {
                return -1
            }
        }
        if (allRecordsSorted[curr].second == recordToFind) {
            return curr
        }
    }
    return -1
}

private fun bubbleSearchComponent(marker: Int, allRecordsSorted: MutableList<Pair<String, String>>): Int {
    var marker1 = marker
    marker1--
    for (j in 0..marker1) {
        if (allRecordsSorted[j].second > allRecordsSorted[j + 1].second) {
            val tmpPair = allRecordsSorted[j + 1]
            allRecordsSorted[j + 1] = allRecordsSorted[j]
            allRecordsSorted[j] = tmpPair
        }
    }
    return marker1
}

fun getTimeDetails(time: Long): Triple<Long, Long, Long> {
    val minutes = time / 60000
    val seconds = (time / 1000) % 60
    val milliseconds = time % 1000
    return Triple(minutes, seconds, milliseconds)
}

fun quickSort(arr: MutableList<Pair<String, String>>, low: Int, high: Int) {
    if (low < high) {
        val pivotIndex = partition(arr, low, high)
        quickSort(arr, low, pivotIndex - 1)
        quickSort(arr, pivotIndex + 1, high)
    }
}

fun partition(arr: MutableList<Pair<String, String>>, low: Int, high: Int): Int {
    val pivot = arr[high]
    var i = low - 1
    for (j in low until high) {
        if (arr[j].second < pivot.second) {
            i++
            swap(arr, i, j)
        }
    }
    swap(arr, i + 1, high)
    return i + 1
}

fun <T> swap(arr: MutableList<T>, i: Int, j: Int) {
    val temp = arr[i]
    arr[i] = arr[j]
    arr[j] = temp
}

fun singleBinarySearch(arr: List<Pair<String, String>>, key: String): Int {
    var low = 0
    var high = arr.size - 1

    while (low <= high) {
        val mid = (low + high) / 2

        when {
            arr[mid].second == key -> return mid
            arr[mid].second < key -> low = mid + 1
            else -> high = mid - 1
        }
    }
    return -1
}
