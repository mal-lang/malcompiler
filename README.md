# MAL (the Meta Attack Language)

## Requirements

Install latest maven-jlink-plugin:
```
git clone https://github.com/apache/maven-jlink-plugin.git
cd maven-jlink-plugin
git checkout -b tmp d474b2d0c664faee2500fb3cb869898aca281b79
mvn install
```

Modularize and install latest javapoet:

### Linux
```
git clone https://github.com/square/javapoet.git
cd javapoet
git checkout -b tmp a03c97888d3afeeaa92e8ee8eaaffb19fccbaba1
cat << EOF > src/main/java/module-info.java
module com.squareup.javapoet {
  requires transitive java.compiler;
  exports com.squareup.javapoet;
}
EOF
sed -i 's#<java.version>1.8</java.version>#<java.version>11</java.version>#g' pom.xml
sed -i 's#<source>${java.version}</source>#<release>${java.version}</release>#g' pom.xml
sed -i '99,100d;102d;104,115d;117,142d' pom.xml
cat << EOF | ed -s src/main/java/com/squareup/javapoet/CodeWriter.java
362
a
    if (className.alwaysQualify) {
      return className.canonicalName();
    }
.
wq
EOF
cat << EOF | ed -s src/main/java/com/squareup/javapoet/ClassName.java
52
a
  public boolean alwaysQualify = false;

.
163
a
  public static ClassName getQualified(Class<?> clazz) {
    var cn = get(clazz);
    cn.alwaysQualify = true;
    return cn;
  }

  public static ClassName getQualified(String packageName, String simpleName, String... simpleNames) {
    var cn = get(packageName, simpleName, simpleNames);
    cn.alwaysQualify = true;
    return cn;
  }

  public static ClassName getQualified(TypeElement element) {
    var cn = get(element);
    cn.alwaysQualify = true;
    return cn;
  }

.
wq
EOF
mvn install -Dmaven.test.skip=true
```

### MacOS
```
git clone https://github.com/square/javapoet.git
cd javapoet
git checkout -b tmp a03c97888d3afeeaa92e8ee8eaaffb19fccbaba1
cat << EOF > src/main/java/module-info.java
module com.squareup.javapoet {
  requires transitive java.compiler;
  exports com.squareup.javapoet;
}
EOF
sed -i '' 's#<java.version>1.8</java.version>#<java.version>11</java.version>#g' pom.xml
sed -i '' 's#<source>${java.version}</source>#<release>${java.version}</release>#g' pom.xml
sed -i '' '99,100d;102d;104,115d;117,142d' pom.xml
cat << EOF | ed -s src/main/java/com/squareup/javapoet/CodeWriter.java
362
a
    if (className.alwaysQualify) {
      return className.canonicalName();
    }
.
wq
EOF
cat << EOF | ed -s src/main/java/com/squareup/javapoet/ClassName.java
52
a
  public boolean alwaysQualify = false;

.
163
a
  public static ClassName getQualified(Class<?> clazz) {
    var cn = get(clazz);
    cn.alwaysQualify = true;
    return cn;
  }

  public static ClassName getQualified(String packageName, String simpleName, String... simpleNames) {
    var cn = get(packageName, simpleName, simpleNames);
    cn.alwaysQualify = true;
    return cn;
  }

  public static ClassName getQualified(TypeElement element) {
    var cn = get(element);
    cn.alwaysQualify = true;
    return cn;
  }

.
wq
EOF
mvn install -Dmaven.test.skip=true
```

## Compile

Compile MAL with:
```
cd malcompiler
mvn package
```

## Install

Install MAL with:
```
mkdir ~/bin/mal
cd ~/bin/mal
unzip /path/to/malcomplier/mal.img/target/mal.img-0.1.0-SNAPSHOT.zip
echo 'export PATH="$PATH:$HOME/bin/mal/bin"' >> ~/.bashrc
source ~/.bashrc
```

## Run

Run MAL with:
```
mal FILE
```