<h1>!WARNING!</h1>
This is framework is in development!<br>
If you want to us it, go ahead, but i cannot guaranty that everything will stay the same or will function perfectly.<br>
So take this framework with a grain of salt at the moment

---

This is an base-framework which provides a number of handy functions.

---

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/Keller/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.thorbenkuck/Keller)    


Maven:

```
<dependency>
    <groupId>com.github.thorbenkuck</groupId>
    <artifactId>Keller</artifactId>
    <version>1.3</version>
</dependency>
```

gradle
```
dependencies {
    compile group: 'com.github.thorbenkuck', name: 'Keller', version: '1.3'
}
```

---

<h1>Functions provided by this framework</h1>
The Keller framework is splited into 6 packages. The packages are briefly described as followed:

<ul>

<li>
<h3>cache</h3>
<p>
This framework provides an observable cache, defined by the <code>Cache</code> interface
</p>
</li>

<li>
<h3>command</h3>
<p>
If you want to use a command-pattern, look no further. You can use this package and get rolling fastly.
</p>
</li>
<! test !>
<li>
<h3>datatypes</h3>
<p>
There are some interfaces and data types, provided by this framework, which include
<ul>
<li>Adapter interface</li>
<li>Factory interface</li>
<li>Handler interface</li>
<li>QueuedAction interface</li>
<li>AwaitingRunnable class</li>
<li>MemoryCacheUnit class</li>
</ul>
</p>
</li>

<li>
<h3>Implementations</h3>
<p>
This package consists of some concrete implementations from the Datatype package, like the MemoryCacheUnit
</p>
</li>

<li>
<h3>Pipeline</h3>
<p>
Also this frameworks contains a base for the "Pipeline-Pattern" which takes of the burden to create and maintain a well functioning Pipeline
</p>
</li>

<li>
<h3>Utility</h3>
<p>
The utility package contains some handy classes for multiple actions
</p>
</li>

</ul>

---

Version: Alpha: 0.1
