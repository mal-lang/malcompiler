# MAL (the Meta Attack Language)

## Requirements

Install latest maven-jlink-plugin:
```
git clone https://github.com/apache/maven-jlink-plugin.git
cd maven-jlink-plugin
mvn install
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
