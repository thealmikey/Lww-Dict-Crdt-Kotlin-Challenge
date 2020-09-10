package almikey

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*

import org.junit.jupiter.api.Test

class LWWDictTest {

    @Test
    fun `Adding a Key-Value set to LWW-Dict SHOULD increase Added items`() {
        val newLwwDict = LWWDict<String, String>()

        newLwwDict.add("key1", "value1")
        assertThat(newLwwDict.added.size, equalTo(1))

        newLwwDict.add("key2", "value2")
        assertThat(newLwwDict.added.size, equalTo(2))
    }

    @Test
    fun `Removing a Key-Value from LWWDict SHOULD increase Removed items`() {
        val newLwwDict = LWWDict<String, String>()

        newLwwDict.remove("key1", "value1")
        assertThat(newLwwDict.removed.size, equalTo(1))

        newLwwDict.remove("key2", "value2")
        assertThat(newLwwDict.removed.size, equalTo(2))
    }

    @Test
    fun `After adding a 1 item to an empty LWW-Dict, lookup for item SHOULD return true`() {
        val newLwwDict = LWWDict<String, String>()

        newLwwDict.add("key1", "value1")
        assertThat(newLwwDict.lookup("key1", "value1"), equalTo(true))
    }

    @Test
    fun `After adding 1 key-value to an empty LWWDict then removing, its lookup SHOULD return false`() {
        val newLwwDict = LWWDict<String, String>()

        newLwwDict.add("key1", "value1")
        Thread.sleep(100)
        newLwwDict.remove("key1", "value1")
        assertThat(newLwwDict.lookup("key1", "value1"), equalTo(false))
    }

    @Test
    fun `Adding 1 item to an empty LWWDict then updating it,lookup with Updated key-value SHOULD return true`() {
        val newLwwDict = LWWDict<String, String>()

        newLwwDict.add("key1", "value1")

        Thread.sleep(1000)

        newLwwDict.update("key1", "updatedValue")
        println(newLwwDict.memberItems())
        assertThat(newLwwDict.lookup("key1", "updatedValue"), equalTo(true))
    }

    @Test
    fun `After adding 1 item to LWWDict then updating it,lookup with Previous key-value SHOULD return false for updated item`() {
        val newLwwDict = LWWDict<String, String>()

        newLwwDict.add("key1", "value1")

        Thread.sleep(100)

        newLwwDict.update("key1", "updatedValue")
        assertThat(newLwwDict.lookup("key1", "value1"), equalTo(false))
    }

    @Test
    fun `After adding 1 item to an empty LWWDict then removing it,updating it SHOULD return False since item is not a member anymore`() {
        val newLwwDict = LWWDict<String, String>()

        newLwwDict.add("key1", "value1")
        Thread.sleep(100)
        newLwwDict.remove("key1", "value1")

        assertThat(
            newLwwDict.update("key1", "updatedValue"),
            equalTo(false)
        )
    }

    @Test
    fun `After Re-Adding removed item to LWWDict,updating it SHOULD return True `() {
        val newLwwDict = LWWDict<String, String>()

        newLwwDict.add("key1", "value1")
        Thread.sleep(100)
        newLwwDict.remove("key1", "value1")
        Thread.sleep(100)
        //Re-Add item
        newLwwDict.add("key1", "value1")
        Thread.sleep(100)

        assertThat(
            "Since it now member once more",
            newLwwDict.update("oneKey", "newValue"),
            equalTo(false)
        )
    }

    @Test
    fun `After merging 2 LWWDicts, lookup SHOULD contain elements from both`() {
        val newLwwDict = LWWDict<String, String>()
        newLwwDict.add("key1", "value1")

        val otherLwwDict = LWWDict<String, String>()
        otherLwwDict.add("otherKey", "otherValue")

        var combinedLwwDict = newLwwDict.merge(otherLwwDict)

        assertThat(combinedLwwDict.lookup("key1", "value1"), equalTo(true))
        assertThat(combinedLwwDict.lookup("otherKey", "otherValue"), equalTo(true))
    }

    @Test
    fun `After merging 2 LWWDicts with similar Keys, lookup SHOULD  return  true for elements with highest Timestamp from the 2`() {

        val newLwwDict = LWWDict<String, String>()
        newLwwDict.add("key1", "value1", 111111)

        val otherLwwDict = LWWDict<String, String>()
        otherLwwDict.add("key1", "value2",222222)

        var combinedLwwDict = newLwwDict.merge(otherLwwDict)

        assertThat(combinedLwwDict.lookup("key1", "value1"), equalTo(false))
        assertThat(combinedLwwDict.lookup("key1", "value2"), equalTo(true))
    }


    @Test
    fun `After merging 2 LWWDicts, lookup with item not originally from both SHOULD fail`() {
        val newLwwDict = LWWDict<String, String>()
        newLwwDict.add("key1", "value1")

        val otherLwwDict = LWWDict<String, String>()
        otherLwwDict.add("otherKey", "otherValue")

        var combinedLwwDict = newLwwDict.merge(otherLwwDict)

        assertThat(combinedLwwDict.lookup("AAAA", "value1"), equalTo(false))
        assertThat(combinedLwwDict.lookup("BBBBB", "otherValue"), equalTo(false))
    }

    @Test
    fun `Comparing 2 LWWDicts, with similar content SHOULD return True`() {
        val newLwwDict = LWWDict<String, String>()
        newLwwDict.add("key1", "value1", 1599720000000)

        val otherLwwDict = LWWDict<String, String>()
        otherLwwDict.add("key1", "value1", 1599720000000)

        assertThat(newLwwDict.compare(otherLwwDict), equalTo(true))

    }


}

