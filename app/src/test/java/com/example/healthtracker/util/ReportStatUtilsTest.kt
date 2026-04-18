package com.example.healthtracker.util

import com.example.healthtracker.util.ReportStatUtils.averageByRealDays
import com.example.healthtracker.util.ReportStatUtils.medianBy
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportStatUtilsTest {

    data class TestRecord(val dayKey: String, val value: Float)

    @Test
    fun testMedianBy() {
        val listOdd = listOf(TestRecord("d1", 10f), TestRecord("d2", 20f), TestRecord("d3", 30f))
        assertEquals(20f, listOdd.medianBy { it.value })

        val listEven = listOf(TestRecord("d1", 10f), TestRecord("d2", 20f), TestRecord("d3", 30f), TestRecord("d4", 40f))
        assertEquals(25f, listEven.medianBy { it.value }) // (20+30)/2 = 25
        
        val emptyList = emptyList<TestRecord>()
        assertEquals(0f, emptyList.medianBy { it.value })
    }

    @Test
    fun testAverageByRealDays() {
        // 第一天有两条记录，总和为30
        // 第二天没有记录
        // 第三天有一条记录，为20
        // 实际记录天数为2天，总和为50，均值为25
        val records = listOf(
            TestRecord("day1", 10f),
            TestRecord("day1", 20f),
            TestRecord("day3", 20f)
        )
        
        val avg = records.averageByRealDays(daySelector = { it.dayKey }, valueSelector = { it.value })
        assertEquals(25f, avg)
    }
}
