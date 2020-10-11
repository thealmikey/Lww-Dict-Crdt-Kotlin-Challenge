# Lww-Dict-Crdt-Kotlin-Challenge
Last-Write-Win-Dictionary CRDT Kotlin implementation.

 Uses a Map<Key, Set<Pair<Value, Timestamp>>> data-structure to represent Added and Removed items dictionary.
 
     -The Key for the Dictionay can be any value we choose, an Int, a String or any object.
     -Internally we represent the value for the Dictionary as a Set that holds a Pairs consisting of a value and the
      timestamp it was added i.e. Set<Pair<Value, Timestamp>>
      With this we can store a value for a key multiple time with different timestamps
      
      e.g.
        Map("MyKey" to Set(Pair("Foo",1599720000000),Pair("Foo","1599721111111"),Pair("Bar","1599723333333"))
        
 The value for "MyKey" key above is the item in the set with the latest timestamp,in this case it's "Bar"
