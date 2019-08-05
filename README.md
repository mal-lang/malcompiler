# MAL (the Meta Attack Language)

## Requirements

Install latest maven-jlink-plugin:
```
git clone https://github.com/apache/maven-jlink-plugin.git
cd maven-jlink-plugin
git checkout -b tmp d474b2d0c664faee2500fb3cb869898aca281b79
mvn install
```

Install modified javapoet:
```
git clone https://github.com/foreseeti/javapoet.git
cd javapoet
mvn install
```

Install modified svgSalamander:
```
git clone https://github.com/foreseeti/svgSalamander.git
cd svgSalamander/svg-core
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
malc FILE
```
