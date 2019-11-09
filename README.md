# ChessGames
## Overview
Inspired by [Command-line Tools can be 235x Faster than your Hadoop 
Cluster](https://aadrake.com/command-line-tools-can-be-235x-faster-than-your-hadoop-cluster.html) 
this is a Java version of the application using the Java 8 streams library. 

Performance of `find`/`xargs`/`mawk` version on a MacBook 12 is:
```
real 0m19.265s
user 0m45.699s
sys  0m9.740s
```

Performance on a MacBook 12 of the Java version is:
```
real 0m15.237s
user 0m29.740s
sys  0m6.552s
```

Both tests used the same 3.5Gb data set. 

If we remove the use of parallel streams then performance drops to:

```
real 0m41.168s
user 0m25.645s
sys  0m7.532s
```

Small efficiencies were ground out through analysis. For example following the advice [here](http://stackoverflow.com/questions/22725537/using-java-8s-optional-with-streamflatmap#22726869) 
and using `flatMap` instead of the `filter`/`map` to unpack the Optional resulted in a ~10-20% 
performance degradation.
 
 ```
 real 0m21.837s
 user 0m39.644s
 sys  0m7.705s
 ```

Removal of unnecessary defensive programming checks also shaved a second or two off execution time.

The RxJava version runs slightly faster than the Java 8 streams version.

Author: Aled Davies <awd@well.com>
Source: Found [here](https://github.com/AledDavies/chessgames)
