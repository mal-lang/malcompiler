# MAL (the Meta Attack Language)

## Requirements

Install latest maven-jlink-plugin:
```
git clone https://github.com/apache/maven-jlink-plugin.git
cd maven-jlink-plugin
mvn install
```

Modularize and install latest javapoet:
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
