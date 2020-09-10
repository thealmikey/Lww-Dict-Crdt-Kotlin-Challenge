package almikey

import java.util.*

typealias Timestamp = Long

/*
     We use a Map<Key, Set<Pair<Value, Timestamp>>> data-structure to represent Added and Removed items dictionary.
     -The Key for the Dictionay can be any value we choose, an Int, a String or any object.
     -Internally we represent the value for the Dictionary as a Set that holds a Pairs consisting of a value and the
      timestamp it was added i.e. Set<Pair<Value, Timestamp>>

     With this we can store a value for a key multiple time with different timestamps
      e.g.
        Map("MyKey" to Set(Pair("Foo",1599720000000),Pair("Foo","1599721111111"),Pair("Bar","1599723333333")))

    The value for "MyKey" key above is the item in the set with the latest timestamp,in this case it's "Bar"


     */
class LWWDict<Key, Value>(
    var added: MutableMap<Key, Set<Pair<Value, Timestamp>>> = mutableMapOf(),
    var removed: MutableMap<Key, Set<Pair<Value, Timestamp>>> = mutableMapOf()
) {


    /*
    Adding a key-value to the LlwDict.

    If the key is newly added, create a Set for it's timestamped values
    e.g. add("item1","value1")
         returns
         Map("item1"->Set(("oneValue",Timestamp1)))
    Else the key already exists with other timestamped values and we append to the set
    e.g. Given key "item1" already exists below,
     Map("item1"->Set(Pair("value1",Timestamp1))),
         When we call
            add("item1","value2")
         We get
            Map("item1"->Set(Pair("value1",Timestamp1),Pair("value2","Timestamp2")))

     */

    fun add(key: Key, value: Value, timestamp: Timestamp = Date().time) {
        //check if key exists
        var valuesSet = added.get(key)
        if (valuesSet == null) {
            //if not create new set
            added.put(key, setOf(Pair(value, timestamp)))
        } else {
            //if it does append to set
            var newSet = valuesSet + Pair(value, timestamp)
            added[key] = newSet
        }
    }

    fun remove(key: Key, value: Value, timestamp: Timestamp = Date().time) {
        //check if key exists
        var valuesSet = removed.get(key)
        if (valuesSet == null) {
            //if not create new set
            removed.put(key, setOf(Pair(value, timestamp)))
        } else {
            //if it does append to set
            var newSet = valuesSet + Pair(value, timestamp)
            removed[key] = newSet
        }
    }

    fun lookup(key: Key, value: Value): Boolean {
        var member = memberItems().get(key)
        /*
        If member doesn't exist return false
         */
        if (member == null) {
            return false
        } else {
            /*
            Return True if member exists and matches the value
             */
            return member == value
        }
    }


    /*
    Return all the member items of the Llw-Dictionary.
    When we have an Item in both Added and Removed Map, it uses the latest timestamp to decide if it's added as a
     member item.
     */
    fun memberItems(): Map<Key, Value> {

        var keysOfItemsInAddedButAbsentInRemoved = added.keys.subtract(removed.keys)

        var latestItemsInAddedAbsentInRemoved = keysOfItemsInAddedButAbsentInRemoved.map { key ->
            var latest = added[key]?.maxBy { (value, timestamp) ->
                timestamp
            }!!.first
            Pair(key, latest)
        }

        var keysPresentInAddedAndRemoved = added.keys.intersect(removed.keys)
        var latestItemsInAddedAndPresentInRemoved: MutableList<Pair<Key, Value>> = mutableListOf()

        for (key in keysPresentInAddedAndRemoved) {

            var addedItemLatestTimestamp: Timestamp = added[key]!!.maxBy { (value, timestamp) ->
                timestamp
            }!!.second
            var removedItemLatestTimestamp: Timestamp = removed[key]!!.maxBy { (value, timestamp) ->
                timestamp
            }!!.second
            if (addedItemLatestTimestamp > removedItemLatestTimestamp) {
                var itemInAddedWithGreaterTimestamp = added[key]!!.maxBy { (value, timestamp) ->
                    timestamp
                }!!.component1()
                latestItemsInAddedAndPresentInRemoved.add(Pair(key, itemInAddedWithGreaterTimestamp))
            } else if (addedItemLatestTimestamp == removedItemLatestTimestamp) {
                /*
                If timestamp for Added equal the timestamp for Removed bias towards add
                 */
                var itemInAddedWithEqualTimestamp = added[key]!!.maxBy { (value, timestamp) ->
                    timestamp
                }!!.component1()
                latestItemsInAddedAndPresentInRemoved.add(Pair(key, itemInAddedWithEqualTimestamp))
            }
        }

        var memberItems = latestItemsInAddedAbsentInRemoved.plus(latestItemsInAddedAndPresentInRemoved)
        return memberItems.toMap()
    }

    fun merge(other: LWWDict<Key, Value>): LWWDict<Key, Value> {
        /*
        Keys present in both Dictionaries would present a merge conflict,
        we use the add() and remove() operation to add each item at a time from the `other` dictionary being merged.
         */
        other.added.forEach { key, set ->
            set.forEach { (value, timestamp) ->
                this.add(key, value, timestamp)
            }
        }

        other.removed.forEach { key, set ->
            set.forEach { (value, timestamp) ->
                this.remove(key, value, timestamp)
            }
        }

        return this

    }

    fun update(key: Key, value: Value, timestamp: Long = Date().time): Boolean {
        var itemExists = memberItems().get(key)
        if (itemExists != null) {
            this.add(key, value, timestamp)
            return true
        } else {
            return false
        }
    }

    fun compare(other: LWWDict<Key, Value>): Boolean {
        return other.added == this.added && other.removed == this.removed
    }


}