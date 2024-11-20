/***
Creator : azamov
Date : 11/20/2024
Current Project :LeetBot
Package :
 ***/
fun main() {
    val array = arrayOf(2,3,4,5)
    val k = 4
    println(maximumSubarraySum(array.toIntArray(),k))
}

fun maximumSubarraySum(nums: IntArray, k: Int): Long {
    var maxSum = 0L
    var currentSum = 0L

    // Katta subarrayni har bir qadamda tekshirish
    for (i in 0 until k) {
        currentSum += nums[i].toLong()
    }

    // HashSet bilan takrorlanishni tekshirish
    for (i in 0 until nums.size - k + 1) {
        // HashSet yordamida takrorlanishni tekshirish
        val seen = mutableSetOf<Int>()
        var isValid = true

        for (j in i until i + k) {
            if (!seen.add(nums[j])) {
                isValid = false
                break
            }
        }

        // Takrorlanish bo'lmasa, summani hisoblash
        if (isValid) {
            currentSum = nums.slice(i until i + k).sumOf { it.toLong() }
            maxSum = maxOf(maxSum, currentSum)
        }
    }
    return maxSum
}

